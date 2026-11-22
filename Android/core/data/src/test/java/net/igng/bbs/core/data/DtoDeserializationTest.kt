package net.igng.bbs.core.data

import kotlinx.serialization.builtins.ListSerializer
import net.igng.bbs.core.model.AdminReviewItemDto
import net.igng.bbs.core.model.BbsBootstrapDto
import net.igng.bbs.core.model.BbsHomeDto
import net.igng.bbs.core.model.BbsRulesDto
import net.igng.bbs.core.model.ColumnDetailDto
import net.igng.bbs.core.model.CommentsListResponseDto
import net.igng.bbs.core.model.CreatorOverviewDto
import net.igng.bbs.core.model.CreatorPostDetailDto
import net.igng.bbs.core.model.CreatorReviewsResponseDto
import net.igng.bbs.core.model.DailyLoginDto
import net.igng.bbs.core.model.DeviceSessionDto
import net.igng.bbs.core.model.InboxResponseDto
import net.igng.bbs.core.model.PaginatedResponseDto
import net.igng.bbs.core.model.PostDetailDto
import net.igng.bbs.core.model.PostSummaryDto
import net.igng.bbs.core.model.UserProfileDto
import net.igng.bbs.core.network.ApiErrorEnvelopeDto
import net.igng.bbs.core.network.IgngJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract verification against all 16 golden JSON fixtures exported from `igng-dev/igng-sites`.
 * Also enforces the strict non-Ink scope invariant across every fixture.
 */
class DtoDeserializationTest {

    private val json = IgngJson.instance

    private fun readFixture(name: String): String {
        val stream = requireNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/v1/$name")) {
            "Missing golden fixture: fixtures/v1/$name"
        }
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    @Test
    fun allSixteenGoldenFixturesDeserializeCleanlyWithoutInkLeakage() {
        val fixtureFiles = listOf(
            "bootstrap.json",
            "home.json",
            "posts_list.json",
            "post_detail.json",
            "comments_list.json",
            "column_detail.json",
            "user_profile.json",
            "inbox.json",
            "rules.json",
            "creator_overview.json",
            "creator_post_detail.json",
            "creator_reviews.json",
            "admin_reviews.json",
            "devices_list.json",
            "daily_login.json",
            "error_envelope.json",
        )
        // Strict BBS-only boundary: none of the golden fixtures may contain Ink keys
        val forbiddenInkRegex = Regex("\"ink_[a-z_]+\"")
        fixtureFiles.forEach { file ->
            val raw = readFixture(file)
            assertFalse("Ink key leaked into $file", forbiddenInkRegex.containsMatchIn(raw))
        }

        val bootstrap = json.decodeFromString(BbsBootstrapDto.serializer(), readFixture("bootstrap.json"))
        assertTrue(bootstrap.categories.isNotEmpty())
        assertTrue(bootstrap.reportReasons.isNotEmpty())
        assertTrue(bootstrap.appealGrounds.isNotEmpty())

        val home = json.decodeFromString(BbsHomeDto.serializer(), readFixture("home.json"))
        assertTrue(home.recommendations.isNotEmpty())
        assertTrue(home.latestPosts.isNotEmpty())

        val posts = json.decodeFromString(
            PaginatedResponseDto.serializer(PostSummaryDto.serializer()),
            readFixture("posts_list.json"),
        )
        assertTrue(posts.data.isNotEmpty())

        val postDetail = json.decodeFromString(PostDetailDto.serializer(), readFixture("post_detail.json"))
        assertTrue(postDetail.title.isNotBlank())
        assertTrue(postDetail.content.isNotBlank())

        val comments = json.decodeFromString(CommentsListResponseDto.serializer(), readFixture("comments_list.json"))
        assertTrue(comments.data.isNotEmpty())

        val columnDetail = json.decodeFromString(ColumnDetailDto.serializer(), readFixture("column_detail.json"))
        assertTrue(columnDetail.name.isNotBlank())

        val profile = json.decodeFromString(UserProfileDto.serializer(), readFixture("user_profile.json"))
        assertTrue(profile.user.username.isNotBlank())

        val inbox = json.decodeFromString(InboxResponseDto.serializer(), readFixture("inbox.json"))
        assertTrue(inbox.data.isNotEmpty())

        val rules = json.decodeFromString(BbsRulesDto.serializer(), readFixture("rules.json"))
        assertTrue(rules.clauses.isNotEmpty())

        val overview = json.decodeFromString(CreatorOverviewDto.serializer(), readFixture("creator_overview.json"))
        assertNotNull(overview.dailyBudget)

        val creatorPost = json.decodeFromString(CreatorPostDetailDto.serializer(), readFixture("creator_post_detail.json"))
        assertTrue(creatorPost.title.isNotBlank())

        val creatorReviews = json.decodeFromString(CreatorReviewsResponseDto.serializer(), readFixture("creator_reviews.json"))
        assertNotNull(creatorReviews.risk)

        val adminReviews = json.decodeFromString(
            PaginatedResponseDto.serializer(AdminReviewItemDto.serializer()),
            readFixture("admin_reviews.json"),
        )
        assertTrue(adminReviews.data.isNotEmpty())

        val devices = json.decodeFromString(
            PaginatedResponseDto.serializer(DeviceSessionDto.serializer()),
            readFixture("devices_list.json"),
        )
        assertTrue(devices.data.isNotEmpty())

        val daily = json.decodeFromString(DailyLoginDto.serializer(), readFixture("daily_login.json"))
        assertTrue(daily.period.isNotBlank())

        val err = json.decodeFromString(ApiErrorEnvelopeDto.serializer(), readFixture("error_envelope.json"))
        assertEquals("bbs.post_not_found", err.error.code)
        assertEquals("req_7f3c1a9e4b2d", err.requestId)
    }
}
