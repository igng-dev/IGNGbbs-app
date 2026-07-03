package com.example.igngbbs

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.Selection
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.view.MotionEvent
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import org.json.JSONArray
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            IgngBbsTheme {
                val context = LocalContext.current
                val sessionStore = remember { SecureSessionStore(context) }
                val settingsStore = remember { AppSettingsStore(context) }
                BbsApp(
                    apiClient = remember {
                        BbsApiClient(BuildConfig.IGNG_BBS_BASE_URL) { sessionStore.token() }
                    },
                    ssoClient = remember { SsoApiClient(BuildConfig.IGNG_SSO_BASE_URL) },
                    sessionStore = sessionStore,
                    settingsStore = settingsStore
                )
            }
        }
    }
}

private sealed interface Screen {
    data object Home : Screen
    data object Columns : Screen
    data object Account : Screen
    data object History : Screen
    data object Notifications : Screen
    data object ConsoleHome : Screen
    data object ConsolePosts : Screen
    data object ConsolePostCreate : Screen
    data class ConsolePostEditor(val postId: Int) : Screen
    data class ConsolePostSettings(val postId: Int) : Screen
    data object ConsoleComments : Screen
    data object ConsoleReview : Screen
    data object ConsoleGroups : Screen
    data object ConsoleGroupCreate : Screen
    data class ConsoleGroupRename(val groupId: Int) : Screen
    data class ConsoleGroupMembers(val groupId: Int) : Screen
    data object ConsolePermissions : Screen
    data object ConsolePersonalization : Screen
    data object ConsoleColumns : Screen
    data class ConsoleColumnEdit(val columnId: Int) : Screen
    data class ConsoleColumnMembers(val columnId: Int) : Screen
    data object ConsoleSeries : Screen
    data class ConsoleSeriesEdit(val seriesId: Int) : Screen
    data class ConsoleSeriesManage(val seriesId: Int) : Screen
    data object ConsoleProfile : Screen
    data class UserProfile(val userId: Int, val title: String) : Screen
    data class FilteredPosts(val title: String, val categoryId: Int? = null, val tag: String? = null, val authorId: Int? = null, val columnId: Int? = null) : Screen
    data class Detail(val postId: Int, val fromInternalLink: Boolean = false) : Screen
    data class Reader(val postId: Int, val initialAnchor: String? = null) : Screen
}

private sealed interface LoadState {
    data object Loading : LoadState
    data class Error(val title: String, val message: String) : LoadState
    data object Content : LoadState
}

private sealed interface AuthState {
    data object Checking : AuthState
    data object Guest : AuthState
    data class SignedIn(val session: AuthSession) : AuthState
}

private data class DetailBlockedReason(
    val title: String,
    val message: String
)

private data class ReportTarget(
    val type: String,
    val id: Int,
    val label: String,
    val contentSnapshot: String,
    val titleSnapshot: String = ""
)

private data class ClipboardPrompt(
    val postId: Int,
    val url: String
)

private data class CreatorSeriesManageSession(
    val seriesId: Int,
    val posts: List<CreatorSeries.SeriesPostItem> = emptyList(),
    val savedPosts: List<CreatorSeries.SeriesPostItem> = emptyList(),
    val candidates: List<CreatorSeriesPostCandidate> = emptyList(),
    val query: String = "",
    val loadingCandidates: Boolean = false,
    val saving: Boolean = false
)

private data class TagEditorState(
    val items: List<String> = emptyList(),
    val draft: String = ""
)

private data class AnalyticsSheetTarget(
    val type: String,
    val id: Int?,
    val title: String,
    val description: String? = null,
    val userKey: Any
)

private data class AppBannerMessage(
    val id: Long,
    val text: String
)

private data class ArticleSectionEntry(
    val title: String,
    val anchor: String,
    val content: String
)

