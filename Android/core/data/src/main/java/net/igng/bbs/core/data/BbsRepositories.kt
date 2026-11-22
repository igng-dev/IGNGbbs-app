package net.igng.bbs.core.data

import net.igng.bbs.core.model.AdminReviewItemDto
import net.igng.bbs.core.model.AdminRiskEntryDto
import net.igng.bbs.core.model.AdminUserRowDto
import net.igng.bbs.core.model.AppealDto
import net.igng.bbs.core.model.AppealSubmitResponseDto
import net.igng.bbs.core.model.AvatarUploadResponseDto
import net.igng.bbs.core.model.BbsBootstrapDto
import net.igng.bbs.core.model.BbsHomeDto
import net.igng.bbs.core.model.BbsRulesDto
import net.igng.bbs.core.model.BbsUserSettingsDto
import net.igng.bbs.core.model.BlockRuleDto
import net.igng.bbs.core.model.ColumnDetailDto
import net.igng.bbs.core.model.ColumnEventDto
import net.igng.bbs.core.model.ColumnMembershipResponseDto
import net.igng.bbs.core.model.ColumnSubscriptionResponseDto
import net.igng.bbs.core.model.ColumnSummaryDto
import net.igng.bbs.core.model.CommentCreateResponseDto
import net.igng.bbs.core.model.CommentLikeResponseDto
import net.igng.bbs.core.model.CommentsListResponseDto
import net.igng.bbs.core.model.CreatorCommentDto
import net.igng.bbs.core.model.CreatorInvitesDto
import net.igng.bbs.core.model.CreatorMediaFileDto
import net.igng.bbs.core.model.CreatorMediaListDto
import net.igng.bbs.core.model.CreatorOverviewDto
import net.igng.bbs.core.model.CreatorPostDetailDto
import net.igng.bbs.core.model.CreatorReviewsResponseDto
import net.igng.bbs.core.model.CreatorSeriesDetailDto
import net.igng.bbs.core.model.DailyLoginDto
import net.igng.bbs.core.model.DeviceSessionDto
import net.igng.bbs.core.model.FavoriteItemDto
import net.igng.bbs.core.model.FollowResponseDto
import net.igng.bbs.core.model.FollowingItemDto
import net.igng.bbs.core.model.InboxResponseDto
import net.igng.bbs.core.model.ListWrapperDto
import net.igng.bbs.core.model.PaginatedResponseDto
import net.igng.bbs.core.model.PendingInviteDto
import net.igng.bbs.core.model.PostAnalyticsDto
import net.igng.bbs.core.model.PostCollaboratorDto
import net.igng.bbs.core.model.PostDetailDto
import net.igng.bbs.core.model.PostFavoriteResponseDto
import net.igng.bbs.core.model.PostLikeResponseDto
import net.igng.bbs.core.model.PostSubmitResponseDto
import net.igng.bbs.core.model.PostSummaryDto
import net.igng.bbs.core.model.PostVersionDto
import net.igng.bbs.core.model.PostViewResponseDto
import net.igng.bbs.core.model.ReportSubmitResponseDto
import net.igng.bbs.core.model.SearchResponseDto
import net.igng.bbs.core.model.SeriesSummaryDto
import net.igng.bbs.core.model.TagStatDto
import net.igng.bbs.core.model.UserBriefDto
import net.igng.bbs.core.model.UserMeDto
import net.igng.bbs.core.model.UserProfileDto
import net.igng.bbs.core.network.ApiResult
import net.igng.bbs.core.network.IgngHttpClient
import net.igng.bbs.core.network.IgngJson

