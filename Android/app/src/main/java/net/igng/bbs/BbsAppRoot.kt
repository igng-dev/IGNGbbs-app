package net.igng.bbs

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.designsystem.BbsFloatingNavBar
import net.igng.bbs.core.designsystem.BbsNavTabItem
import net.igng.bbs.core.designsystem.IgngBbsTheme
import net.igng.bbs.core.designsystem.IgngMotion
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.model.AppPreferences
import net.igng.bbs.core.model.DailyLoginDto
import net.igng.bbs.feature.admin.AdminConsoleScreen
import net.igng.bbs.feature.column.ColumnDetailScreen
import net.igng.bbs.feature.column.ColumnSquareScreen
import net.igng.bbs.feature.creator.CreatorColumnsScreen
import net.igng.bbs.feature.creator.CreatorCommentsScreen
import net.igng.bbs.feature.creator.CreatorHubScreen
import net.igng.bbs.feature.creator.CreatorMediaScreen
import net.igng.bbs.feature.creator.CreatorPostsScreen
import net.igng.bbs.feature.creator.CreatorReviewsScreen
import net.igng.bbs.feature.creator.CreatorSeriesScreen
import net.igng.bbs.feature.creator.NativeWysiwygEditorScreen
import net.igng.bbs.feature.feed.CategoryFeedScreen
import net.igng.bbs.feature.feed.FeedHomeScreen
import net.igng.bbs.feature.feed.RulesScreen
import net.igng.bbs.feature.feed.SearchScreen
import net.igng.bbs.feature.feed.TagFeedScreen
import net.igng.bbs.feature.inbox.InboxScreen
import net.igng.bbs.feature.post.PostReaderScreen
import net.igng.bbs.feature.profile.AccountSecurityScreen
import net.igng.bbs.feature.profile.AppSettingsScreen
import net.igng.bbs.feature.profile.FavoritesScreen
import net.igng.bbs.feature.profile.FollowingScreen
import net.igng.bbs.feature.profile.PrivacyPreferencesScreen
import net.igng.bbs.feature.profile.ProfileCenterScreen
import net.igng.bbs.feature.profile.ReadingHistoryScreen
import net.igng.bbs.feature.profile.UserProfileScreen