private const val ARTICLE_BODY_SELECTION_EPOCH_TAG = 0x1A2B3C4D

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun BbsApp(
    apiClient: BbsApiClient,
    ssoClient: SsoApiClient,
    sessionStore: SecureSessionStore,
    settingsStore: AppSettingsStore
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val cache = remember { mutableStateMapOf<Int, Post>() }
    var backStack by remember { mutableStateOf<List<Screen>>(listOf(Screen.Home)) }
    val screen = backStack.last()
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var columns by remember { mutableStateOf<List<ColumnItem>>(emptyList()) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var usedFallback by remember { mutableStateOf<String?>(null) }
    var detailBlockedReason by remember { mutableStateOf<DetailBlockedReason?>(null) }
    var showRestrictedDialog by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf<LoadState>(LoadState.Loading) }
    var showSeriesSheet by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var nextCursor by remember { mutableStateOf<Int?>(null) }
    var latestRequestId by remember { mutableIntStateOf(0) }
    var latestPostRequestId by remember { mutableIntStateOf(0) }
    var latestAuxRequestId by remember { mutableIntStateOf(0) }
    var navDirection by remember { mutableIntStateOf(1) }
    var authState by remember { mutableStateOf<AuthState>(AuthState.Checking) }
    var authError by remember { mutableStateOf<String?>(null) }
    var savedSessions by remember { mutableStateOf<List<AuthSession>>(emptyList()) }
    var selectionEpoch by remember { mutableIntStateOf(0) }
    var clipboardPrompt by remember { mutableStateOf<ClipboardPrompt?>(null) }
    var hapticsEnabled by remember { mutableStateOf(settingsStore.hapticsEnabled()) }
    var notificationSettings by remember { mutableStateOf(settingsStore.notificationSettings()) }
    var notificationPermissionGranted by remember { mutableStateOf(AppNotificationManager.hasPermission(context)) }
    var unreadNotificationCount by remember { mutableIntStateOf(0) }
    var notificationPromptItems by remember { mutableStateOf<List<NotificationRecipient>>(emptyList()) }
    var showNotificationPrompt by remember { mutableStateOf(false) }
    var notificationPromptAction by remember { mutableStateOf<String?>(null) }
    var latestNotificationRequestId by remember { mutableIntStateOf(0) }
    var appBannerMessage by remember { mutableStateOf<AppBannerMessage?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
        if (authState is AuthState.SignedIn && granted && notificationSettings.hasAnyEnabled()) {
            AppNotificationManager.ensureChannels(context)
            NotificationSyncWorker.schedule(context)
        } else {
            NotificationSyncWorker.cancel(context)
        }
    }
    val appHaptic = LocalHapticFeedback.current
    val viewConfiguration = LocalViewConfiguration.current

    fun performAppHaptic(type: HapticFeedbackType = HapticFeedbackType.TextHandleMove) {
        if (hapticsEnabled) {
            appHaptic.performHapticFeedback(type)
        }
    }

    fun showBanner(text: String) {
        appBannerMessage = AppBannerMessage(System.currentTimeMillis(), text)
    }

    fun clearSelections() {
        selectionEpoch++
    }

    fun inspectClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        if (!clipboard.hasPrimaryClip()) return
        val item = clipboard.primaryClip?.getItemAt(0) ?: return
        val text = item.coerceToText(context)?.toString()?.trim().orEmpty()
        if (text.isBlank()) return
        val postId = internalPostId(text) ?: return
        if (clipboardPrompt?.url == text) return
        clipboardPrompt = ClipboardPrompt(postId, text)
    }

    fun clearNotificationState() {
        unreadNotificationCount = 0
        notificationPromptItems = emptyList()
        showNotificationPrompt = false
        notificationPromptAction = null
        latestNotificationRequestId++
    }

    fun syncNotificationWorker() {
        if (authState !is AuthState.SignedIn) {
            NotificationSyncWorker.cancel(context)
            return
        }
        if (notificationPermissionGranted && notificationSettings.hasAnyEnabled()) {
            AppNotificationManager.ensureChannels(context)
            NotificationSyncWorker.schedule(context)
        } else {
            NotificationSyncWorker.cancel(context)
        }
    }

    fun updateNotificationPrompt(items: List<NotificationRecipient>) {
        notificationPromptItems = items
        if (items.isEmpty()) {
            showNotificationPrompt = false
            notificationPromptAction = null
        }
    }

    fun refreshNotifications(openPromptIfNeeded: Boolean = false) {
        val signedIn = authState as? AuthState.SignedIn
        if (signedIn == null) {
            clearNotificationState()
            return
        }
        val requestId = ++latestNotificationRequestId
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val count = apiClient.fetchUnreadNotificationCount()
                    val shouldLoadPrompt = openPromptIfNeeded &&
                            settingsStore.notificationPromptHiddenUntil() <= System.currentTimeMillis() &&
                            count > 0
                    val promptItems = if (shouldLoadPrompt) {
                        apiClient.fetchNotifications("10", true).items
                    } else {
                        emptyList()
                    }
                    count to promptItems
                }
            }.onSuccess { (count, promptItems) ->
                if (requestId != latestNotificationRequestId) return@onSuccess
                unreadNotificationCount = count
                when {
                    count <= 0 -> {
                        notificationPromptItems = emptyList()
                        showNotificationPrompt = false
                    }
                    openPromptIfNeeded && promptItems.isNotEmpty() -> {
                        notificationPromptItems = promptItems
                        showNotificationPrompt = true
                    }
                }
            }.onFailure {
                if (requestId != latestNotificationRequestId) return@onFailure
                if (unreadNotificationCount <= 0) {
                    showNotificationPrompt = false
                }
            }
        }
    }

    fun refreshNotificationPermissionState() {
        notificationPermissionGranted = AppNotificationManager.hasPermission(context)
        syncNotificationWorker()
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionGranted = true
            syncNotificationWorker()
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationPermissionGranted = true
            syncNotificationWorker()
            return
        }
        if (!settingsStore.notificationPermissionRequested()) {
            settingsStore.setNotificationPermissionRequested(true)
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                inspectClipboard()
                refreshNotificationPermissionState()
                if (authState is AuthState.SignedIn) {
                    refreshNotifications(openPromptIfNeeded = true)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(authState, notificationPermissionGranted, notificationSettings) {
        if (authState is AuthState.SignedIn &&
            !notificationPermissionGranted &&
            !settingsStore.notificationPermissionRequested()
        ) {
            requestNotificationPermission()
        }
        syncNotificationWorker()
    }

    LaunchedEffect(authState, notificationSettings, notificationPermissionGranted) {
        while (authState is AuthState.SignedIn) {
            if (notificationPermissionGranted && notificationSettings.systemEnabled) {
                refreshNotifications(openPromptIfNeeded = false)
            }
            if (notificationPermissionGranted) {
                runCatching {
                    withContext(Dispatchers.IO) {
                        val currentSettings = settingsStore.notificationSettings()
                        val api = BbsApiClient(BuildConfig.IGNG_BBS_BASE_URL) {
                            (authState as? AuthState.SignedIn)?.session?.sessionToken
                        }
                        if (currentSettings.systemEnabled) {
                            val items = api.fetchNotifications("20", true).items
                            val maxId = items.maxOfOrNull { it.id } ?: 0
                            val baseline = settingsStore.systemNotificationBaselineRecipientId()
                            if (baseline <= 0) {
                                settingsStore.setSystemNotificationBaselineRecipientId(maxId)
                            } else {
                                items.filter { it.id > baseline && it.isUnread() }
                                    .sortedBy { it.id }
                                    .forEach { item ->
                                        AppNotificationManager.showNotification(
                                            context,
                                            10_000 + item.id,
                                            item.title.ifBlank { "新的系统通知" },
                                            item.content.ifBlank { "你收到了一条新的系统通知" }
                                        )
                                    }
                                if (maxId > baseline) {
                                    settingsStore.setSystemNotificationBaselineRecipientId(maxId)
                                }
                            }
                        }
                        if (currentSettings.replyEnabled) {
                            val comments = api.fetchCreatorComments("received", 20)
                            val unreadItems = comments.filter { it.mentionUnread && it.mentionId > 0 }
                            val maxMentionId = unreadItems.maxOfOrNull { it.mentionId } ?: 0
                            val baseline = settingsStore.replyNotificationBaselineMentionId()
                            if (baseline <= 0) {
                                settingsStore.setReplyNotificationBaselineMentionId(maxMentionId)
                            } else {
                                unreadItems.filter { it.mentionId > baseline }
                                    .sortedBy { it.mentionId }
                                    .forEach { item ->
                                        val title = if (item.postTitle.isNotBlank()) {
                                            "${item.authorName.ifBlank { "有用户" }} 回复了《${item.postTitle}》"
                                        } else {
                                            "${item.authorName.ifBlank { "有用户" }} 回复了你"
                                        }
                                        AppNotificationManager.showNotification(
                                            context,
                                            20_000 + item.mentionId,
                                            title,
                                            item.content.ifBlank { "点击查看回复内容" }
                                        )
                                    }
                                if (maxMentionId > baseline) {
                                    settingsStore.setReplyNotificationBaselineMentionId(maxMentionId)
                                }
                            }
                        }
                        if (currentSettings.dailyEnabled) {
                            val overview = api.fetchCreatorOverview()
                            val todayKey = java.time.LocalDate.now().toString()
                            val lastKnownDate = settingsStore.dailyReportLastKnownDate()
                            val lastSentDate = settingsStore.dailyReportLastSentDate()
                            if (lastKnownDate != todayKey) {
                                settingsStore.setDailyReportLastKnownDate(todayKey)
                                settingsStore.setDailyReportLastKnownViews(overview.todayViews)
                                if (lastSentDate != todayKey) {
                                    AppNotificationManager.showNotification(
                                        context,
                                        30_000,
                                        "今日浏览日报",
                                        "你今天的文章浏览量为 ${overview.todayViews}"
                                    )
                                    settingsStore.setDailyReportLastSentDate(todayKey)
                                }
                            } else {
                                val lastViews = settingsStore.dailyReportLastKnownViews()
                                if (lastSentDate != todayKey && lastViews != overview.todayViews) {
                                    AppNotificationManager.showNotification(
                                        context,
                                        30_000,
                                        "今日浏览日报",
                                        "你今天的文章浏览量更新为 ${overview.todayViews}"
                                    )
                                    settingsStore.setDailyReportLastSentDate(todayKey)
                                }
                                settingsStore.setDailyReportLastKnownViews(overview.todayViews)
                            }
                        }
                    }
                }
            }
            delay(60_000)
        }
    }

    fun loadPosts(titleScreen: Screen = Screen.Home, reset: Boolean = true) {
        if (!reset && (isLoadingMore || nextCursor == null)) return
        val requestId = ++latestRequestId
        val requestCursor = if (reset) null else nextCursor
        if (reset) {
            if (posts.isEmpty()) state = LoadState.Loading
            isRefreshing = posts.isNotEmpty()
            nextCursor = null
        } else {
            isLoadingMore = true
        }
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    when (titleScreen) {
                        is Screen.FilteredPosts -> apiClient.fetchPostsPage(
                            20,
                            requestCursor,
                            titleScreen.categoryId,
                            titleScreen.tag,
                            titleScreen.authorId,
                            titleScreen.columnId
                        )
                        Screen.Account -> PostPage(emptyList(), null)
                        else -> apiClient.fetchPostsPage(20, requestCursor, null, null, null, null)
                    }
                }
            }.onSuccess { page ->
                if (requestId != latestRequestId) return@onSuccess
                val visiblePosts = page.posts.filter { it.shouldAppearInAppList() }
                posts = if (reset) {
                    visiblePosts
                } else {
                    (posts + visiblePosts).distinctBy { it.id }
                }
                visiblePosts.forEach { cache[it.id] = it }
                nextCursor = page.nextCursor
                state = LoadState.Content
            }.onFailure {
                if (requestId == latestRequestId && reset) {
                    state = LoadState.Error("文章加载失败", it.message ?: "请稍后重试")
                }
            }.also {
                if (requestId == latestRequestId) {
                    isRefreshing = false
                    isLoadingMore = false
                }
            }
        }
    }

    fun loadColumns() {
        val requestId = ++latestAuxRequestId
        state = LoadState.Loading
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchColumns() }
            }.onSuccess {
                if (requestId != latestAuxRequestId) return@onSuccess
                columns = it
                state = LoadState.Content
            }.onFailure {
                if (requestId != latestAuxRequestId) return@onFailure
                state = LoadState.Error("专栏加载失败", it.message ?: "请稍后重试")
            }
        }
    }

    fun loadPost(postId: Int) {
        val requestId = ++latestPostRequestId
        state = LoadState.Loading
        usedFallback = null
        detailBlockedReason = null
        showRestrictedDialog = false
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchPost(postId) }
            }.onSuccess {
                if (requestId != latestPostRequestId) return@onSuccess
                selectedPost = it
                detailBlockedReason = null
                if (it.requiresWarning && (screen as? Screen.Detail)?.fromInternalLink == true) {
                    showRestrictedDialog = true
                }
                state = LoadState.Content
            }.onFailure { error ->
                if (requestId != latestPostRequestId) return@onFailure
                selectedPost = null
                if (error is ApiException && error.isAccessDenied()) {
                    detailBlockedReason = DetailBlockedReason(blockedTitle(error), error.message ?: "无法查看这篇文章")
                    state = LoadState.Content
                    return@onFailure
                }
                val cached = cache[postId]
                if (cached != null) {
                    selectedPost = cached
                    usedFallback = error.message ?: "详情接口暂时不可用"
                    state = LoadState.Content
                } else {
                    state = LoadState.Error("文章无法打开", error.message ?: "请稍后重试")
                }
            }
        }
    }

    fun loadUserProfile(userId: Int) {
        val requestId = ++latestAuxRequestId
        state = LoadState.Loading
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchUserProfile(userId) }
            }.onSuccess {
                if (requestId != latestAuxRequestId) return@onSuccess
                userProfile = it
                state = LoadState.Content
            }.onFailure {
                if (requestId != latestAuxRequestId) return@onFailure
                state = LoadState.Error("用户资料加载失败", it.message ?: "请稍后重试")
            }
        }
    }

    fun refreshCurrent() {
        when (val current = screen) {
            Screen.Home -> loadPosts(Screen.Home, reset = true)
            Screen.Columns -> loadColumns()
            Screen.Account -> Unit
            Screen.History -> Unit
            Screen.Notifications -> Unit
            Screen.ConsoleHome -> Unit
            Screen.ConsolePosts -> Unit
            Screen.ConsolePostCreate -> Unit
            is Screen.ConsolePostEditor -> Unit
            is Screen.ConsolePostSettings -> Unit
            Screen.ConsoleComments -> Unit
            Screen.ConsoleReview -> Unit
            Screen.ConsoleGroups -> Unit
            Screen.ConsoleGroupCreate -> Unit
            is Screen.ConsoleGroupRename -> Unit
            is Screen.ConsoleGroupMembers -> Unit
            Screen.ConsolePermissions -> Unit
            Screen.ConsolePersonalization -> Unit
            Screen.ConsoleColumns -> Unit
            is Screen.ConsoleColumnEdit -> Unit
            is Screen.ConsoleColumnMembers -> Unit
            Screen.ConsoleSeries -> Unit
            is Screen.ConsoleSeriesEdit -> Unit
            is Screen.ConsoleSeriesManage -> Unit
            Screen.ConsoleProfile -> Unit
            is Screen.FilteredPosts -> loadPosts(current, reset = true)
            is Screen.UserProfile -> loadUserProfile(current.userId)
            is Screen.Detail -> loadPost(current.postId)
            is Screen.Reader -> loadPost(current.postId)
        }
    }

    fun dockIndex(target: Screen): Int? {
        return when (target) {
            Screen.Home -> 0
            Screen.Columns -> 1
            Screen.ConsoleHome -> 2
            Screen.Account -> 3
            Screen.History -> null
            Screen.Notifications -> null
            is Screen.FilteredPosts -> 0
            Screen.ConsolePosts,
            Screen.ConsolePostCreate,
            is Screen.ConsolePostEditor,
            is Screen.ConsolePostSettings,
            Screen.ConsoleComments,
            Screen.ConsoleReview,
            Screen.ConsoleGroups,
            Screen.ConsoleGroupCreate,
            is Screen.ConsoleGroupRename,
            is Screen.ConsoleGroupMembers,
            Screen.ConsolePermissions,
            Screen.ConsolePersonalization,
            Screen.ConsoleColumns,
            is Screen.ConsoleColumnEdit,
            is Screen.ConsoleColumnMembers,
            Screen.ConsoleSeries,
            is Screen.ConsoleSeriesEdit,
            is Screen.ConsoleSeriesManage,
            Screen.ConsoleProfile -> 2
            else -> null
        }
    }

    fun openScreen(next: Screen, replace: Boolean = false, preserveNavDirection: Boolean = false) {
        if (!preserveNavDirection) {
            navDirection = 1
        }
        backStack = if (replace) {
            listOf(next)
        } else {
            backStack + next
        }
        selectedPost = null
        userProfile = null
        usedFallback = null
        detailBlockedReason = null
        showRestrictedDialog = false
        showSeriesSheet = false
        isLoadingMore = false
        isRefreshing = false
        latestAuxRequestId++
        latestPostRequestId++
        if (next !is Screen.Detail) {
            posts = emptyList()
            nextCursor = null
            latestRequestId++
        } else {
            latestRequestId++
        }
        when (next) {
            Screen.Home -> loadPosts(next, reset = true)
            Screen.Columns -> loadColumns()
            Screen.Account -> state = LoadState.Content
            Screen.History -> state = LoadState.Content
            Screen.Notifications -> state = LoadState.Content
            Screen.ConsoleHome -> state = LoadState.Content
            Screen.ConsolePosts -> state = LoadState.Content
            Screen.ConsolePostCreate -> state = LoadState.Content
            is Screen.ConsolePostEditor -> state = LoadState.Content
            is Screen.ConsolePostSettings -> state = LoadState.Content
            Screen.ConsoleComments -> state = LoadState.Content
            Screen.ConsoleReview -> state = LoadState.Content
            Screen.ConsoleGroups -> state = LoadState.Content
            Screen.ConsoleGroupCreate -> state = LoadState.Content
            is Screen.ConsoleGroupRename -> state = LoadState.Content
            is Screen.ConsoleGroupMembers -> state = LoadState.Content
            Screen.ConsolePermissions -> state = LoadState.Content
            Screen.ConsolePersonalization -> state = LoadState.Content
            Screen.ConsoleColumns -> state = LoadState.Content
            is Screen.ConsoleColumnEdit -> state = LoadState.Content
            is Screen.ConsoleColumnMembers -> state = LoadState.Content
            Screen.ConsoleSeries -> state = LoadState.Content
            is Screen.ConsoleSeriesEdit -> state = LoadState.Content
            is Screen.ConsoleSeriesManage -> state = LoadState.Content
            Screen.ConsoleProfile -> state = LoadState.Content
            is Screen.FilteredPosts -> loadPosts(next, reset = true)
            is Screen.UserProfile -> loadUserProfile(next.userId)
            is Screen.Detail -> loadPost(next.postId)
            is Screen.Reader -> loadPost(next.postId)
        }
    }

    fun openDockScreen(next: Screen) {
        val currentIndex = dockIndex(screen)
        val nextIndex = dockIndex(next)
        navDirection = if (currentIndex != null && nextIndex != null) {
            nextIndex.compareTo(currentIndex).coerceIn(-1, 1).takeIf { it != 0 } ?: 1
        } else {
            1
        }
        openScreen(next, replace = true, preserveNavDirection = true)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            navDirection = -1
            val leaving = backStack.last()
            backStack = backStack.dropLast(1)
            showSeriesSheet = false
            usedFallback = null
            selectedPost = null
            userProfile = null
            detailBlockedReason = null
            showRestrictedDialog = false
            latestRequestId++
            latestPostRequestId++
            latestAuxRequestId++
            isLoadingMore = false
            isRefreshing = false
            when (val current = backStack.last()) {
                Screen.Home -> {
                    if (leaving !is Screen.Detail) loadPosts(current, reset = true) else state = LoadState.Content
                }
                Screen.Columns -> loadColumns()
                Screen.Account -> state = LoadState.Content
                Screen.History -> state = LoadState.Content
                Screen.Notifications -> state = LoadState.Content
                Screen.ConsoleHome -> state = LoadState.Content
                Screen.ConsolePosts -> state = LoadState.Content
            Screen.ConsolePostCreate -> state = LoadState.Content
            is Screen.ConsolePostEditor -> state = LoadState.Content
                is Screen.ConsolePostSettings -> state = LoadState.Content
                Screen.ConsoleComments -> state = LoadState.Content
                Screen.ConsoleReview -> state = LoadState.Content
                Screen.ConsoleGroups -> state = LoadState.Content
                Screen.ConsoleGroupCreate -> state = LoadState.Content
                is Screen.ConsoleGroupRename -> state = LoadState.Content
                is Screen.ConsoleGroupMembers -> state = LoadState.Content
                Screen.ConsolePermissions -> state = LoadState.Content
                Screen.ConsolePersonalization -> state = LoadState.Content
                Screen.ConsoleColumns -> state = LoadState.Content
                is Screen.ConsoleColumnEdit -> state = LoadState.Content
                is Screen.ConsoleColumnMembers -> state = LoadState.Content
                Screen.ConsoleSeries -> state = LoadState.Content
                is Screen.ConsoleSeriesEdit -> state = LoadState.Content
                is Screen.ConsoleSeriesManage -> state = LoadState.Content
                Screen.ConsoleProfile -> state = LoadState.Content
                is Screen.FilteredPosts -> loadPosts(current, reset = true)
                is Screen.UserProfile -> loadUserProfile(current.userId)
                is Screen.Detail -> loadPost(current.postId)
                is Screen.Reader -> loadPost(current.postId)
            }
        } else if (screen !is Screen.Home) {
            navDirection = -1
            backStack = listOf(Screen.Home)
            selectedPost = null
            userProfile = null
            detailBlockedReason = null
            showRestrictedDialog = false
            latestRequestId++
            latestPostRequestId++
            latestAuxRequestId++
            isLoadingMore = false
            isRefreshing = false
            usedFallback = null
            showSeriesSheet = false
            if (posts.isEmpty()) loadPosts(Screen.Home, reset = true) else state = LoadState.Content
        }
    }

    suspend fun restoreSessionNow() {
        authState = AuthState.Checking
        authError = null
        savedSessions = sessionStore.savedSessions()
        val token = sessionStore.token()
        if (token.isNullOrBlank()) {
            authState = AuthState.Guest
            clearNotificationState()
            return
        }
        runCatching {
            withContext(Dispatchers.IO) { ssoClient.me(token) }
        }.onSuccess {
            sessionStore.save(it)
            authState = AuthState.SignedIn(it)
            savedSessions = sessionStore.savedSessions()
            refreshNotifications(openPromptIfNeeded = true)
        }.onFailure {
            sessionStore.removeActive()
            savedSessions = sessionStore.savedSessions()
            authState = AuthState.Guest
            authError = it.message ?: "登录状态已失效"
            clearNotificationState()
        }
    }

    fun restoreSession() {
        scope.launch { restoreSessionNow() }
    }

    fun logout() {
        val token = sessionStore.token()
        val activeUserId = (authState as? AuthState.SignedIn)?.session?.user?.id
        authState = AuthState.Checking
        scope.launch {
            runCatching {
                if (!token.isNullOrBlank()) {
                    withContext(Dispatchers.IO) { ssoClient.logout(token) }
                }
            }
            if (activeUserId != null && activeUserId > 0) {
                sessionStore.remove(activeUserId)
            } else {
                sessionStore.removeActive()
            }
            savedSessions = sessionStore.savedSessions()
            authState = AuthState.Guest
            cache.clear()
            clearNotificationState()
            if (screen is Screen.Home || screen is Screen.FilteredPosts || screen is Screen.Columns) {
                refreshCurrent()
            }
        }
    }

    fun onLoginSuccess(session: AuthSession) {
        sessionStore.save(session)
        authState = AuthState.SignedIn(session)
        savedSessions = sessionStore.savedSessions()
        authError = null
        cache.clear()
        refreshNotifications(openPromptIfNeeded = true)
        if (screen !is Screen.Account) {
            refreshCurrent()
        }
    }

    fun switchAccount(session: AuthSession) {
        val previousSession = sessionStore.cachedSession()
        authState = AuthState.Checking
        authError = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { ssoClient.me(session.sessionToken) }
            }.onSuccess { verified ->
                sessionStore.save(verified)
                authState = AuthState.SignedIn(verified)
                savedSessions = sessionStore.savedSessions()
                cache.clear()
                refreshNotifications(openPromptIfNeeded = true)
                if (screen !is Screen.Account) {
                    refreshCurrent()
                }
            }.onFailure {
                savedSessions = sessionStore.savedSessions()
                authState = previousSession?.let { AuthState.SignedIn(it) } ?: AuthState.Guest
                authError = it.message ?: "账号验证失败，未切换"
                if (authState !is AuthState.SignedIn) {
                    clearNotificationState()
                }
            }
        }
    }

    fun useGuestAccount() {
        sessionStore.useGuest()
        authState = AuthState.Guest
        savedSessions = sessionStore.savedSessions()
        authError = null
        cache.clear()
        clearNotificationState()
        if (screen !is Screen.Account) {
            refreshCurrent()
        }
    }

    LaunchedEffect(Unit) {
        inspectClipboard()
        savedSessions = sessionStore.savedSessions()
        val cachedSession = sessionStore.cachedSession()
        authState = cachedSession?.let { AuthState.SignedIn(it) } ?: AuthState.Guest
        loadPosts(Screen.Home, reset = true)
        restoreSessionNow()
        val verifiedSession = (authState as? AuthState.SignedIn)?.session
        val authChanged = cachedSession?.user?.id != verifiedSession?.user?.id || (cachedSession != null && verifiedSession == null)
        if (authChanged && backStack.last() is Screen.Home) {
            loadPosts(Screen.Home, reset = true)
        }
    }
    BackHandler(enabled = backStack.size > 1 || screen !is Screen.Home) {
        navigateBack()
    }
    val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh = { refreshCurrent() })

    Scaffold(
        modifier = Modifier.pointerInput(hapticsEnabled) {
            if (!hapticsEnabled) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var moved = false
                var released = false
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Final)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    val dx = change.position.x - down.position.x
                    val dy = change.position.y - down.position.y
                    if (abs(dx) > viewConfiguration.touchSlop || abs(dy) > viewConfiguration.touchSlop) {
                        moved = true
                    }
                    if (!change.pressed) {
                        released = true
                        break
                    }
                }
                if (released && !moved) {
                    performAppHaptic()
                }
            }
        },
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title = {
                    Text(
                        text = screenTitle(screen),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (screen is Screen.Detail || screen is Screen.Reader || screen is Screen.FilteredPosts || screen is Screen.UserProfile || screen is Screen.History || screen is Screen.Notifications || screen is Screen.ConsoleHome || screen is Screen.ConsolePosts || screen is Screen.ConsolePostCreate || screen is Screen.ConsolePostEditor || screen is Screen.ConsolePostSettings || screen is Screen.ConsoleComments || screen is Screen.ConsoleReview || screen is Screen.ConsoleGroups || screen is Screen.ConsoleGroupCreate || screen is Screen.ConsoleGroupRename || screen is Screen.ConsoleGroupMembers || screen is Screen.ConsolePermissions || screen is Screen.ConsolePersonalization || screen is Screen.ConsoleColumns || screen is Screen.ConsoleColumnEdit || screen is Screen.ConsoleColumnMembers || screen is Screen.ConsoleSeries || screen is Screen.ConsoleSeriesEdit || screen is Screen.ConsoleSeriesManage || screen is Screen.ConsoleProfile) {
                        IconButton(onClick = {
                            navigateBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (authState is AuthState.SignedIn) {
                        IconButton(onClick = { openScreen(Screen.Notifications) }) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(Icons.Default.Notifications, contentDescription = "通知中心")
                                if (unreadNotificationCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = 5.dp, y = (-3).dp)
                                            .size(if (unreadNotificationCount > 9) 18.dp else 14.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                            color = MaterialTheme.colorScheme.onError,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                )
            )
        },
        bottomBar = {
            if (screen !is Screen.Detail && screen !is Screen.Reader && screen !is Screen.ConsolePostEditor) {
                NavigationBar {
                    NavigationBarItem(
                        selected = screen is Screen.Home || screen is Screen.FilteredPosts,
                        onClick = { openDockScreen(Screen.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("首页") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Columns,
                        onClick = { openDockScreen(Screen.Columns) },
                        icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null) },
                        label = { Text("专栏") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.ConsoleHome || screen is Screen.ConsolePosts || screen is Screen.ConsolePostCreate || screen is Screen.ConsolePostEditor || screen is Screen.ConsolePostSettings || screen is Screen.ConsoleComments || screen is Screen.ConsoleReview || screen is Screen.ConsoleGroups || screen is Screen.ConsoleGroupCreate || screen is Screen.ConsoleGroupRename || screen is Screen.ConsoleGroupMembers || screen is Screen.ConsolePermissions || screen is Screen.ConsolePersonalization || screen is Screen.ConsoleColumns || screen is Screen.ConsoleColumnEdit || screen is Screen.ConsoleColumnMembers || screen is Screen.ConsoleSeries || screen is Screen.ConsoleSeriesEdit || screen is Screen.ConsoleSeriesManage || screen is Screen.ConsoleProfile,
                        onClick = { openDockScreen(Screen.ConsoleHome) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("控制台") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Account,
                        onClick = { openDockScreen(Screen.Account) },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("我的") }
                    )
                }
            }
        },
        floatingActionButton = {
            val detailPost = selectedPost
            if (screen is Screen.Detail && detailPost != null && detailPost.seriesPosts.isNotEmpty()) {
                FloatingActionButton(onClick = { showSeriesSheet = true }) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "系列文章")
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                    .pullRefresh(pullRefreshState)
            ) {
                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        val inDirection = if (navDirection >= 0) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        val outDirection = if (navDirection >= 0) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        fadeIn(tween(180)) + slideIntoContainer(inDirection, tween(240)) togetherWith
                                fadeOut(tween(120)) + slideOutOfContainer(outDirection, tween(220))
                    },
                    label = "navigation"
                ) { targetScreen ->
                    when (val currentState = state) {
                        LoadState.Loading -> LoadingState()
                        is LoadState.Error -> ErrorState(
                            title = currentState.title,
                            message = currentState.message,
                            onRetry = { refreshCurrent() }
                        )
                        LoadState.Content -> when (targetScreen) {
                            Screen.Home -> PostListScreen(
                                listKey = "home",
                                apiClient = apiClient,
                                posts = posts,
                                emptyText = "暂无可公开展示的文章",
                                searchHint = "搜索首页文章，可输入关键词、ID 或链接",
                                isLoadingMore = isLoadingMore,
                                canLoadMore = nextCursor != null,
                                loadMoreKey = nextCursor,
                                onLoadMore = { loadPosts(Screen.Home, reset = false) },
                                onPostClick = { openScreen(Screen.Detail(it.id)) },
                                onDirectPostOpen = { openScreen(Screen.Detail(it, fromInternalLink = true)) },
                                onCategoryClick = { openScreen(Screen.FilteredPosts("分类：${it.categoryName}", categoryId = it.categoryId)) },
                                onTagClick = { openScreen(Screen.FilteredPosts("标签：$it", tag = it)) },
                                onAuthorClick = { openScreen(Screen.UserProfile(it.userId, it.authorName)) }
                            )
                            is Screen.FilteredPosts -> PostListScreen(
                                listKey = "filtered-${targetScreen.categoryId}-${targetScreen.tag}-${targetScreen.authorId}-${targetScreen.columnId}",
                                apiClient = apiClient,
                                posts = posts,
                                emptyText = "没有符合条件的文章",
                                searchHint = "搜索当前列表文章，可输入关键词、ID 或链接",
                                categoryId = targetScreen.categoryId,
                                tag = targetScreen.tag,
                                authorId = targetScreen.authorId,
                                columnId = targetScreen.columnId,
                                isLoadingMore = isLoadingMore,
                                canLoadMore = nextCursor != null,
                                loadMoreKey = nextCursor,
                                onLoadMore = { loadPosts(targetScreen, reset = false) },
                                onPostClick = { openScreen(Screen.Detail(it.id)) },
                                onDirectPostOpen = { openScreen(Screen.Detail(it, fromInternalLink = true)) },
                                onCategoryClick = { openScreen(Screen.FilteredPosts("分类：${it.categoryName}", categoryId = it.categoryId)) },
                                onTagClick = { openScreen(Screen.FilteredPosts("标签：$it", tag = it)) },
                                onAuthorClick = { openScreen(Screen.UserProfile(it.userId, it.authorName)) }
                            )
                        is Screen.UserProfile -> UserProfileScreen(
                            profile = userProfile,
                            onViewPosts = { profile ->
                                openScreen(Screen.FilteredPosts("用户：${profile.displayName()}", authorId = profile.id))
                            },
                            onPostClick = { openScreen(Screen.Detail(it)) }
                        )
                            Screen.Columns -> ColumnsScreen(
                                columns = columns,
                                onColumnClick = { openScreen(Screen.FilteredPosts("专栏：${it.name}", columnId = it.id)) }
                            )
                            Screen.Account -> AccountScreen(
                                authState = authState,
                                authError = authError,
                                savedSessions = savedSessions,
                                hapticsEnabled = hapticsEnabled,
                                notificationSettings = notificationSettings,
                                notificationPermissionGranted = notificationPermissionGranted,
                                onHapticsEnabledChange = {
                                    hapticsEnabled = it
                                    settingsStore.setHapticsEnabled(it)
                                },
                                onNotificationSettingsChange = {
                                    notificationSettings = it
                                    settingsStore.setNotificationSettings(it)
                                    syncNotificationWorker()
                                },
                                onRequestNotificationPermission = { requestNotificationPermission() },
                                apiClient = apiClient,
                                ssoClient = ssoClient,
                                onLoginSuccess = { onLoginSuccess(it) },
                                onSwitchAccount = { switchAccount(it) },
                                onUseGuest = { useGuestAccount() },
                                onLogout = { logout() },
                                onRetrySession = { restoreSession() },
                                onOpenHistory = { openScreen(Screen.History) }
                            )
                            Screen.History -> HistoryScreen(
                                apiClient = apiClient,
                                authState = authState,
                                onOpenPost = { openScreen(Screen.Detail(it)) },
                                onShowBanner = { showBanner(it) }
                            )
                            Screen.Notifications -> NotificationCenterScreen(
                                apiClient = apiClient,
                                authState = authState,
                                unreadCount = unreadNotificationCount,
                                onUnreadCountChange = { unreadNotificationCount = it.coerceAtLeast(0) },
                                onNotificationStateRefresh = { refreshNotifications(false) }
                            )
                            Screen.ConsoleHome -> CreatorOverviewScreen(
                                apiClient = apiClient,
                                authState = authState,
                                onOpenPosts = { openScreen(Screen.ConsolePosts) },
                                onOpenComments = { openScreen(Screen.ConsoleComments) },
                                onOpenReview = { openScreen(Screen.ConsoleReview) },
                                onOpenGroups = { openScreen(Screen.ConsoleGroups) },
                                onOpenPermissions = { openScreen(Screen.ConsolePermissions) },
                                onOpenPersonalization = { openScreen(Screen.ConsolePersonalization) },
                                onOpenColumns = { openScreen(Screen.ConsoleColumns) },
                                onOpenSeries = { openScreen(Screen.ConsoleSeries) },
                                onOpenProfile = { openScreen(Screen.ConsoleProfile) }
                            )
                            Screen.ConsolePosts -> CreatorPostsScreen(
                                apiClient = apiClient,
                                authState = authState,
                                onOpenPost = { openScreen(Screen.Detail(it)) },
                                onCreatePost = { openScreen(Screen.ConsolePostCreate) },
                                onEditPost = { openScreen(Screen.ConsolePostEditor(it)) },
                                onOpenSettings = { openScreen(Screen.ConsolePostSettings(it)) },
                                onDirectOpenPost = { openScreen(Screen.Detail(it, fromInternalLink = true)) }
                            )
                            Screen.ConsolePostCreate -> CreatorPostCreateScreen(
                                apiClient = apiClient,
                                authState = authState,
                                onClose = { navigateBack() },
                                onCreated = { openScreen(Screen.ConsolePostEditor(it), replace = true) }
                            )
                            is Screen.ConsolePostEditor -> CreatorPostEditorScreen(
                                apiClient = apiClient,
                                authState = authState,
                                postId = targetScreen.postId,
                                onClose = { navigateBack() }
                            )
                            is Screen.ConsolePostSettings -> CreatorPostSettingsScreen(
                                apiClient = apiClient,
                                authState = authState,
                                postId = targetScreen.postId,
                                onClose = { navigateBack() }
                            )
                            Screen.ConsoleComments -> CreatorCommentsScreen(
                                apiClient = apiClient,
                                authState = authState,
                                onOpenPost = { openScreen(Screen.Detail(it)) }
                            )
                            Screen.ConsoleReview -> CreatorReviewCenterScreen(
                                apiClient = apiClient,
                                authState = authState
                            )
                            Screen.ConsoleGroups -> CreatorGroupsScreen(
                                apiClient = apiClient,
                                authState = authState,
                                onCreateGroup = { openScreen(Screen.ConsoleGroupCreate) },
                                onRenameGroup = { openScreen(Screen.ConsoleGroupRename(it)) },
                                onManageMembers = { openScreen(Screen.ConsoleGroupMembers(it)) }
                            )
                            Screen.ConsoleGroupCreate -> CreatorGroupCreateScreen(
                                apiClient = apiClient,
                                authState = authState,
                                onClose = { navigateBack() }
                            )
                            is Screen.ConsoleGroupRename -> CreatorGroupRenameScreen(
                                apiClient = apiClient,
                                authState = authState,
                                groupId = targetScreen.groupId,
                                onClose = { navigateBack() }
                            )
                            is Screen.ConsoleGroupMembers -> CreatorGroupMembersScreen(
                                apiClient = apiClient,
                                authState = authState,
                                groupId = targetScreen.groupId,
                                onClose = { navigateBack() }
                            )
                            Screen.ConsolePermissions -> CreatorPermissionsScreen(
                                apiClient = apiClient,
                                authState = authState
                            )
                            Screen.ConsolePersonalization -> CreatorPersonalizationScreen(
                                apiClient = apiClient,
                                authState = authState
                            )
                            Screen.ConsoleColumns -> CreatorColumnsScreen(
                                apiClient = apiClient,
                                authState = authState,
                                onEditColumn = { openScreen(Screen.ConsoleColumnEdit(it)) },
                                onManageMembers = { openScreen(Screen.ConsoleColumnMembers(it)) }
                            )
                            is Screen.ConsoleColumnEdit -> CreatorColumnEditScreen(
                                apiClient = apiClient,
                                authState = authState,
                                columnId = targetScreen.columnId,
                                onClose = { navigateBack() }
                            )
                            is Screen.ConsoleColumnMembers -> CreatorColumnMembersScreen(
                                apiClient = apiClient,
                                authState = authState,
                                columnId = targetScreen.columnId,
                                onClose = { navigateBack() }
                            )
                            Screen.ConsoleSeries -> CreatorSeriesScreen(
                                apiClient = apiClient,
                                authState = authState,
                                onOpenPost = { openScreen(Screen.Detail(it)) },
                                onManageSeries = { openScreen(Screen.ConsoleSeriesManage(it)) },
                                onEditSeries = { openScreen(Screen.ConsoleSeriesEdit(it)) }
                            )
                            is Screen.ConsoleSeriesEdit -> CreatorSeriesEditScreen(
                                apiClient = apiClient,
                                authState = authState,
                                seriesId = targetScreen.seriesId,
                                onClose = { navigateBack() }
                            )
                            is Screen.ConsoleSeriesManage -> CreatorSeriesManageScreen(
                                apiClient = apiClient,
                                authState = authState,
                                seriesId = targetScreen.seriesId,
                                hapticsEnabled = hapticsEnabled,
                                onClose = { navigateBack() }
                            )
                            Screen.ConsoleProfile -> CreatorProfileScreen(
                                apiClient = apiClient,
                                authState = authState
                            )
                            is Screen.Detail -> DetailScreen(
                                post = selectedPost,
                                fallbackReason = usedFallback,
                                blockedReason = detailBlockedReason,
                                showRestrictedDialog = showRestrictedDialog,
                                onDismissRestrictedDialog = { showRestrictedDialog = false },
                                apiClient = apiClient,
                                authState = authState,
                                selectionEpoch = selectionEpoch,
                                onCategoryClick = { post -> openScreen(Screen.FilteredPosts("分类：${post.categoryName}", categoryId = post.categoryId)) },
                                onTagClick = { tag -> openScreen(Screen.FilteredPosts("标签：$tag", tag = tag)) },
                                onAuthorClick = { post -> openScreen(Screen.UserProfile(post.userId, post.authorName)) },
                                onCommentAuthorClick = { comment -> if (comment.userId > 0) openScreen(Screen.UserProfile(comment.userId, comment.authorName)) },
                                onOpenReader = { postId, anchor -> openScreen(Screen.Reader(postId, anchor)) },
                                onSharePost = { post ->
                                    val link = "https://www.igngbbs.net/post/${post.id}"
                                    val shareText = buildString {
                                        append(post.title.ifBlank { "未命名文章" })
                                        append("\n")
                                        if (post.summary.isNotBlank()) {
                                            append(post.summary.trim())
                                            append("\n")
                                        }
                                        append(link)
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "分享文章"))
                                },
                                onCopyPostLink = { post ->
                                    val link = "https://www.igngbbs.net/post/${post.id}"
                                    val text = buildString {
                                        append(post.title.ifBlank { "未命名文章" })
                                        append("\n")
                                        append(link)
                                    }
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("IGNGbbs 文章链接", text))
                                    showBanner("文章链接已复制")
                                },
                                onShowBanner = { showBanner(it) },
                                onOpenInternalPost = { postId -> openScreen(Screen.Detail(postId, fromInternalLink = true)) }
                            )
                            is Screen.Reader -> ReaderScreen(
                                post = selectedPost,
                                initialAnchor = targetScreen.initialAnchor,
                                apiClient = apiClient,
                                onExit = { navigateBack() },
                                onShowBanner = { showBanner(it) }
                            )
                        }
                    }
                }

                appBannerMessage?.let { banner ->
                    TopBannerNotice(
                        message = banner,
                        onDismiss = {
                            if (appBannerMessage?.id == banner.id) {
                                appBannerMessage = null
                            }
                        }
                    )
                }

                clipboardPrompt?.let { prompt ->
                    AlertDialog(
                        onDismissRequest = {
                            clipboardPrompt = null
                        },
                        title = { Text("检测到站内链接") },
                        text = { Text("剪切板中包含 IGNGbbs 站内文章链接，是否立即查看？") },
                        confirmButton = {
                            TextButton(onClick = {
                                clipboardPrompt = null
                                openScreen(Screen.Detail(prompt.postId, fromInternalLink = true))
                            }) {
                                Text("查看文章")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                clipboardPrompt = null
                            }) {
                                Text("忽略")
                            }
                        }
                    )
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                val detailPost = selectedPost
                if (showSeriesSheet && detailPost != null) {
                    ModalBottomSheet(onDismissRequest = { showSeriesSheet = false }) {
                        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
                            Text(
                                text = detailPost.seriesPosts.firstOrNull()?.seriesName?.ifBlank { "系列文章" } ?: "系列文章",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))
                            detailPost.seriesPosts.forEach { item ->
                                Text(
                                    text = item.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showSeriesSheet = false
                                            openScreen(Screen.Detail(item.postId))
                                        }
                                        .padding(vertical = 14.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (item.postId == detailPost.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNotificationPrompt && authState is AuthState.SignedIn && notificationPromptItems.isNotEmpty()) {
        SiteNotificationPromptSheet(
            notificationItems = notificationPromptItems,
            onDismiss = { showNotificationPrompt = false },
            onHideOneDay = {
                notificationPromptAction = null
                settingsStore.setNotificationPromptHiddenUntil(System.currentTimeMillis() + 24L * 60L * 60L * 1000L)
                showNotificationPrompt = false
            },
            onOpenNotifications = {
                notificationPromptAction = null
                showNotificationPrompt = false
                openScreen(Screen.Notifications)
            },
            actionLoadingKey = notificationPromptAction,
            onMarkOneRead = { item ->
                if (notificationPromptAction != null) return@SiteNotificationPromptSheet
                notificationPromptAction = "read-${item.id}"
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { apiClient.markNotificationRead(item.id) }
                    }.onSuccess {
                        val remaining = notificationPromptItems.filterNot { it.id == item.id }
                        updateNotificationPrompt(remaining)
                        unreadNotificationCount = (unreadNotificationCount - 1).coerceAtLeast(0)
                    }.also {
                        notificationPromptAction = null
                    }
                }
            },
            onMarkAllRead = {
                if (notificationPromptAction != null) return@SiteNotificationPromptSheet
                notificationPromptAction = "read-all"
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { apiClient.markAllNotificationsRead() }
                    }.onSuccess {
                        clearNotificationState()
                    }.also {
                        notificationPromptAction = null
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountScreen(
    authState: AuthState,
    authError: String?,
    savedSessions: List<AuthSession>,
    hapticsEnabled: Boolean,
    notificationSettings: NotificationSettings,
    notificationPermissionGranted: Boolean,
    onHapticsEnabledChange: (Boolean) -> Unit,
    onNotificationSettingsChange: (NotificationSettings) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    apiClient: BbsApiClient,
    ssoClient: SsoApiClient,
    onLoginSuccess: (AuthSession) -> Unit,
    onSwitchAccount: (AuthSession) -> Unit,
    onUseGuest: () -> Unit,
    onLogout: () -> Unit,
    onRetrySession: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var captchaAnswer by remember { mutableStateOf("") }
    var captcha by remember { mutableStateOf<MathCaptcha?>(null) }
    var duration by remember { mutableStateOf("7d") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showLoginForm by remember { mutableStateOf(savedSessions.isEmpty()) }
    var passwordVisible by remember { mutableStateOf(false) }
    val checkinStates = remember { mutableStateMapOf<Int, CheckinState>() }
    val checkinLoading = remember { mutableStateMapOf<Int, Boolean>() }
    val checkinErrors = remember { mutableStateMapOf<Int, String>() }
    var allCheckinLoading by remember { mutableStateOf(false) }

    fun refreshCaptcha() {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { ssoClient.fetchMathCaptcha() }
            }.onSuccess {
                captcha = it
                captchaAnswer = ""
            }.onFailure {
                error = it.message ?: "验证码加载失败"
            }
        }
    }

    fun login() {
        val currentCaptcha = captcha
        if (identifier.isBlank() || password.isBlank()) {
            error = "请输入账号和密码"
            return
        }
        if (currentCaptcha == null || captchaAnswer.isBlank()) {
            error = "请完成验证码"
            return
        }
        loading = true
        error = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    ssoClient.login(
                        identifier.trim(),
                        password,
                        duration,
                        "IGNGbbs Android ${Build.MODEL ?: ""}".trim(),
                        currentCaptcha.token,
                        captchaAnswer.trim()
                    )
                }
            }.onSuccess {
                password = ""
                captchaAnswer = ""
                captcha = null
                showLoginForm = false
                onLoginSuccess(it)
            }.onFailure {
                error = it.message ?: "登录失败"
                refreshCaptcha()
            }.also {
                loading = false
            }
        }
    }

    fun refreshCheckin(session: AuthSession) {
        checkinLoading[session.user.id] = true
        checkinErrors.remove(session.user.id)
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchMobileCheckin(session.sessionToken) }
            }.onSuccess {
                checkinStates[session.user.id] = it
            }.onFailure {
                checkinErrors[session.user.id] = it.message ?: "签到状态获取失败"
            }.also {
                checkinLoading[session.user.id] = false
            }
        }
    }

    fun checkin(session: AuthSession) {
        checkinLoading[session.user.id] = true
        checkinErrors.remove(session.user.id)
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.submitMobileCheckin(session.sessionToken) }
            }.onSuccess {
                checkinStates[session.user.id] = it
            }.onFailure { error ->
                runCatching {
                    withContext(Dispatchers.IO) { apiClient.fetchMobileCheckin(session.sessionToken) }
                }.onSuccess {
                    checkinStates[session.user.id] = it
                    if (it.isCheckedIn) {
                        checkinErrors.remove(session.user.id)
                    }
                }
                if (checkinStates[session.user.id]?.isCheckedIn != true) {
                    checkinErrors[session.user.id] = error.message ?: "签到失败"
                }
            }.also {
                checkinLoading[session.user.id] = false
            }
        }
    }

    fun checkinAll() {
        if (savedSessions.isEmpty() || allCheckinLoading) return
        allCheckinLoading = true
        scope.launch {
            savedSessions.forEach { account ->
                if (checkinStates[account.user.id]?.isCheckedIn == true) return@forEach
                checkinLoading[account.user.id] = true
                checkinErrors.remove(account.user.id)
                runCatching {
                    withContext(Dispatchers.IO) { apiClient.submitMobileCheckin(account.sessionToken) }
                }.onSuccess {
                    checkinStates[account.user.id] = it
                }.onFailure { error ->
                    runCatching {
                        withContext(Dispatchers.IO) { apiClient.fetchMobileCheckin(account.sessionToken) }
                    }.onSuccess {
                        checkinStates[account.user.id] = it
                        if (it.isCheckedIn) {
                            checkinErrors.remove(account.user.id)
                        }
                    }
                    if (checkinStates[account.user.id]?.isCheckedIn != true) {
                        checkinErrors[account.user.id] = error.message ?: "签到失败"
                    }
                }
                checkinLoading[account.user.id] = false
            }
            allCheckinLoading = false
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Guest && captcha == null) {
            refreshCaptcha()
        }
    }

    LaunchedEffect(savedSessions.joinToString("|") { "${it.user.id}:${it.expiresAt}" }) {
        val savedIds = savedSessions.map { it.user.id }.toSet()
        checkinStates.keys.toList().filterNot { it in savedIds }.forEach { checkinStates.remove(it) }
        checkinLoading.keys.toList().filterNot { it in savedIds }.forEach { checkinLoading.remove(it) }
        checkinErrors.keys.toList().filterNot { it in savedIds }.forEach { checkinErrors.remove(it) }
        savedSessions.forEach { account ->
            if (checkinStates[account.user.id] == null) {
                refreshCheckin(account)
            }
        }
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (authState) {
            AuthState.Checking -> item { LoadingState() }
            is AuthState.SignedIn -> {
                item {
                    AccountChooserCard(
                        session = authState.session,
                        savedSessions = savedSessions,
                        checkinStates = checkinStates,
                        checkinLoading = checkinLoading,
                        checkinErrors = checkinErrors,
                        allCheckinLoading = allCheckinLoading,
                        onSwitchAccount = onSwitchAccount,
                        onUseGuest = onUseGuest,
                        onAddAccount = {
                            showLoginForm = true
                            refreshCaptcha()
                        },
                        onLogout = onLogout,
                        onRefresh = {
                            onRetrySession()
                            savedSessions.forEach { refreshCheckin(it) }
                        },
                        onCheckin = { checkin(it) },
                        onCheckinAll = { checkinAll() }
                    )
                }
                item {
                    QuickEntryCard(
                        title = "历史记录",
                        description = "查看最近访问过的文章，并管理阅读进度。",
                        icon = Icons.Default.History,
                        onClick = onOpenHistory
                    )
                }
                item {
                    AppSettingsCard(
                        hapticsEnabled = hapticsEnabled,
                        onHapticsEnabledChange = onHapticsEnabledChange,
                        notificationSettings = notificationSettings,
                        notificationPermissionGranted = notificationPermissionGranted,
                        onNotificationSettingsChange = onNotificationSettingsChange,
                        onRequestNotificationPermission = onRequestNotificationPermission
                    )
                }
                if (showLoginForm) {
                    item {
                        LoginFormCard(
                            title = "添加账号",
                            subtitle = "登录另一个 IGNG 账号后，可以在本页随时切换。",
                            identifier = identifier,
                            onIdentifierChange = { identifier = it },
                            password = password,
                            onPasswordChange = { password = it },
                            passwordVisible = passwordVisible,
                            onPasswordVisibleChange = { passwordVisible = it },
                            duration = duration,
                            onDurationChange = { duration = it },
                            captcha = captcha,
                            captchaAnswer = captchaAnswer,
                            onCaptchaAnswerChange = { captchaAnswer = it },
                            loading = loading,
                            error = error,
                            authError = null,
                            onRefreshCaptcha = { refreshCaptcha() },
                            onLogin = { login() }
                        )
                    }
                }
            }
            AuthState.Guest -> {
                item {
                    AccountChooserCard(
                        session = null,
                        savedSessions = savedSessions,
                        checkinStates = checkinStates,
                        checkinLoading = checkinLoading,
                        checkinErrors = checkinErrors,
                        allCheckinLoading = allCheckinLoading,
                        onSwitchAccount = onSwitchAccount,
                        onUseGuest = onUseGuest,
                        onAddAccount = {
                            showLoginForm = true
                            refreshCaptcha()
                        },
                        onLogout = onLogout,
                        onRefresh = {
                            onRetrySession()
                            savedSessions.forEach { refreshCheckin(it) }
                        },
                        onCheckin = { checkin(it) },
                        onCheckinAll = { checkinAll() }
                    )
                }
                item {
                    QuickEntryCard(
                        title = "历史记录",
                        description = "登录后可查看自己的访问记录与阅读进度。",
                        icon = Icons.Default.History,
                        onClick = onOpenHistory
                    )
                }
                item {
                    AppSettingsCard(
                        hapticsEnabled = hapticsEnabled,
                        onHapticsEnabledChange = onHapticsEnabledChange,
                        notificationSettings = notificationSettings,
                        notificationPermissionGranted = notificationPermissionGranted,
                        onNotificationSettingsChange = onNotificationSettingsChange,
                        onRequestNotificationPermission = onRequestNotificationPermission
                    )
                }
                if (showLoginForm) {
                    item {
                        LoginFormCard(
                        title = "登录 IGNG 账号",
                        subtitle = "登录后可查看你有权限访问的文章、专栏和个人内容。",
                        identifier = identifier,
                        onIdentifierChange = { identifier = it },
                        password = password,
                        onPasswordChange = { password = it },
                        passwordVisible = passwordVisible,
                        onPasswordVisibleChange = { passwordVisible = it },
                        duration = duration,
                        onDurationChange = { duration = it },
                        captcha = captcha,
                        captchaAnswer = captchaAnswer,
                        onCaptchaAnswerChange = { captchaAnswer = it },
                        loading = loading,
                        error = error,
                        authError = authError,
                        onRefreshCaptcha = { refreshCaptcha() },
                        onLogin = { login() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LoginFormCard(
    title: String,
    subtitle: String,
    identifier: String,
    onIdentifierChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    captcha: MathCaptcha?,
    captchaAnswer: String,
    onCaptchaAnswerChange: (String) -> Unit,
    loading: Boolean,
    error: String?,
    authError: String?,
    onRefreshCaptcha: () -> Unit,
    onLogin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            authError?.let { NoticeCard("登录状态", it) }
            error?.let { NoticeCard("无法登录", it) }
            OutlinedTextField(
                value = identifier,
                onValueChange = onIdentifierChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("用户名或邮箱") },
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("密码") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) {
                    androidx.compose.ui.text.input.VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                }
            )
            Text("登录时长", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "30m" to "30 分钟",
                    "1d" to "1 天",
                    "7d" to "7 天",
                    "1m" to "1 个月",
                    "1y" to "1 年"
                ).forEach { (value, label) ->
                    InputChip(
                        selected = duration == value,
                        onClick = { onDurationChange(value) },
                        label = { Text(label) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = captchaAnswer,
                    onValueChange = onCaptchaAnswerChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(captcha?.question() ?: "验证码") },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onRefreshCaptcha) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新验证码")
                }
            }
            Button(
                onClick = onLogin,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("登录")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountChooserCard(
    session: AuthSession?,
    savedSessions: List<AuthSession>,
    checkinStates: Map<Int, CheckinState>,
    checkinLoading: Map<Int, Boolean>,
    checkinErrors: Map<Int, String>,
    allCheckinLoading: Boolean,
    onSwitchAccount: (AuthSession) -> Unit,
    onUseGuest: () -> Unit,
    onAddAccount: () -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onCheckin: (AuthSession) -> Unit,
    onCheckinAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("当前身份", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = if (session == null) "正在以访客身份浏览。" else "正在使用 @${session.user.username}。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            GuestSwitchRow(selected = session == null, onClick = onUseGuest)
            if (savedSessions.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "已保存账号",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onCheckinAll, enabled = !allCheckinLoading) {
                        if (allCheckinLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("全部签到", maxLines = 1)
                    }
                }
                savedSessions.forEach { account ->
                    AccountSwitchRow(
                        session = account,
                        selected = account.user.id == session?.user?.id,
                        checkinState = checkinStates[account.user.id],
                        checkinLoading = checkinLoading[account.user.id] == true,
                        checkinError = checkinErrors[account.user.id],
                        onClick = { onSwitchAccount(account) },
                        onCheckin = { onCheckin(account) }
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onAddAccount) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("添加", maxLines = 1)
                }
                TextButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("刷新", maxLines = 1)
                }
                if (session != null) {
                    TextButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("退出", maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickEntryCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AppSettingsCard(
    hapticsEnabled: Boolean,
    onHapticsEnabledChange: (Boolean) -> Unit,
    notificationSettings: NotificationSettings,
    notificationPermissionGranted: Boolean,
    onNotificationSettingsChange: (NotificationSettings) -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("应用设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "拖拽和重要操作使用系统触觉反馈",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = onHapticsEnabledChange
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable(onClick = onRequestNotificationPermission)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("手机通知", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (notificationPermissionGranted) {
                        "已获得系统通知权限，可按下方开关决定接收哪些通知。"
                    } else {
                        "当前未获得系统通知权限。点击这里可重新弹出通知权限确认窗口。"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            NotificationToggleRow(
                title = "系统通知",
                description = "接收面向全站或包含你的系统通知。",
                checked = notificationSettings.systemEnabled,
                onCheckedChange = {
                    onNotificationSettingsChange(notificationSettings.copy(systemEnabled = it))
                }
            )
            NotificationToggleRow(
                title = "回复通知",
                description = "有人回复你的文章或评论时提醒。",
                checked = notificationSettings.replyEnabled,
                onCheckedChange = {
                    onNotificationSettingsChange(notificationSettings.copy(replyEnabled = it))
                }
            )
            NotificationToggleRow(
                title = "日报",
                description = "推送你自己文章的浏览量信息。",
                checked = notificationSettings.dailyEnabled,
                onCheckedChange = {
                    onNotificationSettingsChange(notificationSettings.copy(dailyEnabled = it))
                }
            )
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun GuestSwitchRow(selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarDot("访客")
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("访客", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("不使用任何已登录账号", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(if (selected) "使用中" else "使用", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun AccountSwitchRow(
    session: AuthSession,
    selected: Boolean,
    checkinState: CheckinState?,
    checkinLoading: Boolean,
    checkinError: String?,
    onClick: () -> Unit,
    onCheckin: () -> Unit
) {
    val checkedIn = checkinState?.isCheckedIn == true
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarDot(session.user.displayName())
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = session.user.displayName().ifBlank { "IGNG 用户" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${session.user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                if (selected) "使用中" else "使用",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = checkinState?.let { "Lv.${it.level} · ${it.exp} EXP · 每日 +${it.rewardExp} EXP" } ?: "正在读取移动签到状态",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!checkinError.isNullOrBlank()) {
                    Text(
                        text = checkinError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            TextButton(
                onClick = onCheckin,
                enabled = !checkinLoading && !checkedIn
            ) {
                if (checkinLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text(if (checkedIn) "已签到" else "签到", maxLines = 1)
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    onOpenPost: (Int) -> Unit,
    onShowBanner: (String) -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再查看历史记录")
        return
    }

    val scope = rememberCoroutineScope()
    var history by remember { mutableStateOf<List<PostHistoryItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        loading = true
        error = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchHistory() }
            }.onSuccess {
                history = it.filter { item -> item.post != null }
            }.onFailure {
                error = it.message ?: "历史记录加载失败"
            }.also {
                loading = false
            }
        }
    }

    fun deleteHistory(item: PostHistoryItem) {
        if (item.id <= 0 || deletingId != null) return
        deletingId = item.id
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.deleteHistory(item.id) }
            }.onSuccess {
                history = history.filterNot { it.id == item.id }
                onShowBanner("历史记录已删除")
            }.onFailure {
                onShowBanner(it.message ?: "删除历史记录失败")
            }.also {
                deletingId = null
            }
        }
    }

    LaunchedEffect(authState.session.user.id) { load() }

    val filtered = remember(history, query) {
        val keyword = query.trim().lowercase(Locale.CHINA)
        if (keyword.isBlank()) {
            history
        } else {
            history.filter { item ->
                val post = item.post ?: return@filter false
                listOf(
                    post.title,
                    post.summary.ifBlank { "无摘要" },
                    post.authorName,
                    post.categoryName,
                    item.deviceName
                ).filter { it.isNotBlank() }.any { it.lowercase(Locale.CHINA).contains(keyword) }
            }
        }
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("历史记录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "查看最近访问过的文章，也可以删除单条历史记录。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("搜索标题、作者、设备") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                }
            }
        }

        when {
            loading -> item { LoadingCard("历史记录加载中...") }
            error != null -> item { NoticeCard("加载失败", error!!) }
            filtered.isEmpty() -> item { EmptyCard(if (query.isBlank()) "你还没有文章访问记录。" else "没有匹配的历史记录。") }
            else -> items(filtered, key = { "history-${it.id}-${it.post?.id ?: 0}" }) { item ->
                val post = item.post ?: return@items
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryPill(post.categoryName)
                            AssistChip(
                                onClick = {},
                                label = { Text(item.deviceName.ifBlank { "未记录设备" }) },
                                leadingIcon = { Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        Text(
                            text = post.title.ifBlank { "未命名文章" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onOpenPost(post.id) }
                        )
                        Text(
                            text = post.summary.ifBlank { "无摘要" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "作者：${post.authorName.ifBlank { "未知" }} · 访问时间：${friendlyDateTime(item.visitedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (item.hasProgressAnchor()) "已记录阅读进度：#${item.progressAnchor}" else "未记录阅读进度",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.hasProgressAnchor()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onOpenPost(post.id) }) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("打开文章")
                            }
                            TextButton(
                                onClick = { deleteHistory(item) },
                                enabled = deletingId != item.id
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (deletingId == item.id) "删除中" else "删除历史")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingCard(message: String) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CreatorOverviewScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    onOpenPosts: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenGroups: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenPersonalization: () -> Unit,
    onOpenColumns: () -> Unit,
    onOpenSeries: () -> Unit,
    onOpenProfile: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再使用控制台")
        return
    }
    var overview by remember { mutableStateOf<CreatorOverview?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var overviewLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState.session.user.id) {
        runCatching {
            withContext(Dispatchers.IO) { apiClient.fetchCreatorOverview() }
        }.onSuccess {
            overview = it
            error = null
        }.onFailure {
            error = it.message ?: "创作者总览加载失败"
        }.also {
            overviewLoading = false
        }
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("创作者总览", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "欢迎回来，${authState.session.user.displayName()}。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    error?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (overviewLoading || overview == null) {
                    repeat(5) {
                        CreatorStatCardLoading(Modifier.weight(1f, fill = false))
                    }
                } else {
                    val stats = overview ?: return@FlowRow
                    CreatorStatCard("文章", stats.postCount.toString(), Modifier.weight(1f, fill = false))
                    CreatorStatCard("草稿", stats.draftCount.toString(), Modifier.weight(1f, fill = false))
                    CreatorStatCard("限制可见", stats.privateCount.toString(), Modifier.weight(1f, fill = false))
                    CreatorStatCard("收到评论", stats.receivedCommentCount.toString(), Modifier.weight(1f, fill = false))
                    CreatorStatCard("今日浏览", stats.todayViews.toString(), Modifier.weight(1f, fill = false))
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onOpenPosts, modifier = Modifier.fillMaxWidth()) { Text("文章管理") }
                    Button(onClick = onOpenComments, modifier = Modifier.fillMaxWidth()) { Text("评论管理") }
                    Button(onClick = onOpenReview, modifier = Modifier.fillMaxWidth()) { Text("处理中心") }
                    Button(onClick = onOpenGroups, modifier = Modifier.fillMaxWidth()) { Text("群组管理") }
                    Button(onClick = onOpenPermissions, modifier = Modifier.fillMaxWidth()) { Text("权限状态") }
                    Button(onClick = onOpenPersonalization, modifier = Modifier.fillMaxWidth()) { Text("个性化") }
                    Button(onClick = onOpenColumns, modifier = Modifier.fillMaxWidth()) { Text("专栏管理") }
                    Button(onClick = onOpenSeries, modifier = Modifier.fillMaxWidth()) { Text("系列管理") }
                    Button(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) { Text("创作者资料") }
                    TextButton(
                        onClick = {
                            overviewLoading = true
                            scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) { apiClient.fetchCreatorOverview() }
                                }.onSuccess {
                                    overview = it
                                    error = null
                                }.onFailure {
                                    error = it.message ?: "创作者总览刷新失败"
                                }.also {
                                    overviewLoading = false
                                }
                            }
                        }
                    ) {
                        Text("刷新统计")
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatorStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(150.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CreatorStatCardLoading(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(150.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text("加载中", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun AnalyticsSection(
    apiClient: BbsApiClient,
    userKey: Any,
    type: String,
    id: Int?,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    val scope = rememberCoroutineScope()
    val calendar = remember(userKey, type, id) { Calendar.getInstance() }
    var year by remember(userKey, type, id) { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember(userKey, type, id) { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var analytics by remember(userKey, type, id) { mutableStateOf<CreatorAnalytics?>(null) }
    var loading by remember(userKey, type, id) { mutableStateOf(true) }
    var error by remember(userKey, type, id) { mutableStateOf<String?>(null) }
    var requestId by remember(userKey, type, id) { mutableIntStateOf(0) }

    fun shiftMonth(delta: Int) {
        var nextYear = year
        var nextMonth = month + delta
        while (nextMonth < 1) {
            nextMonth += 12
            nextYear -= 1
        }
        while (nextMonth > 12) {
            nextMonth -= 12
            nextYear += 1
        }
        year = nextYear
        month = nextMonth
    }

    fun load() {
        val currentRequest = requestId + 1
        requestId = currentRequest
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    apiClient.fetchCreatorAnalytics(type, id, year, month)
                }
            }.onSuccess {
                if (requestId != currentRequest) return@onSuccess
                analytics = it
                error = null
            }.onFailure {
                if (requestId != currentRequest) return@onFailure
                error = it.message ?: "统计加载失败"
            }.also {
                if (requestId == currentRequest) {
                    loading = false
                }
            }
        }
    }

    LaunchedEffect(userKey, type, id, year, month) {
        load()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { shiftMonth(-1) }) { Text("上月") }
                Text("${year}年${month}月", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { shiftMonth(1) }) { Text("下月") }
            }
            description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            if (analytics == null) {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    if (loading) {
                        CircularProgressIndicator()
                    } else {
                        EmptyState(error ?: "暂无统计数据")
                    }
                }
            } else {
                Box {
                    CreatorAnalyticsChartCard(analytics!!)
                    if (loading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.24f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("正在切换月份", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsBottomSheet(
    apiClient: BbsApiClient,
    target: AnalyticsSheetTarget,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 420.dp, max = 620.dp)
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            AnalyticsSection(
                apiClient = apiClient,
                userKey = target.userKey,
                type = target.type,
                id = target.id,
                title = target.title,
                description = target.description
            )
        }
    }
}

@Composable
private fun CreatorAnalyticsChartCard(analytics: CreatorAnalytics) {
    val summary = analytics.summary
    var selectedIndex by remember(analytics.scope.type, analytics.scope.id, analytics.year, analytics.month) {
        mutableIntStateOf((analytics.data.indexOfLast { it.totalViews > 0 }).takeIf { it >= 0 } ?: 0)
    }
    val selectedPoint = analytics.data.getOrNull(selectedIndex)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(analytics.scope.title.ifBlank { "数据统计" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CreatorStatCard("月浏览", summary.total.toString(), Modifier.weight(1f, fill = false))
                CreatorStatCard("本站/API", summary.internal.toString(), Modifier.weight(1f, fill = false))
                CreatorStatCard("搜索引擎", summary.search.toString(), Modifier.weight(1f, fill = false))
                CreatorStatCard("外链", summary.external.toString(), Modifier.weight(1f, fill = false))
            }
            CreatorLineChart(
                points = analytics.data,
                selectedIndex = selectedIndex,
                onPointSelected = { selectedIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .semantics {
                        val peak = analytics.data.maxOfOrNull { it.totalViews } ?: 0
                        contentDescription = "${analytics.scope.title}${analytics.year}年${analytics.month}月浏览趋势，总浏览${summary.total}，峰值${peak}"
                    }
            )
            selectedPoint?.let { point ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(point.date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CreatorStatCard("总浏览", point.totalViews.toString(), Modifier.weight(1f, fill = false))
                            CreatorStatCard("本站/API", point.internalRef.toString(), Modifier.weight(1f, fill = false))
                            CreatorStatCard("搜索", point.searchRef.toString(), Modifier.weight(1f, fill = false))
                            CreatorStatCard("外链", point.externalRef.toString(), Modifier.weight(1f, fill = false))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatorLineChart(
    points: List<CreatorAnalytics.Point>,
    selectedIndex: Int,
    onPointSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val label = MaterialTheme.colorScheme.onSurfaceVariant
    val fill = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val textColor = label.toArgb()
    val density = LocalDensity.current
    Canvas(
        modifier = modifier.pointerInput(points, selectedIndex) {
            detectTapGestures { offset ->
                if (points.isEmpty()) return@detectTapGestures
                val chartLeft = with(density) { 8.dp.toPx() }
                val chartRight = size.width - with(density) { 8.dp.toPx() }
                val safeX = offset.x.coerceIn(chartLeft, chartRight)
                val width = (chartRight - chartLeft).coerceAtLeast(1f)
                val ratio = ((safeX - chartLeft) / width).coerceIn(0f, 1f)
                val index = if (points.size == 1) 0 else (ratio * (points.size - 1)).roundToInt().coerceIn(0, points.lastIndex)
                onPointSelected(index)
            }
        }
    ) {
        val chartLeft = with(density) { 8.dp.toPx() }
        val chartRight = size.width - with(density) { 8.dp.toPx() }
        val chartTop = with(density) { 14.dp.toPx() }
        val chartBottom = size.height - with(density) { 34.dp.toPx() }
        val width = (chartRight - chartLeft).coerceAtLeast(1f)
        val height = (chartBottom - chartTop).coerceAtLeast(1f)
        for (i in 0..3) {
            val y = chartTop + height * i / 3f
            drawLine(grid.copy(alpha = 0.55f), start = androidx.compose.ui.geometry.Offset(chartLeft, y), end = androidx.compose.ui.geometry.Offset(chartRight, y), strokeWidth = 1f)
        }
        if (points.isEmpty()) {
            drawContext.canvas.nativeCanvas.drawText(
                "暂无统计数据",
                chartLeft,
                chartTop + height / 2f,
                android.graphics.Paint().apply {
                    color = textColor
                    textSize = with(density) { 14.dp.toPx() }
                    isAntiAlias = true
                }
            )
            return@Canvas
        }
        val maxValue = max(1, points.maxOf { it.totalViews })
        val linePath = Path()
        val areaPath = Path()
        points.forEachIndexed { index, point ->
            val x = if (points.size == 1) chartLeft + width / 2f else chartLeft + width * index / (points.size - 1).toFloat()
            val y = chartBottom - height * (point.totalViews.toFloat() / maxValue.toFloat())
            if (index == 0) {
                linePath.moveTo(x, y)
                areaPath.moveTo(x, chartBottom)
                areaPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                areaPath.lineTo(x, y)
            }
            if (points.size <= 12 || index == 0 || index == points.lastIndex || index % 5 == 0 || index == selectedIndex) {
                drawCircle(
                    if (index == selectedIndex) tertiary else primary,
                    radius = with(density) { if (index == selectedIndex) 5.dp.toPx() else 3.dp.toPx() },
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
        val lastX = if (points.size == 1) chartLeft + width / 2f else chartRight
        areaPath.lineTo(lastX, chartBottom)
        areaPath.close()
        drawPath(
            areaPath,
            brush = Brush.verticalGradient(listOf(fill, Color.Transparent), startY = chartTop, endY = chartBottom)
        )
        drawPath(linePath, color = primary, style = Stroke(width = with(density) { 3.dp.toPx() }))
        val paint = android.graphics.Paint().apply {
            color = textColor
            textSize = with(density) { 11.dp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val labelIndexes = listOf(0, points.size / 2, points.lastIndex).distinct().filter { it in points.indices }
        labelIndexes.forEach { index ->
            val x = if (points.size == 1) chartLeft + width / 2f else chartLeft + width * index / (points.size - 1).toFloat()
            val text = points[index].date.takeLast(5)
            drawContext.canvas.nativeCanvas.drawText(text, x, size.height - with(density) { 10.dp.toPx() }, paint)
        }
        drawContext.canvas.nativeCanvas.drawText(
            maxValue.toString(),
            chartLeft + with(density) { 10.dp.toPx() },
            chartTop + with(density) { 12.dp.toPx() },
            paint.apply { textAlign = android.graphics.Paint.Align.LEFT }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CreatorPostsScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    onOpenPost: (Int) -> Unit,
    onCreatePost: () -> Unit,
    onEditPost: (Int) -> Unit,
    onOpenSettings: (Int) -> Unit,
    onDirectOpenPost: (Int) -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再管理文章")
        return
    }
    val scope = rememberCoroutineScope()
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingDeletePost by remember { mutableStateOf<Post?>(null) }
    var pendingAppealPost by remember { mutableStateOf<Post?>(null) }
    var analyticsSheetTarget by remember { mutableStateOf<AnalyticsSheetTarget?>(null) }
    var searchDraftQuery by remember { mutableStateOf("") }
    var submittedSearchQuery by remember { mutableStateOf("") }
    var appealReason by remember { mutableStateOf("") }
    var appealSubmitting by remember { mutableStateOf(false) }
    var searchRequestId by remember { mutableIntStateOf(0) }
    var showSearchSheet by remember { mutableStateOf(false) }

    fun load() {
        loading = true
        val requestId = ++searchRequestId
        val query = submittedSearchQuery.trim()
        val requestedStatus = creatorPostRequestedStatus(statusFilter)
        val requestedBanStatus = creatorPostRequestedBanStatus(statusFilter)
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorPosts(requestedStatus, requestedBanStatus, 50, query) }
            }.onSuccess {
                if (requestId == searchRequestId) {
                    posts = it.filter { post -> post.status != 3 }
                    error = null
                }
            }.onFailure {
                if (requestId == searchRequestId) {
                    posts = emptyList()
                    error = it.message ?: "创作者文章加载失败"
                }
            }.also {
                if (requestId == searchRequestId) {
                    loading = false
                }
            }
        }
    }

    LaunchedEffect(statusFilter, submittedSearchQuery, authState.session.user.id) {
        load()
    }

    val visiblePosts = remember(posts, statusFilter) {
        posts.filter { post -> creatorPostMatchesFilter(post, statusFilter) }
    }

    fun submitSearch() {
        val q = searchDraftQuery.trim()
        val directId = internalPostId(q) ?: q.toIntOrNull()
        if (directId != null) {
            onDirectOpenPost(directId)
            return
        }
        submittedSearchQuery = q
        if (q.isBlank()) return
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("文章管理", modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onCreatePost) {
                            Icon(Icons.Default.Add, contentDescription = "新建文章")
                        }
                        IconButton(onClick = { showSearchSheet = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            null to "全部",
                            "published" to "已发布",
                            "draft" to "草稿",
                            "restricted" to "限制查看",
                            "rejected" to "不通过",
                            "reviewing" to "审核中"
                        ).forEach { (value, label) ->
                            InputChip(
                                selected = statusFilter == value,
                                onClick = { statusFilter = value },
                                label = { Text(label) }
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            analyticsSheetTarget = AnalyticsSheetTarget(
                                type = "posts",
                                id = null,
                                title = "浏览量统计",
                                userKey = "posts-${authState.session.user.id}"
                            )
                        }
                    ) { Text("浏览量") }
                    error?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        if (loading) {
            item { LoadingState() }
        } else if (visiblePosts.isEmpty()) {
            item { EmptyState(if (submittedSearchQuery.isBlank()) "当前筛选条件下没有文章" else "没有找到匹配文章") }
        } else {
            items(visiblePosts, key = { "creator-post-${it.id}" }) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(post.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${post.categoryName} · ${creatorPostStatusText(post)} · ${shortDate(post.createdAt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (post.summary.isNotBlank()) {
                            Text(
                                summaryText(post.summary, expanded = false, limit = 110),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            buildList {
                                add("可见性 " + when (post.visibility) {
                                    "private" -> "部分可见"
                                    "group" -> "私密 / 群组"
                                    else -> "公开"
                                })
                                if (post.columnName.isNotBlank()) add("专栏 ${post.columnName}")
                                if (post.tags.isNotBlank()) add("标签 ${post.tags}")
                            }.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onOpenPost(post.id) }) { Text("查看文章") }
                            TextButton(onClick = { onEditPost(post.id) }) { Text("编辑文章") }
                            TextButton(onClick = {
                                analyticsSheetTarget = AnalyticsSheetTarget(
                                    type = "post",
                                    id = post.id,
                                    title = "浏览量统计",
                                    userKey = "post-${post.id}"
                                )
                            }) { Text("浏览量") }
                            TextButton(onClick = { onOpenSettings(post.id) }) { Text("文章设置") }
                            if (post.banStatus == 1 || post.banStatus == 2) {
                                TextButton(onClick = {
                                    pendingAppealPost = post
                                    appealReason = ""
                                }) {
                                    Text("提交复审")
                                }
                            }
                            TextButton(
                                onClick = { pendingDeletePost = post },
                                enabled = post.status != 3
                            ) {
                                Text(if (post.status == 3) "已删除" else "删除文章")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSearchSheet) {
        ModalBottomSheet(onDismissRequest = { showSearchSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp, max = 500.dp)
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("搜索自己的文章", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = searchDraftQuery,
                    onValueChange = { searchDraftQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("关键词、ID 或链接") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            searchDraftQuery = ""
                            submittedSearchQuery = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("清除") }
                    Button(
                        onClick = {
                            submitSearch()
                            if (searchDraftQuery.trim().isNotBlank()) {
                                showSearchSheet = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !loading
                    ) { Text(if (loading && submittedSearchQuery.isNotBlank()) "搜索中..." else "搜索") }
                }
            }
        }
    }

    pendingDeletePost?.let { post ->
        AlertDialog(
            onDismissRequest = { pendingDeletePost = null },
            title = { Text("确认删除文章") },
            text = { Text("确定删除《${post.title.ifBlank { "未命名文章" }}》吗？删除后将不会在控制台列表中显示。") },
            confirmButton = {
                TextButton(onClick = {
                    val target = pendingDeletePost ?: return@TextButton
                    pendingDeletePost = null
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) { apiClient.deleteCreatorPost(target.id) }
                        }.onSuccess { load() }.onFailure {
                            error = it.message ?: "删除文章失败"
                        }
                    }
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePost = null }) { Text("取消") }
            }
        )
    }

    pendingAppealPost?.let { post ->
        AlertDialog(
            onDismissRequest = {
                if (!appealSubmitting) {
                    pendingAppealPost = null
                    appealReason = ""
                }
            },
            title = { Text("提交复审") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("对《${post.title.ifBlank { "未命名文章" }}》发起复审，请填写理由。")
                    OutlinedTextField(
                        value = appealReason,
                        onValueChange = { appealReason = it.take(1000) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("复审理由") },
                        minLines = 4,
                        maxLines = 8
                    )
                    error?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !appealSubmitting && appealReason.trim().isNotBlank(),
                    onClick = {
                        val target = pendingAppealPost ?: return@TextButton
                        val reason = appealReason.trim()
                        appealSubmitting = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    apiClient.submitCreatorAppeal("post", target.id, reason)
                                }
                            }.onSuccess {
                                pendingAppealPost = null
                                appealReason = ""
                                load()
                            }.onFailure {
                                error = it.message ?: "提交复审失败"
                            }.also {
                                appealSubmitting = false
                            }
                        }
                    }
                ) { Text(if (appealSubmitting) "提交中..." else "提交") }
            },
            dismissButton = {
                TextButton(
                    enabled = !appealSubmitting,
                    onClick = {
                        pendingAppealPost = null
                        appealReason = ""
                    }
                ) { Text("取消") }
            }
        )
    }

    analyticsSheetTarget?.let { target ->
        AnalyticsBottomSheet(
            apiClient = apiClient,
            target = target,
            onDismiss = { analyticsSheetTarget = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CreatorCommentsScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    onOpenPost: (Int) -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再管理评论")
        return
    }
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf("received") }
    var comments by remember { mutableStateOf<List<CreatorComment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingDeleteComment by remember { mutableStateOf<CreatorComment?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf("desc") }
    var replyToId by remember { mutableIntStateOf(0) }
    var replyContent by remember { mutableStateOf("") }
    var latestRequestId by remember { mutableIntStateOf(0) }
    var showSearchSheet by remember { mutableStateOf(false) }
    val actionLoading = remember { mutableStateMapOf<String, Boolean>() }

    fun load() {
        val requestId = ++latestRequestId
        loading = true
        replyToId = 0
        replyContent = ""
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorComments(mode, 50) }
            }.onSuccess {
                if (requestId != latestRequestId) return@onSuccess
                comments = it.filter { comment ->
                    comment.postId > 0
                }
                error = null
            }.onFailure {
                if (requestId != latestRequestId) return@onFailure
                error = it.message ?: "创作者评论加载失败"
            }.also {
                if (requestId == latestRequestId) {
                    loading = false
                }
            }
        }
    }

    LaunchedEffect(mode, authState.session.user.id) {
        load()
    }

    val filteredComments by remember(comments, searchQuery, sortOrder) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            comments.filter { comment ->
                if (query.isBlank()) {
                    true
                } else {
                    listOf(
                        comment.id.toString(),
                        comment.parentId.toString(),
                        comment.authorName,
                        comment.postTitle,
                        comment.content
                    ).any { value -> value.lowercase().contains(query) }
                }
            }.sortedBy { it.createdAt }.let { list ->
                if (sortOrder == "desc") list.reversed() else list
            }
        }
    }

    val unreadMentions by remember(comments, mode) {
        derivedStateOf { if (mode == "received") comments.count { it.mentionUnread } else 0 }
    }

    fun markMentionRead(comment: CreatorComment) {
        if (!comment.mentionUnread || comment.mentionId <= 0) return
        val key = "read-${comment.id}"
        actionLoading[key] = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.markMentionRead(comment.mentionId) }
            }.onSuccess {
                comments = comments.map {
                    if (it.id == comment.id) CreatorComment(it.id, it.userId, it.postId, it.parentId, it.content, it.createdAt, it.authorName, it.postTitle, it.mentionId, false) else it
                }
            }.onFailure {
                error = it.message ?: "标记已读失败"
            }.also {
                actionLoading.remove(key)
            }
        }
    }

    fun markAllMentionsRead() {
        if (mode != "received" || unreadMentions <= 0) return
        actionLoading["read-all"] = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.markAllMentionsRead() }
            }.onSuccess {
                comments = comments.map {
                    CreatorComment(it.id, it.userId, it.postId, it.parentId, it.content, it.createdAt, it.authorName, it.postTitle, it.mentionId, false)
                }
            }.onFailure {
                error = it.message ?: "全部标记已读失败"
            }.also {
                actionLoading.remove("read-all")
            }
        }
    }

    fun submitReply(comment: CreatorComment) {
        val content = replyContent.trim()
        if (content.isBlank()) {
            error = "请输入回复内容"
            return
        }
        val key = "reply-${comment.id}"
        actionLoading[key] = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.createComment(comment.postId, content, comment.id) }
            }.onSuccess {
                if (comment.mentionUnread && comment.mentionId > 0) {
                    runCatching {
                        withContext(Dispatchers.IO) { apiClient.markMentionRead(comment.mentionId) }
                    }
                }
                replyToId = 0
                replyContent = ""
                load()
            }.onFailure {
                error = it.message ?: "发送回复失败"
            }.also {
                actionLoading.remove(key)
            }
        }
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "评论管理",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showSearchSheet = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索与排序")
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InputChip(
                            selected = mode == "received",
                            onClick = { mode = "received" },
                            label = { Text(if (unreadMentions > 0) "收到的回复 ($unreadMentions)" else "收到的回复") }
                        )
                        InputChip(selected = mode == "sent", onClick = { mode = "sent" }, label = { Text("我发表的评论") })
                    }
                    if (mode == "received" && unreadMentions > 0) {
                        TextButton(
                            onClick = { markAllMentionsRead() },
                            enabled = actionLoading["read-all"] != true
                        ) { Text("全部标记已读") }
                    }
                    error?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        if (loading) {
            item { LoadingState() }
        } else if (filteredComments.isEmpty()) {
            item { EmptyState("当前没有可管理的评论") }
        } else {
            items(filteredComments, key = { "creator-comment-${it.id}" }) { comment ->
                val isUnread = mode == "received" && comment.mentionUnread
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            buildString {
                                append("评论 #")
                                append(comment.id)
                                if (comment.parentId > 0) {
                                    append(" · 上级 #")
                                    append(comment.parentId)
                                }
                                append(" · ")
                                append(if (comment.authorName.isBlank()) "匿名用户" else comment.authorName)
                                append(" · ")
                                append(shortDate(comment.createdAt))
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SelectableTextBlock(
                            text = comment.content,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurface,
                            textSizeSp = 16f
                        )
                        if (comment.postTitle.isNotBlank()) {
                            Text(
                                "关联文章：${comment.postTitle}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (comment.postId > 0) {
                                TextButton(onClick = { onOpenPost(comment.postId) }) { Text("查看文章") }
                            }
                            if (isUnread && comment.mentionId > 0) {
                                TextButton(
                                    onClick = { markMentionRead(comment) },
                                    enabled = actionLoading["read-${comment.id}"] != true
                                ) { Text("标记已读") }
                            }
                            if (mode == "received" && comment.postId > 0) {
                                TextButton(onClick = {
                                    if (replyToId == comment.id) {
                                        replyToId = 0
                                        replyContent = ""
                                    } else {
                                        replyToId = comment.id
                                        replyContent = ""
                                    }
                                }) {
                                    Text(if (replyToId == comment.id) "收起回复" else "快速回复")
                                }
                            }
                            TextButton(onClick = { pendingDeleteComment = comment }) {
                                Text("删除评论")
                            }
                        }
                        if (replyToId == comment.id) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        "回复 @${comment.authorName.ifBlank { comment.id.toString() }}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    OutlinedTextField(
                                        value = replyContent,
                                        onValueChange = { replyContent = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("回复内容") },
                                        minLines = 3
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = {
                                            replyToId = 0
                                            replyContent = ""
                                        }) { Text("取消") }
                                        Button(
                                            onClick = { submitReply(comment) },
                                            enabled = actionLoading["reply-${comment.id}"] != true && replyContent.isNotBlank()
                                        ) { Text("发送回复") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteComment?.let { comment ->
        AlertDialog(
            onDismissRequest = { pendingDeleteComment = null },
            title = { Text("确认删除评论") },
            text = { Text("确定删除这条评论吗？\n\n${summaryText(comment.content, expanded = false, limit = 80)}") },
            confirmButton = {
                TextButton(onClick = {
                    val target = pendingDeleteComment ?: return@TextButton
                    pendingDeleteComment = null
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) { apiClient.deleteCreatorComment(target.id) }
                        }.onSuccess { load() }.onFailure {
                            error = it.message ?: "删除评论失败"
                        }
                    }
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteComment = null }) { Text("取消") }
            }
        )
    }

    if (showSearchSheet) {
        ModalBottomSheet(onDismissRequest = { showSearchSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 520.dp)
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("搜索与排序", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("搜索评论") },
                    placeholder = { Text("输入评论内容、作者、文章标题或 ID") },
                    singleLine = true
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InputChip(selected = sortOrder == "desc", onClick = { sortOrder = "desc" }, label = { Text("最新") })
                    InputChip(selected = sortOrder == "asc", onClick = { sortOrder = "asc" }, label = { Text("最早") })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (searchQuery.isNotBlank()) {
                        TextButton(onClick = { searchQuery = "" }) { Text("清空") }
                    }
                    Button(onClick = { showSearchSheet = false }) { Text("完成") }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCenterScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    unreadCount: Int,
    onUnreadCountChange: (Int) -> Unit,
    onNotificationStateRefresh: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再查看通知中心")
        return
    }
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf<List<NotificationRecipient>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf("desc") }
    var previewItem by remember { mutableStateOf<NotificationRecipient?>(null) }
    var latestRequestId by remember { mutableIntStateOf(0) }
    var showSearchSheet by remember { mutableStateOf(false) }
    val actionLoading = remember { mutableStateMapOf<String, Boolean>() }

    fun load() {
        val requestId = ++latestRequestId
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchNotifications("all", false) }
            }.onSuccess {
                if (requestId != latestRequestId) return@onSuccess
                notifications = it.items
                onUnreadCountChange(it.items.count { item -> item.isUnread() })
                error = null
            }.onFailure {
                if (requestId != latestRequestId) return@onFailure
                error = it.message ?: "通知加载失败"
            }.also {
                if (requestId == latestRequestId) {
                    loading = false
                }
            }
        }
    }

    LaunchedEffect(authState.session.user.id) { load() }

    val filteredNotifications by remember(notifications, searchQuery, sortOrder) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            notifications.filter { item ->
                if (query.isBlank()) {
                    true
                } else {
                    listOf(item.title, item.content, item.creatorName, item.audienceLabel, item.audienceUsersLabel)
                        .any { value -> value.lowercase().contains(query) }
                }
            }.sortedBy { it.createdAt }.let { list ->
                if (sortOrder == "desc") list.reversed() else list
            }
        }
    }

    fun applyUnreadCount() {
        onUnreadCountChange(notifications.count { it.isUnread() })
        onNotificationStateRefresh()
    }

    fun markOneRead(item: NotificationRecipient) {
        if (!item.isUnread()) return
        val key = "read-${item.id}"
        actionLoading[key] = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.markNotificationRead(item.id) }
            }.onSuccess {
                notifications = notifications.map {
                    if (it.id == item.id) NotificationRecipient(it.id, it.notificationId, it.title, it.content, it.createdAt, it.creatorName, it.audienceType, it.audienceLabel, it.audienceUsersLabel, 1, it.readAt) else it
                }
                applyUnreadCount()
            }.onFailure {
                error = it.message ?: "标记已读失败"
            }.also {
                actionLoading.remove(key)
            }
        }
    }

    fun markAllRead() {
        actionLoading["read-all"] = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.markAllNotificationsRead() }
            }.onSuccess {
                notifications = notifications.map {
                    NotificationRecipient(it.id, it.notificationId, it.title, it.content, it.createdAt, it.creatorName, it.audienceType, it.audienceLabel, it.audienceUsersLabel, 1, it.readAt)
                }
                applyUnreadCount()
            }.onFailure {
                error = it.message ?: "全部标记已读失败"
            }.also {
                actionLoading.remove("read-all")
            }
        }
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "通知中心",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showSearchSheet = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索与排序")
                        }
                    }
                    Text("查看系统通知、接收范围与历史记录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { markAllRead() },
                            enabled = actionLoading["read-all"] != true
                        ) { Text("全部标记已读") }
                    }
                    error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        if (loading) {
            item { LoadingState() }
        } else if (filteredNotifications.isEmpty()) {
            item { EmptyState("暂无系统通知") }
        } else {
            items(filteredNotifications, key = { "notification-${it.id}" }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { previewItem = item },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.isUnread()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                item.title.ifBlank { "未命名通知" },
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (item.isUnread()) {
                                TextButton(
                                    onClick = { markOneRead(item) },
                                    enabled = actionLoading["read-${item.id}"] != true
                                ) { Text("已读") }
                            }
                        }
                        Text(
                            "${shortDate(item.createdAt)} · ${item.creatorName.ifBlank { "系统管理员" }} · ${item.audienceLabel.ifBlank { "未指定范围" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SelectableTextBlock(
                            text = summaryText(item.content, expanded = false, limit = 180),
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurface,
                            textSizeSp = 15f,
                            maxLines = 5,
                            ellipsize = TextUtils.TruncateAt.END
                        )
                        if (item.audienceType != "all" && item.audienceUsersLabel.isNotBlank()) {
                            Text(
                                "接收人：${item.audienceUsersLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    previewItem?.let { item ->
        ModalBottomSheet(onDismissRequest = { previewItem = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp, max = 620.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(item.title.ifBlank { "未命名通知" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${shortDate(item.createdAt)} · ${item.creatorName.ifBlank { "系统管理员" }} · ${item.audienceLabel.ifBlank { "未指定范围" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SelectableTextBlock(
                    text = item.content,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    textSizeSp = 16f
                )
                if (item.audienceType != "all" && item.audienceUsersLabel.isNotBlank()) {
                    Text(
                        "接收人：${item.audienceUsersLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showSearchSheet) {
        ModalBottomSheet(onDismissRequest = { showSearchSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 520.dp)
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("搜索与排序", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("搜索通知") },
                    placeholder = { Text("搜索标题、内容或接收人") },
                    singleLine = true
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InputChip(selected = sortOrder == "desc", onClick = { sortOrder = "desc" }, label = { Text("最新") })
                    InputChip(selected = sortOrder == "asc", onClick = { sortOrder = "asc" }, label = { Text("最早") })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (searchQuery.isNotBlank()) {
                        TextButton(onClick = { searchQuery = "" }) { Text("清空") }
                    }
                    Button(onClick = { showSearchSheet = false }) { Text("完成") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SiteNotificationPromptSheet(
    notificationItems: List<NotificationRecipient>,
    onDismiss: () -> Unit,
    onHideOneDay: () -> Unit,
    onOpenNotifications: () -> Unit,
    actionLoadingKey: String?,
    onMarkOneRead: (NotificationRecipient) -> Unit,
    onMarkAllRead: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp, max = 620.dp)
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (notificationItems.size > 1) "你有 ${notificationItems.size} 条未读系统通知" else "你有 1 条未读系统通知",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("未读通知会在你回到应用时提醒。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            ScrollableLazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 360.dp),
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notificationItems, key = { "notification-prompt-${it.id}" }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    item.title.ifBlank { "未命名通知" },
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(
                                    onClick = { onMarkOneRead(item) },
                                    enabled = actionLoadingKey == null
                                ) { Text("已读") }
                            }
                            Text(
                                "${shortDate(item.createdAt)} · ${item.audienceLabel.ifBlank { "未指定范围" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                summaryText(item.content, expanded = false, limit = 140),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onOpenNotifications, enabled = actionLoadingKey == null) { Text("查看通知中心") }
                OutlinedButton(onClick = onHideOneDay, enabled = actionLoadingKey == null) { Text("1 天内不再提示") }
                Button(onClick = onMarkAllRead, enabled = actionLoadingKey == null) { Text("一键已读") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorReviewCenterScreen(
    apiClient: BbsApiClient,
    authState: AuthState
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再查看处理中心")
        return
    }
    val scope = rememberCoroutineScope()
    var myReports by remember { mutableStateOf<List<CreatorAppeal>>(emptyList()) }
    var contentReports by remember { mutableStateOf<List<CreatorAppeal>>(emptyList()) }
    var reviewQueue by remember { mutableStateOf<List<CreatorAppeal>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    fun load() {
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorReviewCenter() }
            }.onSuccess {
                val reports = it.optJSONArray("myReports")
                val content = it.optJSONArray("contentReports")
                val queue = it.optJSONArray("reviewQueue")
                myReports = parseCreatorAppeals(reports)
                contentReports = parseCreatorAppeals(content)
                reviewQueue = parseCreatorAppeals(queue)
                error = null
            }.onFailure {
                error = it.message ?: "处理中心加载失败"
            }.also {
                loading = false
            }
        }
    }

    LaunchedEffect(authState.session.user.id) { load() }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("处理中心", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("管理我的举报、收到的举报与审核队列。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        if (loading) {
            item { LoadingState() }
        } else {
            item {
                val tabs = listOf(
                    "我的举报" to myReports,
                    "收到的举报" to contentReports,
                    "审核队列" to reviewQueue
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, pair ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text("${pair.first} (${pair.second.size})") }
                            )
                        }
                    }
                    val activeItems = tabs[selectedTab].second
                    if (activeItems.isEmpty()) {
                        EmptyState(
                            when (selectedTab) {
                                0 -> "暂无我的举报"
                                1 -> "暂无收到的举报"
                                else -> "暂无审核队列"
                            }
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            activeItems.forEach { appeal ->
                                ReviewReportCard(
                                    appeal = appeal,
                                    isMyReportTab = selectedTab == 0,
                                    showReporter = selectedTab != 0
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorPermissionsScreen(
    apiClient: BbsApiClient,
    authState: AuthState
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再查看权限状态")
        return
    }
    val scope = rememberCoroutineScope()
    var permissions by remember { mutableStateOf<CreatorPermissionsState?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState.session.user.id) {
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorPermissions() }
            }.onSuccess {
                permissions = it
                error = null
            }.onFailure {
                error = it.message ?: "权限状态加载失败"
            }.also { loading = false }
        }
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("权限状态", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        if (loading) item { LoadingState() }
        else {
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            permissions?.let { state ->
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(state.displayName(), fontWeight = FontWeight.Bold)
                            Text("@${state.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (state.isAdmin) {
                                Text("当前账号为全局管理员，常规功能默认开放。", color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("部分功能仍可能受等级、账号状态或专栏/系列创建门槛影响。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("专栏创建门槛：LV10（900 EXP）", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("系列创建门槛：LV5（400 EXP）", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            items(permissions?.items.orEmpty(), key = { it.key }) { item ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.label, fontWeight = FontWeight.Bold)
                        Text(item.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (item.enabled) "已启用" else "已禁用", color = if (item.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CreatorPersonalizationScreen(
    apiClient: BbsApiClient,
    authState: AuthState
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再查看个性化")
        return
    }
    val scope = rememberCoroutineScope()
    var pref by remember { mutableStateOf<CreatorPersonalization?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var excludedCats by remember { mutableStateOf("") }
    var excludedTagsState by remember { mutableStateOf(rememberTagEditorState("")) }
    var excludedUsers by remember { mutableStateOf<List<CreatorPersonalization.UserOption>>(emptyList()) }
    var userSearchQuery by remember { mutableStateOf("") }
    var userSearchResults by remember { mutableStateOf<List<CreatorGroup.GroupMember>>(emptyList()) }
    var hideAi by remember { mutableStateOf(false) }
    var selectedCategoryIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showUserSearchSheet by remember { mutableStateOf(false) }

    LaunchedEffect(authState.session.user.id) {
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorPersonalization() }
            }.onSuccess {
                pref = it
                excludedCats = it.excludedCats
                excludedTagsState = TagEditorState(items = parseTagLines(it.excludedTags.replace(",", "\n")))
                excludedUsers = it.excludedUserDetails
                hideAi = it.hideAi
                selectedCategoryIds = it.excludedCats.split(",").mapNotNull { value -> value.trim().toIntOrNull() }.toSet()
                error = null
            }.onFailure {
                error = it.message ?: "个性化加载失败"
            }.also { loading = false }
        }
    }

    LaunchedEffect(userSearchQuery) {
        val q = userSearchQuery.trim()
        if (q.length < 2) {
            userSearchResults = emptyList()
            return@LaunchedEffect
        }
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.searchCreatorUsers(q) }
            }.onSuccess {
                userSearchResults = it.filter { candidate -> excludedUsers.none { user -> user.id == candidate.id } }
            }
        }
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("个性化", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        if (loading) item { LoadingState() } else {
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("排除分类", fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pref?.categories.orEmpty().forEach { category ->
                            FilterChip(
                                selected = selectedCategoryIds.contains(category.id),
                                onClick = {
                                    selectedCategoryIds = if (selectedCategoryIds.contains(category.id)) {
                                        selectedCategoryIds - category.id
                                    } else {
                                        selectedCategoryIds + category.id
                                    }
                                    excludedCats = selectedCategoryIds.joinToString(",")
                                },
                                label = { Text(category.name) }
                            )
                        }
                    }
                    Text("与网页端一致，点击分类即可排除。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("屏蔽标签", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = excludedTagsState.draft,
                        onValueChange = { excludedTagsState = excludedTagsState.copy(draft = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("输入标签后添加") },
                        singleLine = true
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        excludedTagsState.items.forEach { tag ->
                            AssistChip(
                                onClick = {
                                    excludedTagsState = excludedTagsState.copy(
                                        items = excludedTagsState.items.filterNot { it == tag }
                                    )
                                },
                                label = { Text(tag) }
                            )
                        }
                        if (excludedTagsState.draft.trim().isNotBlank()) {
                            AssistChip(
                                onClick = {
                                    excludedTagsState = TagEditorState(
                                        items = appendTag(excludedTagsState.items, excludedTagsState.draft),
                                        draft = ""
                                    )
                                },
                                label = { Text("添加 ${excludedTagsState.draft.trim()}") }
                            )
                        }
                    }
                    Text("点击已有标签可移除，输入后点“添加”可新增。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("屏蔽用户", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showUserSearchSheet = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索屏蔽用户")
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        excludedUsers.forEach { user ->
                            AssistChip(
                                onClick = { excludedUsers = excludedUsers.filterNot { it.id == user.id } },
                                label = { Text(user.displayName()) }
                            )
                        }
                    }
                    Text("屏蔽用户实际按用户 ID 存储，与网页端逻辑一致。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                InputChip(selected = hideAi, onClick = { hideAi = !hideAi }, label = { Text(if (hideAi) "隐藏纯 AI 文章" else "显示纯 AI 文章") })
            }
            item {
                Button(
                    onClick = {
                        saving = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    apiClient.saveCreatorPersonalization(
                                        selectedCategoryIds.joinToString(","),
                                        excludedTagsState.items.joinToString(","),
                                        excludedUsers.joinToString(",") { it.id.toString() },
                                        hideAi
                                    )
                                }
                            }.onFailure {
                                error = it.message ?: "保存个性化失败"
                            }.also { saving = false }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (saving) "保存中..." else "保存") }
            }
        }
    }

    if (showUserSearchSheet) {
        ModalBottomSheet(onDismissRequest = { showUserSearchSheet = false }) {
            ScrollableLazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 560.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("搜索并添加屏蔽用户", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                item {
                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = { userSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("搜索用户") },
                        singleLine = true
                    )
                }
                if (userSearchResults.isEmpty()) {
                    item { SubtleText(if (userSearchQuery.trim().length < 2) "至少输入 2 个字符后开始搜索。" else "没有匹配用户。") }
                } else {
                    items(userSearchResults.take(5), key = { "blocked-user-search-${it.id}" }) { candidate ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(candidate.displayName(), modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                excludedUsers = excludedUsers + CreatorPersonalization.UserOption(candidate.id, candidate.username, candidate.nickname)
                                userSearchQuery = ""
                                userSearchResults = emptyList()
                                showUserSearchSheet = false
                            }) { Text("添加") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorGroupsScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    onCreateGroup: () -> Unit,
    onRenameGroup: (Int) -> Unit,
    onManageMembers: (Int) -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再管理群组")
        return
    }
    val scope = rememberCoroutineScope()
    var groups by remember { mutableStateOf<List<CreatorGroup>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorGroups() }
            }.onSuccess {
                groups = it
                error = null
            }.onFailure {
                error = it.message ?: "群组加载失败"
            }.also { loading = false }
        }
    }

    LaunchedEffect(authState.session.user.id) { load() }
    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("群组管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Button(onClick = onCreateGroup, modifier = Modifier.fillMaxWidth()) {
                        Text("新建群组")
                    }
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        if (loading) item { LoadingState() } else {
            items(groups, key = { it.id }) { group ->
                CreatorGroupCard(
                    group = group,
                    onRename = { onRenameGroup(group.id) },
                    onDelete = {
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) { apiClient.deleteCreatorGroup(group.id) }
                            }.onSuccess { load() }.onFailure {
                                error = it.message ?: "删除群组失败"
                            }
                        }
                    },
                    onManageMembers = { onManageMembers(group.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorGroupCard(
    group: CreatorGroup,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onManageMembers: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(group.name, fontWeight = FontWeight.Bold)
            Text("成员 ${group.members.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                group.members.forEach { member ->
                    AssistChip(
                        onClick = { },
                        label = { Text(member.displayName()) }
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onRename) { Text("重命名") }
                TextButton(onClick = onManageMembers) { Text("成员管理") }
                TextButton(onClick = onDelete) { Text("删除") }
            }
            if (group.members.isNotEmpty()) {
                Text("成员添加与移除请通过“成员管理”窗口操作。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReviewReportCard(
    appeal: CreatorAppeal,
    isMyReportTab: Boolean = false,
    showReporter: Boolean = false
) {
    val statusLabel = creatorReportStatusText(
        status = appeal.reviewStatus.ifBlank { appeal.status },
        isMyReport = isMyReportTab,
        targetBanStatus = appeal.targetBanStatus
    )
    val reviewTypeLabel = creatorReviewTypeText(appeal.reviewType)
    val targetTitle = when {
        appeal.targetTitle.isNotBlank() -> appeal.targetTitle
        appeal.titleSnapshot.isNotBlank() -> appeal.titleSnapshot
        appeal.targetType == "user_bio" -> "用户介绍"
        else -> "无标题目标"
    }
    val snapshot = listOf(appeal.contentSnapshot, appeal.targetContent, appeal.reason)
        .firstOrNull { it.isNotBlank() }
        .orEmpty()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(targetTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                "目标 ${appeal.targetType} #${appeal.targetId} · ${reviewTypeLabel} · ${shortDate(appeal.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                statusLabel,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    statusLabel.contains("违规") || statusLabel.contains("禁止") || statusLabel.contains("驳回") -> MaterialTheme.colorScheme.error
                    statusLabel.contains("限制") || statusLabel.contains("等待") || statusLabel.contains("审核中") -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            if (appeal.reason.isNotBlank()) {
                Text("原因：${appeal.reason}", style = MaterialTheme.typography.bodyMedium)
            }
            if (snapshot.isNotBlank()) {
                Text(
                    "内容：${summaryText(snapshot, expanded = false, limit = 220)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showReporter && appeal.reporterName.isNotBlank()) {
                Text("举报人：${appeal.reporterName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (appeal.targetPostId > 0) {
                Text("所属文章：#${appeal.targetPostId}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (appeal.rejectionReason.isNotBlank()) {
                Text("处理理由：${appeal.rejectionReason}", color = MaterialTheme.colorScheme.error)
            }
            if (appeal.suggestion.isNotBlank()) {
                Text("解决建议：${appeal.suggestion}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun parseCreatorAppeals(items: JSONArray?): List<CreatorAppeal> {
    val result = mutableListOf<CreatorAppeal>()
    if (items == null) return result
    for (i in 0 until items.length()) {
        val item = items.optJSONObject(i) ?: continue
        result.add(CreatorAppeal.fromJson(item))
    }
    return result
}

private fun parseTagLines(raw: String): List<String> = raw
    .lines()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .distinct()

private fun appendTag(existing: List<String>, candidate: String): List<String> {
    val clean = candidate.trim()
    if (clean.isBlank()) return existing
    return (existing + clean).distinct()
}

private fun creatorScopeLabel(scope: String): String = when (scope.uppercase()) {
    "ALL" -> "全部文章"
    "OWN" -> "仅自己文章"
    "NONE" -> "不可操作"
    else -> "未设置"
}

private fun rememberTagEditorState(initialRaw: String): TagEditorState =
    TagEditorState(items = parseTagLines(initialRaw))

@Composable
private fun ScrollableDialogColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 520.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorColumnsScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    onEditColumn: (Int) -> Unit,
    onManageMembers: (Int) -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再管理专栏")
        return
    }
    val scope = rememberCoroutineScope()
    var columns by remember { mutableStateOf<List<CreatorColumn>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var analyticsSheetTarget by remember { mutableStateOf<AnalyticsSheetTarget?>(null) }

    fun load() {
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorColumns() }
            }.onSuccess {
                columns = it
                error = null
            }.onFailure {
                error = it.message ?: "专栏列表加载失败"
            }.also { loading = false }
        }
    }

    LaunchedEffect(authState.session.user.id) { load() }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("专栏管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        if (loading) {
            item { LoadingState() }
        } else if (columns.isEmpty()) {
            item { EmptyState("暂无可管理的专栏") }
        } else {
            items(columns, key = { "column-${it.id}" }) { column ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(column.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("SLUG: ${column.slug}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        if (column.description.isNotBlank()) Text(column.description)
                        Text("创建者 ${column.creatorName} · 管理员 ${column.managerCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (column.managerNames.isNotEmpty()) {
                            Text("现有管理员：${column.managerNames.joinToString(" / ")}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("文章 ${column.postCount} · 子专栏 ${column.childCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("访客发布 ${if (column.visitorPostAllowed) "允许" else "禁止"} · 可见 ${creatorScopeLabel(column.visitorViewScope)} · 编辑 ${creatorScopeLabel(column.visitorEditScope)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("管理员发布 ${if (column.managerPostAllowed) "允许" else "禁止"} · 可见 ${creatorScopeLabel(column.managerViewScope)} · 编辑 ${creatorScopeLabel(column.managerEditScope)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                analyticsSheetTarget = AnalyticsSheetTarget(
                                    type = "column",
                                    id = column.id,
                                    title = "浏览量统计",
                                    description = "专栏统计按专栏内自己的文章聚合。",
                                    userKey = "column-${column.id}"
                                )
                            }) { Text("浏览量") }
                            TextButton(onClick = { onEditColumn(column.id) }) { Text("专栏设置") }
                            TextButton(onClick = { onManageMembers(column.id) }) { Text("权限管理") }
                        }
                    }
                }
            }
        }
    }

    analyticsSheetTarget?.let { target ->
        AnalyticsBottomSheet(
            apiClient = apiClient,
            target = target,
            onDismiss = { analyticsSheetTarget = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorSeriesScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    onOpenPost: (Int) -> Unit,
    onManageSeries: (Int) -> Unit,
    onEditSeries: (Int) -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再管理系列")
        return
    }
    val scope = rememberCoroutineScope()
    var series by remember { mutableStateOf<List<CreatorSeries>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingDeleteSeries by remember { mutableStateOf<CreatorSeries?>(null) }

    fun load() {
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorSeries() }
            }.onSuccess {
                series = it
                error = null
            }.onFailure {
                error = it.message ?: "系列列表加载失败"
            }.also { loading = false }
        }
    }

    LaunchedEffect(authState.session.user.id) { load() }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("系列管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        if (loading) {
            item { LoadingState() }
        } else if (series.isEmpty()) {
            item { EmptyState("暂无可管理的系列") }
        } else {
            items(series, key = { "series-${it.id}" }) { itemSeries ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(itemSeries.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (itemSeries.description.isNotBlank()) Text(itemSeries.description)
                        Text("文章 ${itemSeries.postCount} · 浏览 ${itemSeries.views}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemSeries.posts.take(3).forEach { post ->
                                TextButton(onClick = { onOpenPost(post.postId) }) { Text(post.title) }
                            }
                            TextButton(onClick = { onEditSeries(itemSeries.id) }) { Text("系列设置") }
                            TextButton(onClick = { onManageSeries(itemSeries.id) }) { Text("文章排序") }
                            TextButton(onClick = { pendingDeleteSeries = itemSeries }) { Text("删除系列") }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteSeries?.let { itemSeries ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSeries = null },
            title = { Text("确认删除系列") },
            text = { Text("确定删除系列《${itemSeries.name}》吗？该操作不会删除文章本身。") },
            confirmButton = {
                TextButton(onClick = {
                    val target = pendingDeleteSeries ?: return@TextButton
                    pendingDeleteSeries = null
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) { apiClient.deleteCreatorSeries(target.id) }
                        }.onSuccess { load() }.onFailure {
                            error = it.message ?: "删除系列失败"
                        }
                    }
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSeries = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun CreatorProfileScreen(
    apiClient: BbsApiClient,
    authState: AuthState
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再编辑创作者资料")
        return
    }
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<CreatorProfile?>(null) }
    var bioText by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorProfile() }
            }.onSuccess {
                profile = it
                bioText = it.bio
                error = null
            }.onFailure {
                error = it.message ?: "创作者资料加载失败"
            }
        }
    }

    LaunchedEffect(authState.session.user.id) {
        load()
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("创作者资料", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    profile?.let {
                        Text(
                            "${it.displayName()} · @${it.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (it.reviewStatus.isNotBlank()) {
                            Text(
                                "审核状态：${creatorReviewStatusText(it.reviewStatus)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (it.rejectionReason.isNotBlank()) {
                            Text(it.rejectionReason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                        if (it.suggestion.isNotBlank()) {
                            Text(it.suggestion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedTextField(
                        value = bioText,
                        onValueChange = { bioText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("自我介绍") },
                        minLines = 5,
                        maxLines = 8
                    )
                    error?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            saving = true
                            scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) { apiClient.updateCreatorProfile(bioText.trim()) }
                                }.onSuccess {
                                    load()
                                }.onFailure {
                                    error = it.message ?: "保存创作者资料失败"
                                }.also {
                                    saving = false
                                }
                            }
                        },
                        enabled = !saving && bioText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("提交审核")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorPostCreateScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    onClose: () -> Unit,
    onCreated: (Int) -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再新建文章")
        return
    }
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableIntStateOf(0) }
    var format by remember { mutableStateOf("markdown") }
    var aiUsage by remember { mutableIntStateOf(0) }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var newTag by remember { mutableStateOf("") }

    LaunchedEffect(authState.session.user.id) {
        loading = true
        runCatching {
            withContext(Dispatchers.IO) { apiClient.fetchCreatorCategories() }
        }.onSuccess {
            categories = it
            selectedCategoryId = it.firstOrNull { item -> item.id == 11 }?.id ?: it.firstOrNull()?.id ?: 0
            error = null
        }.onFailure {
            error = it.message ?: "分类加载失败"
        }.also {
            loading = false
        }
    }

    if (loading) {
        LoadingState()
        return
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("新建文章", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("分类和排版格式初始化后将锁定。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { if (it.length <= 500) title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("标题") },
                        singleLine = true
                    )
                    Text("${title.length} / 500", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("分类", fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { category ->
                            InputChip(selected = selectedCategoryId == category.id, onClick = { selectedCategoryId = category.id }, label = { Text(category.name) })
                        }
                    }
                    Text("排版格式", fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InputChip(selected = format == "markdown", onClick = { format = "markdown" }, label = { Text("Markdown") })
                        InputChip(selected = format == "html", onClick = { format = "html" }, label = { Text("HTML") })
                    }
                    Text("AI 参与度", fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "人工原创", 1 to "AI 辅助", 2 to "完全 AI").forEach { (value, label) ->
                            InputChip(selected = aiUsage == value, onClick = { aiUsage = value }, label = { Text(label) })
                        }
                    }
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("新增标签") },
                        singleLine = true
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { tag -> AssistChip(onClick = { tags = tags.filterNot { item -> item == tag } }, label = { Text(tag) }) }
                        if (newTag.isNotBlank()) {
                            AssistChip(onClick = {
                                val candidate = newTag.trim()
                                if (candidate.isNotBlank() && candidate !in tags) tags = tags + candidate
                                newTag = ""
                            }, label = { Text("添加 ${newTag.trim()}") })
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButtonCompat(text = "取消", modifier = Modifier.weight(1f), enabled = !saving, onClick = onClose)
                Button(
                    onClick = {
                        val cleanTitle = title.trim()
                        if (cleanTitle.isBlank()) {
                            error = "标题不能为空"
                            return@Button
                        }
                        if (selectedCategoryId <= 0) {
                            error = "请选择分类"
                            return@Button
                        }
                        saving = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    apiClient.createCreatorPost(cleanTitle, "", selectedCategoryId, tags.joinToString(","), aiUsage, format)
                                }
                            }.onSuccess { id ->
                                if (id > 0) onCreated(id) else error = "创建失败：响应缺少文章 ID"
                            }.onFailure {
                                error = it.message ?: "创建文章失败"
                            }.also {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (saving) "创建中..." else "进入编辑器")
                }
            }
        }
    }
}

private data class EditorInsertAction(
    val label: String,
    val prefix: String,
    val suffix: String = "",
    val placeholder: String = "",
    val mode: String = "wrap"
)

private fun TextFieldValue.insertWrapped(action: EditorInsertAction): TextFieldValue {
    if (action.mode == "line-prefix") return applyLinePrefix(action.prefix)
    if (action.mode == "ordered-list") return applyLinePrefix("1. ", ordered = true)
    if (action.mode == "fence") return applyFence(action.prefix, action.suffix, action.placeholder)
    if (action.mode == "line-wrap") return applyLineWrap(action.prefix, action.suffix, action.placeholder)
    val start = selection.start.coerceIn(0, text.length)
    val end = selection.end.coerceIn(0, text.length)
    val left = minOf(start, end)
    val right = maxOf(start, end)
    val selected = text.substring(left, right)
    val body = selected.ifBlank { action.placeholder }
    val insert = action.prefix + body + action.suffix
    val nextText = text.replaceRange(left, right, insert)
    val cursor = if (selected.isBlank() && action.placeholder.isNotBlank()) {
        left + action.prefix.length + action.placeholder.length
    } else {
        left + insert.length
    }
    return copy(text = nextText, selection = TextRange(cursor.coerceIn(0, nextText.length)))
}

private fun TextFieldValue.selectedLineRange(): IntRange {
    val start = minOf(selection.start, selection.end).coerceIn(0, text.length)
    val end = maxOf(selection.start, selection.end).coerceIn(0, text.length)
    val lineStart = text.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
    val lineEnd = if (end > start) {
        val adjusted = if (end > 0 && text.getOrNull(end - 1) == '\n') end - 1 else end
        text.indexOf('\n', adjusted).let { if (it < 0) text.length else it }
    } else {
        text.indexOf('\n', start).let { if (it < 0) text.length else it }
    }
    return lineStart..lineEnd
}

private fun TextFieldValue.applyLinePrefix(prefix: String, ordered: Boolean = false): TextFieldValue {
    val range = selectedLineRange()
    val original = text.substring(range.first, range.last)
    val lines = original.split('\n')
    val transformed = lines.mapIndexed { index, line ->
        val cleaned = line
            .replace(Regex("^\\s{0,3}#{1,6}\\s+"), "")
            .replace(Regex("^\\s*[-*+]\\s+"), "")
            .replace(Regex("^\\s*\\d+\\.\\s+"), "")
            .replace(Regex("^\\s*>\\s?"), "")
        if (ordered) "${index + 1}. $cleaned" else prefix + cleaned
    }.joinToString("\n")
    val nextText = text.replaceRange(range.first, range.last, transformed)
    return copy(text = nextText, selection = TextRange(range.first, range.first + transformed.length))
}

private fun TextFieldValue.applyFence(prefix: String, suffix: String, placeholder: String): TextFieldValue {
    val range = selectedLineRange()
    val original = text.substring(range.first, range.last).ifBlank { placeholder }
    val cleaned = original
        .removePrefix("```\n")
        .removeSuffix("\n```")
    val insert = prefix + cleaned + suffix
    val nextText = text.replaceRange(range.first, range.last, insert)
    return copy(text = nextText, selection = TextRange(range.first, range.first + insert.length))
}

private fun TextFieldValue.applyLineWrap(prefix: String, suffix: String, placeholder: String): TextFieldValue {
    val range = selectedLineRange()
    val original = text.substring(range.first, range.last).ifBlank { placeholder }
    val insert = prefix + original + suffix
    val nextText = text.replaceRange(range.first, range.last, insert)
    return copy(text = nextText, selection = TextRange(range.first, range.first + insert.length))
}

private fun markdownInsertActions(format: String): List<EditorInsertAction> {
    if (format.equals("html", ignoreCase = true)) {
        return listOf(
            EditorInsertAction("H2", "<h2>", "</h2>", "小标题", "line-wrap"),
            EditorInsertAction("粗体", "<strong>", "</strong>", "加粗文字"),
            EditorInsertAction("斜体", "<em>", "</em>", "斜体文字"),
            EditorInsertAction("链接", "<a href=\"https://\">", "</a>", "链接文字"),
            EditorInsertAction("图片", "<img src=\"https://\" alt=\"", "\">", "图片说明"),
            EditorInsertAction("引用", "<blockquote>", "</blockquote>", "引用内容", "line-wrap"),
            EditorInsertAction("代码", "<pre><code>", "</code></pre>", "代码", "line-wrap"),
            EditorInsertAction("表格", "<table><thead><tr><th>标题</th><th>标题</th></tr></thead><tbody><tr><td>", "</td><td></td></tr></tbody></table>", "内容"),
            EditorInsertAction("分割线", "\n<hr>\n")
        )
    }
    return listOf(
        EditorInsertAction("H2", "## ", mode = "line-prefix"),
        EditorInsertAction("H3", "### ", mode = "line-prefix"),
        EditorInsertAction("粗体", "**", "**", "加粗文字"),
        EditorInsertAction("斜体", "*", "*", "斜体文字"),
        EditorInsertAction("链接", "[", "](https://)", "链接文字"),
        EditorInsertAction("图片", "![", "](https://)", "图片说明"),
        EditorInsertAction("列表", "- ", mode = "line-prefix"),
        EditorInsertAction("编号", "1. ", mode = "ordered-list"),
        EditorInsertAction("引用", "> ", mode = "line-prefix"),
        EditorInsertAction("代码", "```\n", "\n```", "代码", "fence"),
        EditorInsertAction("表格", "\n| 标题 | 标题 |\n| --- | --- |\n| ", " |  |\n", "内容"),
        EditorInsertAction("分割线", "\n---\n")
    )
}

private fun editorSaveTimeText(value: String): String {
    if (value.isBlank()) return "尚未保存"
    return value.replace('T', ' ').substringBefore('.').take(16)
}

private fun nowSaveTimeText(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date())
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlainArticleEditor(
    value: TextFieldValue,
    format: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit,
    onPreview: () -> Unit
) {
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            enabled = enabled,
            label = { Text("正文") },
            placeholder = { Text(if (format == "html") "输入 HTML 正文..." else "输入 Markdown 正文...") },
            minLines = 18
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    markdownInsertActions(format).forEach { action ->
                        AssistChip(
                            onClick = { onValueChange(value.insertWrapped(action)) },
                            enabled = enabled,
                            label = { Text(action.label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onPreview, modifier = Modifier.weight(1f)) {
                        Text("预览")
                    }
                    Text(
                        if (format == "html") "HTML 纯文本" else "Markdown 纯文本",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticlePreviewBody(
    content: String,
    format: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .build()
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { viewContext ->
            TextView(viewContext).apply {
                textSize = 16f
                setLineSpacing(6f, 1.05f)
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = { textView ->
            textView.setTextColor(textColor.toArgb())
            if (format.equals("html", ignoreCase = true)) {
                textView.text = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
            } else {
                markwon.setMarkdown(textView, content)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CreatorPostEditorScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    postId: Int,
    onClose: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再编辑文章")
        return
    }
    val scope = rememberCoroutineScope()
    var detail by remember(postId) { mutableStateOf<CreatorPostDetail?>(null) }
    var groups by remember { mutableStateOf<List<CreatorGroup>>(emptyList()) }
    var loading by remember(postId) { mutableStateOf(true) }
    var saving by remember(postId) { mutableStateOf(false) }
    var autoSaving by remember(postId) { mutableStateOf(false) }
    var error by remember(postId) { mutableStateOf<String?>(null) }
    var title by remember(postId) { mutableStateOf("") }
    var contentField by remember(postId) { mutableStateOf(TextFieldValue("")) }
    var contentLength by remember(postId) { mutableIntStateOf(0) }
    var savedSnapshot by remember(postId) { mutableStateOf("" to "") }
    var lastSavedAt by remember(postId) { mutableStateOf("") }
    var autoSaveEnabled by remember(postId) { mutableStateOf(false) }
    var tags by remember(postId) { mutableStateOf<List<String>>(emptyList()) }
    var newTag by remember(postId) { mutableStateOf("") }
    var visibility by remember(postId) { mutableStateOf("public") }
    var groupFilterMode by remember(postId) { mutableStateOf("include") }
    var selectedGroupIds by remember(postId) { mutableStateOf<Set<Int>>(emptySet()) }
    var aiUsage by remember(postId) { mutableIntStateOf(0) }
    var requestTags by remember(postId) { mutableStateOf(false) }
    var requestSummary by remember(postId) { mutableStateOf(false) }
    var submitDialog by remember(postId) { mutableStateOf(false) }
    var dailyLimit by remember(postId) { mutableStateOf<CreatorDailyLimit?>(null) }
    var selectedColumn by remember(postId) { mutableStateOf<ColumnItem?>(null) }
    var columnSearch by remember(postId) { mutableStateOf("") }
    var columnOptions by remember(postId) { mutableStateOf<List<ColumnItem>>(emptyList()) }
    var showTitleEditor by remember(postId) { mutableStateOf(false) }
    var showSettingsSheet by remember(postId) { mutableStateOf(false) }
    var showPreviewSheet by remember(postId) { mutableStateOf(false) }

    LaunchedEffect(postId, authState.session.user.id) {
        loading = true
        runCatching {
            withContext(Dispatchers.IO) {
                apiClient.fetchCreatorPostDetail(postId) to apiClient.fetchCreatorGroups()
            }
        }.onSuccess { (postDetail, userGroups) ->
            detail = postDetail
            groups = userGroups
            val post = postDetail.post
            title = postDetail.draftTitle.ifBlank { post?.title ?: "" }
            val loadedContent = postDetail.draftContent.ifBlank { post?.content ?: "" }
            contentField = TextFieldValue(loadedContent, selection = TextRange(loadedContent.length))
            contentLength = loadedContent.length
            savedSnapshot = title.trim() to loadedContent
            lastSavedAt = postDetail.draftUpdatedAt
            tags = (post?.tags ?: "").split(",").map { it.trim() }.filter { it.isNotBlank() }
            visibility = post?.visibility ?: "public"
            groupFilterMode = post?.groupFilterMode ?: "include"
            selectedGroupIds = postDetail.postGroups.map { it.groupId }.filter { it > 0 }.toSet()
            aiUsage = post?.aiUsage ?: 0
            selectedColumn = if (post != null && post.columnId > 0) ColumnItem(post.columnId, post.columnName, "", "") else null
            error = null
        }.onFailure {
            error = it.message ?: "加载编辑器失败"
        }.also {
            loading = false
        }
    }

    if (loading) {
        LoadingState()
        return
    }
    val current = detail
    val post = current?.post
    if (post == null) {
        EmptyState(error ?: "文章不存在或无权编辑")
        return
    }
    val pendingReview = current.pendingReviewStatus.isNotBlank()
    val content = contentField.text
    val isOverLimit = contentLength > 100000 || title.length > 500

    fun save(submit: Boolean, automatic: Boolean = false) {
        if (title.trim().isBlank()) {
            if (!automatic) error = "标题不能为空"
            return
        }
        if (isOverLimit) {
            if (!automatic) error = "标题或正文超出字数限制"
            return
        }
        if (automatic) autoSaving = true else saving = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    apiClient.updateCreatorPostFull(
                        postId,
                        title.trim(),
                        content,
                        tags.joinToString(","),
                        post.categoryId,
                        selectedColumn?.id,
                        aiUsage,
                        visibility,
                        groupFilterMode,
                        selectedGroupIds.toList(),
                        submit,
                        requestTags,
                        requestSummary
                    )
                }
            }.onSuccess {
                if (submit) {
                    submitDialog = false
                    onClose()
                } else {
                    savedSnapshot = title.trim() to content
                    lastSavedAt = nowSaveTimeText()
                    error = if (automatic) null else "草稿已保存"
                }
            }.onFailure {
                error = it.message ?: if (submit) "发布失败" else if (automatic) "自动保存失败" else "保存草稿失败"
            }.also {
                if (automatic) autoSaving = false else saving = false
            }
        }
    }

    LaunchedEffect(autoSaveEnabled, title, content, tags, selectedColumn, visibility, groupFilterMode, selectedGroupIds) {
        if (!autoSaveEnabled || pendingReview || loading) return@LaunchedEffect
        if (title.trim().isBlank() || isOverLimit) return@LaunchedEffect
        if (savedSnapshot == (title.trim() to content)) return@LaunchedEffect
        delay(8000)
        if (autoSaveEnabled && savedSnapshot != (title.trim() to content)) {
            save(submit = false, automatic = true)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        title.ifBlank { "未命名文章" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        buildList {
                            add(if (post.format == "html") "HTML" else "Markdown")
                            add(post.categoryName)
                            add("${contentLength} / 100000")
                            add("上次保存 ${editorSaveTimeText(lastSavedAt)}")
                            if (autoSaveEnabled) add(if (autoSaving) "自动保存中" else "自动保存")
                            if (pendingReview) add("审核中")
                        }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(onClick = { showTitleEditor = !showTitleEditor }) { Text("标题") }
                TextButton(onClick = { showSettingsSheet = true }) { Text("设置") }
            }
            if (showTitleEditor) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 500) title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("标题") },
                    singleLine = true
                )
            }
            error?.let { Text(it, color = if (it.contains("已保存")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }

        PlainArticleEditor(
            value = contentField,
            format = post.format,
            enabled = !pendingReview,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onValueChange = { value ->
                contentField = value
                contentLength = value.text.length
            },
            onPreview = { showPreviewSheet = true }
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            tonalElevation = 3.dp
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { showSettingsSheet = true }, modifier = Modifier.weight(0.9f)) {
                    Text("设置")
                }
                Button(onClick = { save(false) }, enabled = !saving && !pendingReview, modifier = Modifier.weight(1f)) {
                    Text(if (saving) "处理中..." else "保存")
                }
                Button(onClick = {
                    scope.launch {
                        dailyLimit = runCatching { withContext(Dispatchers.IO) { apiClient.fetchCreatorDailyLimit() } }.getOrNull()
                        submitDialog = true
                    }
                }, enabled = !saving && !pendingReview, modifier = Modifier.weight(1f)) {
                    Text("发布")
                }
            }
        }
    }

    if (showSettingsSheet) {
        ModalBottomSheet(onDismissRequest = { showSettingsSheet = false }) {
            ScrollableLazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 360.dp, max = 620.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item {
                Text("文章设置", fontWeight = FontWeight.Bold)
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("自动保存", fontWeight = FontWeight.SemiBold)
                        Text(
                            "开启后停止输入约 8 秒自动保存草稿。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = autoSaveEnabled, onCheckedChange = { autoSaveEnabled = it }, enabled = !pendingReview)
                }
                Text("上次保存：${editorSaveTimeText(lastSavedAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "人工原创", 1 to "AI 辅助", 2 to "完全 AI").forEach { (value, label) ->
                        InputChip(selected = aiUsage == value, onClick = { aiUsage = value }, label = { Text(label) })
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("新增标签") },
                    singleLine = true
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag -> AssistChip(onClick = { tags = tags.filterNot { item -> item == tag } }, label = { Text(tag) }) }
                    if (newTag.isNotBlank()) {
                        AssistChip(onClick = {
                            val candidate = newTag.trim()
                            if (candidate.isNotBlank() && candidate !in tags) tags = tags + candidate
                            newTag = ""
                        }, label = { Text("添加 ${newTag.trim()}") })
                    }
                }
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("public" to "公开", "private" to "私密", "group" to "群组").forEach { (value, label) ->
                        InputChip(selected = visibility == value, onClick = { visibility = value }, label = { Text(label) })
                    }
                }
            }
            if (visibility == "group") {
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InputChip(selected = groupFilterMode == "include", onClick = { groupFilterMode = "include" }, label = { Text("仅选定群组可见") })
                        InputChip(selected = groupFilterMode == "exclude", onClick = { groupFilterMode = "exclude" }, label = { Text("选定群组不可见") })
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        groups.forEach { group ->
                            InputChip(
                                selected = group.id in selectedGroupIds,
                                onClick = { selectedGroupIds = if (group.id in selectedGroupIds) selectedGroupIds - group.id else selectedGroupIds + group.id },
                                label = { Text("${group.name} (${group.members.size})") }
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = columnSearch,
                    onValueChange = { value ->
                        columnSearch = value
                        if (value.trim().isBlank()) {
                            columnOptions = emptyList()
                        } else {
                            scope.launch {
                                columnOptions = runCatching { withContext(Dispatchers.IO) { apiClient.searchColumns(value.trim()) } }.getOrDefault(emptyList())
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("搜索专栏") },
                    singleLine = true
                )
                selectedColumn?.let { AssistChip(onClick = { selectedColumn = null }, label = { Text("已选专栏：${it.name}") }) }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    columnOptions.forEach { column ->
                        AssistChip(onClick = {
                            selectedColumn = column
                            columnSearch = ""
                            columnOptions = emptyList()
                        }, label = { Text(column.name) })
                    }
                }
            }
        }
    }
    }

    if (showPreviewSheet) {
        ModalBottomSheet(onDismissRequest = { showPreviewSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp, max = 560.dp)
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("预览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showPreviewSheet = false }) { Text("关闭") }
                }
                if (content.isBlank()) {
                    Text("正文为空", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        ArticlePreviewBody(content = content, format = post.format)
                    }
                }
            }
        }
    }

    if (submitDialog) {
        AlertDialog(
            onDismissRequest = { submitDialog = false },
            title = { Text("发布文章") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("发布后文章会进入审核队列，审核期间无法编辑正文。")
                    dailyLimit?.let { Text("今日更新次数 ${it.used}/${it.limit}，剩余 ${it.remaining} 次，LV${it.level}") }
                    InputChip(selected = requestSummary, onClick = { requestSummary = !requestSummary }, label = { Text("AI 更新摘要") })
                    InputChip(selected = requestTags, onClick = { requestTags = !requestTags }, label = { Text("AI 生成标签") })
                }
            },
            confirmButton = {
                TextButton(onClick = { save(true) }, enabled = !saving && dailyLimit?.allowed != false) { Text("发布") }
            },
            dismissButton = {
                TextButton(onClick = { submitDialog = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorPostSettingsScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    postId: Int,
    onClose: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再设置文章")
        return
    }
    val scope = rememberCoroutineScope()
    var detail by remember(postId) { mutableStateOf<CreatorPostDetail?>(null) }
    var loading by remember(postId) { mutableStateOf(true) }
    var saving by remember(postId) { mutableStateOf(false) }
    var error by remember(postId) { mutableStateOf<String?>(null) }
    var title by remember(postId) { mutableStateOf("") }
    var tags by remember(postId) { mutableStateOf<List<String>>(emptyList()) }
    var newTag by remember(postId) { mutableStateOf("") }
    var visibility by remember(postId) { mutableStateOf("public") }
    var requestTags by remember(postId) { mutableStateOf(false) }
    var requestSummary by remember(postId) { mutableStateOf(false) }

    LaunchedEffect(postId) {
        loading = true
        runCatching {
            withContext(Dispatchers.IO) { apiClient.fetchCreatorPostDetail(postId) }
        }.onSuccess {
            detail = it
            title = it.post?.title ?: ""
            tags = (it.post?.tags ?: "").split(",").map { value -> value.trim() }.filter { value -> value.isNotBlank() }
            visibility = it.post?.visibility ?: "public"
            error = null
        }.onFailure {
            error = it.message ?: "加载文章设置失败"
        }.also {
            loading = false
        }
    }

    if (loading) {
        LoadingState()
        return
    }

    val current = detail
    if (current?.post == null) {
        EmptyState(error ?: "文章设置为空")
        return
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("文章设置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (current.post.summary.isNotBlank()) {
                        Text(
                            "当前摘要：${current.post.summary}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (current.pendingReviewStatus.isNotBlank()) {
                        Text(
                            "待处理状态：${creatorReviewStatusText(current.pendingReviewStatus)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (current.rejectionReason.isNotBlank()) {
                        Text(current.rejectionReason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    if (current.suggestion.isNotBlank()) {
                        Text(current.suggestion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    error?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("标题") }
                    )
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("新增标签") },
                        singleLine = true
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { tag ->
                            AssistChip(onClick = { tags = tags.filterNot { item -> item == tag } }, label = { Text(tag) })
                        }
                        if (newTag.isNotBlank()) {
                            AssistChip(
                                onClick = {
                                    val candidate = newTag.trim()
                                    if (candidate.isNotBlank() && candidate !in tags) {
                                        tags = tags + candidate
                                    }
                                    newTag = ""
                                },
                                label = { Text("添加 ${newTag.trim()}") }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("public" to "公开", "private" to "部分可见", "group" to "群组可见").forEach { (value, label) ->
                            InputChip(selected = visibility == value, onClick = { visibility = value }, label = { Text(label) })
                        }
                    }
                    Text(
                        "当前移动端不支持单篇文章评论开关，评论权限沿用站点现有规则。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InputChip(selected = requestTags, onClick = { requestTags = !requestTags }, label = { Text("更新标签") })
                        InputChip(selected = requestSummary, onClick = { requestSummary = !requestSummary }, label = { Text("更新摘要") })
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButtonCompat(text = "关闭", modifier = Modifier.weight(1f), enabled = !saving, onClick = onClose)
                Button(
                    onClick = {
                        saving = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    apiClient.updateCreatorPostSettings(
                                        postId,
                                        title.trim().takeIf { it.isNotBlank() },
                                        null,
                                        tags.joinToString(","),
                                        current.post.categoryId,
                                        current.post.columnId.takeIf { it > 0 },
                                        visibility,
                                        null,
                                        false,
                                        requestTags,
                                        requestSummary
                                    )
                                }
                            }.onSuccess {
                                onClose()
                            }.onFailure {
                                error = it.message ?: "保存文章设置失败"
                            }.also {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (saving) "保存中..." else "保存")
                }
            }
        }
    }
}

@Composable
private fun CreatorSeriesEditScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    seriesId: Int,
    onClose: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再设置系列")
        return
    }
    val scope = rememberCoroutineScope()
    var loading by remember(seriesId) { mutableStateOf(true) }
    var saving by remember(seriesId) { mutableStateOf(false) }
    var error by remember(seriesId) { mutableStateOf<String?>(null) }
    var name by remember(seriesId) { mutableStateOf("") }
    var slug by remember(seriesId) { mutableStateOf("") }
    var description by remember(seriesId) { mutableStateOf("") }
    var loadedSeriesName by remember(seriesId) { mutableStateOf("") }

    LaunchedEffect(seriesId) {
        loading = true
        runCatching {
            withContext(Dispatchers.IO) { apiClient.fetchCreatorSeriesDetail(seriesId) }
        }.onSuccess {
            name = it.name
            slug = it.slug
            description = it.description
            loadedSeriesName = it.name
            error = null
        }.onFailure {
            error = it.message ?: "加载系列设置失败"
        }.also {
            loading = false
        }
    }

    if (loading) {
        LoadingState()
        return
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("名称") }, singleLine = true)
                    OutlinedTextField(value = slug, onValueChange = { slug = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Slug") }, singleLine = true)
                    OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), label = { Text("简介") }, minLines = 4)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        item {
            AnalyticsSection(
                apiClient = apiClient,
                userKey = seriesId,
                type = "series",
                id = seriesId,
                title = "系列浏览量趋势",
                description = loadedSeriesName.ifBlank { null }?.let { "当前系列：$it" }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButtonCompat(text = "关闭", modifier = Modifier.weight(1f), enabled = !saving, onClick = onClose)
                Button(
                    onClick = {
                        saving = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    apiClient.updateCreatorSeriesDetails(seriesId, name.trim(), slug.trim(), description.trim())
                                }
                            }.onSuccess {
                                onClose()
                            }.onFailure {
                                error = it.message ?: "保存系列配置失败"
                            }.also {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.weight(1f)
                ) { Text(if (saving) "保存中..." else "保存") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorColumnEditScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    columnId: Int,
    onClose: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再设置专栏")
        return
    }
    val scope = rememberCoroutineScope()
    var loading by remember(columnId) { mutableStateOf(true) }
    var saving by remember(columnId) { mutableStateOf(false) }
    var error by remember(columnId) { mutableStateOf<String?>(null) }
    var name by remember(columnId) { mutableStateOf("") }
    var slug by remember(columnId) { mutableStateOf("") }
    var description by remember(columnId) { mutableStateOf("") }
    var visitorPostAllowed by remember(columnId) { mutableStateOf(false) }
    var visitorViewScope by remember(columnId) { mutableStateOf("ALL") }
    var visitorEditScope by remember(columnId) { mutableStateOf("OWN") }
    var managerPostAllowed by remember(columnId) { mutableStateOf(false) }
    var managerViewScope by remember(columnId) { mutableStateOf("ALL") }
    var managerEditScope by remember(columnId) { mutableStateOf("ALL") }

    LaunchedEffect(columnId) {
        loading = true
        runCatching {
            withContext(Dispatchers.IO) { apiClient.fetchCreatorColumnDetail(columnId) }
        }.onSuccess {
            name = it.name
            slug = it.slug
            description = it.description
            visitorPostAllowed = it.visitorPostAllowed
            visitorViewScope = it.visitorViewScope.ifBlank { "ALL" }
            visitorEditScope = it.visitorEditScope.ifBlank { "OWN" }
            managerPostAllowed = it.managerPostAllowed
            managerViewScope = it.managerViewScope.ifBlank { "ALL" }
            managerEditScope = it.managerEditScope.ifBlank { "ALL" }
            error = null
        }.onFailure {
            error = it.message ?: "加载专栏设置失败"
        }.also {
            loading = false
        }
    }

    if (loading) {
        LoadingState()
        return
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("名称") }, singleLine = true)
                    OutlinedTextField(value = slug, onValueChange = { slug = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Slug") }, singleLine = true)
                    OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), label = { Text("简介") }, minLines = 4)
                    InputChip(selected = visitorPostAllowed, onClick = { visitorPostAllowed = !visitorPostAllowed }, label = { Text(if (visitorPostAllowed) "允许访客发布文章" else "禁止访客发布文章") })
                    InputChip(selected = managerPostAllowed, onClick = { managerPostAllowed = !managerPostAllowed }, label = { Text(if (managerPostAllowed) "允许管理员发布文章" else "禁止管理员发布文章") })
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL", "OWN", "NONE").forEach { scopeValue ->
                            InputChip(selected = visitorViewScope == scopeValue, onClick = { visitorViewScope = scopeValue }, label = { Text("访客可见 ${creatorScopeLabel(scopeValue)}") })
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL", "OWN", "NONE").forEach { scopeValue ->
                            InputChip(selected = visitorEditScope == scopeValue, onClick = { visitorEditScope = scopeValue }, label = { Text("访客编辑 ${creatorScopeLabel(scopeValue)}") })
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL", "OWN", "NONE").forEach { scopeValue ->
                            InputChip(selected = managerViewScope == scopeValue, onClick = { managerViewScope = scopeValue }, label = { Text("管理员可见 ${creatorScopeLabel(scopeValue)}") })
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL", "OWN", "NONE").forEach { scopeValue ->
                            InputChip(selected = managerEditScope == scopeValue, onClick = { managerEditScope = scopeValue }, label = { Text("管理员编辑 ${creatorScopeLabel(scopeValue)}") })
                        }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButtonCompat(text = "关闭", modifier = Modifier.weight(1f), enabled = !saving, onClick = onClose)
                Button(
                    onClick = {
                        saving = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    apiClient.updateCreatorColumnConfig(
                                        columnId,
                                        name.trim(),
                                        slug.trim(),
                                        description.trim(),
                                        visitorPostAllowed,
                                        visitorViewScope,
                                        visitorEditScope,
                                        managerPostAllowed,
                                        managerViewScope,
                                        managerEditScope
                                    )
                                }
                            }.onSuccess {
                                onClose()
                            }.onFailure {
                                error = it.message ?: "保存专栏设置失败"
                            }.also {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.weight(1f)
                ) { Text(if (saving) "保存中..." else "保存") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatorColumnMembersScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    columnId: Int,
    onClose: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再管理专栏权限")
        return
    }
    val scope = rememberCoroutineScope()
    var loading by remember(columnId) { mutableStateOf(true) }
    var error by remember(columnId) { mutableStateOf<String?>(null) }
    var columnName by remember(columnId) { mutableStateOf("") }
    var columnManagerList by remember(columnId) { mutableStateOf<List<String>>(emptyList()) }
    var managerQuery by remember(columnId) { mutableStateOf("") }
    var managerResults by remember(columnId) { mutableStateOf<List<CreatorGroup.GroupMember>>(emptyList()) }
    var showSearchSheet by remember(columnId) { mutableStateOf(false) }

    fun load() {
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorColumnDetail(columnId) }
            }.onSuccess {
                columnName = it.name
                columnManagerList = it.managerNames
                error = null
            }.onFailure {
                error = it.message ?: "加载专栏权限失败"
            }.also {
                loading = false
            }
        }
    }

    LaunchedEffect(columnId) { load() }
    LaunchedEffect(columnId, managerQuery, columnManagerList) {
        val q = managerQuery.trim()
        if (q.length < 2) {
            managerResults = emptyList()
            return@LaunchedEffect
        }
        runCatching {
            withContext(Dispatchers.IO) { apiClient.searchCreatorUsers(q) }
        }.onSuccess {
            managerResults = it.filter { item -> item.displayName() !in columnManagerList }
        }
    }

    if (loading) {
        LoadingState()
        return
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(columnName.ifBlank { "权限管理" }, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showSearchSheet = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索用户")
                        }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        if (columnManagerList.isEmpty()) {
            item { SubtleText("当前还没有管理员。") }
        } else {
            items(columnManagerList, key = { "column-manager-$it" }) { managerName ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Text(managerName, modifier = Modifier.padding(16.dp))
                }
            }
        }
        item {
            OutlinedButtonCompat(text = "关闭", modifier = Modifier.fillMaxWidth(), onClick = onClose)
        }
    }

    if (showSearchSheet) {
        ModalBottomSheet(onDismissRequest = { showSearchSheet = false }) {
            ScrollableLazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 560.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("搜索用户", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                item {
                    OutlinedTextField(
                        value = managerQuery,
                        onValueChange = { managerQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("搜索用户") },
                        singleLine = true
                    )
                }
                if (managerResults.isEmpty()) {
                    item {
                        SubtleText(if (managerQuery.trim().length < 2) "至少输入 2 个字符后开始搜索。" else "没有匹配用户。")
                    }
                } else {
                    items(managerResults.take(5), key = { "column-search-${it.id}" }) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.displayName(), modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                scope.launch {
                                    runCatching {
                                        withContext(Dispatchers.IO) { apiClient.addCreatorColumnManager(columnId, item.id) }
                                    }.onSuccess {
                                        managerQuery = ""
                                        managerResults = emptyList()
                                        showSearchSheet = false
                                        load()
                                    }.onFailure {
                                        error = it.message ?: "添加专栏管理员失败"
                                    }
                                }
                            }) { Text("添加") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatorGroupCreateScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    onClose: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再新建群组")
        return
    }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("群组名称") }, singleLine = true)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButtonCompat(text = "关闭", modifier = Modifier.weight(1f), enabled = !saving, onClick = onClose)
                Button(
                    onClick = {
                        saving = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) { apiClient.createCreatorGroup(name.trim()) }
                            }.onSuccess {
                                onClose()
                            }.onFailure {
                                error = it.message ?: "创建群组失败"
                            }.also {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving && name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text(if (saving) "创建中..." else "确认创建") }
            }
        }
    }
}

@Composable
private fun CreatorGroupRenameScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    groupId: Int,
    onClose: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再重命名群组")
        return
    }
    val scope = rememberCoroutineScope()
    var loading by remember(groupId) { mutableStateOf(true) }
    var saving by remember(groupId) { mutableStateOf(false) }
    var error by remember(groupId) { mutableStateOf<String?>(null) }
    var name by remember(groupId) { mutableStateOf("") }

    LaunchedEffect(groupId) {
        loading = true
        runCatching {
            withContext(Dispatchers.IO) { apiClient.fetchCreatorGroups() }
        }.onSuccess { groups ->
            name = groups.firstOrNull { it.id == groupId }?.name.orEmpty()
            error = null
        }.onFailure {
            error = it.message ?: "加载群组失败"
        }.also {
            loading = false
        }
    }

    if (loading) {
        LoadingState()
        return
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("名称") }, singleLine = true)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButtonCompat(text = "关闭", modifier = Modifier.weight(1f), enabled = !saving, onClick = onClose)
                Button(
                    onClick = {
                        saving = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) { apiClient.renameCreatorGroup(groupId, name.trim()) }
                            }.onSuccess {
                                onClose()
                            }.onFailure {
                                error = it.message ?: "重命名群组失败"
                            }.also {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving && name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text(if (saving) "保存中..." else "保存") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatorGroupMembersScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    groupId: Int,
    onClose: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再管理群组成员")
        return
    }
    val scope = rememberCoroutineScope()
    var groupName by remember(groupId) { mutableStateOf("") }
    var loading by remember(groupId) { mutableStateOf(true) }
    var error by remember(groupId) { mutableStateOf<String?>(null) }
    var members by remember(groupId) { mutableStateOf<List<CreatorGroup.GroupMember>>(emptyList()) }
    var memberQuery by remember(groupId) { mutableStateOf("") }
    var memberSearchResults by remember(groupId) { mutableStateOf<List<CreatorGroup.GroupMember>>(emptyList()) }
    var showSearchSheet by remember(groupId) { mutableStateOf(false) }

    fun load() {
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorGroups() }
            }.onSuccess { groups ->
                val current = groups.firstOrNull { it.id == groupId }
                groupName = current?.name.orEmpty()
                members = current?.members ?: emptyList()
                error = null
            }.onFailure {
                error = it.message ?: "加载群组成员失败"
            }.also {
                loading = false
            }
        }
    }

    LaunchedEffect(groupId) { load() }
    LaunchedEffect(groupId, memberQuery, members) {
        val q = memberQuery.trim()
        if (q.length < 2) {
            memberSearchResults = emptyList()
            return@LaunchedEffect
        }
        runCatching {
            withContext(Dispatchers.IO) { apiClient.searchCreatorUsers(q) }
        }.onSuccess {
            memberSearchResults = it.filter { candidate -> members.none { member -> member.id == candidate.id } }
        }
    }

    if (loading) {
        LoadingState()
        return
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(groupName.ifBlank { "成员管理" }, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showSearchSheet = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索用户")
                        }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        if (members.isEmpty()) {
            item { SubtleText("当前群组还没有成员。") }
        } else {
            items(members, key = { "group-member-${it.id}" }) { member ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(member.displayName(), modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) { apiClient.removeCreatorGroupMember(groupId, member.id) }
                            }.onSuccess {
                                load()
                            }.onFailure {
                                error = it.message ?: "移除成员失败"
                            }
                        }
                    }) { Text("移除") }
                }
            }
        }
        item {
            OutlinedButtonCompat(text = "关闭", modifier = Modifier.fillMaxWidth(), onClick = onClose)
        }
    }

    if (showSearchSheet) {
        ModalBottomSheet(onDismissRequest = { showSearchSheet = false }) {
            ScrollableLazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 560.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("搜索用户", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                item {
                    OutlinedTextField(
                        value = memberQuery,
                        onValueChange = { memberQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("搜索用户") },
                        singleLine = true
                    )
                }
                if (memberSearchResults.isEmpty()) {
                    item {
                        SubtleText(if (memberQuery.trim().length < 2) "至少输入 2 个字符后开始搜索。" else "没有匹配用户。")
                    }
                } else {
                    items(memberSearchResults.take(5), key = { "group-search-${it.id}" }) { member ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(member.displayName(), modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                scope.launch {
                                    runCatching {
                                        withContext(Dispatchers.IO) { apiClient.addCreatorGroupMember(groupId, member.username) }
                                    }.onSuccess {
                                        memberQuery = ""
                                        memberSearchResults = emptyList()
                                        showSearchSheet = false
                                        load()
                                    }.onFailure {
                                        error = it.message ?: "添加成员失败"
                                    }
                                }
                            }) { Text("添加") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlinedButtonCompat(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Text(text)
    }
}

@Composable
private fun SeriesDropPlaceholder(height: Int) {
    val placeholderHeight = with(LocalDensity.current) { height.coerceAtLeast(72).toDp() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(placeholderHeight),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text("放置到这里", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SeriesPostSortCard(
    post: CreatorSeries.SeriesPostItem,
    index: Int,
    saving: Boolean,
    placeholder: Boolean = false,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit,
    onHandleLongDrag: () -> Unit,
    onHandleDrag: (Float) -> Unit,
    onHandleDragEnd: () -> Unit,
    onHandleDragCancel: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (placeholder) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        )
    ) {
        Box(Modifier.fillMaxWidth()) {
            if (placeholder) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("放置到这里", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).alpha(if (placeholder) 0f else 1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "${index + 1}. ${post.title.ifBlank { "未命名文章" }}",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "文章 ID ${post.postId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onRemove, enabled = !saving && !placeholder) { Text("移除") }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .pointerInput(post.postId, saving) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                if (saving) return@awaitEachGesture
                                val longPress = awaitLongPressOrCancellation(down.id)
                                if (longPress == null) {
                                    onHandleDragCancel()
                                    return@awaitEachGesture
                                }
                                longPress.consume()
                                onHandleLongDrag()
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    val deltaY = change.positionChange().y
                                    if (deltaY != 0f) {
                                        change.consume()
                                        onHandleDrag(deltaY)
                                    }
                                }
                                onHandleDragEnd()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "拖动排序"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatorSeriesManageScreen(
    apiClient: BbsApiClient,
    authState: AuthState,
    seriesId: Int,
    hapticsEnabled: Boolean,
    onClose: () -> Unit
) {
    if (authState !is AuthState.SignedIn) {
        EmptyState("请先登录后再管理系列文章")
        return
    }
    val scope = rememberCoroutineScope()
    var session by remember(seriesId) { mutableStateOf(CreatorSeriesManageSession(seriesId = seriesId, loadingCandidates = true)) }
    var seriesName by remember(seriesId) { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingRemovePost by remember(seriesId) { mutableStateOf<CreatorSeries.SeriesPostItem?>(null) }
    var draggedPostId by remember(seriesId) { mutableStateOf<Int?>(null) }
    var dragStartIndex by remember(seriesId) { mutableStateOf<Int?>(null) }
    var dropIndex by remember(seriesId) { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember(seriesId) { mutableStateOf(0f) }
    var draggedStartTop by remember(seriesId) { mutableStateOf(0f) }
    var draggedItemHeight by remember(seriesId) { mutableStateOf(0) }
    var listContainerTop by remember(seriesId) { mutableStateOf(0f) }
    val itemTopByPostId = remember(seriesId) { mutableStateMapOf<Int, Float>() }
    val itemHeightByPostId = remember(seriesId) { mutableStateMapOf<Int, Int>() }
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    var showSearchSheet by remember(seriesId) { mutableStateOf(false) }

    fun performHaptic(type: HapticFeedbackType = HapticFeedbackType.TextHandleMove) {
        if (hapticsEnabled) {
            haptic.performHapticFeedback(type)
        }
    }

    fun updateDropIndex(pointerCenterY: Float) {
        val draggedId = draggedPostId ?: return
        val targetIndex = session.posts
            .filter { it.postId != draggedId }
            .count { item ->
                val top = itemTopByPostId[item.postId] ?: return@count false
                val height = itemHeightByPostId[item.postId] ?: return@count false
                pointerCenterY > top + height / 2f
            }
            .coerceIn(0, session.posts.lastIndex)
        if (dropIndex != targetIndex) {
            dropIndex = targetIndex
            performHaptic()
        }
    }

    fun finishDrag(commit: Boolean) {
        val draggedId = draggedPostId
        val targetIndex = dropIndex
        if (commit && draggedId != null && targetIndex != null) {
            val fromIndex = session.posts.indexOfFirst { it.postId == draggedId }
            if (fromIndex >= 0) {
                val updated = session.posts.toMutableList()
                val item = updated.removeAt(fromIndex)
                updated.add(targetIndex.coerceIn(0, updated.size), item)
                session = session.copy(posts = updated)
            }
        }
        draggedPostId = null
        dragStartIndex = null
        dropIndex = null
        dragOffsetY = 0f
        draggedStartTop = 0f
        draggedItemHeight = 0
        performHaptic()
    }

    fun refreshCandidates() {
        session = session.copy(loadingCandidates = true, candidates = emptyList())
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.fetchCreatorSeriesPostCandidates(seriesId) }
            }.onSuccess {
                session = session.copy(candidates = it, loadingCandidates = false)
            }.onFailure {
                error = it.message ?: "刷新系列文章失败"
                session = session.copy(loadingCandidates = false)
            }
        }
    }

    LaunchedEffect(seriesId) {
        runCatching {
            withContext(Dispatchers.IO) { apiClient.fetchCreatorSeriesDetail(seriesId) }
        }.onSuccess { detail ->
            val orderedPosts = detail.posts.sortedBy { it.order }
            seriesName = detail.name
            session = session.copy(posts = orderedPosts, savedPosts = orderedPosts, loadingCandidates = false)
            refreshCandidates()
        }.onFailure {
            error = it.message ?: "加载系列详情失败"
            session = session.copy(loadingCandidates = false)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(1f)
                .onGloballyPositioned { listContainerTop = it.positionInRoot().y }
        ) {
            val draggedPost = session.posts.firstOrNull { it.postId == draggedPostId }
            ScrollableLazyColumn(
                state = listState,
                showScrollbar = false,
                userScrollEnabled = draggedPostId == null,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(seriesName.ifBlank { "文章排序" }, modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { showSearchSheet = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "搜索文章")
                                }
                            }
                            Text("长按拖动右侧排序按钮调整顺序，保存后才会提交。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
                if (session.posts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Text(
                                "当前系列还没有文章。",
                                modifier = Modifier.padding(18.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val targetDropIndex = dropIndex
                    if (targetDropIndex == 0 && draggedPost != null) {
                        item(key = "series-placeholder-top") {
                            SeriesDropPlaceholder(height = draggedItemHeight)
                        }
                    }
                    items(session.posts, key = { "series-manage-${it.postId}" }) { post ->
                        val currentIndex = session.posts.indexOfFirst { item -> item.postId == post.postId }
                        SeriesPostSortCard(
                            post = post,
                            index = currentIndex,
                            saving = session.saving,
                            placeholder = post.postId == draggedPostId,
                            onRemove = { pendingRemovePost = post },
                            onHandleLongDrag = {
                                val startIndex = session.posts.indexOfFirst { item -> item.postId == post.postId }
                                if (startIndex < 0 || session.saving) return@SeriesPostSortCard
                                draggedPostId = post.postId
                                dragStartIndex = startIndex
                                dropIndex = startIndex
                                dragOffsetY = 0f
                                draggedStartTop = itemTopByPostId[post.postId] ?: 0f
                                draggedItemHeight = itemHeightByPostId[post.postId] ?: 0
                                performHaptic(HapticFeedbackType.LongPress)
                            },
                            onHandleDrag = { deltaY ->
                                if (draggedPostId != post.postId) return@SeriesPostSortCard
                                dragOffsetY += deltaY
                                updateDropIndex(draggedStartTop + dragOffsetY + draggedItemHeight / 2f)
                            },
                            onHandleDragEnd = {
                                if (draggedPostId == post.postId) finishDrag(commit = true)
                            },
                            onHandleDragCancel = {
                                if (draggedPostId == post.postId) finishDrag(commit = false)
                            },
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                itemTopByPostId[post.postId] = coordinates.positionInRoot().y
                                itemHeightByPostId[post.postId] = coordinates.size.height
                            }
                        )
                        val originalIndex = session.posts.indexOfFirst { item -> item.postId == post.postId }
                        if (post.postId != draggedPostId && targetDropIndex == originalIndex + 1 && draggedPost != null) {
                            SeriesDropPlaceholder(height = draggedItemHeight)
                        }
                    }
                }
            }
            if (draggedPost != null) {
                SeriesPostSortCard(
                    post = draggedPost,
                    index = dragStartIndex ?: session.posts.indexOfFirst { it.postId == draggedPost.postId },
                    saving = session.saving,
                    onRemove = {},
                    onHandleLongDrag = {},
                    onHandleDrag = {},
                    onHandleDragEnd = {},
                    onHandleDragCancel = {},
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .offset { IntOffset(0, (draggedStartTop - listContainerTop + dragOffsetY).roundToInt()) }
                        .zIndex(10f)
                )
            }
        }

        if (showSearchSheet) {
            val linkedPostIds = session.posts.map { it.postId }.toSet()
            val filteredCandidates = session.candidates
                .filter { !linkedPostIds.contains(it.postId) }
                .filter {
                    val query = session.query.trim()
                    query.isBlank() || it.title.contains(query, ignoreCase = true) || it.tags.contains(query, ignoreCase = true)
                }
                .sortedByDescending { it.createdAt }
            ModalBottomSheet(onDismissRequest = { showSearchSheet = false }) {
                ScrollableLazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 320.dp, max = 620.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Text("搜索自己的文章并添加", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                    item {
                        OutlinedTextField(
                            value = session.query,
                            onValueChange = { session = session.copy(query = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("标题或标签") },
                            singleLine = true
                        )
                    }
                    if (session.query.trim().isBlank()) {
                        item { SubtleText("输入标题或标签后开始搜索。") }
                    } else if (filteredCandidates.isEmpty()) {
                        item { SubtleText("没有匹配的可添加文章。") }
                    } else {
                        items(filteredCandidates, key = { "series-candidate-sheet-${it.postId}" }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.title, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                                    TextButton(
                                        enabled = !session.saving,
                                        onClick = {
                                            scope.launch {
                                                runCatching {
                                                    withContext(Dispatchers.IO) { apiClient.addPostToCreatorSeries(seriesId, item.postId) }
                                                }.onSuccess {
                                                    val nextPosts = session.posts + CreatorSeries.SeriesPostItem(0, session.posts.size + 1, item.postId, item.title, item.status, item.banStatus)
                                                    session = session.copy(posts = nextPosts, query = "")
                                                    performHaptic()
                                                    showSearchSheet = false
                                                    refreshCandidates()
                                                }.onFailure {
                                                    error = it.message ?: "添加系列文章失败"
                                                }
                                            }
                                        }
                                    ) { Text("添加") }
                                }
                            }
                        }
                    }
                }
            }
        }
        Surface(shadowElevation = 6.dp, tonalElevation = 3.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButtonCompat(
                    text = "关闭",
                    modifier = Modifier.weight(1f),
                    enabled = !session.saving,
                    onClick = onClose
                )
                Button(
                    onClick = {
                        if (session.posts == session.savedPosts) {
                            onClose()
                            return@Button
                        }
                        session = session.copy(saving = true)
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) { apiClient.reorderCreatorSeriesPosts(seriesId, session.posts) }
                            }.onSuccess {
                                session = session.copy(savedPosts = session.posts, saving = false)
                                performHaptic()
                                onClose()
                            }.onFailure {
                                error = it.message ?: "保存系列排序失败"
                                session = session.copy(saving = false)
                            }
                        }
                    },
                    enabled = !session.saving,
                    modifier = Modifier.weight(1f)
                ) {
                    if (session.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("保存")
                }
            }
        }
    }

    pendingRemovePost?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingRemovePost = null },
            title = { Text("确认移除文章") },
            text = { Text("确定将《${target.title.ifBlank { "未命名文章" }}》移出该系列吗？") },
            confirmButton = {
                TextButton(onClick = {
                    pendingRemovePost = null
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) { apiClient.removePostFromCreatorSeries(seriesId, target.postId) }
                        }.onSuccess {
                            val nextPosts = session.posts.filterNot { it.postId == target.postId }
                            session = session.copy(posts = nextPosts)
                            performHaptic()
                            refreshCandidates()
                        }.onFailure {
                            error = it.message ?: "移除系列文章失败"
                        }
                    }
                }) { Text("移除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemovePost = null }) { Text("取消") }
            }
        )
    }
}

private fun creatorPostStatusText(post: Post): String {
    return when {
        post.pendingReview -> "审核中"
        post.banStatus == 2 -> "不通过"
        post.banStatus == 1 -> "限制查看"
        post.status == 0 && post.visibility == "private" -> "部分可见"
        post.status == 0 && post.visibility == "group" -> "群组可见"
        post.status == 0 -> "已发布"
        post.status == 1 -> "草稿"
        post.status == 3 -> "已删除"
        else -> "状态 ${post.status}"
    }
}

private fun creatorPostRequestedStatus(filter: String?): Int? {
    return when (filter) {
        "published", "restricted", "rejected", "reviewing" -> 0
        "draft" -> 1
        else -> null
    }
}

private fun creatorPostRequestedBanStatus(filter: String?): Int? {
    return when (filter) {
        "published" -> 0
        "restricted" -> 1
        "rejected" -> 2
        "reviewing" -> 3
        else -> null
    }
}

private fun creatorPostMatchesFilter(post: Post, filter: String?): Boolean {
    return when (filter) {
        "published" -> post.status == 0 && post.banStatus == 0 && !post.pendingReview
        "draft" -> post.status == 1
        "restricted" -> post.status == 0 && post.banStatus == 1
        "rejected" -> post.status == 0 && post.banStatus == 2
        "reviewing" -> post.status == 0 && post.pendingReview
        else -> post.status != 3
    }
}

private fun creatorReviewStatusText(status: String): String {
    return when (status) {
        "pending" -> "审核中"
        "processing" -> "处理中"
        "approved" -> "已通过"
        "rejected" -> "已驳回"
        "failed" -> "失败"
        else -> status.ifBlank { "未知" }
    }
}

private fun creatorReviewTypeText(value: String): String {
    return when (value) {
        "initial" -> "初次发布"
        "update" -> "改帖更新"
        "update_summary" -> "摘要/标签更新"
        "report" -> "举报"
        "appeal" -> "申诉"
        "user_bio_initial" -> "介绍初审"
        "user_bio_update" -> "介绍更新"
        else -> value.ifBlank { "未知类型" }
    }
}

private fun creatorReportStatusText(status: String, isMyReport: Boolean, targetBanStatus: Int): String {
    if (isMyReport) {
        return when (status) {
            "approved" -> "被驳回"
            "failed", "rejected" -> when (targetBanStatus) {
                2 -> "判定为禁止访问"
                1 -> "判定为限制访问"
                else -> "判定违规"
            }
            "pending", "processing" -> "等待处理"
            else -> status.ifBlank { "未知" }
        }
    }
    return when (status) {
        "pending", "processing" -> "等待处理"
        "approved" -> "审核通过"
        "failed", "rejected" -> "判定违规"
        else -> status.ifBlank { "未知" }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostListScreen(
    listKey: String,
    apiClient: BbsApiClient,
    posts: List<Post>,
    emptyText: String,
    searchHint: String,
    categoryId: Int? = null,
    tag: String? = null,
    authorId: Int? = null,
    columnId: Int? = null,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    loadMoreKey: Int?,
    onLoadMore: () -> Unit,
    onPostClick: (Post) -> Unit,
    onDirectPostOpen: (Int) -> Unit,
    onCategoryClick: (Post) -> Unit,
    onTagClick: (String) -> Unit,
    onAuthorClick: (Post) -> Unit
) {
    key(listKey) {
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var userScrolled by remember { mutableStateOf(false) }
        var searchDraftQuery by remember { mutableStateOf("") }
        var submittedSearchQuery by remember { mutableStateOf("") }
        var searchResults by remember { mutableStateOf<List<Post>>(emptyList()) }
        var searchLoading by remember { mutableStateOf(false) }
        var searchError by remember { mutableStateOf<String?>(null) }
        var searchRequestId by remember { mutableIntStateOf(0) }
        var showSearchSheet by remember { mutableStateOf(false) }
        val currentOnLoadMore by rememberUpdatedState(onLoadMore)
        val displayedPosts = if (submittedSearchQuery.isBlank()) posts else searchResults
        val shouldLoadMore by remember(posts.size, canLoadMore, isLoadingMore, userScrolled, submittedSearchQuery) {
            derivedStateOf {
                val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                userScrolled && submittedSearchQuery.isBlank() && canLoadMore && !isLoadingMore && posts.isNotEmpty() && lastVisibleIndex >= posts.size - 3
            }
        }

        fun submitSearch() {
            val query = searchDraftQuery.trim()
            internalPostId(query)?.let {
                onDirectPostOpen(it)
                return
            }
            query.toIntOrNull()?.let {
                onDirectPostOpen(it)
                return
            }
            submittedSearchQuery = query
            if (query.isBlank()) {
                searchResults = emptyList()
                searchError = null
                searchLoading = false
                return
            }
            searchLoading = true
            searchError = null
            val requestId = ++searchRequestId
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        apiClient.searchPosts(query, 30, categoryId, tag, authorId, columnId)
                    }
                }.onSuccess {
                    if (requestId == searchRequestId && submittedSearchQuery == query) {
                        searchResults = it.filter { post -> post.shouldAppearInAppList() }
                    }
                }.onFailure {
                    if (requestId == searchRequestId && submittedSearchQuery == query) {
                        searchResults = emptyList()
                        searchError = it.message ?: "搜索失败"
                    }
                }.also {
                    if (requestId == searchRequestId && submittedSearchQuery == query) {
                        searchLoading = false
                    }
                }
            }
        }

        LaunchedEffect(listState.isScrollInProgress) {
            if (listState.isScrollInProgress) {
                userScrolled = true
            }
        }
        LaunchedEffect(shouldLoadMore, loadMoreKey) {
            if (shouldLoadMore) {
                currentOnLoadMore()
            }
        }

        ScrollableLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                searchHint,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { showSearchSheet = true }) {
                                Icon(Icons.Default.Search, contentDescription = "搜索")
                            }
                        }
                        searchError?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            if (searchLoading && submittedSearchQuery.isNotBlank()) {
                item { LoadingState() }
            } else if (displayedPosts.isEmpty()) {
                item { EmptyState(if (submittedSearchQuery.isBlank()) emptyText else "没有找到匹配文章") }
            } else {
                items(displayedPosts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        onClick = { onPostClick(post) },
                        onCategoryClick = { onCategoryClick(post) },
                        onTagClick = onTagClick,
                        onAuthorClick = { onAuthorClick(post) }
                    )
                }
                item {
                    if (submittedSearchQuery.isBlank() && isLoadingMore) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else if (canLoadMore) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("继续向下滑动加载更多", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("没有更多了", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (showSearchSheet) {
            ModalBottomSheet(onDismissRequest = { showSearchSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 260.dp, max = 500.dp)
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("搜索", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = searchDraftQuery,
                        onValueChange = {
                            searchDraftQuery = it
                            if (it.isBlank()) {
                                submittedSearchQuery = ""
                                searchResults = emptyList()
                                searchError = null
                                searchLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(searchHint) },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                searchDraftQuery = ""
                                submittedSearchQuery = ""
                                searchResults = emptyList()
                                searchError = null
                                searchLoading = false
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("清除") }
                        Button(
                            onClick = {
                                submitSearch()
                                if (searchDraftQuery.trim().isNotBlank()) {
                                    showSearchSheet = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !searchLoading
                        ) { Text(if (searchLoading) "搜索中..." else "搜索") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollableLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    showScrollbar: Boolean = true,
    userScrollEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: LazyListScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val observedItemSizes = remember(state) { mutableStateMapOf<Int, Int>() }
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
        val layoutInfo = state.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo.size
        layoutInfo.visibleItemsInfo.forEach { item ->
            if (item.size > 0) {
                observedItemSizes[item.index] = item.size
            }
        }
        if (showScrollbar && totalItems > visibleItems && visibleItems > 0) {
            val trackHeight = maxHeight
            val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()
            val averageVisibleSize = layoutInfo.visibleItemsInfo.map { it.size }.average().takeIf { it > 0 }?.toFloat() ?: 1f
            val knownTotalHeight = observedItemSizes.values.sum().toFloat()
            val knownAverage = if (observedItemSizes.isNotEmpty()) {
                knownTotalHeight / observedItemSizes.size.toFloat()
            } else {
                averageVisibleSize
            }.coerceAtLeast(1f)
            fun estimatedSizeFor(index: Int): Float = observedItemSizes[index]?.toFloat() ?: knownAverage
            val estimatedContentHeight = (
                knownTotalHeight + ((totalItems - observedItemSizes.size).coerceAtLeast(0) * knownAverage)
            ).coerceAtLeast(knownAverage)
            val viewportHeight = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(1f)
            val firstIndex = firstVisible?.index ?: 0
            var knownBeforeHeight = 0f
            var knownBeforeCount = 0
            observedItemSizes.forEach { (index, size) ->
                if (index < firstIndex) {
                    knownBeforeHeight += size
                    knownBeforeCount++
                }
            }
            val unknownBeforeCount = (firstIndex - knownBeforeCount).coerceAtLeast(0)
            val beforeFirstHeight = knownBeforeHeight + (unknownBeforeCount * knownAverage)
            val withinFirstOffset = ((firstVisible?.offset ?: 0) * -1f).coerceAtLeast(0f)
            val estimatedScrollOffset = (beforeFirstHeight + withinFirstOffset).coerceAtLeast(0f)
            val maxScrollable = (estimatedContentHeight - viewportHeight).coerceAtLeast(1f)
            val progress = (estimatedScrollOffset / maxScrollable).coerceIn(0f, 1f)
            val thumbHeight = 72.dp
            val maxThumbTravelPx = with(density) { (trackHeight - thumbHeight).toPx() }.coerceAtLeast(1f)
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = spring(stiffness = 700f, dampingRatio = 0.95f),
                label = "scrollbarProgress"
            )
            val thumbOffset = with(density) { (animatedProgress * maxThumbTravelPx).toDp() }
            val trackWidth = 22.dp
            val thumbWidth = 4.dp
            val thumbHeightPx = with(density) { thumbHeight.toPx() }
            var dragStartProgress by remember(totalItems, visibleItems) { mutableStateOf(0f) }
            var dragDeltaPx by remember(totalItems, visibleItems) { mutableStateOf(0f) }

            fun scrollToProgress(targetProgress: Float) {
                val clamped = targetProgress.coerceIn(0f, 1f)
                val targetOffsetPx = maxScrollable * clamped
                var remainingOffset = targetOffsetPx
                var targetIndex = 0
                while (targetIndex < totalItems - 1) {
                    val size = estimatedSizeFor(targetIndex)
                    if (remainingOffset < size) break
                    remainingOffset -= size
                    targetIndex++
                }
                val offsetPx = remainingOffset.toInt().coerceAtLeast(0)
                state.requestScrollToItem(targetIndex, offsetPx)
            }

            fun scrollToTrackPosition(pointerY: Float, centerThumb: Boolean) {
                val adjusted = if (centerThumb) pointerY - (thumbHeightPx / 2f) else pointerY
                val progressFromTrack = if (maxThumbTravelPx <= 0f) 0f else (adjusted / maxThumbTravelPx)
                scrollToProgress(progressFromTrack)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 6.dp, top = 12.dp, bottom = 12.dp)
                    .width(trackWidth)
                    .fillMaxHeight()
                    .pointerInput(totalItems, visibleItems, knownAverage, progress) {
                        detectTapGestures(
                            onTap = { offset ->
                                scrollToTrackPosition(offset.y, centerThumb = true)
                            }
                        )
                    }
                    .pointerInput(totalItems, visibleItems, knownAverage, progress) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                dragStartProgress = progress
                                dragDeltaPx = 0f
                                scrollToTrackPosition(offset.y, centerThumb = true)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragDeltaPx += dragAmount.y
                                val targetProgress = dragStartProgress + (dragDeltaPx / maxThumbTravelPx)
                                scrollToProgress(targetProgress)
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(2.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = thumbOffset)
                        .width(thumbWidth)
                        .height(thumbHeight)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.78f))
                )
            }
        }
    }
}

@Composable
private fun ColumnsScreen(columns: List<ColumnItem>, onColumnClick: (ColumnItem) -> Unit) {
    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (columns.isEmpty()) {
            item { EmptyState("暂无可查看的专栏") }
        } else {
            items(columns, key = { it.id }) { column ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onColumnClick(column) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(column.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (column.description.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                column.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileScreen(
    profile: UserProfile?,
    onViewPosts: (UserProfile) -> Unit,
    onPostClick: (Int) -> Unit
) {
    if (profile == null) {
        EmptyState("用户资料为空")
        return
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarDot(profile.displayName())
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = profile.displayName(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "@${profile.username.ifBlank { profile.id.toString() }} · 加入于 ${shortDate(profile.createdAt)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (profile.bio.isNotBlank()) {
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "这个用户还没有填写个人介绍。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        ProfileStat("文章", profile.postCount)
                        ProfileStat("评论", profile.commentCount)
                        ProfileStat("点赞", profile.likeCount)
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { onViewPosts(profile) }) {
                        Text("查看 TA 的文章")
                    }
                }
            }
        }

        item { SectionTitle("近期点赞") }
        if (profile.recentLikedPosts.isEmpty()) {
            item { SubtleText("最近 7 天没有公开点赞记录") }
        } else {
            items(profile.recentLikedPosts, key = { "like-${it.postId}-${it.createdAt}" }) { item ->
                ActivityCard(
                    title = item.title,
                    subtitle = listOf(item.category, shortDate(item.createdAt)).filter { it.isNotBlank() }.joinToString(" · "),
                    onClick = { if (item.postId > 0) onPostClick(item.postId) }
                )
            }
        }

        item { SectionTitle("近期评论") }
        if (profile.recentComments.isEmpty()) {
            item { SubtleText("最近 7 天没有公开评论记录") }
        } else {
            items(profile.recentComments, key = { "comment-${it.commentId}" }) { item ->
                ActivityCard(
                    title = item.postTitle.ifBlank { "关联文章" },
                    subtitle = summaryText(item.content, expanded = false, limit = 90),
                    onClick = { if (item.postId > 0) onPostClick(item.postId) }
                )
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SubtleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
private fun ActivityCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title.ifBlank { "未命名文章" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostCard(
    post: Post,
    onClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onTagClick: (String) -> Unit,
    onAuthorClick: () -> Unit
) {
    var summaryExpanded by remember(post.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryPill(post.categoryName, onClick = onCategoryClick)
                if (post.hasVisibilityBadge()) {
                    Spacer(Modifier.width(8.dp))
                    VisibilityPill(post.visibilityBadgeText())
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = shortDate(post.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = summaryText(post.safeSummary().ifBlank { "摘要暂无" }, summaryExpanded),
                modifier = Modifier.clickable { summaryExpanded = !summaryExpanded },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Clip
            )
            val tagItems = post.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(4)
            if (tagItems.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tagItems.forEach { tag ->
                        TagPill(tag, onClick = { onTagClick(tag) })
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarDot(post.authorName, modifier = Modifier.clickable(onClick = onAuthorClick))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = post.authorName.ifBlank { "未知作者" },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onAuthorClick)
                )
                Stat(Icons.Default.FavoriteBorder, post.likes.toString())
                Spacer(Modifier.width(12.dp))
                Stat(Icons.Default.Visibility, post.views.toString())
                Spacer(Modifier.width(12.dp))
                Stat(Icons.Default.ChatBubbleOutline, post.comments.toString())
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailScreen(
    post: Post?,
    fallbackReason: String?,
    blockedReason: DetailBlockedReason?,
    showRestrictedDialog: Boolean,
    onDismissRestrictedDialog: () -> Unit,
    apiClient: BbsApiClient,
    authState: AuthState,
    selectionEpoch: Int,
    onCategoryClick: (Post) -> Unit,
    onTagClick: (String) -> Unit,
    onAuthorClick: (Post) -> Unit,
    onCommentAuthorClick: (CommentItem) -> Unit,
    onOpenReader: (Int, String?) -> Unit,
    onSharePost: (Post) -> Unit,
    onCopyPostLink: (Post) -> Unit,
    onShowBanner: (String) -> Unit,
    onOpenInternalPost: (Int) -> Unit
) {
    if (blockedReason != null) {
        BlockedDetailScreen(blockedReason)
        return
    }
    if (post == null) {
        EmptyState("文章内容为空")
        return
    }
    if (showRestrictedDialog && post.accessNoticeText().isNotBlank()) {
        AlertDialog(
            onDismissRequest = onDismissRestrictedDialog,
            confirmButton = { TextButton(onClick = onDismissRestrictedDialog) { Text("继续阅读") } },
            title = { Text("限制查看文章") },
            text = { Text(post.accessNoticeText()) }
        )
    }
    var summaryExpanded by remember(post.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val signedIn = authState is AuthState.SignedIn
    var likeState by remember(post.id) { mutableStateOf(LikeState(false, post.likes)) }
    var likeError by remember(post.id) { mutableStateOf<String?>(null) }
    var likeLoading by remember(post.id) { mutableStateOf(false) }
    var comments by remember(post.id) { mutableStateOf<List<CommentItem>>(emptyList()) }
    var commentsLoading by remember(post.id) { mutableStateOf(false) }
    var commentError by remember(post.id) { mutableStateOf<String?>(null) }
    var commentText by remember(post.id) { mutableStateOf("") }
    var replyTarget by remember(post.id) { mutableStateOf<CommentItem?>(null) }
    var commentSubmitting by remember(post.id) { mutableStateOf(false) }
    var likingCommentIds by remember(post.id) { mutableStateOf<Set<Int>>(emptySet()) }
    var reportTarget by remember(post.id) { mutableStateOf<ReportTarget?>(null) }
    var reportReason by remember(post.id) { mutableStateOf("") }
    var reportError by remember(post.id) { mutableStateOf<String?>(null) }
    var reportSubmitting by remember(post.id) { mutableStateOf(false) }
    var reportedTargets by remember(post.id) { mutableStateOf<Set<String>>(emptySet()) }
    var historyItem by remember(post.id) { mutableStateOf<PostHistoryItem?>(null) }
    var historyLoading by remember(post.id) { mutableStateOf(signedIn) }
    var progressSaving by remember(post.id) { mutableStateOf(false) }
    var latestSocialRequestId by remember { mutableIntStateOf(0) }
    val currentPostId by rememberUpdatedState(post.id)
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val articleSections = remember(post.id, post.content, post.format) { extractArticleSections(post) }
    val leadingItemCount = (if (post.hasAccessNotice()) 1 else 0) +
        (if (fallbackReason != null) 1 else 0) +
        (if (signedIn && !historyLoading && historyItem?.hasProgressAnchor() == true) 1 else 0) +
        1

    fun loadSocial() {
        val requestId = ++latestSocialRequestId
        val requestPostId = post.id
        commentsLoading = true
        commentError = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val fetchedLike = apiClient.fetchPostLike(post.id)
                    val fetchedComments = apiClient.fetchComments(post.id)
                    fetchedLike to fetchedComments
                }
            }.onSuccess { (fetchedLike, fetchedComments) ->
                if (requestId != latestSocialRequestId || currentPostId != requestPostId) return@onSuccess
                likeState = fetchedLike
                comments = fetchedComments
            }.onFailure {
                if (requestId != latestSocialRequestId || currentPostId != requestPostId) return@onFailure
                commentError = it.message ?: "互动内容加载失败"
            }.also {
                if (requestId == latestSocialRequestId && currentPostId == requestPostId) {
                    commentsLoading = false
                }
            }
        }
    }

    fun toggleLike() {
        if (!signedIn) {
            likeError = "请先在“我的”页面选择登录账号"
            return
        }
        if (likeLoading) return
        likeLoading = true
        likeError = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.togglePostLike(post.id) }
            }.onSuccess {
                likeState = it
            }.onFailure {
                likeError = it.message ?: "点赞失败"
            }.also {
                likeLoading = false
            }
        }
    }

    fun submitComment() {
        val content = commentText.trim()
        if (!signedIn) {
            commentError = "请先在“我的”页面选择登录账号"
            return
        }
        if (content.isBlank()) {
            commentError = "请填写评论内容"
            return
        }
        commentSubmitting = true
        commentError = null
        val requestPostId = post.id
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.createComment(requestPostId, content, replyTarget?.id) }
            }.onSuccess {
                if (currentPostId != requestPostId) return@onSuccess
                commentText = ""
                replyTarget = null
                loadSocial()
            }.onFailure {
                if (currentPostId != requestPostId) return@onFailure
                commentError = it.message ?: "评论发布失败"
            }.also {
                if (currentPostId == requestPostId) {
                    commentSubmitting = false
                }
            }
        }
    }

    fun reportKey(target: ReportTarget): String = "${target.type}:${target.id}"

    fun openReport(target: ReportTarget) {
        if (!signedIn) {
            commentError = "请先在“我的”页面选择登录账号后再举报"
            return
        }
        reportTarget = target
        reportReason = ""
        reportError = null
    }

    fun dismissReport() {
        if (reportSubmitting) return
        reportTarget = null
        reportReason = ""
        reportError = null
    }

    fun submitReport() {
        val target = reportTarget ?: return
        if (!signedIn) {
            reportError = "请先登录后再举报"
            return
        }
        reportSubmitting = true
        reportError = null
        val requestPostId = post.id
        val targetKey = reportKey(target)
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    apiClient.report(
                        target.type,
                        target.id,
                        reportReason,
                        target.contentSnapshot,
                        target.titleSnapshot
                    )
                }
            }.onSuccess {
                if (currentPostId != requestPostId || reportTarget != target) return@onSuccess
                reportedTargets = reportedTargets + targetKey
                reportTarget = null
                reportReason = ""
                commentError = "${target.label}举报已提交，等待审核处理"
            }.onFailure {
                if (currentPostId != requestPostId || reportTarget != target) return@onFailure
                reportError = it.message ?: "举报提交失败"
            }.also {
                if (currentPostId == requestPostId && reportTarget == target) {
                    reportSubmitting = false
                }
            }
        }
    }

    fun toggleCommentLike(comment: CommentItem) {
        if (!signedIn) {
            commentError = "请先在“我的”页面选择登录账号"
            return
        }
        if (comment.id <= 0 || comment.id in likingCommentIds) return
        val previous = comment
        val optimistic = LikeState(!comment.liked, if (comment.liked) (comment.likeCount - 1).coerceAtLeast(0) else comment.likeCount + 1)
        val requestPostId = post.id
        likingCommentIds = likingCommentIds + comment.id
        commentError = null
        comments = comments.map { if (it.id == comment.id) it.withLikeState(optimistic) else it }
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { apiClient.toggleCommentLike(comment.id) }
            }.onSuccess { state ->
                if (currentPostId != requestPostId) return@onSuccess
                comments = comments.map { if (it.id == comment.id) it.withLikeState(state) else it }
            }.onFailure {
                if (currentPostId != requestPostId) return@onFailure
                comments = comments.map { if (it.id == comment.id) previous else it }
                commentError = it.message ?: "评论点赞失败"
            }.also {
                if (currentPostId == requestPostId) {
                    likingCommentIds = likingCommentIds - comment.id
                }
            }
        }
    }

    LaunchedEffect(post.id, authState) {
        loadSocial()
    }

    LaunchedEffect(post.id) {
        runCatching {
            withContext(Dispatchers.IO) { apiClient.recordPostView(post.id) }
        }
    }

    LaunchedEffect(post.id, signedIn) {
        if (!signedIn) {
            historyItem = null
            historyLoading = false
            return@LaunchedEffect
        }
        historyLoading = true
        runCatching {
            withContext(Dispatchers.IO) { apiClient.fetchPostHistory(post.id) }
        }.onSuccess {
            historyItem = it
        }.onFailure {
            historyItem = null
        }.also {
            historyLoading = false
        }
    }

    fun saveProgress(anchor: String) {
        if (!signedIn || anchor.isBlank() || progressSaving) return
        progressSaving = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    apiClient.savePostProgress(post.id, anchor)
                    apiClient.fetchPostHistory(post.id)
                }
            }.onSuccess {
                historyItem = it
                onShowBanner("阅读进度已保存")
            }.onFailure {
                onShowBanner(it.message ?: "阅读进度保存失败")
            }.also {
                progressSaving = false
            }
        }
    }

    fun copySectionLink(section: ArticleSectionEntry) {
        val link = "https://www.igngbbs.net/post/${post.id}#${section.anchor}"
        val text = buildString {
            append(section.title.ifBlank { post.title.ifBlank { "未命名文章" } })
            append("\n")
            append(link)
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("IGNGbbs 章节链接", text))
        onShowBanner("章节链接已复制")
    }

    fun scrollToProgressAnchor(anchor: String) {
        val sectionIndex = articleSections.indexOfFirst { it.anchor == anchor }
        if (sectionIndex < 0) {
            onShowBanner("未找到上次记录的标题位置")
            return
        }
        scope.launch {
            listState.animateScrollToItem(leadingItemCount + sectionIndex)
            onShowBanner("已定位到上次阅读位置")
        }
    }

    reportTarget?.let { target ->
        ReportDialog(
            target = target,
            reason = reportReason,
            error = reportError,
            submitting = reportSubmitting,
            alreadyReported = reportKey(target) in reportedTargets,
            onReasonChange = { reportReason = it },
            onDismiss = { dismissReport() },
            onSubmit = { submitReport() }
        )
    }

    ScrollableLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (post.hasAccessNotice()) {
            item {
                NoticeCard(
                    title = when (post.accessState) {
                        "restricted" -> "限制查看"
                        "group" -> "群组可见"
                        "private" -> "私密文章"
                        else -> "访问提示"
                    },
                    message = post.accessNoticeText()
                )
            }
        }
        if (fallbackReason != null) {
            item {
                NoticeCard("已使用首页缓存展示", "详情接口暂时不可用：$fallbackReason")
            }
        }
        if (signedIn && !historyLoading && historyItem?.hasProgressAnchor() == true) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("存在阅读进度", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "上次位置：#${historyItem?.progressAnchor} · ${friendlyDateTime(historyItem?.visitedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        TextButton(onClick = {
                            historyItem?.progressAnchor?.takeIf { it.isNotBlank() }?.let { scrollToProgressAnchor(it) }
                        }) {
                            Text("回到上次进度")
                        }
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(20.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryPill(post.categoryName, onClick = { onCategoryClick(post) })
                        if (post.hasVisibilityBadge()) {
                            VisibilityPill(post.visibilityBadgeText())
                        }
                        post.tags.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .take(6)
                            .forEach { TagPill(it, onClick = { onTagClick(it) }) }
                    }
                    Spacer(Modifier.height(16.dp))
                    SelectableTextBlock(
                        text = post.title,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface,
                        textSizeSp = 30f,
                        typefaceStyle = Typeface.BOLD,
                        maxLines = 4,
                        ellipsize = TextUtils.TruncateAt.END
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarDot(post.authorName, modifier = Modifier.clickable { onAuthorClick(post) })
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${post.authorName.ifBlank { "未知作者" }} · ${shortDate(post.createdAt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAuthorClick(post) }
                        )
                    }
                    if (post.summary.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        SelectableTextBlock(
                            text = summaryText(post.summary, summaryExpanded),
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textSizeSp = 16f
                        )
                        TextButton(onClick = { summaryExpanded = !summaryExpanded }) {
                            Text(if (summaryExpanded) "收起摘要" else "展开摘要")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Stat(Icons.Default.Visibility, "${post.views} 阅读")
                        Stat(if (likeState.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "${likeState.likeCount} 喜欢")
                        Stat(Icons.Default.ChatBubbleOutline, "${post.comments} 评论")
                    }
                    likeError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(onClick = { toggleLike() }, enabled = !likeLoading) {
                            Icon(
                                if (likeState.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (likeState.liked) "取消喜欢" else "喜欢")
                        }
                        TextButton(onClick = { onSharePost(post) }) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("分享")
                        }
                        TextButton(onClick = { onCopyPostLink(post) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("复制链接")
                        }
                        TextButton(onClick = { onOpenReader(post.id, historyItem?.progressAnchor) }) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("阅读器")
                        }
                        TextButton(
                            onClick = {
                                val anchor = articleSections.firstOrNull()?.anchor.orEmpty()
                                if (anchor.isBlank()) {
                                    onShowBanner("当前文章没有可记录的章节标题")
                                } else {
                                    saveProgress(anchor)
                                }
                            },
                            enabled = signedIn && !progressSaving
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (progressSaving) "保存中" else "记录进度")
                        }
                        TextButton(
                            onClick = {
                                openReport(
                                    ReportTarget(
                                        type = "post",
                                        id = post.id,
                                        label = "文章",
                                        contentSnapshot = post.content.take(2000),
                                        titleSnapshot = post.title
                                    )
                                )
                            },
                            enabled = reportKey(ReportTarget("post", post.id, "文章", "", "")) !in reportedTargets
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (reportKey(ReportTarget("post", post.id, "文章", "", "")) in reportedTargets) "已举报" else "举报")
                        }
                    }
                }
            }
        }
        if (post.content.isBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Text(
                        text = "文章为空",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(articleSections.size, key = { index -> "section-${post.id}-${articleSections[index].anchor}" }) { index ->
                val section = articleSections[index]
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = section.title.ifBlank { "未命名章节" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "#${section.anchor}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(onClick = { copySectionLink(section) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("复制链接")
                                }
                                TextButton(
                                    onClick = { saveProgress(section.anchor) },
                                    enabled = signedIn && !progressSaving
                                ) {
                                    Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (progressSaving) "保存中" else "记录进度")
                                }
                            }
                        }
                        ArticleContentBlock(
                            content = section.content,
                            format = post.format,
                            modifier = Modifier.fillMaxWidth(),
                            selectionEpoch = selectionEpoch,
                            onOpenInternalPost = onOpenInternalPost
                        )
                    }
                }
            }
        }
        item {
            CommentsSection(
                comments = comments,
                loading = commentsLoading,
                error = commentError,
                signedIn = signedIn,
                selectionEpoch = selectionEpoch,
                commentText = commentText,
                replyTarget = replyTarget,
                submitting = commentSubmitting,
                onCommentTextChange = { commentText = it },
                onReply = {
                    replyTarget = it
                    commentError = null
                },
                onCancelReply = { replyTarget = null },
                onSubmit = { submitComment() },
                onRetry = { loadSocial() },
                likingCommentIds = likingCommentIds,
                reportedTargets = reportedTargets,
                onCommentLike = { toggleCommentLike(it) },
                onReport = { openReport(it) },
                onCommentAuthorClick = onCommentAuthorClick
            )
        }
    }
}