class FeedRepository(
    private val http: IgngHttpClient,
    private val settings: SettingsRepository,
) {
    suspend fun fetchBootstrap(): ApiResult<BbsBootstrapDto> {
        val result = http.get<BbsBootstrapDto>("/bbs/bootstrap")
        if (result is ApiResult.Success) {
            settings.writeCache("bootstrap", IgngJson.instance.encodeToString(BbsBootstrapDto.serializer(), result.data))
        }
        return result
    }

    suspend fun loadCachedBootstrap(): BbsBootstrapDto? = settings.readCache("bootstrap")?.let { raw ->
        runCatching { IgngJson.instance.decodeFromString(BbsBootstrapDto.serializer(), raw) }.getOrNull()
    }

    suspend fun fetchHome(): ApiResult<BbsHomeDto> {
        val result = http.get<BbsHomeDto>("/bbs/home")
        if (result is ApiResult.Success) {
            settings.writeCache("home", IgngJson.instance.encodeToString(BbsHomeDto.serializer(), result.data))
        }
        return result
    }

    suspend fun loadCachedHome(): BbsHomeDto? = settings.readCache("home")?.let { raw ->
        runCatching { IgngJson.instance.decodeFromString(BbsHomeDto.serializer(), raw) }.getOrNull()
    }

    suspend fun listPosts(
        page: Int = 1,
        pageSize: Int = 20,
        sort: String = "latest",
        category: String? = null,
        tag: String? = null,
        columnId: String? = null,
        authorId: String? = null,
    ): ApiResult<PaginatedResponseDto<PostSummaryDto>> = http.get(
        path = "/bbs/posts",
        query = mapOf(
            "page" to page.toString(),
            "page_size" to pageSize.toString(),
            "sort" to sort,
            "category" to category,
            "tag" to tag,
            "column_id" to columnId,
            "author_id" to authorId,
        ),
    )

    suspend fun search(
        query: String,
        page: Int = 1,
        pageSize: Int = 20,
        category: String? = null,
        tag: String? = null,
    ): ApiResult<SearchResponseDto> = http.get(
        path = "/bbs/search",
        query = mapOf(
            "q" to query,
            "page" to page.toString(),
            "page_size" to pageSize.toString(),
            "category" to category,
            "tag" to tag,
        ),
    )

    suspend fun listTags(limit: Int = 40): ApiResult<ListWrapperDto<TagStatDto>> =
        http.get("/bbs/tags", query = mapOf("limit" to limit.toString()))

    suspend fun fetchRules(): ApiResult<BbsRulesDto> = http.get("/bbs/rules")
}

class ColumnRepository(private val http: IgngHttpClient) {
    suspend fun listColumns(
        page: Int = 1,
        pageSize: Int = 20,
        query: String? = null,
        categoryId: String? = null,
        mine: Boolean = false,
    ): ApiResult<PaginatedResponseDto<ColumnSummaryDto>> = http.get(
        path = "/bbs/columns",
        query = mapOf(
            "page" to page.toString(),
            "page_size" to pageSize.toString(),
            "q" to query,
            "category_id" to categoryId,
            "mine" to if (mine) "true" else null,
        ),
    )

    suspend fun getColumnDetail(columnIdOrSlug: String): ApiResult<ColumnDetailDto> =
        http.get("/bbs/columns/$columnIdOrSlug")

    suspend fun joinColumn(columnId: String, message: String? = null): ApiResult<ColumnMembershipResponseDto> =
        http.post("/bbs/columns/$columnId/membership", ColumnMembershipRequest(message = message))

