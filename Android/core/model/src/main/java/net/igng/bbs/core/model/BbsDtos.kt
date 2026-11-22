package net.igng.bbs.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginationDto(
    val page: Int = 1,
    @SerialName("page_size") val pageSize: Int = 20,
    val total: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 1,
)

@Serializable
data class PaginatedResponseDto<T>(
    val data: List<T> = emptyList(),
    val pagination: PaginationDto = PaginationDto(),
)

@Serializable
data class ListWrapperDto<T>(
    val data: List<T> = emptyList(),
)

@Serializable
data class UserBriefDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val username: String = "",
    val nickname: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    @SerialName("bio_status") val bioStatus: Int = 0,
    @SerialName("is_following") val isFollowing: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
) {
    val displayName: String get() = nickname.ifBlank { username.ifBlank { "匿名用户" } }
}

@Serializable
data class CategoryDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    @SerialName("post_count") val postCount: Int? = null,
)

@Serializable
data class TagStatDto(
    val name: String = "",
    val count: Int = 0,
)

@Serializable
data class ColumnBriefDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val name: String = "",
    val slug: String = "",
)

@Serializable
data class SeriesBriefDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val name: String = "",
    val slug: String = "",
    val order: Int = 0,
)

@Serializable
data class PostSummaryDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val title: String = "",
    val excerpt: String = "",
    val summary: String? = null,
    val format: String = "markdown",
    val tags: List<String> = emptyList(),
    val views: Int = 0,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("hot_score") val hotScore: Int = 0,
    val status: Int = 0,
    @SerialName("ban_status") val banStatus: Int = 0,
    val visibility: String = "public",
    val lifecycle: String = "published",
    val restriction: String = "none",
    val author: UserBriefDto? = null,
    val category: CategoryDto? = null,
    val column: ColumnBriefDto? = null,
    val series: SeriesBriefDto? = null,
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("has_draft") val hasDraft: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("web_url") val webUrl: String? = null,
    @SerialName("moderation_status") val moderationStatus: String? = null,
    @SerialName("moderation_version") val moderationVersion: Int? = null,
    @SerialName("published_version") val publishedVersion: Int? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    val suggestion: String? = null,
)

@Serializable
data class ArticleRiskDto(
    val active: Boolean = false,
    val until: String? = null,
    val reason: String? = null,
    val approved: Int = 0,
    val rejected: Int = 0,
    val total: Int = 0,
    @SerialName("rejection_rate") val rejectionRate: Double = 0.0,
    @SerialName("window_days") val windowDays: Int = 30,
)

@Serializable
data class CommentRiskDto(
    val active: Boolean = false,
    val until: String? = null,
    val reason: String? = null,
    val strikes: Int = 0,
    @SerialName("window_days") val windowDays: Int = 30,
)

@Serializable
data class RiskSummaryDto(
    val article: ArticleRiskDto = ArticleRiskDto(),
    val comment: CommentRiskDto = CommentRiskDto(),
)

@Serializable
data class AdminAccessDto(
    @SerialName("is_bbs_admin") val isBbsAdmin: Boolean = false,
    @SerialName("can_manage_content") val canManageContent: Boolean = false,
    @SerialName("can_manage_users") val canManageUsers: Boolean = false,
    @SerialName("is_super_admin") val isSuperAdmin: Boolean = false,
    val groups: List<String> = emptyList(),
)

@Serializable
data class UnreadCountsDto(
    val notifications: Int = 0,
    val mentions: Int = 0,
    @SerialName("post_invites") val postInvites: Int = 0,
    @SerialName("column_events") val columnEvents: Int = 0,
    val total: Int = 0,
)

@Serializable
data class ReportReasonDto(
    val code: String = "",
    val title: String = "",
    val description: String = "",
    val severity: Int = 0,
)

@Serializable
data class AppealGroundDto(
    val code: String = "",
    val label: String = "",
)

