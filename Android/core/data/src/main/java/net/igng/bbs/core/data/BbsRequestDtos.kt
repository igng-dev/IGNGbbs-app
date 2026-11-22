package net.igng.bbs.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.igng.bbs.core.model.CategoryDto
import net.igng.bbs.core.model.RiskSummaryDto
import net.igng.bbs.core.model.UserBriefDto

// ---- Social / Interaction Requests ----

@Serializable
data class CreateCommentRequest(
    val content: String,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("re_edit_comment_id") val reEditCommentId: String? = null,
    @SerialName("mention_user_ids") val mentionUserIds: List<String>? = null,
)

@Serializable
data class ColumnMembershipRequest(
    val message: String? = null,
)

@Serializable
data class ColumnSubscriptionRequest(
    val verification: String? = null,
    val message: String? = null,
)

@Serializable
data class MarkInboxReadRequest(
    @SerialName("mark_all") val markAll: Boolean = false,
    val scope: String? = null,
    @SerialName("item_id") val itemId: String? = null,
)

@Serializable
data class MarkInboxReadResponseDto(
    @SerialName("marked_all") val markedAll: Boolean = false,
    @SerialName("item_id") val itemId: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
)

@Serializable
data class CreateReportRequest(
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("reason_code") val reasonCode: String,
    val reason: String? = null,
)

@Serializable
data class CreateAppealRequest(
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    val reason: String,
    val grounds: String = "OTHER",
)

@Serializable
data class UpdateUserPreferencesPatch(
    @SerialName("excluded_category_ids") val excludedCategoryIds: List<String>? = null,
    @SerialName("excluded_tags") val excludedTags: List<String>? = null,
    @SerialName("excluded_user_ids") val excludedUserIds: List<String>? = null,
    @SerialName("hide_ai") val hideAi: Boolean? = null,
)

@Serializable
data class UpdateMySettingsRequest(
    val bio: String? = null,
    @SerialName("allow_followers_visibility") val allowFollowersVisibility: Boolean? = null,
    @SerialName("allow_favorites_visibility") val allowFavoritesVisibility: Boolean? = null,
    val preferences: UpdateUserPreferencesPatch? = null,
)

@Serializable
data class UpsertBlockRuleRequest(
    @SerialName("target_user_id") val targetUserId: String,
    @SerialName("block_follow") val blockFollow: Boolean = false,
    @SerialName("block_view") val blockView: Boolean = false,
)

@Serializable
data class UpdateProfileMeRequest(
    val username: String,
)

@Serializable
data class RenameDeviceRequest(
    @SerialName("device_name") val deviceName: String,
)

// ---- Creator Requests & Detail DTOs ----

@Serializable
data class CreateCreatorPostRequest(
    @SerialName("category_id") val categoryId: String,
    val title: String,
    val content: String,
    val summary: String? = null,
    val tags: List<String>? = null,
    val format: String = "markdown",
    val visibility: String = "public",
    @SerialName("column_id") val columnId: String? = null,
    @SerialName("save_as_draft") val saveAsDraft: Boolean = false,
    @SerialName("request_summary") val requestSummary: Boolean = false,
    @SerialName("request_tags") val requestTags: Boolean = false,
)

@Serializable
data class UpdateCreatorPostRequest(
    val title: String? = null,
    val content: String? = null,
    val summary: String? = null,
    val tags: List<String>? = null,
    val format: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("column_id") val columnId: String? = null,
    val visibility: String? = null,
)

@Serializable
data class SubmitCreatorPostRequest(
    val title: String? = null,
    val content: String? = null,
    val summary: String? = null,
    val tags: List<String>? = null,
    val format: String? = null,
    @SerialName("request_summary") val requestSummary: Boolean = false,
    @SerialName("request_tags") val requestTags: Boolean = false,
)

@Serializable
data class RestorePostVersionRequest(
    @SerialName("revision_no") val revisionNo: Int,
)

@Serializable
data class InviteCollaboratorRequest(
    @SerialName("target_user_id") val targetUserId: String,
    @SerialName("role_title") val roleTitle: String = "联合作者",
    val permission: String = "edit",
    val message: String? = null,
)

@Serializable
data class UpdateCollaboratorRequest(
    @SerialName("role_title") val roleTitle: String? = null,
    val permission: String? = null,
)

@Serializable
data class RespondInviteRequest(
    val action: String,
)

@Serializable
data class RespondActionResultDto(
    val id: String = "",
    val status: String = "",
    val message: String? = null,
)