    suspend fun leaveColumn(columnId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/columns/$columnId/membership")

    suspend fun subscribeColumn(
        columnId: String,
        verification: String? = null,
        message: String? = null,
    ): ApiResult<ColumnSubscriptionResponseDto> =
        http.post("/bbs/columns/$columnId/subscription", ColumnSubscriptionRequest(verification, message))

    suspend fun unsubscribeColumn(columnId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/columns/$columnId/subscription")
}

class PostRepository(private val http: IgngHttpClient) {
    suspend fun getPostDetail(postId: String): ApiResult<PostDetailDto> =
        http.get("/bbs/posts/$postId")

    suspend fun recordView(postId: String): ApiResult<PostViewResponseDto> =
        http.postEmpty("/bbs/posts/$postId/view", requireAuth = false)

    suspend fun toggleLike(postId: String): ApiResult<PostLikeResponseDto> =
        http.postEmpty("/bbs/posts/$postId/like")

    suspend fun toggleFavorite(postId: String): ApiResult<PostFavoriteResponseDto> =
        http.postEmpty("/bbs/posts/$postId/favorite")

    suspend fun listComments(
        postId: String,
        page: Int = 1,
        pageSize: Int = 20,
        sortBy: String = "oldest",
        afterId: String? = null,
    ): ApiResult<CommentsListResponseDto> = http.get(
        path = "/bbs/posts/$postId/comments",
        query = mapOf(
            "page" to page.toString(),
            "page_size" to pageSize.toString(),
            "sort_by" to sortBy,
            "after_id" to afterId,
        ),
    )

    suspend fun createComment(postId: String, request: CreateCommentRequest): ApiResult<CommentCreateResponseDto> =
        http.post("/bbs/posts/$postId/comments", request)

    suspend fun deleteComment(commentId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/comments/$commentId")

    suspend fun toggleCommentLike(commentId: String): ApiResult<CommentLikeResponseDto> =
        http.postEmpty("/bbs/comments/$commentId/like")

    suspend fun searchUsers(query: String, limit: Int = 10): ApiResult<ListWrapperDto<UserBriefDto>> =
        http.get("/bbs/users/search", query = mapOf("q" to query, "limit" to limit.toString()))

    suspend fun submitReport(request: CreateReportRequest): ApiResult<ReportSubmitResponseDto> =
        http.post("/bbs/reports", request)
}

class InboxRepository(private val http: IgngHttpClient) {
    suspend fun listInbox(
        tab: String = "all",
        page: Int = 1,
        pageSize: Int = 25,
        query: String? = null,
        unreadOnly: Boolean = false,
    ): ApiResult<InboxResponseDto> = http.get(
        path = "/bbs/inbox",
        query = mapOf(
            "tab" to tab,
            "page" to page.toString(),
            "page_size" to pageSize.toString(),
            "q" to query,
            "unread_only" to if (unreadOnly) "true" else null,
        ),
        requireAuth = true,
    )

    suspend fun markItemRead(itemId: String): ApiResult<MarkInboxReadResponseDto> =
        http.post("/bbs/inbox/read", MarkInboxReadRequest(markAll = false, itemId = itemId))

    suspend fun markAllRead(scope: String = "all"): ApiResult<MarkInboxReadResponseDto> =
        http.post("/bbs/inbox/read", MarkInboxReadRequest(markAll = true, scope = scope))
}

class ProfileRepository(private val http: IgngHttpClient) {
    suspend fun getUserProfile(userIdOrUsername: String): ApiResult<UserProfileDto> =
        http.get("/bbs/users/$userIdOrUsername/profile")

    suspend fun toggleFollow(userId: String): ApiResult<FollowResponseDto> =
        http.postEmpty("/bbs/users/$userId/follow")

    suspend fun listFavorites(page: Int = 1, pageSize: Int = 20): ApiResult<PaginatedResponseDto<FavoriteItemDto>> =
        http.get("/bbs/me/favorites", mapOf("page" to page.toString(), "page_size" to pageSize.toString()), requireAuth = true)

    suspend fun listFollowing(
        tab: String = "following",
        page: Int = 1,
        pageSize: Int = 25,
    ): ApiResult<PaginatedResponseDto<FollowingItemDto>> = http.get(
        path = "/bbs/me/following",
        query = mapOf("tab" to tab, "page" to page.toString(), "page_size" to pageSize.toString()),
        requireAuth = true,
    )

    suspend fun getMySettings(): ApiResult<BbsUserSettingsDto> =
        http.get("/bbs/me/settings", requireAuth = true)

    suspend fun updateMySettings(request: UpdateMySettingsRequest): ApiResult<BbsUserSettingsDto> =
        http.patch("/bbs/me/settings", request)

    suspend fun upsertBlockRule(request: UpsertBlockRuleRequest): ApiResult<BlockRuleDto> =
        http.post("/bbs/me/block-rules", request)

    suspend fun deleteBlockRule(targetUserId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/me/block-rules/$targetUserId")

    suspend fun getMe(): ApiResult<UserMeDto> = http.get("/users/me", requireAuth = true)

    suspend fun updateUsername(username: String): ApiResult<UserMeDto> =
        http.patch("/users/me", UpdateProfileMeRequest(username))

    suspend fun uploadAvatar(fileName: String, mimeType: String, bytes: ByteArray): ApiResult<AvatarUploadResponseDto> =
        http.uploadMultipart("/users/me/avatar", fieldName = "file", fileName = fileName, mimeType = mimeType, bytes = bytes)

    suspend fun listDevices(page: Int = 1, pageSize: Int = 30): ApiResult<PaginatedResponseDto<DeviceSessionDto>> =
        http.get("/devices", mapOf("page" to page.toString(), "page_size" to pageSize.toString()), requireAuth = true)

    suspend fun renameDevice(deviceId: String, newName: String): ApiResult<DeviceSessionDto> =
        http.patch("/devices/$deviceId", RenameDeviceRequest(newName))

    suspend fun revokeDevice(deviceId: String): ApiResult<Unit> =
        http.deleteNoContent("/devices/$deviceId")

    suspend fun settleDailyLogin(): ApiResult<DailyLoginDto> =
        http.postEmpty("/users/me/daily-login")
}

class CreatorRepository(private val http: IgngHttpClient) {
    suspend fun getOverview(): ApiResult<CreatorOverviewDto> =
        http.get("/bbs/creator/overview", requireAuth = true)

    suspend fun listPosts(
        filter: String = "all",
        page: Int = 1,
        pageSize: Int = 20,
        query: String? = null,
    ): ApiResult<PaginatedResponseDto<PostSummaryDto>> = http.get(
        path = "/bbs/creator/posts",
        query = mapOf("filter" to filter, "page" to page.toString(), "page_size" to pageSize.toString(), "q" to query),
        requireAuth = true,
    )

    suspend fun createPost(request: CreateCreatorPostRequest): ApiResult<CreatorPostDetailDto> =
        http.post("/bbs/creator/posts", request)

    suspend fun getPostDetail(postId: String): ApiResult<CreatorPostDetailDto> =
        http.get("/bbs/creator/posts/$postId", requireAuth = true)

    suspend fun updatePost(postId: String, request: UpdateCreatorPostRequest): ApiResult<CreatorPostDetailDto> =
        http.patch("/bbs/creator/posts/$postId", request)

    suspend fun deletePost(postId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/creator/posts/$postId")

    suspend fun submitPost(postId: String, request: SubmitCreatorPostRequest = SubmitCreatorPostRequest()): ApiResult<PostSubmitResponseDto> =
        http.post("/bbs/creator/posts/$postId/submit", request)

    suspend fun withdrawSubmission(postId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/creator/posts/$postId/submit")

    suspend fun listVersions(postId: String): ApiResult<ListWrapperDto<PostVersionDto>> =
        http.get("/bbs/creator/posts/$postId/versions", requireAuth = true)

    suspend fun restoreVersion(postId: String, revisionNo: Int): ApiResult<CreatorPostDetailDto> =
        http.post("/bbs/creator/posts/$postId/versions", RestorePostVersionRequest(revisionNo))

    suspend fun getPostAnalytics(postId: String, days: Int = 30): ApiResult<PostAnalyticsDto> =
        http.get("/bbs/creator/posts/$postId/analytics", mapOf("days" to days.toString()), requireAuth = true)

    suspend fun inviteCollaborator(postId: String, request: InviteCollaboratorRequest): ApiResult<PendingInviteDto> =
        http.post("/bbs/creator/posts/$postId/collaborators", request)

    suspend fun updateCollaborator(postId: String, collaboratorId: String, request: UpdateCollaboratorRequest): ApiResult<PostCollaboratorDto> =
        http.patch("/bbs/creator/posts/$postId/collaborators/$collaboratorId", request)

    suspend fun removeCollaborator(postId: String, collaboratorId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/creator/posts/$postId/collaborators/$collaboratorId")

    suspend fun listInvites(): ApiResult<CreatorInvitesDto> =
        http.get("/bbs/creator/invites", requireAuth = true)

    suspend fun respondInvite(inviteId: String, action: String): ApiResult<RespondActionResultDto> =
        http.post("/bbs/creator/invites/$inviteId/respond", RespondInviteRequest(action))

    suspend fun listColumns(): ApiResult<ListWrapperDto<ColumnSummaryDto>> =
        http.get("/bbs/creator/columns", requireAuth = true)

    suspend fun createColumn(request: CreateCreatorColumnRequest): ApiResult<ColumnSummaryDto> =
        http.post("/bbs/creator/columns", request)

    suspend fun getColumnDetail(columnId: String): ApiResult<CreatorColumnDetailDto> =
        http.get("/bbs/creator/columns/$columnId", requireAuth = true)

    suspend fun updateColumn(columnId: String, request: UpdateCreatorColumnRequest): ApiResult<CreatorColumnDetailDto> =
        http.patch("/bbs/creator/columns/$columnId", request)

    suspend fun deleteColumn(columnId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/creator/columns/$columnId")

    suspend fun performColumnAction(columnId: String, request: CreatorColumnActionRequest): ApiResult<RespondActionResultDto> =
        http.post("/bbs/creator/columns/$columnId/actions", request)

    suspend fun listColumnEvents(): ApiResult<ListWrapperDto<ColumnEventDto>> =
        http.get("/bbs/creator/column-events", requireAuth = true)

    suspend fun respondColumnEvent(eventId: String, action: String, message: String? = null): ApiResult<RespondActionResultDto> =
        http.post("/bbs/creator/column-events/$eventId/respond", RespondColumnEventRequest(action, message))

    suspend fun listSeries(): ApiResult<ListWrapperDto<SeriesSummaryDto>> =
        http.get("/bbs/creator/series", requireAuth = true)

    suspend fun createSeries(request: CreateSeriesRequest): ApiResult<SeriesSummaryDto> =
        http.post("/bbs/creator/series", request)

    suspend fun getSeriesDetail(seriesId: String): ApiResult<CreatorSeriesDetailDto> =
        http.get("/bbs/creator/series/$seriesId", requireAuth = true)

    suspend fun updateSeries(seriesId: String, request: UpdateSeriesRequest): ApiResult<CreatorSeriesDetailDto> =
        http.patch("/bbs/creator/series/$seriesId", request)

    suspend fun deleteSeries(seriesId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/creator/series/$seriesId")

    suspend fun listComments(postId: String? = null, page: Int = 1, pageSize: Int = 25): ApiResult<PaginatedResponseDto<CreatorCommentDto>> =
        http.get(
            path = "/bbs/creator/comments",
            query = mapOf("post_id" to postId, "page" to page.toString(), "page_size" to pageSize.toString()),
            requireAuth = true,
        )

    suspend fun listReviews(includePassed: Boolean = false): ApiResult<CreatorReviewsResponseDto> =
        http.get(
            path = "/bbs/creator/reviews",
            query = mapOf("include_passed" to if (includePassed) "true" else null),
            requireAuth = true,
        )

    suspend fun listAppeals(page: Int = 1, pageSize: Int = 25): ApiResult<PaginatedResponseDto<AppealDto>> =
        http.get("/bbs/appeals", mapOf("page" to page.toString(), "page_size" to pageSize.toString()), requireAuth = true)

    suspend fun submitAppeal(request: CreateAppealRequest): ApiResult<AppealSubmitResponseDto> =
        http.post("/bbs/appeals", request)

    suspend fun listMedia(page: Int = 1, pageSize: Int = 30): ApiResult<CreatorMediaListDto> =
        http.get("/bbs/creator/media", mapOf("page" to page.toString(), "page_size" to pageSize.toString()), requireAuth = true)

    suspend fun uploadMedia(fileName: String, mimeType: String, bytes: ByteArray): ApiResult<CreatorMediaFileDto> =
        http.uploadMultipart("/bbs/creator/media", fieldName = "file", fileName = fileName, mimeType = mimeType, bytes = bytes)

    suspend fun appealMedia(mediaId: String, reason: String): ApiResult<RespondActionResultDto> =
        http.post("/media/$mediaId/appeal", MediaAppealRequest(reason))
}

class AdminRepository(private val http: IgngHttpClient) {
    suspend fun listPosts(
        query: String? = null,
        banStatus: Int? = null,
        moderationStatus: String? = null,
        page: Int = 1,
        pageSize: Int = 25,
    ): ApiResult<PaginatedResponseDto<PostSummaryDto>> = http.get(
        path = "/bbs/admin/posts",
        query = mapOf(
            "q" to query,
            "ban_status" to banStatus?.toString(),
            "moderation_status" to moderationStatus,
            "page" to page.toString(),
            "page_size" to pageSize.toString(),
        ),
        requireAuth = true,
    )

    suspend fun updatePost(postId: String, request: AdminUpdatePostRequest): ApiResult<PostSummaryDto> =
        http.patch("/bbs/admin/posts/$postId", request)

    suspend fun deletePost(postId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/admin/posts/$postId")

    suspend fun listReviews(
        status: String? = null,
        targetType: String? = null,
        page: Int = 1,
        pageSize: Int = 25,
    ): ApiResult<PaginatedResponseDto<AdminReviewItemDto>> = http.get(
        path = "/bbs/admin/reviews",
        query = mapOf(
            "status" to status,
            "target_type" to targetType,
            "page" to page.toString(),
            "page_size" to pageSize.toString(),
        ),
        requireAuth = true,
    )

    suspend fun decideReview(reviewId: String, request: AdminDecideReviewRequest): ApiResult<RespondActionResultDto> =
        http.post("/bbs/admin/reviews/$reviewId/decide", request)

    suspend fun listReports(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 25,
    ): ApiResult<PaginatedResponseDto<AdminReviewItemDto>> = http.get(
        path = "/bbs/admin/reports",
        query = mapOf("status" to status, "page" to page.toString(), "page_size" to pageSize.toString()),
        requireAuth = true,
    )

    suspend fun decideReport(reportId: String, request: AdminDecideReportRequest): ApiResult<RespondActionResultDto> =
        http.post("/bbs/admin/reports/$reportId/decide", request)

    suspend fun listRiskStates(scope: String? = null): ApiResult<ListWrapperDto<AdminRiskEntryDto>> =
        http.get("/bbs/admin/risk", query = mapOf("scope" to scope), requireAuth = true)

    suspend fun lookupUserRisk(userId: String): ApiResult<AdminUserRiskLookupDto> =
        http.get("/bbs/admin/risk", query = mapOf("user_id" to userId), requireAuth = true)

    suspend fun manageRisk(request: AdminManageRiskRequest): ApiResult<AdminRiskActionResponseDto> =
        http.post("/bbs/admin/risk", request)

    suspend fun listUsers(query: String? = null, page: Int = 1, pageSize: Int = 25): ApiResult<PaginatedResponseDto<AdminUserRowDto>> =
        http.get("/bbs/admin/users", mapOf("q" to query, "page" to page.toString(), "page_size" to pageSize.toString()), requireAuth = true)

    suspend fun listColumns(query: String? = null, page: Int = 1, pageSize: Int = 25): ApiResult<PaginatedResponseDto<ColumnSummaryDto>> =
        http.get("/bbs/admin/columns", mapOf("q" to query, "page" to page.toString(), "page_size" to pageSize.toString()), requireAuth = true)

    suspend fun updateColumn(columnId: String, request: AdminUpdateColumnRequest): ApiResult<ColumnSummaryDto> =
        http.patch("/bbs/admin/columns/$columnId", request)

    suspend fun listSeries(query: String? = null, page: Int = 1, pageSize: Int = 25): ApiResult<PaginatedResponseDto<SeriesSummaryDto>> =
        http.get("/bbs/admin/series", mapOf("q" to query, "page" to page.toString(), "page_size" to pageSize.toString()), requireAuth = true)

    suspend fun deleteSeries(seriesId: String): ApiResult<Unit> =
        http.deleteNoContent("/bbs/admin/series/$seriesId")
}
