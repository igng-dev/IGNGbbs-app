package net.igng.bbs.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.data.UpdateMySettingsRequest
import net.igng.bbs.core.data.UpdateUserPreferencesPatch
import net.igng.bbs.core.designsystem.IgngAvatar
import net.igng.bbs.core.designsystem.IgngInfoBanner
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPrimaryButton
import net.igng.bbs.core.designsystem.IgngSecondaryButton
import net.igng.bbs.core.designsystem.IgngSectionHeader
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.IgngTopBar
import net.igng.bbs.core.model.AppPreferences
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.BbsUserSettingsDto
import net.igng.bbs.core.model.DeviceSessionDto
import net.igng.bbs.core.model.MotionIntensity
import net.igng.bbs.core.model.ReaderCanvasTone
import net.igng.bbs.core.model.ThemeMode
import net.igng.bbs.core.network.ApiResult

@Composable
fun AccountSecurityScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onAddAccount: () -> Unit,
) {
    val context = LocalContext.current
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val accounts by container.sessionStore.accounts.collectAsState()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()

    var devices by remember { mutableStateOf<List<DeviceSessionDto>>(emptyList()) }
    var usernameInput by remember(activeAccount?.username) { mutableStateOf(activeAccount?.username.orEmpty()) }
    var notice by remember { mutableStateOf<String?>(null) }
    var renameDeviceTarget by remember { mutableStateOf<DeviceSessionDto?>(null) }
    var renameText by remember { mutableStateOf("") }

    suspend fun loadDevices() {
        when (val r = container.profileRepository.listDevices()) {
            is ApiResult.Success -> devices = r.data.data
            is ApiResult.Failure -> notice = r.error.message
        }
    }
    LaunchedEffect(activeAccount?.userId) { if (activeAccount != null) loadDevices() }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val payload = withContext(Dispatchers.IO) {
                runCatching {
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@runCatching null
                    Triple(bytes, mime, "avatar_${System.currentTimeMillis()}.jpg")
                }.getOrNull()
            }
            if (payload != null) {
                when (val r = container.profileRepository.uploadAvatar(payload.third, payload.second, payload.first)) {
                    is ApiResult.Success -> {
                        container.sessionStore.updateActiveProfile(avatarUrl = r.data.avatarUrl)
                        container.refreshBootstrap()
                        notice = "头像已更新"
                    }
                    is ApiResult.Failure -> notice = r.error.message
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(title = "账户与设备安全", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            notice?.let { item { IgngInfoBanner("提示", it, IgngTone.ACCENT) } }

            // Multi-account switcher
            item {
                IgngTile {
                    Text("已保存账户（${accounts.size}）", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(10.dp))
                    accounts.forEach { acc ->
                        val isCurrent = acc.userId == activeAccount?.userId
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            IgngAvatar(acc.avatarUrl, acc.displayName, size = 36.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(acc.displayName, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                Text("@${acc.username} · #${acc.userId}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                            }
                            if (isCurrent) {
                                IgngTagChip("当前使用", tone = IgngTone.SUCCESS)
                            } else {
                                IgngTagChip("切换", tone = IgngTone.ACCENT, onClick = {
                                    scope.launch { container.switchAccount(acc.userId); notice = "已切换到 ${acc.displayName}" }
                                })
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        IgngSecondaryButton(text = "添加其他账户", onClick = onAddAccount, modifier = Modifier.weight(1f))
                        if (activeAccount != null) {
                            IgngSecondaryButton(
                                text = "退出当前账户",
                                onClick = { scope.launch { container.logoutActive(); notice = "已退出当前账户" } },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // Profile identity edit (avatar & username)
            if (activeAccount != null) {
                item {
                    IgngTile {
                        Text("头像与用户名", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(10.dp))
                        IgngSecondaryButton(
                            text = "从相册更换头像",
                            onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("用户名") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        IgngPrimaryButton(text = "保存用户名", onClick = {
                            scope.launch {
                                when (val r = container.profileRepository.updateUsername(usernameInput.trim())) {
                                    is ApiResult.Success -> {
                                        container.sessionStore.updateActiveProfile(username = r.data.username)
                                        container.refreshBootstrap()
                                        notice = "用户名已保存"
                                    }
                                    is ApiResult.Failure -> notice = r.error.message
                                }
                            }
                        })
                    }
                }

                // Active Login Devices
                item { IgngSectionHeader("登录设备管理（${devices.size}）") }
                items(devices, key = { it.id }) { dev ->
                    IgngTile(padding = PaddingValues(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dev.deviceName, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                Text(
                                    "${dev.clientLabel ?: "登录会话"} · 最近活跃 ${BbsFormatters.relativeTime(dev.lastActiveAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                )
                            }
                            if (dev.isCurrent) {
                                IgngTagChip("本机", tone = IgngTone.SUCCESS)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IgngTagChip("重命名", onClick = { renameDeviceTarget = dev; renameText = dev.deviceName })
                            if (!dev.isCurrent) {
                                IgngTagChip("吊销会话", tone = IgngTone.DANGER, onClick = {
                                    scope.launch {
                                        when (val r = container.profileRepository.revokeDevice(dev.id)) {
                                            is ApiResult.Success -> { notice = "已吊销该设备登录状态"; loadDevices() }
                                            is ApiResult.Failure -> notice = r.error.message
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    renameDeviceTarget?.let { dev ->
        AlertDialog(
            onDismissRequest = { renameDeviceTarget = null },
            title = { Text("重命名登录设备") },
            text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it }, label = { Text("设备名称") }) },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = renameText.trim()
                    if (trimmed.isEmpty()) return@TextButton
                    renameDeviceTarget = null
                    scope.launch {
                        when (val r = container.profileRepository.renameDevice(dev.id, trimmed)) {
                            is ApiResult.Success -> { notice = "设备名称已更新"; loadDevices() }
                            is ApiResult.Failure -> notice = r.error.message
                        }
                    }
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { renameDeviceTarget = null }) { Text("取消") } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrivacyPreferencesScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val bootstrap by container.bootstrap.collectAsState()

    var settings by remember { mutableStateOf<BbsUserSettingsDto?>(null) }
    var bio by remember { mutableStateOf("") }
    var allowFollowers by remember { mutableStateOf(true) }
    var allowFavorites by remember { mutableStateOf(true) }
    var hideAi by remember { mutableStateOf(false) }
    var excludedCats by remember { mutableStateOf<Set<String>>(emptySet()) }
    var excludedTagsText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var notice by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        loading = true
        when (val r = container.profileRepository.getMySettings()) {
            is ApiResult.Success -> {
                settings = r.data
                bio = r.data.bio
                allowFollowers = r.data.allowFollowersVisibility
                allowFavorites = r.data.allowFavoritesVisibility
                hideAi = r.data.preferences.hideAi
                excludedCats = r.data.preferences.excludedCategoryIds.toSet()
                excludedTagsText = r.data.preferences.excludedTags.joinToString(", ")
            }
            is ApiResult.Failure -> notice = r.error.message
        }
        loading = false
    }
    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(title = "隐私与内容偏好", onBack = onBack)
        if (loading && settings == null) {
            IgngLoadingCenter(loading = true)
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                notice?.let { item { IgngInfoBanner("提示", it, IgngTone.ACCENT) } }
                item {
                    IgngTile {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("个人简介", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            settings?.bioStatus?.let { st ->
                                if (st == 1) IgngTagChip("简介审核中", tone = IgngTone.WARNING)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("向社区介绍你自己（修改后将进入审核）") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        SwitchRow("公开我的粉丝与关注列表", allowFollowers) { allowFollowers = it }
                        SwitchRow("公开我的收藏夹", allowFavorites) { allowFavorites = it }
                        SwitchRow("在信息流隐藏 AI 辅助生成内容", hideAi) { hideAi = it }
                    }
                }
                item {
                    IgngTile {
                        Text("排除的分区与标签", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            bootstrap?.categories.orEmpty().forEach { cat ->
                                val selected = cat.id in excludedCats
                                IgngTagChip(
                                    text = cat.name,
                                    selected = selected,
                                    tone = if (selected) IgngTone.DANGER else IgngTone.NEUTRAL,
                                    onClick = {
                                        excludedCats = if (selected) excludedCats - cat.id else excludedCats + cat.id
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = excludedTagsText,
                            onValueChange = { excludedTagsText = it },
                            label = { Text("屏蔽标签（逗号分隔）") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(12.dp))
                        IgngPrimaryButton(text = "保存隐私与偏好设置", onClick = {
                            val tags = excludedTagsText.split(',', '，').map { it.trim() }.filter { it.isNotEmpty() }
                            scope.launch {
                                when (val r = container.profileRepository.updateMySettings(
                                    UpdateMySettingsRequest(
                                        bio = bio,
                                        allowFollowersVisibility = allowFollowers,
                                        allowFavoritesVisibility = allowFavorites,
                                        preferences = UpdateUserPreferencesPatch(
                                            excludedCategoryIds = excludedCats.toList(),
                                            excludedTags = tags,
                                            hideAi = hideAi,
                                        ),
                                    ),
                                )) {
                                    is ApiResult.Success -> { settings = r.data; notice = "隐私与偏好已同步到云端" }
                                    is ApiResult.Failure -> notice = r.error.message
                                }
                            }
                        })
                    }
                }
                val rules = settings?.blockRules.orEmpty()
                if (rules.isNotEmpty()) {
                    item { IgngSectionHeader("已屏蔽用户（${rules.size}）") }
                    items(rules, key = { it.id }) { rule ->
                        IgngTile(padding = PaddingValues(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${rule.targetUser.displayName} (@${rule.targetUser.username})", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                IgngTagChip("解除屏蔽", tone = IgngTone.DANGER, onClick = {
                                    scope.launch {
                                        container.profileRepository.deleteBlockRule(rule.targetUser.id)
                                        load()
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppSettingsScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val prefs by container.settingsRepository.preferences.collectAsState(initial = AppPreferences())
    var cacheCleared by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(title = "应用设置", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                IgngTile {
                    Text("外观主题", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IgngTagChip("跟随系统", selected = prefs.themeMode == ThemeMode.SYSTEM, onClick = { scope.launch { container.settingsRepository.setThemeMode(ThemeMode.SYSTEM) } })
                        IgngTagChip("浅色", selected = prefs.themeMode == ThemeMode.LIGHT, onClick = { scope.launch { container.settingsRepository.setThemeMode(ThemeMode.LIGHT) } })
                        IgngTagChip("深色", selected = prefs.themeMode == ThemeMode.DARK, onClick = { scope.launch { container.settingsRepository.setThemeMode(ThemeMode.DARK) } })
                    }
                }
            }
            item {
                IgngTile {
                    Text("动效与触感反馈", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text("默认采用舒适柔和的物理减速曲线，也可按个人习惯降低动效强度。", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IgngTagChip("完整动效", selected = prefs.motionIntensity == MotionIntensity.FULL, onClick = { scope.launch { container.settingsRepository.setMotionIntensity(MotionIntensity.FULL) } })
                        IgngTagChip("轻柔动效", selected = prefs.motionIntensity == MotionIntensity.CALM, onClick = { scope.launch { container.settingsRepository.setMotionIntensity(MotionIntensity.CALM) } })
                        IgngTagChip("减少动态", selected = prefs.motionIntensity == MotionIntensity.REDUCED, onClick = { scope.launch { container.settingsRepository.setMotionIntensity(MotionIntensity.REDUCED) } })
                    }
                    Spacer(Modifier.height(12.dp))
                    SwitchRow("交互微触感反馈", prefs.hapticsEnabled) { scope.launch { container.settingsRepository.setHapticsEnabled(it) } }
                    SwitchRow("首页自动折叠同系列文章", prefs.collapseSeriesInFeed) { scope.launch { container.settingsRepository.setCollapseSeriesInFeed(it) } }
                    SwitchRow("后台定期同步未读消息通知", prefs.backgroundNotificationsEnabled) { scope.launch { container.settingsRepository.setBackgroundNotificationsEnabled(it) } }
                }
            }
            item {
                IgngTile {
                    Text("默认阅读画布", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IgngTagChip("跟随主题", selected = prefs.reader.canvasTone == ReaderCanvasTone.DEFAULT, onClick = { scope.launch { container.settingsRepository.updateReaderPrefs { it.copy(canvasTone = ReaderCanvasTone.DEFAULT) } } })
                        IgngTagChip("暖纸护眼", selected = prefs.reader.canvasTone == ReaderCanvasTone.WARM_PAPER, onClick = { scope.launch { container.settingsRepository.updateReaderPrefs { it.copy(canvasTone = ReaderCanvasTone.WARM_PAPER) } } })
                        IgngTagChip("深墨夜间", selected = prefs.reader.canvasTone == ReaderCanvasTone.INK_DARK, onClick = { scope.launch { container.settingsRepository.updateReaderPrefs { it.copy(canvasTone = ReaderCanvasTone.INK_DARK) } } })
                    }
                }
            }
            item {
                IgngTile {
                    Text("缓存与版本", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("IGNG BBS Android 客户端 · 版本 ${container.appVersion}（net.igng.bbs）", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Spacer(Modifier.height(10.dp))
                    IgngSecondaryButton(
                        text = if (cacheCleared) "本地缓存已清理" else "清理本地文章与图片缓存",
                        onClick = {
                            scope.launch {
                                container.settingsRepository.clearLocalCache()
                                cacheCleared = true
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = IgngTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