@Composable
private fun CategoryPill(text: String, onClick: () -> Unit = {}) {
    AssistChip(
        onClick = onClick,
        label = { Text(text.ifBlank { "未分类" }) },
        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp)) }
    )
}

@Composable
private fun TagPill(text: String, onClick: () -> Unit = {}) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp)) }
    )
}

@Composable
private fun VisibilityPill(text: String) {
    if (text.isBlank()) return
    AssistChip(
        onClick = {},
        label = { Text(text) },
        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp)) }
    )
}

@Composable
private fun BlockedDetailScreen(reason: DetailBlockedReason) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        NoticeCard(reason.title, reason.message)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderScreen(
    post: Post?,
    initialAnchor: String?,
    apiClient: BbsApiClient,
    onExit: () -> Unit,
    onShowBanner: (String) -> Unit
) {
    if (post == null) {
        EmptyState("文章内容为空")
        return
    }

    val scope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val bodyMaxLines = (((maxHeight.value - 180f) / 29f).toInt()).coerceIn(10, 24)
        val titleMaxLines = 3
        val firstPageBodyLines = (bodyMaxLines - titleMaxLines).coerceAtLeast(7)
        val charsPerLine = (((maxWidth.value - 44f) / 11.6f).toInt()).coerceIn(10, 36)
        val firstPageCapacity = (firstPageBodyLines * charsPerLine * 0.82f).toInt().coerceAtLeast(140)
        val pageCapacity = (bodyMaxLines * charsPerLine * 0.82f).toInt().coerceAtLeast(180)
        val sections = remember(post.id, post.content, post.format, maxWidth, maxHeight) {
            paginateReaderSections(
                sections = extractReaderSections(post),
                firstPageChars = firstPageCapacity,
                pageChars = pageCapacity
            )
        }
        var sectionIndex by remember(post.id) { mutableIntStateOf(0) }
        var pageIndex by remember(post.id) { mutableIntStateOf(0) }
        var tocOpen by remember(post.id) { mutableStateOf(false) }
        var saving by remember(post.id) { mutableStateOf(false) }

        LaunchedEffect(post.id, initialAnchor, sections) {
            val targetIndex = initialAnchor?.trim()?.takeIf { it.isNotEmpty() }?.let { anchor ->
                sections.indexOfFirst { it.anchor == anchor }
            } ?: -1
            if (targetIndex >= 0) {
                sectionIndex = targetIndex
                pageIndex = 0
            } else {
                sectionIndex = 0
                pageIndex = 0
            }
        }

        val activeSection = sections.getOrNull(sectionIndex)
        val activePage = activeSection?.pages?.getOrNull(pageIndex).orEmpty()
        val hasPrevPage = pageIndex > 0 || sectionIndex > 0
        val hasNextPage = activeSection != null && pageIndex < activeSection.pages.lastIndex
        val hasNextSection = sectionIndex < sections.lastIndex

        fun goPrev() {
            if (pageIndex > 0) {
                pageIndex -= 1
                return
            }
            if (sectionIndex > 0) {
                val prevSectionIndex = sectionIndex - 1
                val prevLastPage = (sections.getOrNull(prevSectionIndex)?.pages?.lastIndex ?: 0).coerceAtLeast(0)
                sectionIndex = prevSectionIndex
                pageIndex = prevLastPage
            }
        }

        fun goNext() {
            if (hasNextPage) {
                pageIndex += 1
                return
            }
            if (!hasNextSection || activeSection == null || saving) {
                return
            }
            saving = true
            val nextSectionIndex = sectionIndex + 1
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) { apiClient.savePostProgress(post.id, activeSection.anchor) }
                }.onSuccess {
                    sectionIndex = nextSectionIndex
                    pageIndex = 0
                    onShowBanner("已记录阅读进度，并进入下一章")
                }.onFailure {
                    onShowBanner(it.message ?: "阅读进度记录失败")
                }.also {
                    saving = false
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activeSection, pageIndex, hasPrevPage, hasNextPage, hasNextSection, saving) {
                    detectTapGestures { offset ->
                        when {
                            offset.x <= size.width * 0.35f -> goPrev()
                            offset.x >= size.width * 0.65f -> goNext()
                            else -> Unit
                        }
                    }
                }
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            post.title.ifBlank { "未命名文章" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "第 ${sectionIndex + 1} 章 · 第 ${pageIndex + 1} 页",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { tocOpen = true }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "目录")
                    }
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.Close, contentDescription = "关闭阅读器")
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 22.dp, vertical = 26.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (pageIndex == 0) {
                            Text(
                                text = activeSection?.title?.ifBlank { "未命名章节" } ?: "未命名章节",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = activePage.ifBlank { "本页暂无内容" },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (pageIndex == 0) firstPageBodyLines else bodyMaxLines,
                            overflow = TextOverflow.Clip
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = { goPrev() }, enabled = hasPrevPage) { Text("上一页") }
                    Text(
                        text = "${pageIndex + 1}/${activeSection?.pages?.size ?: 1}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = { goNext() }, enabled = !saving && (hasNextPage || hasNextSection)) {
                        Text(if (saving) "处理中" else "下一页")
                    }
                }
            }
        }

        if (tocOpen) {
            ModalBottomSheet(onDismissRequest = { tocOpen = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 260.dp, max = 540.dp)
                        .padding(start = 18.dp, end = 18.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("目录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "当前章节：${activeSection?.title?.ifBlank { "未命名章节" } ?: "未命名章节"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    sections.forEachIndexed { index, section ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sectionIndex = index
                                    pageIndex = 0
                                    tocOpen = false
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (index == sectionIndex) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "第 ${index + 1} 章",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(section.title.ifBlank { "未命名章节" }, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "#${section.anchor} · ${section.pages.size} 页",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBannerNotice(
    message: AppBannerMessage,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message.id) {
        delay(2200)
        onDismiss()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 14.dp)
            .zIndex(2f),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier.padding(horizontal = 20.dp),
            shape = RoundedCornerShape(999.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(message.text, color = MaterialTheme.colorScheme.inverseOnSurface)
                IconButton(onClick = onDismiss, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = MaterialTheme.colorScheme.inverseOnSurface)
                }
            }
        }
    }
}

@Composable
private fun AvatarDot(name: String, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().firstOrNull()?.toString()?.uppercase() ?: "I",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Stat(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NoticeCard(title: String, message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            SelectionContainer {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(Modifier.height(4.dp))
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }
    }
}

@Composable
private fun ResettableSelectionContainer(
    selectionEpoch: Int,
    content: @Composable () -> Unit
) {
    key(selectionEpoch) {
        SelectionContainer {
            content()
        }
    }
}

@Composable
private fun SelectableTextBlock(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textSizeSp: Float = 16f,
    typefaceStyle: Int = Typeface.NORMAL,
    maxLines: Int = Int.MAX_VALUE,
    ellipsize: TextUtils.TruncateAt? = null
) {
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            TextView(viewContext).apply {
                setTextIsSelectable(true)
                textSize = textSizeSp
                setLineSpacing(4f, 1.05f)
            }
        },
        update = { textView ->
            textView.text = text
            textView.setTextColor(color.toArgb())
            textView.setTypeface(null, typefaceStyle)
            textView.maxLines = maxLines
            textView.ellipsize = ellipsize
        }
    )
}

@Composable
private fun CommentsSection(
    comments: List<CommentItem>,
    loading: Boolean,
    error: String?,
    signedIn: Boolean,
    selectionEpoch: Int,
    commentText: String,
    replyTarget: CommentItem?,
    submitting: Boolean,
    onCommentTextChange: (String) -> Unit,
    onReply: (CommentItem) -> Unit,
    onCancelReply: () -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    likingCommentIds: Set<Int>,
    reportedTargets: Set<String>,
    onCommentLike: (CommentItem) -> Unit,
    onReport: (ReportTarget) -> Unit,
    onCommentAuthorClick: (CommentItem) -> Unit
) {
    val commentGroups = remember(comments) { comments.groupBy { it.parentId } }
    val rootComments = remember(commentGroups) { commentGroups[0].orEmpty() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("评论", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onRetry, enabled = !loading) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("刷新", maxLines = 1)
                }
            }
            if (signedIn) {
                if (replyTarget == null) {
                    CommentComposer(
                        value = commentText,
                        onValueChange = onCommentTextChange,
                        submitting = submitting,
                        submitLabel = "发布评论",
                        fieldLabel = "写下评论",
                        onSubmit = onSubmit
                    )
                }
            } else {
                Text(
                    "选择登录账号后可以发表评论。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            when {
                loading -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                rootComments.isEmpty() -> Text(
                    "暂无评论",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> rootComments.forEach { comment ->
                    CommentNode(
                        comment = comment,
                        childrenByParent = commentGroups,
                        signedIn = signedIn,
                        selectionEpoch = selectionEpoch,
                        depth = 0,
                        visited = emptySet(),
                        onReply = onReply,
                        likingCommentIds = likingCommentIds,
                        reportedTargets = reportedTargets,
                        onCommentLike = onCommentLike,
                        onReport = onReport,
                        onCommentAuthorClick = onCommentAuthorClick,
                        replyTarget = replyTarget,
                        replyText = commentText,
                        submittingReply = submitting,
                        onReplyTextChange = onCommentTextChange,
                        onCancelReply = onCancelReply,
                        onSubmitReply = onSubmit
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentNode(
    comment: CommentItem,
    childrenByParent: Map<Int, List<CommentItem>>,
    signedIn: Boolean,
    selectionEpoch: Int,
    depth: Int,
    visited: Set<Int>,
    onReply: (CommentItem) -> Unit,
    likingCommentIds: Set<Int>,
    reportedTargets: Set<String>,
    onCommentLike: (CommentItem) -> Unit,
    onReport: (ReportTarget) -> Unit,
    onCommentAuthorClick: (CommentItem) -> Unit,
    replyTarget: CommentItem?,
    replyText: String,
    submittingReply: Boolean,
    onReplyTextChange: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSubmitReply: () -> Unit
) {
    if (comment.id in visited) return
    val nextVisited = visited + comment.id
    Column(Modifier.fillMaxWidth()) {
        CommentCard(
            comment = comment,
            signedIn = signedIn,
            selectionEpoch = selectionEpoch,
            depth = depth,
            liking = comment.id in likingCommentIds,
            reported = "comment:${comment.id}" in reportedTargets,
            onLike = { onCommentLike(comment) },
            onReply = { onReply(comment) },
            onAuthorClick = { onCommentAuthorClick(comment) },
            onReport = {
                onReport(
                    ReportTarget(
                        type = "comment",
                        id = comment.id,
                        label = "评论",
                        contentSnapshot = comment.content.take(2000)
                    )
                )
            },
            isReplying = replyTarget?.id == comment.id,
            replyText = replyText,
            submittingReply = submittingReply,
            onReplyTextChange = onReplyTextChange,
            onCancelReply = onCancelReply,
            onSubmitReply = onSubmitReply
        )
        childrenByParent[comment.id].orEmpty().filterNot { it.id in nextVisited }.forEach { child ->
            Spacer(Modifier.height(8.dp))
            CommentNode(
                comment = child,
                childrenByParent = childrenByParent,
                signedIn = signedIn,
                selectionEpoch = selectionEpoch,
                depth = depth + 1,
                visited = nextVisited,
                onReply = onReply,
                likingCommentIds = likingCommentIds,
                reportedTargets = reportedTargets,
                onCommentLike = onCommentLike,
                onReport = onReport,
                onCommentAuthorClick = onCommentAuthorClick,
                replyTarget = replyTarget,
                replyText = replyText,
                submittingReply = submittingReply,
                onReplyTextChange = onReplyTextChange,
                onCancelReply = onCancelReply,
                onSubmitReply = onSubmitReply
            )
        }
    }
}

@Composable
private fun CommentCard(
    comment: CommentItem,
    signedIn: Boolean,
    selectionEpoch: Int,
    depth: Int,
    liking: Boolean,
    reported: Boolean,
    onLike: () -> Unit,
    onReply: () -> Unit,
    onAuthorClick: () -> Unit,
    onReport: () -> Unit,
    isReplying: Boolean,
    replyText: String,
    submittingReply: Boolean,
    onReplyTextChange: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSubmitReply: () -> Unit
) {
    val indent = (depth.coerceAtMost(6) * 14).dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .clip(RoundedCornerShape(18.dp))
            .background(if (depth == 0) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarDot(comment.authorName, modifier = Modifier.clickable(onClick = onAuthorClick))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Column {
                    Text(
                        comment.authorName.ifBlank { "未知用户" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable(onClick = onAuthorClick)
                    )
                    Text(
                        shortDate(comment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onLike, enabled = !liking) {
                Icon(
                    if (comment.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (comment.likeCount > 0) comment.likeCount.toString() else "赞同", maxLines = 1)
            }
        }
        Spacer(Modifier.height(10.dp))
        SelectableTextBlock(
            text = comment.content,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            textSizeSp = 16f
        )
        Spacer(Modifier.height(8.dp))
        SelectableTextBlock(
            text = if (comment.parentId > 0) "ID #${comment.id} · 上级 #${comment.parentId}" else "ID #${comment.id} · 顶层评论",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textSizeSp = 12f
        )
        if (signedIn) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onReply) {
                    Text("回复", maxLines = 1)
                }
                TextButton(onClick = onReport, enabled = !reported) {
                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (reported) "已举报" else "举报", maxLines = 1)
                }
            }
            if (isReplying) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "回复 ${comment.authorName.ifBlank { "评论" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                CommentComposer(
                    value = replyText,
                    onValueChange = onReplyTextChange,
                    submitting = submittingReply,
                    submitLabel = "发布回复",
                    fieldLabel = "写下回复",
                    onSubmit = onSubmitReply,
                    onCancel = onCancelReply
                )
            }
        }
    }
}

@Composable
private fun CommentComposer(
    value: String,
    onValueChange: (String) -> Unit,
    submitting: Boolean,
    submitLabel: String,
    fieldLabel: String,
    onSubmit: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(fieldLabel) },
        minLines = 2,
        maxLines = 5
    )
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        onCancel?.let {
            TextButton(onClick = it, enabled = !submitting) {
                Text("取消")
            }
        }
        Button(onClick = onSubmit, enabled = !submitting && value.isNotBlank()) {
            if (submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(submitLabel)
        }
    }
}

@Composable
private fun ReportDialog(
    target: ReportTarget,
    reason: String,
    error: String?,
    submitting: Boolean,
    alreadyReported: Boolean,
    onReasonChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Flag, contentDescription = null) },
        title = { Text("举报${target.label}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "请描述该${target.label}存在的问题。举报后将由 AI 进行审核处理，请勿恶意举报。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("举报理由（可选）") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !submitting && !alreadyReported
                )
                if (alreadyReported) {
                    Text(
                        "该内容已被其他人或您自己举报，需等待当前审核完成后才能再次举报",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) {
                Text("取消")
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit, enabled = !submitting && !alreadyReported) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text("确认举报")
            }
        }
    )
}