@Serializable
data class BootstrapUrlsDto(
    @SerialName("api_base_url") val apiBaseUrl: String = SiteEndpoints.DEFAULT_API_BASE_URL,
    @SerialName("sso_base_url") val ssoBaseUrl: String = SiteEndpoints.DEFAULT_SSO_BASE_URL,
    @SerialName("bbs_web_url") val bbsWebUrl: String = SiteEndpoints.DEFAULT_BBS_WEB_BASE_URL,
    @SerialName("bbs_dash_url") val bbsDashUrl: String = "https://dash.igngbbs.net",
)

@Serializable
data class BootstrapViewerDto(
    val authenticated: Boolean = false,
    val user: UserBriefDto? = null,
    val exp: Int? = null,
    @SerialName("base_exp") val baseExp: Int? = null,
    val multiplier: Int = 1,
    val level: Int? = null,
    val unread: UnreadCountsDto = UnreadCountsDto(),
    val risk: RiskSummaryDto = RiskSummaryDto(),
    val admin: AdminAccessDto = AdminAccessDto(),
)

@Serializable
data class BbsBootstrapDto(
    @SerialName("api_version") val apiVersion: String = "",
    @SerialName("contract_version") val contractVersion: String = "",
    @SerialName("server_time") val serverTime: String = "",
    val urls: BootstrapUrlsDto = BootstrapUrlsDto(),
    val categories: List<CategoryDto> = emptyList(),
    @SerialName("report_reasons") val reportReasons: List<ReportReasonDto> = emptyList(),
    @SerialName("appeal_grounds") val appealGrounds: List<AppealGroundDto> = emptyList(),
    val viewer: BootstrapViewerDto = BootstrapViewerDto(),
)

@Serializable
data class RecommendationSlotDto(
    val type: String = "hot",
    val reason: String = "",
    val post: PostSummaryDto = PostSummaryDto(),
)

@Serializable
data class BbsHomeDto(
    val recommendations: List<RecommendationSlotDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    @SerialName("hot_tags") val hotTags: List<TagStatDto> = emptyList(),
    @SerialName("latest_posts") val latestPosts: List<PostSummaryDto> = emptyList(),
)

@Serializable
data class PostCollaboratorDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    @SerialName("role_title") val roleTitle: String = "联合作者",
    val permission: String = "view_credit",
    val user: UserBriefDto? = null,
)

@Serializable
data class SeriesChapterDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val title: String = "",
    val order: Int = 0,
)

@Serializable
data class PostSeriesNavDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    @SerialName("current_index") val currentIndex: Int = 0,
    @SerialName("total_chapters") val totalChapters: Int = 0,
    @SerialName("prev_post") val prevPost: SeriesChapterDto? = null,
    @SerialName("next_post") val nextPost: SeriesChapterDto? = null,
    val chapters: List<SeriesChapterDto> = emptyList(),
)

@Serializable
data class PostViewerStateDto(
    @SerialName("is_liked") val isLiked: Boolean = false,
    @SerialName("is_favorited") val isFavorited: Boolean = false,
    @SerialName("is_following_author") val isFollowingAuthor: Boolean = false,
    @SerialName("is_owner") val isOwner: Boolean = false,
    @SerialName("can_edit") val canEdit: Boolean = false,
    @SerialName("can_manage") val canManage: Boolean = false,
)

@Serializable
data class ColumnSummaryDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    @SerialName("raw_description") val rawDescription: String? = null,
    @SerialName("bio_status") val bioStatus: Int = 0,
    @SerialName("join_mode") val joinMode: String = "open",
    @SerialName("visitor_view_scope") val visitorViewScope: String = "all",
    @SerialName("subscription_mode") val subscriptionMode: String = "direct",
    @SerialName("parent_id") val parentId: String? = null,
    val creator: UserBriefDto? = null,
    val categories: List<CategoryDto> = emptyList(),
    @SerialName("member_count") val memberCount: Int = 0,
    @SerialName("post_count") val postCount: Int = 0,
    @SerialName("subscriber_count") val subscriberCount: Int = 0,
    @SerialName("child_count") val childCount: Int = 0,
    @SerialName("my_role") val myRole: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("web_url") val webUrl: String? = null,
)

