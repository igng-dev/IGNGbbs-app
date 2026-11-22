package net.igng.bbs.core.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PkceGenerator {
    private val secureRandom = SecureRandom()

    fun generateCodeVerifier(byteLength: Int = 48): String {
        val bytes = ByteArray(byteLength.coerceIn(32, 96))
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    fun generateState(byteLength: Int = 24): String {
        val bytes = ByteArray(byteLength.coerceIn(16, 48))
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
