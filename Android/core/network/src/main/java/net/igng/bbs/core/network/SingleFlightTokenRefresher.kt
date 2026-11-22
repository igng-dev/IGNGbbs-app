package net.igng.bbs.core.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coordinates concurrent OAuth 2.1 refresh-token rotation across parallel API calls.
 * Because IGNG SSO revokes the entire token family if a rotated refresh token is replayed,
 * only one refresh RPC may execute at a time; queued callers receive the newly rotated
 * access token automatically if their stale token was already superseded.
 */
class SingleFlightTokenRefresher(
    private val currentAccessToken: () -> String?,
    private val performRefresh: suspend (failedAccessToken: String?) -> String?,
) {
    private val mutex = Mutex()

    suspend fun refreshIfNeeded(failedAccessToken: String?): String? = mutex.withLock {
        val latest = currentAccessToken()
        if (!latest.isNullOrBlank() && failedAccessToken != null && latest != failedAccessToken) {
            return@withLock latest
        }
        performRefresh(failedAccessToken)
    }
}