@Serializable
data class PostDetailDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val title: String = "",
    val excerpt: String = "",
    val summary: String? = null,
    val content: String = "",
    val format: String = "markdown",
    val tags: List<String> = emptyList(),
    val views: Int = 0,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("hot_score") val hotScore: Int = 0,
    val status: Int = 0,
    @SerialName("ban_status") val banStatus: Int = 0,
    val visibility: String = "public",
    val lifecycle: String = "published",
    val restriction: String = "none",
    @SerialName("word_count") val wordCount: Int = 0,
    @SerialName("reading_minutes") val readingMinutes: Int = 1,
    @SerialName("restricted_warning") val restrictedWarning: Boolean = false,
    @SerialName("restricted_reason") val restrictedReason: String? = null,
    val author: UserBriefDto? = null,
    val category: CategoryDto? = null,
    val collaborators: List<PostCollaboratorDto> = emptyList(),
    val column: ColumnSummaryDto? = null,
    val series: PostSeriesNavDto? = null,
    @SerialName("viewer_state") val viewerState: PostViewerStateDto = PostViewerStateDto(),
    @SerialName("media_urls") val mediaUrls: Map<String, String> = emptyMap(),
    @SerialName("moderation_status") val moderationStatus: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    val suggestion: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("web_url") val webUrl: String? = null,
)

@Serializable
data class CommentModerationDto(
    val action: String = "",
    val summary: String = "",
    val explanation: String = "",
    val suggestion: String = "",
    val appealable: Boolean = false,
)

@Serializable
data class CommentDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    @SerialName("post_id") val postId: String? = null,
    @SerialName("parent_id") val parentId: String? = null,
    val content: String = "",
    val status: Int = 0,
    val folded: Boolean = false,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("is_liked") val isLiked: Boolean = false,
    @SerialName("is_owner") val isOwner: Boolean = false,
    @SerialName("can_delete") val canDelete: Boolean = false,
    @SerialName("can_re_edit") val canReEdit: Boolean = false,
    val moderation: CommentModerationDto? = null,
    val author: UserBriefDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val replies: List<CommentDto> = emptyList(),
)

@Serializable
data class CommentsListResponseDto(
    val data: List<CommentDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto(),
    @SerialName("latest_comment_id") val latestCommentId: String? = null,
)

@Serializable
data class CommentCreateResponseDto(
    val comment: CommentDto = CommentDto(),
    @SerialName("review_pending") val reviewPending: Boolean = false,
)

@Serializable
data class PostLikeResponseDto(
    @SerialName("post_id") val postId: String = "",
    val liked: Boolean = false,
    @SerialName("like_count") val likeCount: Int = 0,
)

@Serializable
data class CommentLikeResponseDto(
    @SerialName("comment_id") val commentId: String = "",
    val liked: Boolean = false,
    @SerialName("like_count") val likeCount: Int = 0,
)

@Serializable
data class PostFavoriteResponseDto(
    @SerialName("post_id") val postId: String = "",
    val favorited: Boolean = false,
)

@Serializable
data class PostViewResponseDto(
    @SerialName("post_id") val postId: String = "",
    val recorded: Boolean = false,
    val views: Int = 0,
)

@Serializable
data class SearchResponseDto(
    val data: List<PostSummaryDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto(),
    val query: String = "",
    val columns: List<ColumnSummaryDto> = emptyList(),
    val users: List<UserBriefDto> = emptyList(),
)

