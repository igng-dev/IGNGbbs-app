package net.igng.bbs.core.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import net.igng.bbs.core.model.OAuthPendingRequest
import net.igng.bbs.core.model.SavedAccount
import net.igng.bbs.core.network.IgngJson
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@Serializable
private data class VaultPayload(
    val activeUserId: Long? = null,
    val accounts: List<SavedAccount> = emptyList(),
)

/**
 * Hardware-backed Android Keystore AES-256-GCM encrypted store for multi-account OAuth 2.1
 * credentials, persistent `dev_*` device identifier, and short-lived PKCE pending state.
 */
class SecureSessionStore(context: Context) {
    private val appContext = context.applicationContext
    private val vaultPrefs = appContext.getSharedPreferences(PREFS_VAULT, Context.MODE_PRIVATE)
    private val pendingPrefs = appContext.getSharedPreferences(PREFS_PENDING, Context.MODE_PRIVATE)
    private val lock = Any()

    private val _accounts = MutableStateFlow<List<SavedAccount>>(emptyList())
    val accounts: StateFlow<List<SavedAccount>> = _accounts.asStateFlow()

    private val _activeAccount = MutableStateFlow<SavedAccount?>(null)
    val activeAccount: StateFlow<SavedAccount?> = _activeAccount.asStateFlow()

    init {
        reloadFromDisk()
    }

    fun getOrCreateDeviceId(): String = synchronized(lock) {
        val existing = vaultPrefs.getString(KEY_DEVICE_ID, null)?.trim()
        if (!existing.isNullOrBlank() && DEVICE_ID_RE.matches(existing)) return existing
        val randomBytes = ByteArray(12)
        SecureRandom().nextBytes(randomBytes)
        val hex = randomBytes.joinToString("") { "%02x".format(it) }
        val generated = "dev_$hex"
        vaultPrefs.edit().putString(KEY_DEVICE_ID, generated).apply()
        generated
    }

    fun currentAccessToken(): String? = activeAccount.value?.accessToken

    fun upsertAccount(account: SavedAccount, makeActive: Boolean = true): Unit = synchronized(lock) {
        val list = _accounts.value.filterNot { it.userId == account.userId } + account
        val activeId = if (makeActive) account.userId else (_activeAccount.value?.userId ?: account.userId)
        persistVault(VaultPayload(activeUserId = activeId, accounts = list))
    }

    fun switchAccount(userId: Long): SavedAccount? = synchronized(lock) {
        val target = _accounts.value.firstOrNull { it.userId == userId } ?: return null
        persistVault(VaultPayload(activeUserId = target.userId, accounts = _accounts.value))
        target
    }

    fun updateActiveProfile(username: String? = null, nickname: String? = null, avatarUrl: String? = null) = synchronized(lock) {
        val current = _activeAccount.value ?: return
        val updated = current.copy(
            username = username ?: current.username,
            nickname = nickname ?: current.nickname,
            avatarUrl = avatarUrl ?: current.avatarUrl,
        )
        upsertAccount(updated, makeActive = true)
    }

    fun removeAccount(userId: Long): Unit = synchronized(lock) {
        val remaining = _accounts.value.filterNot { it.userId == userId }
        val nextActive = if (_activeAccount.value?.userId == userId) remaining.firstOrNull()?.userId else _activeAccount.value?.userId
        persistVault(VaultPayload(activeUserId = nextActive, accounts = remaining))
    }

    fun clearActiveAccount(): Unit = synchronized(lock) {
        val active = _activeAccount.value ?: return
        removeAccount(active.userId)
    }

    fun savePendingOAuth(request: OAuthPendingRequest): Unit = synchronized(lock) {
        val raw = IgngJson.instance.encodeToString(OAuthPendingRequest.serializer(), request)
        val encrypted = encryptString(raw)
        pendingPrefs.edit().putString(KEY_PENDING_OAUTH, encrypted).apply()
    }

    fun consumePendingOAuth(expectedState: String): OAuthPendingRequest? = synchronized(lock) {
        val encrypted = pendingPrefs.getString(KEY_PENDING_OAUTH, null) ?: return null
        pendingPrefs.edit().remove(KEY_PENDING_OAUTH).apply()
        val decoded = runCatching {
            val raw = decryptString(encrypted) ?: return null
            IgngJson.instance.decodeFromString(OAuthPendingRequest.serializer(), raw)
        }.getOrNull() ?: return null
        if (decoded.state != expectedState || decoded.isExpired()) return null
        decoded
    }

    private fun reloadFromDisk() = synchronized(lock) {
        val encrypted = vaultPrefs.getString(KEY_VAULT_BLOB, null)
        if (encrypted.isNullOrBlank()) {
            _accounts.value = emptyList()
            _activeAccount.value = null
            return
        }
        val payload = runCatching {
            val plain = decryptString(encrypted) ?: return@runCatching null
            IgngJson.instance.decodeFromString(VaultPayload.serializer(), plain)
        }.getOrNull()
        if (payload == null) {
            vaultPrefs.edit().remove(KEY_VAULT_BLOB).apply()
            _accounts.value = emptyList()
            _activeAccount.value = null
            return
        }
        _accounts.value = payload.accounts
        _activeAccount.value = payload.accounts.firstOrNull { it.userId == payload.activeUserId }
            ?: payload.accounts.firstOrNull()
    }

    private fun persistVault(payload: VaultPayload) {
        val encoded = IgngJson.instance.encodeToString(VaultPayload.serializer(), payload)
        val encrypted = encryptString(encoded)
        vaultPrefs.edit().putString(KEY_VAULT_BLOB, encrypted).apply()
        _accounts.value = payload.accounts
        _activeAccount.value = payload.accounts.firstOrNull { it.userId == payload.activeUserId }
            ?: payload.accounts.firstOrNull()
    }

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = ks.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private fun encryptString(plain: String): String {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(1 + iv.size + ciphertext.size)
        combined[0] = iv.size.toByte()
        System.arraycopy(iv, 0, combined, 1, iv.size)
        System.arraycopy(ciphertext, 0, combined, 1 + iv.size, ciphertext.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decryptString(encoded: String): String? {
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        if (bytes.size < 14) return null
        val ivLen = bytes[0].toInt() and 0xFF
        if (bytes.size <= 1 + ivLen) return null
        val iv = bytes.copyOfRange(1, 1 + ivLen)
        val ciphertext = bytes.copyOfRange(1 + ivLen, bytes.size)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    companion object {
        private const val PREFS_VAULT = "igngbbs_secure_vault"
        private const val PREFS_PENDING = "igngbbs_oauth_pending"
        private const val KEY_VAULT_BLOB = "vault_v2"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_PENDING_OAUTH = "pending_oauth"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "net.igng.bbs.vault.aes256"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private val DEVICE_ID_RE = Regex("^[a-z0-9_-]{8,64}$")
    }
}
