package net.igng.bbs.core.data

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import net.igng.bbs.core.auth.OAuthManager
import net.igng.bbs.core.auth.SecureSessionStore
import net.igng.bbs.core.model.BbsBootstrapDto
import net.igng.bbs.core.model.DailyLoginDto
import net.igng.bbs.core.model.SiteEndpoints
import net.igng.bbs.core.network.ApiResult
import net.igng.bbs.core.network.IgngHttpClient
import net.igng.bbs.core.network.SessionExpiredNotifier
import java.time.LocalDate
import java.time.ZoneOffset

class BbsAppContainer(
    context: Context,
    val endpoints: SiteEndpoints,
    val appVersion: String,
) {
    val appContext: Context = context.applicationContext
    val sessionStore = SecureSessionStore(appContext)
    val oauthManager = OAuthManager(endpoints = endpoints, appVersion = appVersion, sessionStore = sessionStore)
    val sessionExpiredNotifier = SessionExpiredNotifier()
    val httpClient = IgngHttpClient(
        endpoints = endpoints,
        appVersion = appVersion,
        deviceIdProvider = { sessionStore.getOrCreateDeviceId() },
        authTokenProvider = oauthManager,
        sessionExpiredNotifier = sessionExpiredNotifier,
    )
    val settingsRepository = SettingsRepository(appContext)

    val feedRepository = FeedRepository(httpClient, settingsRepository)
    val columnRepository = ColumnRepository(httpClient)
    val postRepository = PostRepository(httpClient)
    val inboxRepository = InboxRepository(httpClient)
    val profileRepository = ProfileRepository(httpClient)
    val creatorRepository = CreatorRepository(httpClient)
    val adminRepository = AdminRepository(httpClient)

    private val _bootstrap = MutableStateFlow<BbsBootstrapDto?>(null)
    val bootstrap: StateFlow<BbsBootstrapDto?> = _bootstrap.asStateFlow()

    private val _dailyLoginReward = MutableSharedFlow<DailyLoginDto>(extraBufferCapacity = 1)
    val dailyLoginReward: SharedFlow<DailyLoginDto> = _dailyLoginReward.asSharedFlow()

    suspend fun hydrateCachedBootstrap() {
        if (_bootstrap.value == null) {
            _bootstrap.value = feedRepository.loadCachedBootstrap()
        }
    }

    suspend fun refreshBootstrap(): ApiResult<BbsBootstrapDto> {
        val res = feedRepository.fetchBootstrap()
        if (res is ApiResult.Success) {
            _bootstrap.value = res.data
            res.data.viewer.user?.let { viewer ->
                sessionStore.updateActiveProfile(
                    username = viewer.username.takeIf { it.isNotBlank() },
                    nickname = viewer.nickname.takeIf { it.isNotBlank() },
                    avatarUrl = viewer.avatarUrl,
                )
            }
        }
        return res
    }

    suspend fun settleDailyLoginIfNeeded() {
        val active = sessionStore.activeAccount.value ?: return
        val todayUtc = LocalDate.now(ZoneOffset.UTC).toString()
        val lastPeriod = settingsRepository.getLastDailyLoginPeriod(active.userId)
        if (lastPeriod == todayUtc) return
        when (val res = profileRepository.settleDailyLogin()) {
            is ApiResult.Success -> {
                val period = res.data.period.ifBlank { todayUtc }
                settingsRepository.markDailyLoginPeriod(active.userId, period)
                if (res.data.awarded) {
                    _dailyLoginReward.tryEmit(res.data)
                    refreshBootstrap()
                }
            }
            is ApiResult.Failure -> Unit
        }
    }

    suspend fun switchAccount(userId: Long) {
        sessionStore.switchAccount(userId)
        refreshBootstrap()
        settleDailyLoginIfNeeded()
    }

    suspend fun logoutActive() {
        oauthManager.logoutActiveAccount()
        refreshBootstrap()
    }
}