@Serializable
data class ColumnViewerStateDto(
    @SerialName("is_creator") val isCreator: Boolean = false,
    @SerialName("is_manager") val isManager: Boolean = false,
    @SerialName("is_member") val isMember: Boolean = false,
    @SerialName("is_blacklisted") val isBlacklisted: Boolean = false,
    @SerialName("pending_apply") val pendingApply: Boolean = false,
    @SerialName("subscription_status") val subscriptionStatus: String? = null,
    @SerialName("can_post") val canPost: Boolean = false,
    @SerialName("can_view_posts") val canViewPosts: Boolean = true,
)

@Serializable
data class ColumnDetailDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    @SerialName("bio_status") val bioStatus: Int = 0,
    @SerialName("join_mode") val joinMode: String = "open",
    @SerialName("visitor_view_scope") val visitorViewScope: String = "all",
    @SerialName("subscription_mode") val subscriptionMode: String = "direct",
    @SerialName("parent_id") val parentId: String? = null,
    val creator: UserBriefDto? = null,
    val categories: List<CategoryDto> = emptyList(),
    @SerialName("member_count") val memberCount: Int = 0,
    @SerialName("post_count") val postCount: Int = 0,
    @SerialName("subscriber_count") val subscriberCount: Int = 0,
    @SerialName("child_count") val childCount: Int = 0,
    val breadcrumbs: List<ColumnBriefDto> = emptyList(),
    @SerialName("sub_columns") val subColumns: List<ColumnSummaryDto> = emptyList(),
    val managers: List<UserBriefDto> = emptyList(),
    @SerialName("members_preview") val membersPreview: List<UserBriefDto> = emptyList(),
    @SerialName("pinned_posts") val pinnedPosts: List<PostSummaryDto> = emptyList(),
    val posts: List<PostSummaryDto> = emptyList(),
    @SerialName("viewer_state") val viewerState: ColumnViewerStateDto = ColumnViewerStateDto(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("web_url") val webUrl: String? = null,
)

@Serializable
data class ColumnMembershipResponseDto(
    @SerialName("column_id") val columnId: String = "",
    val joined: Boolean = false,
    val applied: Boolean = false,
    val message: String = "",
)

@Serializable
data class ColumnSubscriptionResponseDto(
    @SerialName("column_id") val columnId: String = "",
    val status: String = "approved",
)

@Serializable
data class UserProfileStatsDto(
    val posts: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    @SerialName("total_likes") val totalLikes: Int = 0,
    @SerialName("total_views") val totalViews: Int = 0,
)

@Serializable
data class UserPrivacyDto(
    @SerialName("allow_followers_visibility") val allowFollowersVisibility: Boolean = true,
    @SerialName("allow_favorites_visibility") val allowFavoritesVisibility: Boolean = true,
)

@Serializable
data class UserProfileViewerStateDto(
    @SerialName("is_self") val isSelf: Boolean = false,
    @SerialName("is_following") val isFollowing: Boolean = false,
    @SerialName("block_follow") val blockFollow: Boolean = false,
    @SerialName("block_view") val blockView: Boolean = false,
)

@Serializable
data class CollaboratedPostDto(
    @SerialName("role_title") val roleTitle: String = "",
    val permission: String = "view_credit",
    val post: PostSummaryDto = PostSummaryDto(),
)

