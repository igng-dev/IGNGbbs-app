package net.igng.bbs.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PkceGeneratorTest {

    @Test
    fun codeVerifierMatchesRfc7636Constraints() {
        val v1 = PkceGenerator.generateCodeVerifier()
        val v2 = PkceGenerator.generateCodeVerifier()
        assertTrue(v1.length in 43..128)
        assertNotEquals(v1, v2)
        assertTrue(Regex("^[A-Za-z0-9_-]+$").matches(v1))
    }

    @Test
    fun codeChallengeMatchesRfc7636TestVector() {
        // RFC 7636 Appendix B official test vector
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val expected = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
        assertEquals(expected, PkceGenerator.generateCodeChallenge(verifier))
    }

    @Test
    fun stateIsUrlSafeAndHighEntropy() {
        val s1 = PkceGenerator.generateState()
        val s2 = PkceGenerator.generateState()
        assertTrue(s1.length >= 32)
        assertNotEquals(s1, s2)
        assertTrue(Regex("^[A-Za-z0-9_-]+$").matches(s1))
    }
}