@Serializable
data class CreateCreatorColumnRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
    @SerialName("join_mode") val joinMode: String = "open",
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("category_ids") val categoryIds: List<String>? = null,
)

@Serializable
data class UpdateCreatorColumnRequest(
    val name: String? = null,
    val slug: String? = null,
    val description: String? = null,
    @SerialName("join_mode") val joinMode: String? = null,
    @SerialName("visitor_view_scope") val visitorViewScope: String? = null,
    @SerialName("manager_view_scope") val managerViewScope: String? = null,
    @SerialName("manager_edit_scope") val managerEditScope: String? = null,
    @SerialName("manager_post_allowed") val managerPostAllowed: Boolean? = null,
    @SerialName("subscription_mode") val subscriptionMode: String? = null,
    @SerialName("subscription_secret") val subscriptionSecret: String? = null,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("category_ids") val categoryIds: List<String>? = null,
)

@Serializable
data class CreatorColumnActionRequest(
    val action: String,
    @SerialName("target_user_id") val targetUserId: String? = null,
    @SerialName("subscription_id") val subscriptionId: String? = null,
    @SerialName("post_id") val postId: String? = null,
    val approved: Boolean? = null,
    val blacklist: Boolean? = null,
    @SerialName("sort_order") val sortOrder: Int? = null,
    val message: String? = null,
)

@Serializable
data class ColumnSubscriptionRowDto(
    val id: String = "",
    @SerialName("numeric_id") val numericId: Int = 0,
    val status: String = "pending",
    @SerialName("verification_text") val verificationText: String? = null,
    val user: UserBriefDto = UserBriefDto(),
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class ColumnBlacklistRowDto(
    val user: UserBriefDto = UserBriefDto(),
    val reason: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class ColumnPinRowDto(
    @SerialName("post_id") val postId: String = "",
    val title: String = "",
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class CreatorColumnDetailDto(
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
    @SerialName("manager_post_allowed") val managerPostAllowed: Boolean = true,
    @SerialName("manager_view_scope") val managerViewScope: String = "all",
    @SerialName("manager_edit_scope") val managerEditScope: String = "own",
    @SerialName("has_subscription_secret") val hasSubscriptionSecret: Boolean = false,
    @SerialName("my_role") val myRole: String = "creator",
    val categories: List<CategoryDto> = emptyList(),
    val managers: List<UserBriefDto> = emptyList(),
    val members: List<UserBriefDto> = emptyList(),
    val subscriptions: List<ColumnSubscriptionRowDto> = emptyList(),
    val blacklist: List<ColumnBlacklistRowDto> = emptyList(),
    val pins: List<ColumnPinRowDto> = emptyList(),
)

@Serializable
data class RespondColumnEventRequest(
    val action: String,
    val message: String? = null,
)

@Serializable
data class CreateSeriesRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
)

@Serializable
data class UpdateSeriesRequest(
    val name: String? = null,
    val slug: String? = null,
    val description: String? = null,
    @SerialName("post_ids") val postIds: List<String>? = null,
)

@Serializable
data class MediaAppealRequest(
    val reason: String,
)

// ---- Admin Requests & Responses ----

@Serializable
data class AdminUpdatePostRequest(
    @SerialName("ban_status") val banStatus: Int? = null,
    val status: Int? = null,
    val visibility: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
)

@Serializable
data class AdminDecideReviewRequest(
    val decision: String,
    val reason: String? = null,
    val suggestion: String? = null,
)

@Serializable
data class AdminDecideReportRequest(
    val action: String,
    val reason: String? = null,
    val suggestion: String? = null,
)

@Serializable
data class AdminManageRiskRequest(
    @SerialName("user_id") val userId: String,
    val scope: String,
    val action: String,
    val days: Int? = null,
    val reason: String? = null,
)

@Serializable
data class AdminUserRiskLookupDto(
    val user: UserBriefDto = UserBriefDto(),
    val risk: RiskSummaryDto = RiskSummaryDto(),
)

@Serializable
data class AdminRiskActionResponseDto(
    @SerialName("user_id") val userId: String = "",
    val scope: String = "",
    val action: String = "",
    val risk: RiskSummaryDto = RiskSummaryDto(),
)

@Serializable
data class AdminUpdateColumnRequest(
    @SerialName("bio_status") val bioStatus: Int? = null,
    @SerialName("join_mode") val joinMode: String? = null,
    @SerialName("visitor_view_scope") val visitorViewScope: String? = null,
)
