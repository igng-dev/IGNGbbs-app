package com.example.igngbbs

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureSessionStore(context: Context) {
    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences("igngbbs_auth", Context.MODE_PRIVATE)
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    fun save(session: AuthSession) {
        val accountKey = accountKey(session.user.id)
        val accountIds = accountIds().toMutableSet()
        accountIds.add(session.user.id.toString())
        preferences.edit()
            .putStringSet(KEY_ACCOUNT_IDS, accountIds)
            .putString(KEY_ACTIVE_USER_ID, session.user.id.toString())
            .putString("${accountKey}_token", encrypt(session.sessionToken))
            .putString("${accountKey}_expires_at", session.expiresAt)
            .putString("${accountKey}_session_id", session.sessionId)
            .putString("${accountKey}_device_name", session.deviceName)
            .putString("${accountKey}_created_at", session.createdAt)
            .putString("${accountKey}_username", session.user.username)
            .putString("${accountKey}_nickname", session.user.nickname)
            .putString("${accountKey}_email", session.user.email)
            .apply()
    }

    fun token(): String? {
        val activeUserId = activeUserId() ?: return null
        val encrypted = preferences.getString("${accountKey(activeUserId)}_token", null) ?: return null
        return runCatching { decrypt(encrypted) }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    fun cachedSession(): AuthSession? {
        val activeUserId = activeUserId() ?: return null
        return cachedSession(activeUserId)
    }

    fun savedSessions(): List<AuthSession> {
        return accountIds()
            .mapNotNull { it.toIntOrNull() }
            .sorted()
            .mapNotNull { cachedSession(it) }
    }

    fun switchTo(userId: Int): AuthSession? {
        val session = cachedSession(userId) ?: return null
        preferences.edit().putString(KEY_ACTIVE_USER_ID, userId.toString()).apply()
        return session
    }

    fun useGuest() {
        preferences.edit().remove(KEY_ACTIVE_USER_ID).apply()
    }

    fun removeActive() {
        val activeUserId = activeUserId() ?: return
        remove(activeUserId)
    }

    fun remove(userId: Int) {
        val accountKey = accountKey(userId)
        val accountIds = accountIds().toMutableSet()
        accountIds.remove(userId.toString())
        val editor = preferences.edit()
            .remove("${accountKey}_token")
            .remove("${accountKey}_expires_at")
            .remove("${accountKey}_session_id")
            .remove("${accountKey}_device_name")
            .remove("${accountKey}_created_at")
            .remove("${accountKey}_username")
            .remove("${accountKey}_nickname")
            .remove("${accountKey}_email")
            .putStringSet(KEY_ACCOUNT_IDS, accountIds)

        if (activeUserId() == userId) {
            editor.remove(KEY_ACTIVE_USER_ID)
        }
        editor.apply()
    }

    private fun cachedSession(userId: Int): AuthSession? {
        val accountKey = accountKey(userId)
        val encrypted = preferences.getString("${accountKey}_token", null) ?: return null
        val token = runCatching { decrypt(encrypted) }.getOrNull()?.takeIf { it.isNotBlank() } ?: return null
        return AuthSession(
            token,
            preferences.getString("${accountKey}_expires_at", "") ?: "",
            preferences.getString("${accountKey}_session_id", "") ?: "",
            preferences.getString("${accountKey}_device_name", "") ?: "",
            preferences.getString("${accountKey}_created_at", "") ?: "",
            AuthUser(
                userId,
                preferences.getString("${accountKey}_username", "") ?: "",
                preferences.getString("${accountKey}_nickname", "") ?: "",
                preferences.getString("${accountKey}_email", "") ?: ""
            )
        )
    }

    private fun accountIds(): Set<String> {
        return preferences.getStringSet(KEY_ACCOUNT_IDS, emptySet()).orEmpty()
    }

    private fun activeUserId(): Int? {
        return preferences.getString(KEY_ACTIVE_USER_ID, null)?.toIntOrNull()
    }

    private fun accountKey(userId: Int): String {
        return "account_$userId"
    }

    private fun secretKey(): SecretKey {
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey
        val generator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        generator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val cipherText = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return base64(cipher.iv) + "." + base64(cipherText)
    }

    private fun decrypt(value: String): String {
        val parts = value.split(".")
        if (parts.size != 2) return ""
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, decode(parts[0])))
        return String(cipher.doFinal(decode(parts[1])), StandardCharsets.UTF_8)
    }

    private fun base64(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray =
        Base64.decode(value, Base64.NO_WRAP)

    private companion object {
        const val KEY_ALIAS = "igngbbs_session_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_ACCOUNT_IDS = "account_ids"
        const val KEY_ACTIVE_USER_ID = "active_user_id"
    }
}