@Serializable
data class UserActivityDto(
    val kind: String = "post",
    @SerialName("target_id") val targetId: String? = null,
    val title: String = "",
    val excerpt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class UserProfileDto(
    val user: UserBriefDto = UserBriefDto(),
    val exp: Int = 0,
    val level: Int = 1,
    val stats: UserProfileStatsDto = UserProfileStatsDto(),
    val privacy: UserPrivacyDto = UserPrivacyDto(),
    @SerialName("viewer_state") val viewerState: UserProfileViewerStateDto = UserProfileViewerStateDto(),
    val posts: List<PostSummaryDto> = emptyList(),
    @SerialName("collaborated_posts") val collaboratedPosts: List<CollaboratedPostDto> = emptyList(),
    @SerialName("recent_activities") val recentActivities: List<UserActivityDto> = emptyList(),
)

@Serializable
data class FollowResponseDto(
    @SerialName("user_id") val userId: String = "",
    val following: Boolean = false,
    @SerialName("follower_count") val followerCount: Int = 0,
)

@Serializable
data class FavoriteItemDto(
    @SerialName("favorited_at") val favoritedAt: String? = null,
    val post: PostSummaryDto = PostSummaryDto(),
)

@Serializable
data class FollowingItemDto(
    val user: UserBriefDto = UserBriefDto(),
    @SerialName("is_following") val isFollowing: Boolean = true,
    @SerialName("followed_at") val followedAt: String? = null,
)

@Serializable
data class UserPreferencesDto(
    @SerialName("excluded_category_ids") val excludedCategoryIds: List<String> = emptyList(),
    @SerialName("excluded_tags") val excludedTags: List<String> = emptyList(),
    @SerialName("excluded_user_ids") val excludedUserIds: List<String> = emptyList(),
    @SerialName("hide_ai") val hideAi: Boolean = false,
)

@Serializable
data class BlockRuleDto(
    val id: String = "",
    @SerialName("target_user") val targetUser: UserBriefDto = UserBriefDto(),
    @SerialName("block_follow") val blockFollow: Boolean = false,
    @SerialName("block_view") val blockView: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class BbsUserSettingsDto(
    val bio: String = "",
    @SerialName("bio_status") val bioStatus: Int = 0,
    @SerialName("bio_updated_at") val bioUpdatedAt: String? = null,
    @SerialName("allow_followers_visibility") val allowFollowersVisibility: Boolean = true,
    @SerialName("allow_favorites_visibility") val allowFavoritesVisibility: Boolean = true,
    val preferences: UserPreferencesDto = UserPreferencesDto(),
    @SerialName("block_rules") val blockRules: List<BlockRuleDto> = emptyList(),
)

@Serializable
data class InboxItemDto(
    val id: String = "",
    val kind: String = "system",
    val source: String = "notification",
    val title: String = "",
    val content: String = "",
    @SerialName("is_read") val isRead: Boolean = false,
    val sender: UserBriefDto? = null,
    @SerialName("post_id") val postId: String? = null,
    @SerialName("comment_id") val commentId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class InboxResponseDto(
    val data: List<InboxItemDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto(),
    val unread: UnreadCountsDto = UnreadCountsDto(),
)

@Serializable
data class RuleTitleTextDto(
    val title: String = "",
    val text: String = "",
)

@Serializable
data class RuleExamplesDto(
    val violate: List<String> = emptyList(),
    val allow: List<String> = emptyList(),
)

@Serializable
data class RuleClauseDto(
    val code: String = "",
    val anchor: String = "",
    val title: String = "",
    val severity: Int = 0,
    val description: String = "",
    @SerialName("human_only") val humanOnly: Boolean = false,
    val elements: List<String> = emptyList(),
    val excludes: List<String> = emptyList(),
    val examples: RuleExamplesDto = RuleExamplesDto(),
)

@Serializable
data class RuleSeverityDto(
    val level: Int = 0,
    val text: String = "",
)

@Serializable
data class RuleActionDto(
    val code: String = "",
    val title: String = "",
    val text: String = "",
)

@Serializable
data class RuleChangelogDto(
    val version: String = "",
    val date: String = "",
    val text: String = "",
)

@Serializable
data class BbsRulesDto(
    val version: String = "",
    val digest: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    val principles: List<RuleTitleTextDto> = emptyList(),
    val clauses: List<RuleClauseDto> = emptyList(),
    @SerialName("general_excludes") val generalExcludes: List<String> = emptyList(),
    val severities: List<RuleSeverityDto> = emptyList(),
    val actions: List<RuleActionDto> = emptyList(),
    val flow: List<RuleTitleTextDto> = emptyList(),
    val appeal: List<RuleTitleTextDto> = emptyList(),
    val changelog: List<RuleChangelogDto> = emptyList(),
)

@Serializable
data class DailyBudgetDto(
    val allowed: Boolean = true,
    val used: Int = 0,
    val limit: Int = 5,
    val level: Int = 1,
)

@Serializable
data class DailyViewsPointDto(
    val date: String = "",
    val views: Int = 0,
    @SerialName("total_views") val totalViews: Int = 0,
    @SerialName("internal_ref") val internalRef: Int = 0,
    @SerialName("search_ref") val searchRef: Int = 0,
    @SerialName("external_ref") val externalRef: Int = 0,
)

@Serializable
data class CreatorTotalsDto(
    val posts: Int = 0,
    val published: Int = 0,
    val drafts: Int = 0,
    val pending: Int = 0,
    val rejected: Int = 0,
    val views: Int = 0,
    val likes: Int = 0,
    val comments: Int = 0,
)

@Serializable
data class CreatorTodosDto(
    @SerialName("pending_post_invites") val pendingPostInvites: Int = 0,
    @SerialName("pending_column_events") val pendingColumnEvents: Int = 0,
    @SerialName("pending_subscriptions") val pendingSubscriptions: Int = 0,
    @SerialName("rejected_posts") val rejectedPosts: Int = 0,
)

@Serializable
data class CreatorOverviewDto(
    val totals: CreatorTotalsDto = CreatorTotalsDto(),
    @SerialName("daily_budget") val dailyBudget: DailyBudgetDto = DailyBudgetDto(),
    val risk: RiskSummaryDto = RiskSummaryDto(),
    val todos: CreatorTodosDto = CreatorTodosDto(),
    @SerialName("views_trend") val viewsTrend: List<DailyViewsPointDto> = emptyList(),
    @SerialName("continue_writing") val continueWriting: List<PostSummaryDto> = emptyList(),
)

@Serializable
data class PostDraftDto(
    val title: String = "",
    val content: String = "",
    val summary: String? = null,
    val tags: List<String> = emptyList(),
    val format: String = "markdown",
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class PendingInviteDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val direction: String = "received",
    @SerialName("post_id") val postId: String = "",
    @SerialName("post_title") val postTitle: String = "",
    @SerialName("role_title") val roleTitle: String = "",
    val permission: String = "edit",
    val status: String = "pending",
    val message: String? = null,
    @SerialName("target_user") val targetUser: UserBriefDto? = null,
    val counterparty: UserBriefDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class CreatorInvitesDto(
    val received: List<PendingInviteDto> = emptyList(),
    val sent: List<PendingInviteDto> = emptyList(),
)

@Serializable
data class CreatorPostDetailDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val title: String = "",
    val content: String = "",
    val excerpt: String = "",
    val summary: String? = null,
    val format: String = "markdown",
    val tags: List<String> = emptyList(),
    val status: Int = 1,
    @SerialName("ban_status") val banStatus: Int = 0,
    val visibility: String = "public",
    val category: CategoryDto? = null,
    val column: ColumnBriefDto? = null,
    val draft: PostDraftDto? = null,
    val collaborators: List<PostCollaboratorDto> = emptyList(),
    @SerialName("pending_invites") val pendingInvites: List<PendingInviteDto> = emptyList(),
    @SerialName("series_ids") val seriesIds: List<String> = emptyList(),
    @SerialName("versions_count") val versionsCount: Int = 0,
    @SerialName("daily_budget") val dailyBudget: DailyBudgetDto = DailyBudgetDto(),
    @SerialName("is_owner") val isOwner: Boolean = true,
    @SerialName("can_edit") val canEdit: Boolean = true,
    @SerialName("moderation_status") val moderationStatus: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    val suggestion: String? = null,
    val provisional: Boolean = false,
    @SerialName("risk_at_submission") val riskAtSubmission: Boolean = false,
)

@Serializable
data class PostSubmitResponseDto(
    @SerialName("post_id") val postId: String = "",
    @SerialName("review_id") val reviewId: String? = null,
    val provisional: Boolean = false,
    @SerialName("risk_at_submission") val riskAtSubmission: Boolean = false,
    val rejected: Boolean = false,
    @SerialName("moderation_status") val moderationStatus: String = "pending",
)

@Serializable
data class PostVersionDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    @SerialName("revision_no") val revisionNo: Int = 1,
    val title: String = "",
    val excerpt: String = "",
    val format: String = "markdown",
    val tags: List<String> = emptyList(),
    val summary: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
)

@Serializable
data class PostAnalyticsTotalsDto(
    val views: Int = 0,
    @SerialName("internal_ref") val internalRef: Int = 0,
    @SerialName("search_ref") val searchRef: Int = 0,
    @SerialName("external_ref") val externalRef: Int = 0,
)

@Serializable
data class PostAnalyticsDto(
    @SerialName("post_id") val postId: String = "",
    val title: String = "",
    @SerialName("lifetime_views") val lifetimeViews: Int = 0,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("window_days") val windowDays: Int = 30,
    val totals: PostAnalyticsTotalsDto = PostAnalyticsTotalsDto(),
    val daily: List<DailyViewsPointDto> = emptyList(),
)

@Serializable
data class SeriesSummaryDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    val views: Int = 0,
    @SerialName("post_count") val postCount: Int = 0,
    val creator: UserBriefDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class SeriesPostEntryDto(
    val order: Int = 0,
    val post: PostSummaryDto = PostSummaryDto(),
)

@Serializable
data class CreatorSeriesDetailDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    val views: Int = 0,
    @SerialName("post_count") val postCount: Int = 0,
    val posts: List<SeriesPostEntryDto> = emptyList(),
    @SerialName("views_trend") val viewsTrend: List<DailyViewsPointDto> = emptyList(),
)

@Serializable
data class ColumnEventDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val type: String = "",
    val status: String = "pending",
    val message: String? = null,
    val column: ColumnBriefDto? = null,
    val initiator: UserBriefDto? = null,
    @SerialName("target_user") val targetUser: UserBriefDto? = null,
    @SerialName("can_respond") val canRespond: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class CreatorCommentDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val content: String = "",
    val status: Int = 0,
    val folded: Boolean = false,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("post_id") val postId: String? = null,
    @SerialName("post_title") val postTitle: String = "",
    val author: UserBriefDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AppealArgumentDto(
    val point: String = "",
    val type: String = "",
    val accepted: Boolean = false,
    val reply: String = "",
)

@Serializable
data class AppealDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    @SerialName("target_type") val targetType: String = "post",
    @SerialName("target_id") val targetId: String = "",
    val reason: String = "",
    val grounds: String? = null,
    val round: Int = 1,
    @SerialName("max_rounds") val maxRounds: Int = 3,
    val status: String = "pending",
    @SerialName("decided_by") val decidedBy: String? = null,
    val reply: String? = null,
    val arguments: List<AppealArgumentDto> = emptyList(),
    val clauses: List<String> = emptyList(),
    @SerialName("can_appeal_again") val canAppealAgain: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class CreatorReviewItemDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    @SerialName("target_type") val targetType: String = "post",
    @SerialName("target_id") val targetId: String = "",
    @SerialName("target_title") val targetTitle: String = "",
    @SerialName("review_type") val reviewType: String = "",
    val status: String = "",
    val outcome: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    val suggestion: String? = null,
    @SerialName("has_pending_appeal") val hasPendingAppeal: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("decided_at") val decidedAt: String? = null,
)