@Composable
private fun ArticleContentBlock(
    content: String,
    format: String,
    modifier: Modifier = Modifier,
    selectionEpoch: Int,
    onOpenInternalPost: (Int) -> Unit
) {
    val context = LocalContext.current
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .build()
    }
    val textColor = MaterialTheme.colorScheme.onSurface

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { viewContext ->
            TextView(viewContext).apply {
                setTextIsSelectable(true)
                textSize = 17f
                setLineSpacing(6f, 1.05f)
                setTextColor(textColor.toArgb())
                setOnTouchListener { view, event ->
                    if (event.actionMasked != MotionEvent.ACTION_UP) {
                        return@setOnTouchListener false
                    }
                    val widget = view as? TextView ?: return@setOnTouchListener false
                    val text = widget.text as? Spanned ?: return@setOnTouchListener false
                    if (widget.hasSelection()) return@setOnTouchListener false
                    val layout = widget.layout ?: return@setOnTouchListener false
                    val x = (event.x - widget.totalPaddingLeft + widget.scrollX).toInt()
                    val y = (event.y - widget.totalPaddingTop + widget.scrollY).toInt()
                    if (y < 0 || x < 0 || y > layout.height) return@setOnTouchListener false
                    val line = layout.getLineForVertical(y)
                    val offset = layout.getOffsetForHorizontal(line, x.toFloat())
                    val spans = text.getSpans(offset, offset, ClickableSpan::class.java)
                    val span = spans.firstOrNull() ?: return@setOnTouchListener false
                    span.onClick(widget)
                    true
                }
            }
        },
        update = { textView ->
            val previousEpoch = textView.getTag(ARTICLE_BODY_SELECTION_EPOCH_TAG) as? Int
            if (previousEpoch != selectionEpoch) {
                (textView.text as? Spannable)?.let { Selection.removeSelection(it) }
                textView.clearFocus()
                textView.getTag(ARTICLE_BODY_SELECTION_EPOCH_TAG)
                textView.setTag(ARTICLE_BODY_SELECTION_EPOCH_TAG, selectionEpoch)
            }
            textView.setTextColor(textColor.toArgb())
            if (format.equals("html", ignoreCase = true)) {
                textView.text = interceptInternalLinks(
                    Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY),
                    onOpenInternalPost
                )
            } else {
                markwon.setMarkdown(textView, content)
                textView.text = interceptInternalLinks(textView.text, onOpenInternalPost)
            }
            (textView.text as? Spannable)?.let { spannable ->
                if (previousEpoch != selectionEpoch) {
                    Selection.removeSelection(spannable)
                }
            }
            textView.setTag(ARTICLE_BODY_SELECTION_EPOCH_TAG, selectionEpoch)
        }
    )
}

