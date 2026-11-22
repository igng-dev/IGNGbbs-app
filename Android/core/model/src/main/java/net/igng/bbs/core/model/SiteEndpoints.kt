package net.igng.bbs.core.model

import java.net.URI

data class SiteEndpoints(
    val apiBaseUrl: String = DEFAULT_API_BASE_URL,
    val ssoBaseUrl: String = DEFAULT_SSO_BASE_URL,
    val bbsWebBaseUrl: String = DEFAULT_BBS_WEB_BASE_URL,
) {
    val apiRoot: String = apiBaseUrl.trimEnd('/')
    val ssoRoot: String = ssoBaseUrl.trimEnd('/')
    val bbsWebRoot: String = bbsWebBaseUrl.trimEnd('/')

    val apiV1Base: String = "$apiRoot/api/v1"
    val oauthAuthorizeUrl: String = "$ssoRoot/oauth/authorize"
    val oauthTokenUrl: String = "$ssoRoot/api/oauth/token"

    val trustedAuthHosts: Set<String> = buildSet {
        extractHost(apiRoot)?.let(::add)
        extractHost(ssoRoot)?.let(::add)
    }

    fun isTrustedAuthHost(url: String): Boolean {
        val host = extractHost(url) ?: return false
        return host in trustedAuthHosts
    }

    fun resolveMediaUrl(rawUrl: String?, postId: String? = null): String? {
        val value = rawUrl?.trim().orEmpty()
        if (value.isEmpty()) return null
        // Convert legacy relative `/api/media/files/<hex32>/content` into authenticated v1 binary route
        val legacyMatch = LEGACY_MEDIA_RE.find(value)
        if (legacyMatch != null) {
            val hex = legacyMatch.groupValues[1].lowercase()
            val query = if (!postId.isNullOrBlank()) "?post_id=$postId" else ""
            return "$apiV1Base/media/med_$hex/content$query"
        }
        if (value.startsWith("https://") || value.startsWith("http://")) return value
        if (value.startsWith("/api/v1/")) return "$apiRoot$value"
        if (value.startsWith("/")) return "$bbsWebRoot$value"
        return value
    }

    fun postWebUrl(numericId: Int): String = "$bbsWebRoot/post/$numericId"
    fun columnWebUrl(slugOrId: String): String = "$bbsWebRoot/columns/$slugOrId"
    fun userWebUrl(idOrUsername: String): String = "$bbsWebRoot/user/$idOrUsername"

    companion object {
        const val CLIENT_ID = "igng_bbs_android"
        const val OAUTH_REDIRECT_URI = "igngbbs://auth/callback"
        const val DEFAULT_API_BASE_URL = "https://www.igng.net"
        const val DEFAULT_SSO_BASE_URL = "https://sso.igng.net"
        const val DEFAULT_BBS_WEB_BASE_URL = "https://www.igngbbs.net"

        private val LEGACY_MEDIA_RE = Regex("/api/media/files/([a-fA-F0-9]{32})/content")

        fun extractHost(url: String): String? = runCatching {
            URI(url).host?.lowercase()
        }.getOrNull()
    }
}
