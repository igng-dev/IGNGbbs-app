package net.igng.bbs

import android.net.Uri
import net.igng.bbs.core.model.V1Id

object AppRoutes {
    const val FEED = "feed"
    const val COLUMNS = "columns"
    const val CREATOR = "creator"
    const val PROFILE = "profile"

    const val SEARCH = "search"
    const val RULES = "rules"
    const val INBOX = "inbox"
    const val CATEGORY = "category/{slug}/{title}"
    const val TAG = "tag/{tag}"
    const val COLUMN_DETAIL = "column/{columnId}"
    const val POST_DETAIL = "post/{postId}"
    const val USER_PROFILE = "user/{userId}"

    const val CREATOR_EDITOR = "creator/editor?postId={postId}&columnId={columnId}"
    const val CREATOR_POSTS = "creator/posts"
    const val CREATOR_COLUMNS = "creator/columns"
    const val CREATOR_SERIES = "creator/series"
    const val CREATOR_COMMENTS = "creator/comments"
    const val CREATOR_REVIEWS = "creator/reviews"
    const val CREATOR_MEDIA = "creator/media"

    const val FAVORITES = "profile/favorites"
    const val FOLLOWING = "profile/following"
    const val HISTORY = "profile/history"
    const val ACCOUNT_SECURITY = "profile/account"
    const val PRIVACY_PREFERENCES = "profile/privacy"
    const val APP_SETTINGS = "profile/settings"
    const val ADMIN_CONSOLE = "admin"

    val TOP_LEVEL_ROUTES = setOf(FEED, COLUMNS, CREATOR, PROFILE)

    fun category(slug: String, title: String): String =
        "category/${Uri.encode(slug)}/${Uri.encode(title)}"

    fun tag(tag: String): String = "tag/${Uri.encode(tag)}"
    fun column(columnId: String): String = "column/${Uri.encode(columnId)}"
    fun post(postId: String): String = "post/${Uri.encode(postId)}"
    fun user(userId: String): String = "user/${Uri.encode(userId)}"

    fun editor(postId: String? = null, columnId: String? = null): String {
        val p = Uri.encode(postId.orEmpty())
        val c = Uri.encode(columnId.orEmpty())
        return "creator/editor?postId=$p&columnId=$c"
    }

    /**
     * Maps Android App Links (`https://www.igngbbs.net/...`) and app shortcuts (`igngbbs://open/...`)
     * into in-app navigation destinations. Returns null for unrecognized URLs or OAuth callbacks.
     */
    fun resolveDeepLink(uri: Uri?): String? {
        if (uri == null) return null
        val scheme = uri.scheme?.lowercase().orEmpty()
        val host = uri.host?.lowercase().orEmpty()
        val segments = uri.pathSegments.orEmpty()

        if (scheme == "igngbbs") {
            if (host == "auth") return null // handled by OAuthManager
            if (host == "open") {
                return when (segments.firstOrNull()?.lowercase()) {
                    "search" -> SEARCH
                    "editor" -> editor()
                    "inbox" -> INBOX
                    "rules" -> RULES
                    else -> null
                }
            }
            if (host == "post" && segments.isNotEmpty()) {
                val id = segments.first().toIntOrNull()?.let { V1Id.post(it) } ?: segments.first()
                return post(id)
            }
            return null
        }

        if ((scheme == "https" || scheme == "http") && (host == "www.igngbbs.net" || host == "igngbbs.net")) {
            val first = segments.getOrNull(0)?.lowercase() ?: return null
            val second = segments.getOrNull(1)
            return when (first) {
                "post" -> second?.let { raw ->
                    val normalized = raw.toIntOrNull()?.let { V1Id.post(it) } ?: raw
                    post(normalized)
                }
                "user" -> second?.let { user(it) }
                "columns" -> if (second != null) column(second) else COLUMNS
                "tags" -> second?.let { tag(it) }
                "pages" -> if (second?.lowercase() == "rules") RULES else null
                else -> null
            }
        }
        return null
    }
}