@Composable
private fun ArticleBody(post: Post, modifier: Modifier = Modifier, selectionEpoch: Int, onOpenInternalPost: (Int) -> Unit) {
    ArticleContentBlock(
        content = post.content,
        format = post.format,
        modifier = modifier,
        selectionEpoch = selectionEpoch,
        onOpenInternalPost = onOpenInternalPost
    )
}

private fun interceptInternalLinks(text: CharSequence, onOpenInternalPost: (Int) -> Unit): CharSequence {
    val spanned = text as? Spanned ?: return text
    val builder = SpannableStringBuilder(spanned)
    val spans = builder.getSpans(0, builder.length, URLSpan::class.java)
    spans.forEach { span ->
        val start = builder.getSpanStart(span)
        val end = builder.getSpanEnd(span)
        val flags = builder.getSpanFlags(span)
        val postId = internalPostId(span.url)
        if (postId != null) {
            builder.removeSpan(span)
            builder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onOpenInternalPost(postId)
                }
            }, start, end, flags)
        }
    }
    return builder
}

private fun internalPostId(url: String?): Int? {
    if (url.isNullOrBlank()) return null
    val match = Regex("""(?:https?://(?:www\.)?igngbbs\.net)?/(?:post|api/v1/posts)/(\d+)""").find(url)
    return match?.groupValues?.getOrNull(1)?.toIntOrNull()
}