@Composable
fun BbsAppRoot(
    container: BbsAppContainer,
    incomingDeepLinks: Flow<String>,
    incomingAuthMessages: Flow<String>,
) {
    val prefs by container.settingsRepository.preferences.collectAsState(initial = AppPreferences())
    val bootstrap by container.bootstrap.collectAsState()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    IgngBbsTheme(
        themeMode = prefs.themeMode,
        motionIntensity = prefs.motionIntensity,
        hapticsEnabled = prefs.hapticsEnabled,
    ) {
        val colors = IgngTheme.colors
        val intensity = IgngTheme.motionIntensity
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }

        var sessionExpiredReason by remember { mutableStateOf<String?>(null) }
        var expToast by remember { mutableStateOf<DailyLoginDto?>(null) }

        // Initial hydration + bootstrap + daily login settlement
        LaunchedEffect(activeAccount?.userId) {
            container.hydrateCachedBootstrap()
            container.refreshBootstrap()
            if (activeAccount != null) {
                container.settleDailyLoginIfNeeded()
            }
        }

        // Toggle background worker based on preferences
        LaunchedEffect(prefs.backgroundNotificationsEnabled) {
            if (prefs.backgroundNotificationsEnabled) {
                NotificationSyncWorker.schedule(context)
            } else {
                NotificationSyncWorker.cancel(context)
            }
        }

        // Global 401 session expired observer
        LaunchedEffect(Unit) {
            container.sessionExpiredNotifier.events.collect { reason ->
                sessionExpiredReason = reason
            }
        }

        // Daily EXP toast pill
        LaunchedEffect(Unit) {
            container.dailyLoginReward.collect { reward ->
                expToast = reward
                delay(3200L)
                expToast = null
            }
        }

        // OAuth status toast
        LaunchedEffect(Unit) {
            incomingAuthMessages.collect { msg ->
                snackbarHostState.showSnackbar(msg)
            }
        }

        // Incoming App Links & Shortcuts
        LaunchedEffect(Unit) {
            incomingDeepLinks.collect { route ->
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
        }

        fun openUrlOrDeepLink(url: String) {
            val uri = runCatching { Uri.parse(url) }.getOrNull()
            val internal = AppRoutes.resolveDeepLink(uri)
            if (internal != null) {
                navController.navigate(internal)
            } else if (uri != null && (uri.scheme == "https" || uri.scheme == "http")) {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }

        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStack?.destination?.route ?: AppRoutes.FEED
        val showBottomBar = currentRoute in AppRoutes.TOP_LEVEL_ROUTES

        val creatorTodos = 0
        val tabs = remember(creatorTodos) {
            listOf(
                BbsNavTabItem(AppRoutes.FEED, "首页", Icons.Rounded.Home),
                BbsNavTabItem(AppRoutes.COLUMNS, "专栏", Icons.Rounded.FolderShared),
                BbsNavTabItem(AppRoutes.CREATOR, "创作", Icons.Rounded.Create, badgeCount = creatorTodos),
                BbsNavTabItem(AppRoutes.PROFILE, "我的", Icons.Rounded.Person),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.canvas),
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoutes.FEED,
                enterTransition = { IgngMotion.pushEnter(intensity) },
                exitTransition = { IgngMotion.pushExit(intensity) },
                popEnterTransition = { IgngMotion.popEnter(intensity) },
                popExitTransition = { IgngMotion.popExit(intensity) },
            ) {
                composable(AppRoutes.FEED) {
                    FeedHomeScreen(
                        container = container,
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                        onOpenCategory = { slug, title -> navController.navigate(AppRoutes.category(slug, title)) },
                        onOpenTag = { navController.navigate(AppRoutes.tag(it)) },
                        onOpenUser = { navController.navigate(AppRoutes.user(it)) },
                        onOpenSearch = { navController.navigate(AppRoutes.SEARCH) },
                        onOpenInbox = { navController.navigate(AppRoutes.INBOX) },
                        onOpenRules = { navController.navigate(AppRoutes.RULES) },
                    )
                }

                composable(AppRoutes.COLUMNS) {
                    ColumnSquareScreen(
                        container = container,
                        onOpenColumn = { navController.navigate(AppRoutes.column(it)) },
                        onOpenCreatorColumns = { navController.navigate(AppRoutes.CREATOR_COLUMNS) },
                    )
                }

                composable(AppRoutes.CREATOR) {
                    CreatorHubScreen(
                        container = container,
                        onNewArticle = { navController.navigate(AppRoutes.editor()) },
                        onEditPost = { navController.navigate(AppRoutes.editor(postId = it)) },
                        onOpenPosts = { navController.navigate(AppRoutes.CREATOR_POSTS) },
                        onOpenColumns = { navController.navigate(AppRoutes.CREATOR_COLUMNS) },
                        onOpenSeries = { navController.navigate(AppRoutes.CREATOR_SERIES) },
                        onOpenComments = { navController.navigate(AppRoutes.CREATOR_COMMENTS) },
                        onOpenReviews = { navController.navigate(AppRoutes.CREATOR_REVIEWS) },
                        onOpenMedia = { navController.navigate(AppRoutes.CREATOR_MEDIA) },
                        onRequireLogin = { container.oauthManager.launchLogin(context) },
                    )
                }

                composable(AppRoutes.PROFILE) {
                    ProfileCenterScreen(
                        container = container,
                        onLogin = { container.oauthManager.launchLogin(context) },
                        onOpenMyProfile = { navController.navigate(AppRoutes.user(it)) },
                        onOpenFavorites = { navController.navigate(AppRoutes.FAVORITES) },
                        onOpenFollowing = { navController.navigate(AppRoutes.FOLLOWING) },
                        onOpenHistory = { navController.navigate(AppRoutes.HISTORY) },
                        onOpenCreator = {
                            navController.navigate(AppRoutes.CREATOR) {
                                popUpTo(AppRoutes.FEED) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onOpenAdmin = { navController.navigate(AppRoutes.ADMIN_CONSOLE) },
                        onOpenAccountSecurity = { navController.navigate(AppRoutes.ACCOUNT_SECURITY) },
                        onOpenPrivacyPreferences = { navController.navigate(AppRoutes.PRIVACY_PREFERENCES) },
                        onOpenAppSettings = { navController.navigate(AppRoutes.APP_SETTINGS) },
                        onOpenRules = { navController.navigate(AppRoutes.RULES) },
                    )
                }

                composable(AppRoutes.SEARCH) {
                    SearchScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                        onOpenColumn = { navController.navigate(AppRoutes.column(it)) },
                        onOpenUser = { navController.navigate(AppRoutes.user(it)) },
                        onOpenTag = { navController.navigate(AppRoutes.tag(it)) },
                    )
                }

                composable(AppRoutes.RULES) {
                    RulesScreen(container = container, onBack = { navController.popBackStack() })
                }

                composable(AppRoutes.INBOX) {
                    InboxScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                        onRequireLogin = { container.oauthManager.launchLogin(context) },
                    )
                }

                composable(
                    route = AppRoutes.CATEGORY,
                    arguments = listOf(
                        navArgument("slug") { type = NavType.StringType },
                        navArgument("title") { type = NavType.StringType },
                    ),
                ) { entry ->
                    CategoryFeedScreen(
                        container = container,
                        categorySlug = entry.arguments?.getString("slug").orEmpty(),
                        categoryTitle = entry.arguments?.getString("title").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                        onOpenUser = { navController.navigate(AppRoutes.user(it)) },
                        onOpenTag = { navController.navigate(AppRoutes.tag(it)) },
                    )
                }

                composable(
                    route = AppRoutes.TAG,
                    arguments = listOf(navArgument("tag") { type = NavType.StringType }),
                ) { entry ->
                    TagFeedScreen(
                        container = container,
                        tag = entry.arguments?.getString("tag").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                        onOpenUser = { navController.navigate(AppRoutes.user(it)) },
                        onOpenTag = { navController.navigate(AppRoutes.tag(it)) },
                    )
                }

                composable(
                    route = AppRoutes.COLUMN_DETAIL,
                    arguments = listOf(navArgument("columnId") { type = NavType.StringType }),
                ) { entry ->
                    ColumnDetailScreen(
                        container = container,
                        columnIdOrSlug = entry.arguments?.getString("columnId").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                        onOpenColumn = { navController.navigate(AppRoutes.column(it)) },
                        onOpenUser = { navController.navigate(AppRoutes.user(it)) },
                        onOpenEditorForColumn = { colId -> navController.navigate(AppRoutes.editor(columnId = colId)) },
                        onRequireLogin = { container.oauthManager.launchLogin(context) },
                    )
                }

                composable(
                    route = AppRoutes.POST_DETAIL,
                    arguments = listOf(navArgument("postId") { type = NavType.StringType }),
                ) { entry ->
                    PostReaderScreen(
                        container = container,
                        postId = entry.arguments?.getString("postId").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                        onOpenColumn = { navController.navigate(AppRoutes.column(it)) },
                        onOpenUser = { navController.navigate(AppRoutes.user(it)) },
                        onOpenTag = { navController.navigate(AppRoutes.tag(it)) },
                        onOpenLink = ::openUrlOrDeepLink,
                        onEditPost = { navController.navigate(AppRoutes.editor(postId = it)) },
                        onRequireLogin = { container.oauthManager.launchLogin(context) },
                    )
                }

                composable(
                    route = AppRoutes.USER_PROFILE,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType }),
                ) { entry ->
                    UserProfileScreen(
                        container = container,
                        userIdOrUsername = entry.arguments?.getString("userId").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                        onRequireLogin = { container.oauthManager.launchLogin(context) },
                    )
                }

                composable(
                    route = AppRoutes.CREATOR_EDITOR,
                    arguments = listOf(
                        navArgument("postId") { type = NavType.StringType; defaultValue = "" },
                        navArgument("columnId") { type = NavType.StringType; defaultValue = "" },
                    ),
                ) { entry ->
                    val pid = entry.arguments?.getString("postId")?.takeIf { it.isNotBlank() }
                    val cid = entry.arguments?.getString("columnId")?.takeIf { it.isNotBlank() }
                    NativeWysiwygEditorScreen(
                        container = container,
                        postId = pid,
                        initialColumnId = cid,
                        onBack = { navController.popBackStack() },
                        onPublished = { publishedId ->
                            navController.popBackStack()
                            navController.navigate(AppRoutes.post(publishedId))
                        },
                    )
                }

                composable(AppRoutes.CREATOR_POSTS) {
                    CreatorPostsScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onNewArticle = { navController.navigate(AppRoutes.editor()) },
                        onEditPost = { navController.navigate(AppRoutes.editor(postId = it)) },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                    )
                }

                composable(AppRoutes.CREATOR_COLUMNS) {
                    CreatorColumnsScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onOpenColumn = { navController.navigate(AppRoutes.column(it)) },
                    )
                }

                composable(AppRoutes.CREATOR_SERIES) {
                    CreatorSeriesScreen(container = container, onBack = { navController.popBackStack() })
                }

                composable(AppRoutes.CREATOR_COMMENTS) {
                    CreatorCommentsScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                    )
                }

                composable(AppRoutes.CREATOR_REVIEWS) {
                    CreatorReviewsScreen(container = container, onBack = { navController.popBackStack() })
                }

                composable(AppRoutes.CREATOR_MEDIA) {
                    CreatorMediaScreen(container = container, onBack = { navController.popBackStack() })
                }

                composable(AppRoutes.FAVORITES) {
                    FavoritesScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                    )
                }

                composable(AppRoutes.FOLLOWING) {
                    FollowingScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onOpenUser = { navController.navigate(AppRoutes.user(it)) },
                    )
                }

                composable(AppRoutes.HISTORY) {
                    ReadingHistoryScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                    )
                }

                composable(AppRoutes.ACCOUNT_SECURITY) {
                    AccountSecurityScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onAddAccount = { container.oauthManager.launchLogin(context, forceSwitchAccount = true) },
                    )
                }

                composable(AppRoutes.PRIVACY_PREFERENCES) {
                    PrivacyPreferencesScreen(container = container, onBack = { navController.popBackStack() })
                }

                composable(AppRoutes.APP_SETTINGS) {
                    AppSettingsScreen(container = container, onBack = { navController.popBackStack() })
                }

                composable(AppRoutes.ADMIN_CONSOLE) {
                    AdminConsoleScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onOpenPost = { navController.navigate(AppRoutes.post(it)) },
                        onOpenUser = { navController.navigate(AppRoutes.user(it)) },
                    )
                }
            }

            // Floating 4-tab Bento Pill Bar
            if (showBottomBar) {
                BbsFloatingNavBar(
                    tabs = tabs,
                    selectedRoute = currentRoute,
                    onSelect = { target ->
                        if (target != currentRoute) {
                            navController.navigate(target) {
                                popUpTo(AppRoutes.FEED) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            // Animated Daily EXP Reward Pill
            AnimatedVisibility(
                visible = expToast != null,
                enter = slideInVertically(IgngMotion.standardSpring(intensity)) { -it } + fadeIn(IgngMotion.easeTween(180, intensity)),
                exit = slideOutVertically(IgngMotion.standardSpring(intensity)) { -it } + fadeOut(IgngMotion.easeTween(140, intensity)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp),
            ) {
                expToast?.let { reward ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(IgngRadius.pill))
                            .background(colors.tile)
                            .border(1.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(IgngRadius.pill))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "每日签到已结算 · 当前 Lv.${reward.level ?: 1}（${reward.exp ?: 0} EXP）",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.accent,
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (showBottomBar) 88.dp else 16.dp),
            )
        }

        // Global 401 Session Expired Dialog
        sessionExpiredReason?.let { reason ->
            AlertDialog(
                onDismissRequest = { sessionExpiredReason = null },
                title = { Text("需要重新登录") },
                text = { Text(reason) },
                confirmButton = {
                    TextButton(onClick = {
                        sessionExpiredReason = null
                        container.oauthManager.launchLogin(context)
                    }) { Text("重新登录") }
                },
                dismissButton = {
                    TextButton(onClick = { sessionExpiredReason = null }) { Text("稍后再说") }
                },
            )
        }
    }
}
