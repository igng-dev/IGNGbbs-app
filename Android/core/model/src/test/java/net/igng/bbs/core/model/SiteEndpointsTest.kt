package net.igng.bbs.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteEndpointsTest {

    private val endpoints = SiteEndpoints()

    @Test
    fun trustedHostsOnlyIncludeConfiguredIgngOrigins() {
        assertTrue(endpoints.isTrustedAuthHost("https://www.igng.net/api/v1/bbs/home"))
        assertTrue(endpoints.isTrustedAuthHost("https://sso.igng.net/api/oauth/token"))
        assertFalse(endpoints.isTrustedAuthHost("https://evil.example.com/api/v1/bbs/home"))
    }

    @Test
    fun resolvesLegacyMediaPathIntoV1AuthenticatedEndpoint() {
        val raw = "/api/media/files/0123456789abcdef0123456789abcdef/content"
        val resolved = endpoints.resolveMediaUrl(raw, postId = "post_42")
        assertEquals(
            "https://www.igng.net/api/v1/media/med_0123456789abcdef0123456789abcdef/content?post_id=post_42",
            resolved,
        )
    }

    @Test
    fun v1IdHelpersParseAndFormatIds() {
        assertEquals(108, V1Id.numericOf("post_108"))
        assertEquals(108, V1Id.numericOf("108"))
        assertEquals("post_108", V1Id.post(108))
    }
}