private fun blockedTitle(error: ApiException): String {
    return when (error.errorCode) {
        "POST_NOT_FOUND" -> "文章不存在"
        "POST_BANNED" -> "文章已禁止查看"
        "POST_REVIEWING" -> "文章正在审核"
        "POST_DRAFT" -> "草稿无法查看"
        "POST_PRIVATE_DENIED" -> "私密文章"
        "POST_GROUP_DENIED" -> "群组权限不足"
        else -> if (error.statusCode == 404) "文章不存在" else "无法查看文章"
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(title: String, message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            TextButton(onClick = onRetry) { Text("重试") }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun IgngBbsTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val colorScheme = if (android.os.Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (dark) darkColorScheme(
            primary = Color(0xFF9ECBFF),
            secondary = Color(0xFFB9C7DC),
            tertiary = Color(0xFFFFC48D)
        ) else lightColorScheme(
            primary = Color(0xFF005FAF),
            secondary = Color(0xFF526070),
            tertiary = Color(0xFF805600)
        )
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

private fun shortDate(value: String?): String {
    if (value == null || value.length < 10) return "未知时间"
    return value.substring(0, 10)
}

private fun screenTitle(screen: Screen): String {
    return when (screen) {
        Screen.Home -> "IGNGbbs"
        Screen.Columns -> "专栏"
        Screen.History -> "历史记录"
        Screen.Notifications -> "通知中心"
        Screen.ConsoleHome -> "控制台"
        Screen.ConsolePosts -> "文章管理"
        Screen.ConsolePostCreate -> "新建文章"
        is Screen.ConsolePostEditor -> "编辑文章"
        is Screen.ConsolePostSettings -> "文章设置"
        Screen.ConsoleComments -> "评论管理"
        Screen.ConsoleReview -> "处理中心"
        Screen.ConsoleGroups -> "群组管理"
        Screen.ConsoleGroupCreate -> "新建群组"
        is Screen.ConsoleGroupRename -> "重命名群组"
        is Screen.ConsoleGroupMembers -> "成员管理"
        Screen.ConsolePermissions -> "权限状态"
        Screen.ConsolePersonalization -> "个性化"
        Screen.ConsoleColumns -> "专栏管理"
        is Screen.ConsoleColumnEdit -> "专栏设置"
        is Screen.ConsoleColumnMembers -> "权限管理"
        Screen.ConsoleSeries -> "系列管理"
        is Screen.ConsoleSeriesEdit -> "系列设置"
        is Screen.ConsoleSeriesManage -> "文章排序"
        Screen.ConsoleProfile -> "创作者资料"
        Screen.Account -> "我的"
        is Screen.UserProfile -> screen.title.ifBlank { "用户主页" }
        is Screen.FilteredPosts -> screen.title
        is Screen.Detail -> "查看文章"
        is Screen.Reader -> "阅读器"
    }
}

private fun summaryText(value: String, expanded: Boolean, limit: Int = 120): String {
    val text = value.replace(Regex("\\s+"), " ").trim()
    if (expanded || text.length <= limit) return text
    return text.take(limit).trimEnd() + "…"
}

private fun friendlyDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "未知时间"
    return value.replace("T", " ").replace("Z", "").take(19)
}

private fun extractArticleSections(post: Post): List<ArticleSectionEntry> {
    return extractReaderSections(post).map { section ->
        ArticleSectionEntry(
            title = section.title,
            anchor = section.anchor,
            content = section.content
        )
    }
}

private fun extractReaderSections(post: Post): List<ReaderSection> {
    val source = if (post.format.equals("html", ignoreCase = true)) {
        Html.fromHtml(post.content, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        post.content
    }
    val normalized = source.replace("\r\n", "\n")
    val lines = normalized.lines()
    val sections = mutableListOf<ReaderSection>()
    var currentTitle = post.title.ifBlank { "正文" }
    var currentAnchor = "start"
    val buffer = StringBuilder()

    fun flush() {
        val content = normalizeReaderText(buffer.toString())
        if (content.isNotBlank()) {
            sections += ReaderSection(
                title = currentTitle,
                anchor = currentAnchor,
                content = content,
                pages = emptyList()
            )
        }
        buffer.clear()
    }

    lines.forEach { rawLine ->
        val line = rawLine.trimEnd()
        val heading = Regex("""^(#{1,6})\s+(.+)$""").find(line)
        if (heading != null) {
            flush()
            currentTitle = heading.groupValues[2].trim()
            currentAnchor = readerAnchorFromTitle(currentTitle)
        } else {
            buffer.appendLine(line)
        }
    }
    flush()
    if (sections.isEmpty()) {
        sections += ReaderSection(
            title = currentTitle,
            anchor = currentAnchor,
            content = "本章暂无内容",
            pages = emptyList()
        )
    }
    return sections.ifEmpty {
        listOf(
            ReaderSection(
                title = post.title.ifBlank { "正文" },
                anchor = "start",
                content = normalizeReaderText(normalized).ifBlank { "文章暂无内容" },
                pages = emptyList()
            )
        )
    }
}

private fun paginateReaderSections(
    sections: List<ReaderSection>,
    firstPageChars: Int,
    pageChars: Int
): List<ReaderSection> {
    return sections.map { section ->
        section.copy(pages = paginateReaderText(section.content, firstPageChars, pageChars))
    }
}

private fun paginateReaderText(content: String, firstPageChars: Int, pageChars: Int): List<String> {
    val text = content.trim().ifBlank { "本章暂无内容" }
    val normalized = text
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
    if (normalized.isBlank()) return listOf("本章暂无内容")

    val pageCap = pageChars.coerceAtLeast(120)
    val firstCap = firstPageChars.coerceIn(80, pageCap)
    val pages = mutableListOf<String>()
    var cursor = 0
    var currentCap = firstCap
    while (cursor < normalized.length) {
        val remaining = normalized.length - cursor
        val takeCount = minOf(currentCap, remaining)
        val slice = normalized.substring(cursor, cursor + takeCount)
        pages += slice.trimEnd()
        cursor += takeCount
        currentCap = pageCap
    }
    return pages.ifEmpty { listOf("本章暂无内容") }
}

private fun normalizeReaderText(value: String): String {
    return value
        .replace(Regex("```[\\s\\S]*?```"), "\n[代码片段]\n")
        .replace(Regex("`([^`]*)`"), "$1")
        .replace(Regex("""!\[[^\]]*]\([^)]+\)"""), "[图片]")
        .replace(Regex("""\[(.*?)\]\([^)]+\)"""), "$1")
        .replace(Regex("""(^|\n)\s*[-*+]\s+"""), "$1• ")
        .replace(Regex("""(^|\n)\s*\d+\.\s+"""), "$1")
        .replace(Regex("""[#>*_~]"""), "")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}

private fun readerAnchorFromTitle(title: String): String {
    val normalized = title.lowercase(Locale.CHINA)
        .replace(Regex("""[^\p{L}\p{N}\s-]"""), "")
        .trim()
        .replace(Regex("""\s+"""), "-")
    return normalized.ifBlank { "section" }
}