@Serializable
data class CreatorReviewsResponseDto(
    val risk: RiskSummaryDto = RiskSummaryDto(),
    val reviews: List<CreatorReviewItemDto> = emptyList(),
    val appeals: List<AppealDto> = emptyList(),
)

@Serializable
data class MediaQuotaDto(
    @SerialName("used_bytes") val usedBytes: Long = 0L,
    @SerialName("limit_bytes") val limitBytes: Long = 104_857_600L,
)

@Serializable
data class CreatorMediaFileDto(
    val id: String = "",
    @SerialName("raw_id") val rawId: String = "",
    val url: String = "",
    @SerialName("markdown_url") val markdownUrl: String = "",
    @SerialName("original_name") val originalName: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("size_bytes") val sizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val status: String = "active",
    @SerialName("audit_status") val auditStatus: String = "passed",
    @SerialName("audit_reason") val auditReason: String? = null,
    @SerialName("appeal_status") val appealStatus: String = "none",
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class CreatorMediaListDto(
    val data: List<CreatorMediaFileDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto(),
    val quota: MediaQuotaDto = MediaQuotaDto(),
)

@Serializable
data class AdminReviewItemDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    @SerialName("target_type") val targetType: String = "post",
    @SerialName("target_id") val targetId: String = "",
    @SerialName("review_type") val reviewType: String = "",
    val status: String = "",
    val outcome: String? = null,
    val priority: Int = 0,
    @SerialName("title_snapshot") val titleSnapshot: String? = null,
    @SerialName("content_snapshot") val contentSnapshot: String? = null,
    val reason: String? = null,
    @SerialName("reason_code") val reasonCode: String? = null,
    val suggestion: String? = null,
    val reporter: UserBriefDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("decided_at") val decidedAt: String? = null,
)

@Serializable
data class AdminRiskEntryDto(
    val user: UserBriefDto = UserBriefDto(),
    val scope: String = "article",
    @SerialName("trigger_kind") val triggerKind: String = "manual",
    val reason: String? = null,
    @SerialName("triggered_at") val triggeredAt: String? = null,
    @SerialName("active_until") val activeUntil: String? = null,
)

@Serializable
data class AdminUserRowDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val username: String = "",
    val nickname: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val status: String = "normal",
    @SerialName("bio_status") val bioStatus: Int = 0,
    @SerialName("post_count") val postCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_login_at") val lastLoginAt: String? = null,
)

@Serializable
data class DeviceSessionDto(
    val id: String = "",
    @SerialName("session_id") val sessionId: String = "",
    @SerialName("device_name") val deviceName: String = "",
    @SerialName("client_label") val clientLabel: String? = null,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("is_current") val isCurrent: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_active_at") val lastActiveAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)

@Serializable
data class DailyLoginDto(
    val awarded: Boolean = false,
    val exp: Int? = null,
    @SerialName("base_exp") val baseExp: Int? = null,
    val multiplier: Int = 1,
    val level: Int? = null,
    val period: String = "",
)

@Serializable
data class ReportSubmitResponseDto(
    @SerialName("review_id") val reviewId: String = "",
    val status: String = "queued",
)

@Serializable
data class AppealSubmitResponseDto(
    @SerialName("review_id") val reviewId: String = "",
    val round: Int = 1,
    @SerialName("max_rounds") val maxRounds: Int = 3,
    val status: String = "pending",
)

@Serializable
data class UserMeDto(
    val id: String = "",
    val username: String = "",
    val email: String? = null,
    val status: String = "normal",
    @SerialName("avatar_media_id") val avatarMediaId: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AvatarUploadResponseDto(
    @SerialName("avatar_media_id") val avatarMediaId: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
)

@Serializable
data class LocalReadingHistoryEntry(
    val postId: String,
    val numericId: Int,
    val title: String,
    val authorName: String,
    val categoryName: String? = null,
    val progressRatio: Float = 0f,
    val scrollIndex: Int = 0,
    val visitedAtEpochMs: Long = System.currentTimeMillis(),
)
