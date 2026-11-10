package com.example.wxxx

/**
 * =========================================================================================
 * ⚙️ Aura 19.0 设置中心与二级全屏交互系统 (AuraSettingsScreen.kt)
 * =========================================================================================
 * 【工程定位】：
 * 本文件承载 Aura 应用的二级配置中心、沉浸式子页面路由系统以及高级动态视效。
 *
 * 【核心系统一览】：
 * 1. 顶部无界全屏礼花粒子物理画板（Edge-to-Edge 穿透左右挡板的 Confetti 物理引擎 + 96.dp 弹簧 Logo）
 * 2. 二级全屏子页面路由状态机（安全应用锁、时态调色盘、缓存清理、推广二维码、检查更新）
 * 3. 沉浸式导航底栏联动隐藏机制（onSubScreenChange 驱动外部底栏滑出隐藏）
 * 4. 专业级 HSV 调色盘与纤细极简黑白滑块组件（AuraSlimSlider）
 * 5. 高清二维码生成与双胶囊操作区（【系统分享】与【保存到相册】对称布局）
 * 6. GitHub API 检查更新与 APK 离线安装拉起
 * =========================================================================================
 */

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.imageLoader
import com.example.wxxx.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 🛠️ AuraSettingsScreen - 官方主设置页 (ChatGPT 分组大卡片架构)
 *
 * 架构特点：
 * 1. 沉浸式流体背景：继承 Aura 动态呼吸渐变与果冻方块底层，通透玻璃拟态。
 * 2. 顶部纯净留白大卡片：200.dp 标准高度占位与高透玻璃底衬，预留未来画板拓展。
 * 3. ChatGPT 风格分组卡片：统一 16.dp 圆角大卡片、0.5.dp 细分割线、统一高度与右向箭头。
 * 4. 二级全屏导航框架：内置平滑水平滑入/滑出动画架构，6 大核心功能专属二级控制台。
 */

data class AuraSettingsItemConfig(
    val id: String,
    val title: String,
    val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val iconTint: Color = Color(0xFF1C1C1E),
    val iconBgColor: Color = Color.Transparent,
    val subtitle: String? = null,
    val badgeText: String? = null, // 💡 状态驱动小灯牌文本（如 "新版本"），为 null 时完全不渲染
    val onClick: () -> Unit = {}
)

data class AuraSettingsSectionConfig(
    val title: String,
    val items: List<AuraSettingsItemConfig>
)

// =========================================================================================
// ⚙️ [设置中心主控界面与二级路由调度系统：AuraSettingsScreen]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 19.0 的设置中枢与二级全屏子页面状态机调度容器。
// 2. 它能干什么？
//    - 展示画框数量滑块配置、四时段光晕调色盘入口、生物锁配置入口、清理缓存入口、分享与更新检测；
//    - 监听 activeSubScreenId 路由状态，在进入二级子页面时通过 onSubScreenChange(true) 通知外部隐藏底栏，
//      返回主设置时通过 onSubScreenChange(false) 恢复底栏显示；
//    - 接管物理返回键（BackHandler），优先退出子页面。
// 3. 怎么调整参数？
//    - 若需增加新的设置分组/条目：在 settingsSections 配置列表中添加 AuraSettingsSectionConfig。
//    - 若需调整默认画框数量或默认时态颜色：修改 currentCount 或 morningColor 等参数默认值。
// =========================================================================================
@Composable
fun AuraSettingsScreen(
    currentCount: Int = 7,
    onCountChange: (Int) -> Unit = {},
    themeMode: AppThemeMode = AppThemeMode.MODE_LIGHT,
    onThemeChange: (AppThemeMode) -> Unit = {},
    morningColor: Color = DEFAULT_COLOR_MORNING,
    noonColor: Color = DEFAULT_COLOR_NOON,
    afternoonColor: Color = DEFAULT_COLOR_AFTERNOON,
    nightColor: Color = DEFAULT_COLOR_NIGHT,
    onSaveColors: (Color, Color, Color, Color) -> Unit = { _, _, _, _ -> },
    isBiometricLockEnabled: Boolean = false,
    lockTimeoutMinutes: Int = 0,
    onToggleBiometricLock: (Boolean) -> Unit = {},
    onTimeoutChange: (Int) -> Unit = {},
    onSubScreenChange: (Boolean) -> Unit = {},
    bottomGradientColor: Color = Color(0xFFABEE4F),
    glowFraction: Float = 0.6f,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme = LocalIsDarkTheme.current
    var activeSubScreenId by remember { mutableStateOf<String?>(null) }

    // 跨滑动生命周期门控：确保 Tab 4 进入时开屏动效与欢庆彩屑仅自动播放一次
    // 在 LazyColumn 滚动列表移出视口再滑回顶部时，不再重复触发全屏烟花喷射；仅保留点击 Logo 时的手动触发
    var hasPlayedEntranceAnimation by rememberSaveable { mutableStateOf(false) }

    // 🌟 全局响应式状态驱动：直接监听 UpdateManager 全局版本更新流
    val hasNewVersion by UpdateManager.hasNewVersion.collectAsState()

    // 🌟 进入设置页时，若尚未进行过云端版本检测，静默在后台触发一次现有 checkUpdate 以同步灯牌状态
    LaunchedEffect(Unit) {
        if (UpdateManager.latestReleaseInfo.value == null) {
            try {
                val currentAppVersion = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            android.content.pm.PackageManager.PackageInfoFlags.of(0)
                        ).versionName ?: UpdateManager.AURA_CURRENT_VERSION
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: UpdateManager.AURA_CURRENT_VERSION
                    }
                } catch (e: Exception) {
                    UpdateManager.AURA_CURRENT_VERSION
                }
                UpdateManager.checkUpdate(currentVersion = currentAppVersion)
            } catch (_: Exception) {}
        }
    }

    // 🌟 通知外层宿主二级页面路由切换状态，用于联动控制底部导航栏显隐
    LaunchedEffect(activeSubScreenId) {
        onSubScreenChange(activeSubScreenId != null)
    }
    DisposableEffect(Unit) {
        onDispose {
            onSubScreenChange(false)
        }
    }

    // 系统返回键分流处理：如果处在三级页面返回二级，二级子页面返回一级设置页；否则触发退出设置
    BackHandler(enabled = activeSubScreenId != null) {
        if (activeSubScreenId == "performance") {
            activeSubScreenId = "lab_reserve"
        } else {
            activeSubScreenId = null
        }
    }
    BackHandler(enabled = activeSubScreenId == null) {
        onBack()
    }

    // 四大核心分组数据配置 (依照草图 1:1 分组)
    val sections = remember(isBiometricLockEnabled, lockTimeoutMinutes, hasNewVersion, themeMode, isDarkTheme) {
        listOf(
            // Section 1 - 常规
            AuraSettingsSectionConfig(
                title = "常规",
                items = listOf(
                    AuraSettingsItemConfig(
                        id = "home_palette",
                        title = "主页背景调色",
                        iconRes = R.drawable.ic_custom_palette,
                        iconTint = Color(0xFF1C1C1E),
                        iconBgColor = Color.Transparent,
                        onClick = {}
                    ),
                    AuraSettingsItemConfig(
                        id = "swipe_count",
                        title = "照片展示数量",
                        iconRes = R.drawable.ic_swipe_up,
                        iconTint = Color(0xFF1C1C1E),
                        iconBgColor = Color.Transparent,
                        onClick = {}
                    ),
                    AuraSettingsItemConfig(
                        id = "app_theme",
                        title = "应用主题",
                        iconRes = R.drawable.ic_theme_mode,
                        iconTint = Color(0xFF1C1C1E),
                        iconBgColor = Color.Transparent,
                        onClick = {}
                    )
                )
            ),

            // Section 2 - 安全
            AuraSettingsSectionConfig(
                title = "安全",
                items = listOf(
                    AuraSettingsItemConfig(
                        id = "app_lock",
                        title = "应用锁定",
                        iconRes = R.drawable.ic_mobile_unlock,
                        iconTint = Color(0xFF1C1C1E),
                        iconBgColor = Color.Transparent,
                        onClick = {}
                    )
                )
            ),

            // Section 3 - 工具
            AuraSettingsSectionConfig(
                title = "工具",
                items = listOf(
                    AuraSettingsItemConfig(
                        id = "cache_clean",
                        title = "存储空间",
                        iconRes = R.drawable.ic_delete_sweep,
                        iconTint = Color(0xFF1C1C1E),
                        iconBgColor = Color.Transparent,
                        onClick = {}
                    )
                )
            ),

            // Section 4 - 其他
            AuraSettingsSectionConfig(
                title = "其他",
                items = listOf(
                    AuraSettingsItemConfig(
                        id = "qr_share",
                        title = "分享二维码",
                        iconVector = Icons.Default.QrCode,
                        iconTint = Color(0xFF1C1C1E),
                        iconBgColor = Color.Transparent,
                        onClick = {}
                    ),
                    AuraSettingsItemConfig(
                        id = "check_update",
                        title = "检查更新",
                        iconVector = Icons.Default.Refresh,
                        iconTint = Color(0xFF1C1C1E),
                        iconBgColor = Color.Transparent,
                        badgeText = if (hasNewVersion) "新版本" else null, // 💡 状态驱动：有新版本时点亮专属小灯牌
                        onClick = {}
                    ),
                    AuraSettingsItemConfig(
                        id = "lab_reserve",
                        title = "实验性功能",
                        iconVector = Icons.Default.Science,
                        iconTint = Color(0xFF1C1C1E),
                        iconBgColor = Color.Transparent,
                        onClick = {}
                    )
                )
            )
        )
    }

    // 🌟 外层沉浸式背景：半透明微白基底 + 垂直微光呼吸渐变
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { } } // 阻止事件穿透
            .background(if (isDarkTheme) Color(0xFF121212).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.90f))
            .auraBottomGradient(
                color = bottomGradientColor,
                glowFractionProvider = { glowFraction }
            )
    ) {
        // 🌟 二级/三级页面无缝平滑滑动切换动画框架 (支持前进右入左出、返回右出左入)
        AnimatedContent(
            targetState = activeSubScreenId,
            transitionSpec = {
                val slideSpec = tween<IntOffset>(durationMillis = 300, easing = FastOutSlowInEasing)
                val fadeSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)

                fun getLevel(id: String?): Int = when (id) {
                    null -> 0
                    "performance" -> 2
                    else -> 1
                }

                val isForward = getLevel(targetState) > getLevel(initialState)

                if (isForward) {
                    // 进入更深层页面 (Push)：新页面由右向左滑入，旧页面向左微移推出
                    (slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }, animationSpec = slideSpec) +
                     fadeIn(animationSpec = fadeSpec))
                        .togetherWith(
                            slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth / 4 }, animationSpec = slideSpec) +
                            fadeOut(animationSpec = fadeSpec)
                        ).apply {
                            targetContentZIndex = 1f
                        }
                } else {
                    // 返回上一层页面 (Pop)：当前页面从左向右滑出屏幕（向右退回），底层页面由左向右滑回复位
                    (slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth / 4 }, animationSpec = slideSpec) +
                     fadeIn(animationSpec = fadeSpec))
                        .togetherWith(
                            slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = slideSpec) +
                            fadeOut(animationSpec = fadeSpec)
                        ).apply {
                            targetContentZIndex = -1f
                        }
                }
            },
            label = "SettingsScreenNavigation"
        ) { targetId ->
            if (targetId == null) {
                // 一级主设置列表 (LazyColumn)：移除 statusBarsPadding 与水平 padding，画板与烟花横向全屏穿透 (Edge-to-Edge)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 120.dp)
                ) {
                    // 1. 顶部 200.dp 视觉画板 (开箱双侧礼花 + 弹簧 App 图标 + 极客打字机)
                    // 🌟 横向全屏破壁：彻底拆除外层水平 padding 挡板，让礼花与画板横向 100% 铺满延伸至屏幕物理左右边缘
                    item(key = "top_hero_canvas", contentType = "hero_canvas") {
                        AuraTopHeroCanvas(
                            hasPlayedEntranceAnimation = hasPlayedEntranceAnimation,
                            onEntranceAnimationPlayed = { hasPlayedEntranceAnimation = true }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 2. 各分组大卡片 (1:1 ChatGPT 风格)
                    sections.forEach { section ->
                        item(key = "section_${section.title}", contentType = "settings_section") {
                            AuraSettingsSectionCard(
                                section = section,
                                onItemClick = { item ->
                                    activeSubScreenId = item.id
                                    item.onClick()
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            } else {
                // 二级全屏页面 (主页背景调色、划动图片数量与应用锁定已完全接入真实逻辑)
                // 🌟 底图层级加固：赋予完全不透明的实色基底 + 微光呼吸渐变材质，彻底阻断动画期间的底层 Overdraw
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDarkTheme) Color(0xFF121212) else Color.White)
                        .navigationBarsPadding() // 🌟 动态避让系统导航条与三键导航，杜绝遮挡操作按钮
                        .auraBottomGradient(
                            color = bottomGradientColor,
                            glowFractionProvider = { glowFraction }
                        )
                ) {
                    AuraSubScreenHost(
                        subScreenId = targetId,
                        currentCount = currentCount,
                        onCountChange = onCountChange,
                        themeMode = themeMode,
                        onThemeChange = onThemeChange,
                        isBiometricLockEnabled = isBiometricLockEnabled,
                        lockTimeoutMinutes = lockTimeoutMinutes,
                        onToggleBiometricLock = onToggleBiometricLock,
                        onTimeoutChange = onTimeoutChange,
                        morningColor = morningColor,
                        noonColor = noonColor,
                        afternoonColor = afternoonColor,
                        nightColor = nightColor,
                        onSaveColors = onSaveColors,
                        onNavigateSubScreen = { activeSubScreenId = it },
                        onBack = {
                            if (activeSubScreenId == "performance") {
                                activeSubScreenId = "lab_reserve"
                            } else {
                                activeSubScreenId = null
                            }
                        }
                    )
                }
            }
        }
    }
}


/**
 * 🌟 ChatGPT 风格分组大卡片组件：统一 16.dp 圆角、玻璃拟态背景、0.5.dp 细分割线
 */
@Composable
private fun AuraSettingsSectionCard(
    section: AuraSettingsSectionConfig,
    onItemClick: (AuraSettingsItemConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalIsDarkTheme.current
    Column(modifier = modifier.fillMaxWidth()) {
        // 分组标题小字
        Text(
            text = section.title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )

        // 统一圆角大卡片容器 (16.dp) - 由最外层父容器统一提供背景、圆角及描边，内部子项透明透底
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.88f)
                    else Color.White.copy(alpha = 0.88f)
                )
                .border(
                    width = 1.dp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.12f)
                    else Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                section.items.forEachIndexed { index, item ->
                    AuraSettingsItemRow(
                        item = item,
                        onClick = { onItemClick(item) }
                    )

                    // 内部细线分割：最后一个 Item 下方无分割线
                    if (index < section.items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 62.dp, end = 18.dp),
                            thickness = 0.5.dp,
                            color = if (isDarkTheme) Color(0xFF334155).copy(alpha = 0.6f)
                            else Color(0xFFE2E8F0).copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🌟 卡片内部统一 Item (1:1 X/Twitter 黑白极简线条风格)
 */
@Composable
private fun AuraSettingsItemRow(
    item: AuraSettingsItemConfig,
    onClick: () -> Unit
) {
    val isDarkTheme = LocalIsDarkTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // 统一标准高度
            .background(Color.Transparent) // 纯净透底
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧功能图标 (1:1 X 纯黑/纯白线条风，彻底移除彩色，安全放大至 28.dp)
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            val iconTint = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1C1C1E)
            if (item.iconRes != null) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.title,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            } else if (item.iconVector != null) {
                Icon(
                    imageVector = item.iconVector,
                    contentDescription = item.title,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp)) // 1:1 X 紧凑干练间距

        // 中间单行主标题 (1:1 X 纯黑力量感 Bold 粗体，极致纯净左对齐)
        Text(
            text = item.title,
            fontSize = 16.5.sp,
            fontWeight = FontWeight.Bold, // 🌟 1:1 对齐 X (Twitter) 粗体
            color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1C1C1E),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 🌟 [X App 风格专属状态小灯牌] 当且仅当有角标文本时优雅呈现（如静默检测到新版本）
        if (item.badgeText != null) {
            Spacer(modifier = Modifier.width(8.dp)) // 标题与灯牌之间的精致间距
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50)) // 🌟 全圆角胶囊造型 (对标 X App 极简圆角)
                    .background(Color(0xFF1D9BF0)) // 💡 X (Twitter) 经典社交蓝
                    .padding(horizontal = 6.dp, vertical = 2.dp), // 紧凑精致内边距，严防撑大 56.dp 行高
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.badgeText,
                    color = Color.White, // 纯白高对比度文字
                    fontSize = 10.5.sp, // 💡 调整此处更改灯牌字号（建议 10.sp ~ 11.sp）
                    fontWeight = FontWeight.Bold, // 加粗力量感
                    lineHeight = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// =========================================================================================
// 🎨 [设置页顶部破壁画板：全屏粒子礼花引擎 + 弹簧 Logo + 极客打字机]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 19.0 核心视觉画板，集成横向全屏打通（Edge-to-Edge 无阻挡破壁）Canvas 物理粒子系统。
// 2. 它能干什么？
//    - 居中呈现 96.dp 紧凑精致 App 图标与极客打字机单行签名；
//    - 点击图标触发触觉震动反馈、Logo 弹性压缩回弹（snapTo 0.85 -> 1.0），以及 60+ 枚彩色粒子喷涌；
//    - 粒子引擎具有重力加速度、初速度阻尼衰减、双向自旋及圆形/矩形随机形态；
//    - 外层 Box 强制 fillMaxWidth 并彻底移除左右水平限制，确保粒子畅通无阻飞溅至手机屏幕最左与最右物理极限。
// 3. 怎么调整参数？
//    - 图标尺寸：修改图标容器的 size（当前 96.dp，内部 Logo 68.dp）。
//    - 粒子喷发时长：修改 confettiProgress.animateTo 的 tween(durationMillis = 2400)。
//    - 粒子爆发力：修改 AuraConfettiParticle 的 vxRatio / vyRatio 初速度因子。
//    - 破壁铁律：包裹本组件的父级容器严禁添加 horizontal padding，以防左右截断。
// =========================================================================================

/**
 * 顶部 200.dp 视觉画板物理与动画配置项 (结构化与集中管理)
 */
private object AuraHeroConfig {
    // 文本与打字机配置
    const val FULL_TEXT = "// Hello I am Aura"
    const val TYPING_START_DELAY_MS = 320L // 打字机启动前的沉淀等待延迟
    const val TYPING_CHAR_INTERVAL_MS = 90L // 终端逐字打印时间间隔

    // 光标动效配置 (打字完成后闪烁有限轮次后休眠，彻底消除持续 CPU 唤醒)
    const val CURSOR_BLINK_HALF_CYCLE_MS = 480 // 光标单次翻转半周期 (ms)
    const val CURSOR_BLINK_CYCLES = 4 // 打字完毕后光标闪烁轮数
    const val CURSOR_DORMANT_FADE_DURATION_MS = 300 // 休眠淡出耗时 (ms)

    // 动画时长
    const val CONFETTI_DURATION_MS = 2400 // 礼花粒子喷射与自然飘落总时长 (ms)
    const val CONFETTI_TOTAL_TIME_SEC = 2.4f // 礼花物理引擎模拟总时长 (秒)
    const val ICON_ENTRANCE_MS = 450 // App 图标入场淡入耗时 (ms)

    // 图标动画与尺寸参数
    const val ICON_INITIAL_SCALE = 0.5f // 图标初始缩放比例
    const val ICON_CLICK_SCALE = 0.85f // 点击图标时的按压回弹初始缩放比例
    const val HERO_CONTAINER_HEIGHT_DP = 200 // 顶部 Hero 区域总高度 (dp)
    const val ICON_CARD_SIZE_DP = 96 // App 图标外层卡片边长 (dp)
    const val ICON_CORNER_RADIUS_DP = 22 // App 图标外层卡片平滑圆角 (dp)
    const val ICON_LOGO_SIZE_DP = 68 // 内部 Logo 矢量图边长 (dp)

    // 礼花物理引擎与抛物线参数
    const val CONFETTI_CANON_PARTICLES = 32 // 左右两侧炮筒各自发射粒子数 (合计 64)
    const val RANDOM_SEED = 42L // 固定种子保证初速度分布稳定对称
    const val GRAVITY_ACCELERATION = 1.95f // 相对高度重力加速度 (/ 秒^2)
    const val DRAG_COEFFICIENT = 0.12f // 水平空气阻力衰减系数
    const val FADE_OUT_DURATION_SEC = 1.0f // 尾部淡出时间窗口 (秒)
    const val FADE_IN_THRESHOLD_SEC = 0.1f // 头部喷出淡入阈值 (秒)
    const val CANVAS_BOUNDS_BUFFER_PX = 60f // 视口裁剪外扩缓冲区 (px)

    // 粒子生成范围与初速度
    const val CANON_Y0_RATIO = 0.95f // 发射筒底部 Y 相对高度比例
    const val LEFT_CANON_X0_BASE = 0.08f // 左下炮筒 X 基础比例
    const val LEFT_CANON_X0_RANGE = 0.08f // 左下炮筒 X 抖动范围
    const val RIGHT_CANON_X0_BASE = 0.92f // 右下炮筒 X 基础比例
    const val RIGHT_CANON_X0_RANGE = 0.08f // 右下炮筒 X 抖动范围
    const val VX_BASE = 0.45f // X 初速度绝对值基础
    const val VX_RANGE = 0.95f // X 初速度随机浮动范围
    const val VY_BASE = 1.25f // Y 初速度绝对值基础 (向上)
    const val VY_RANGE = 1.55f // Y 初速度随机浮动范围
    const val PARTICLE_WIDTH_BASE_DP = 5f // 纸片宽度基础值 (dp)
    const val PARTICLE_WIDTH_RANGE_DP = 5f // 纸片宽度随机范围 (dp)
    const val PARTICLE_HEIGHT_BASE_DP = 7f // 纸片高度基础值 (dp)
    const val PARTICLE_HEIGHT_RANGE_DP = 8f // 纸片高度随机范围 (dp)
    const val ROTATION_SPEED_RANGE = 720f // 纸片旋转角速度浮动跨度 (度/秒)
}

/**
 * 礼花纸屑粒子数据模型
 */
private class AuraConfettiParticle(
    val x0Ratio: Float,       // 初始 X 相对画框宽度的比例
    val y0Ratio: Float,       // 初始 Y 相对画框高度的比例
    val vxRatio: Float,       // X 轴初速度 (相对画框宽度的每秒位移比例)
    val vyRatio: Float,       // Y 轴初速度 (相对画框高度的每秒位移比例，负数代表向上喷射)
    val color: Color,         // 纸屑颜色
    val widthDp: Float,       // 纸片宽度
    val heightDp: Float,      // 纸片高度
    val initialRotation: Float,// 初始旋转角
    val rotationSpeed: Float, // 旋转角速度
    val isCircle: Boolean     // 是否圆形纸屑
)

/**
 * 顶部 200.dp 视觉画板主组件 (纯 Compose 动画 + 高性能轻量级 Canvas 粒子系统)
 */
@Composable
private fun AuraTopHeroCanvas(
    hasPlayedEntranceAnimation: Boolean = false,
    onEntranceAnimationPlayed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isDarkTheme = LocalIsDarkTheme.current

    // 1. 动效控制器 (若已播放过开场动效，直接固定为最终稳态，避免列表回滑时二次触发)
    val confettiProgress = remember { Animatable(if (hasPlayedEntranceAnimation) 1f else 0f) }
    val iconAlpha = remember { Animatable(if (hasPlayedEntranceAnimation) 1f else 0f) }
    val iconScale = remember { Animatable(if (hasPlayedEntranceAnimation) 1f else AuraHeroConfig.ICON_INITIAL_SCALE) }

    // 打字机状态
    val fullText = AuraHeroConfig.FULL_TEXT
    var displayedCharCount by remember { mutableIntStateOf(if (hasPlayedEntranceAnimation) fullText.length else 0) }
    var isTypingDone by remember { mutableStateOf(hasPlayedEntranceAnimation) }

    // 终端光标：在打字结束后闪烁有限轮次后休眠，彻底避免无限重组/CPU 持续唤醒
    val cursorAlpha = remember { Animatable(if (hasPlayedEntranceAnimation) 0f else 1f) }

    // 2. 页面进入时自启动全套开箱动效 (受 hasPlayedEntranceAnimation 跨滑动门控保护)
    LaunchedEffect(Unit) {
        if (!hasPlayedEntranceAnimation) {
            onEntranceAnimationPlayed()

            // A. 礼花喷射动画 (持续约 2.4 秒，放完即止，不循环)
            launch {
                confettiProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = AuraHeroConfig.CONFETTI_DURATION_MS, easing = LinearEasing)
                )
            }

            // B. APP 图标弹簧爆发式入场 (透明度渐显 + Spring 弹簧缩放 0.5 -> 1.0)
            launch {
                iconAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = AuraHeroConfig.ICON_ENTRANCE_MS, easing = FastOutSlowInEasing)
                )
            }
            launch {
                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // C. 极客打字机逐字输出 (等待图标弹起稍作沉淀后，以约 90ms 间隔逐字敲出)
            delay(AuraHeroConfig.TYPING_START_DELAY_MS)
            for (i in 1..fullText.length) {
                displayedCharCount = i
                delay(AuraHeroConfig.TYPING_CHAR_INTERVAL_MS)
            }
            isTypingDone = true

            // D. 光标闪烁收敛：打字完成后翻转闪烁若干轮次，随后平滑淡出休眠，消除常驻耗电
            repeat(AuraHeroConfig.CURSOR_BLINK_CYCLES) {
                cursorAlpha.animateTo(0f, tween(durationMillis = AuraHeroConfig.CURSOR_BLINK_HALF_CYCLE_MS, easing = LinearEasing))
                cursorAlpha.animateTo(1f, tween(durationMillis = AuraHeroConfig.CURSOR_BLINK_HALF_CYCLE_MS, easing = LinearEasing))
            }
            cursorAlpha.animateTo(0f, tween(durationMillis = AuraHeroConfig.CURSOR_DORMANT_FADE_DURATION_MS))
        }
    }

    // 3. 预先随机生成的粒子池 (左右双侧对角喷射，共 64 颗高明度五彩纸屑)
    val particles = remember {
        val palette = listOf(
            Color(0xFFFF3366), // 鲜亮粉红
            Color(0xFFFF9900), // 活力暖橙
            Color(0xFFFFCC00), // 柠檬明黄
            Color(0xFF10B981), // 极客翠绿
            Color(0xFF00BFFF), // 晴空蔚蓝
            Color(0xFF6366F1), // 经典靛蓝
            Color(0xFF8B5CF6), // 梦幻罗兰紫
            Color(0xFFFF69B4)  // 樱花绚丽粉
        )
        val list = mutableListOf<AuraConfettiParticle>()
        val random = java.util.Random(AuraHeroConfig.RANDOM_SEED)

        // 左下侧炮筒 (向右上喷射)
        repeat(AuraHeroConfig.CONFETTI_CANON_PARTICLES) {
            val vx = AuraHeroConfig.VX_BASE + random.nextFloat() * AuraHeroConfig.VX_RANGE        // 向右
            val vy = -(AuraHeroConfig.VY_BASE + random.nextFloat() * AuraHeroConfig.VY_RANGE)     // 强烈向上
            list.add(
                AuraConfettiParticle(
                    x0Ratio = AuraHeroConfig.LEFT_CANON_X0_BASE + random.nextFloat() * AuraHeroConfig.LEFT_CANON_X0_RANGE,
                    y0Ratio = AuraHeroConfig.CANON_Y0_RATIO,
                    vxRatio = vx,
                    vyRatio = vy,
                    color = palette[it % palette.size],
                    widthDp = AuraHeroConfig.PARTICLE_WIDTH_BASE_DP + random.nextFloat() * AuraHeroConfig.PARTICLE_WIDTH_RANGE_DP,
                    heightDp = AuraHeroConfig.PARTICLE_HEIGHT_BASE_DP + random.nextFloat() * AuraHeroConfig.PARTICLE_HEIGHT_RANGE_DP,
                    initialRotation = random.nextFloat() * 360f,
                    rotationSpeed = (random.nextFloat() - 0.5f) * AuraHeroConfig.ROTATION_SPEED_RANGE,
                    isCircle = it % 4 == 0
                )
            )
        }

        // 右下侧炮筒 (向左上喷射)
        repeat(AuraHeroConfig.CONFETTI_CANON_PARTICLES) {
            val vx = -(AuraHeroConfig.VX_BASE + random.nextFloat() * AuraHeroConfig.VX_RANGE)     // 向左
            val vy = -(AuraHeroConfig.VY_BASE + random.nextFloat() * AuraHeroConfig.VY_RANGE)     // 强烈向上
            list.add(
                AuraConfettiParticle(
                    x0Ratio = AuraHeroConfig.RIGHT_CANON_X0_BASE - random.nextFloat() * AuraHeroConfig.RIGHT_CANON_X0_RANGE,
                    y0Ratio = AuraHeroConfig.CANON_Y0_RATIO,
                    vxRatio = vx,
                    vyRatio = vy,
                    color = palette[(it + 3) % palette.size],
                    widthDp = AuraHeroConfig.PARTICLE_WIDTH_BASE_DP + random.nextFloat() * AuraHeroConfig.PARTICLE_WIDTH_RANGE_DP,
                    heightDp = AuraHeroConfig.PARTICLE_HEIGHT_BASE_DP + random.nextFloat() * AuraHeroConfig.PARTICLE_HEIGHT_RANGE_DP,
                    initialRotation = random.nextFloat() * 360f,
                    rotationSpeed = (random.nextFloat() - 0.5f) * AuraHeroConfig.ROTATION_SPEED_RANGE,
                    isCircle = it % 4 == 0
                )
            )
        }
        list
    }

    // 4. 画板容器：横向全屏打通 (fillMaxWidth)，彻底拆除 clipToBounds 与边框限制，粒子自由飞越全屏边界
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AuraHeroConfig.HERO_CONTAINER_HEIGHT_DP.dp),
        contentAlignment = Alignment.Center
    ) {
        // 层级 1: 居中内容区域 (App 图标 + 打字机终端文案)
        // 🌟 核心保护：单独为内部 UI 容器施加 statusBarsPadding，实现“烟花特效全屏穿透，图标文案安全避让”
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // A. Aura 官方应用图标 (App Store 级高定 squircle 画框，微调至 96.dp 精致黄金比例，支持点击重喷礼花)
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                        alpha = iconAlpha.value
                    }
                    .size(AuraHeroConfig.ICON_CARD_SIZE_DP.dp)
                    .clip(RoundedCornerShape(AuraHeroConfig.ICON_CORNER_RADIUS_DP.dp))
                    .background(if (isDarkTheme) Color(0xFF1E1E1E) else Color.White)
                    .border(
                        1.dp,
                        if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0),
                        RoundedCornerShape(AuraHeroConfig.ICON_CORNER_RADIUS_DP.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            launch {
                                iconScale.snapTo(AuraHeroConfig.ICON_CLICK_SCALE)
                                iconScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                confettiProgress.snapTo(0f)
                                confettiProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = AuraHeroConfig.CONFETTI_DURATION_MS, easing = LinearEasing)
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_aura_logo),
                    contentDescription = "Aura App Icon",
                    modifier = Modifier.size(AuraHeroConfig.ICON_LOGO_SIZE_DP.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // B. 极客打字机单行签名 + 终端光标
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = fullText.take(displayedCharCount),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569),
                    letterSpacing = 0.3.sp
                )
                Text(
                    text = "_",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = (if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)).copy(
                        alpha = if (!isTypingDone) 1f else cursorAlpha.value
                    )
                )
            }
        }

        // 层级 2 (最顶层): 礼花粒子 Canvas (位于最顶层，无 clipToBounds 限制，自由飞向全屏左右最边缘)
        if (confettiProgress.value < 1f) {
            val progress = confettiProgress.value
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            ) {
                val canvasW = size.width
                val canvasH = size.height
                val timeSec = progress * AuraHeroConfig.CONFETTI_TOTAL_TIME_SEC
                val gravityAcc = AuraHeroConfig.GRAVITY_ACCELERATION

                // 淡出 Alpha：在 1.4s ~ 2.4s 逐渐消散
                val globalAlpha = ((AuraHeroConfig.CONFETTI_TOTAL_TIME_SEC - timeSec) / AuraHeroConfig.FADE_OUT_DURATION_SEC).coerceIn(0f, 1f)

                particles.forEach { p ->
                    // 物理抛物线坐标计算
                    val curX = (p.x0Ratio + p.vxRatio * timeSec * (1f - AuraHeroConfig.DRAG_COEFFICIENT * timeSec)) * canvasW
                    val curY = (p.y0Ratio + p.vyRatio * timeSec + 0.5f * gravityAcc * timeSec * timeSec) * canvasH
                    val curRot = p.initialRotation + p.rotationSpeed * timeSec
                    val particleAlpha = globalAlpha * (if (timeSec < AuraHeroConfig.FADE_IN_THRESHOLD_SEC) timeSec / AuraHeroConfig.FADE_IN_THRESHOLD_SEC else 1f)

                    if (particleAlpha > 0.01f && curX in -AuraHeroConfig.CANVAS_BOUNDS_BUFFER_PX..(canvasW + AuraHeroConfig.CANVAS_BOUNDS_BUFFER_PX) && curY in -AuraHeroConfig.CANVAS_BOUNDS_BUFFER_PX..(canvasH + AuraHeroConfig.CANVAS_BOUNDS_BUFFER_PX)) {
                        val particleColor = p.color.copy(alpha = particleAlpha)
                        val wPx = p.widthDp * density
                        val hPx = p.heightDp * density

                        if (p.isCircle) {
                            drawCircle(
                                color = particleColor,
                                radius = wPx / 2f,
                                center = Offset(curX, curY)
                            )
                        } else {
                            withTransform({
                                rotate(degrees = curRot, pivot = Offset(curX, curY))
                            }) {
                                drawRect(
                                    color = particleColor,
                                    topLeft = Offset(curX - wPx / 2f, curY - hPx / 2f),
                                    size = Size(wPx, hPx)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🌟 二级全屏页面路由宿主容器 (AuraSubScreenHost)
 * 调度各独立专属二级与三级 Composable 页面；彻底清理早期占位脚手架死代码
 */
@Composable
private fun AuraSubScreenHost(
    subScreenId: String,
    currentCount: Int,
    onCountChange: (Int) -> Unit,
    themeMode: AppThemeMode = AppThemeMode.MODE_LIGHT,
    onThemeChange: (AppThemeMode) -> Unit = {},
    isBiometricLockEnabled: Boolean,
    lockTimeoutMinutes: Int,
    onToggleBiometricLock: (Boolean) -> Unit,
    onTimeoutChange: (Int) -> Unit,
    morningColor: Color,
    noonColor: Color,
    afternoonColor: Color,
    nightColor: Color,
    onSaveColors: (Color, Color, Color, Color) -> Unit,
    onNavigateSubScreen: (String) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (subScreenId) {
        "app_theme" -> {
            // 🎨 完整接入的应用主题切换面板
            AuraThemeScreen(
                currentThemeMode = themeMode,
                onThemeSelected = onThemeChange,
                onBack = onBack,
                modifier = modifier
            )
        }
        "home_palette" -> {
            // 🎨 完整接入的主页背景调色真实功能面板
            AuraHomePaletteSubScreen(
                morningColor = morningColor,
                noonColor = noonColor,
                afternoonColor = afternoonColor,
                nightColor = nightColor,
                onSaveColors = onSaveColors,
                onBack = onBack,
                modifier = modifier
            )
        }
        "swipe_count" -> {
            // 📱 完整接入的划动选择图片数量真实功能面板
            AuraSwipeCountSubScreen(
                currentCount = currentCount,
                onCountChange = onCountChange,
                onBack = onBack,
                modifier = modifier
            )
        }
        "app_lock" -> {
            // 🔒 完整接入的应用锁定安全控制面板
            AuraAppLockSubScreen(
                isBiometricEnabled = isBiometricLockEnabled,
                currentTimeout = lockTimeoutMinutes,
                onToggleBiometric = onToggleBiometricLock,
                onTimeoutChange = onTimeoutChange,
                onBack = onBack,
                modifier = modifier
            )
        }
        "cache_clean" -> {
            // 🧹 完整接入的清理图片缓存真实功能面板
            AuraCacheCleanSubScreen(
                onBack = onBack,
                modifier = modifier
            )
        }
        "qr_share" -> {
            // 📱 完整接入的分享二维码专属全屏面板
            AuraQRCodeSubScreen(
                onBack = onBack,
                modifier = modifier
            )
        }
        "check_update" -> {
            // 🚀 完整接入的检查更新专属全屏面板
            AuraUpdateSubScreen(
                onBack = onBack,
                modifier = modifier
            )
        }
        "lab_reserve" -> {
            // 🧪 完整接入的实验性功能全屏面板 (二级面板)
            AuraExperimentalSubScreen(
                onNavigateToPerformance = { onNavigateSubScreen("performance") },
                onBack = onBack,
                modifier = modifier
            )
        }
        "performance" -> {
            // ⚡ 完整接入的设备性能实时看板 (三级面板)
            PerformanceScreen(
                onBack = onBack,
                modifier = modifier
            )
        }
        else -> Unit
    }
}

/**
 * 划动选数快速预设值 (纯数字列表，消除每次重组时的重复对象与死字符串分配)
 */
private val PHOTO_COUNT_QUICK_PRESETS = listOf(3, 5, 7, MAX_FRAME_COUNT)

/**
 * 📱 划动选择图片数量二级全屏面板：包含刻度滚轮选择器、物理吸附居中、马达振动、灵感快捷选数与实况矩阵预览
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AuraSwipeCountSubScreen(
    currentCount: Int,
    onCountChange: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (currentCount - 1).coerceIn(0, MAX_FRAME_COUNT - 1)
    )

    // 精准判定滚动时物理坐标的中心点，选中中间的数字
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf -1

            val viewportCenter =
                layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            var closestIndex = -1
            var minDistance = Int.MAX_VALUE

            for (i in visibleItems.indices) {
                val item = visibleItems[i]
                val itemCenter = item.offset + item.size / 2
                val distance = kotlin.math.abs(itemCenter - viewportCenter)
                if (distance < minDistance) {
                    minDistance = distance
                    closestIndex = item.index
                }
            }
            closestIndex
        }
    }

    // 滚动过程中马达机械震动反馈与实时通知主层状态保存
    var lastVibratedIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(centerIndex) {
        if (centerIndex != -1 && centerIndex != lastVibratedIndex) {
            lastVibratedIndex = centerIndex
            val newCount = centerIndex + 1
            if (newCount in 1..MAX_FRAME_COUNT && newCount != currentCount) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCountChange(newCount)
            }
        }
    }

    val activeCount = if (centerIndex != -1) (centerIndex + 1) else currentCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部返回与状态栏 (沉浸式下推安全区：极简留白，左右两端对齐)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 返回按钮
            AuraCapsuleBackButton(
                onBack = {
                    if (activeCount in 1..MAX_FRAME_COUNT && activeCount != currentCount) {
                        onCountChange(activeCount)
                    }
                    onBack()
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 2. 核心 Hero 滚轮选择器大卡片 (1:1 V2 设计语言)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 24.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "当前展示",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$activeCount",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "张",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 原生物理磁吸引擎滚轮容器
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF8FAFC).copy(alpha = 0.75f))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // 中心选区标尺微光槽
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Color(0xFFEFF6FF).copy(alpha = 0.85f))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                        contentPadding = PaddingValues(vertical = 45.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
                    ) {
                        items(
                            count = MAX_FRAME_COUNT,
                            contentType = { "wheel_item" }
                        ) { index ->
                            val number = index + 1
                            val isSelected = index == centerIndex
                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$number",
                                    fontSize = if (isSelected) 28.sp else 16.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF2563EB) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. 灵感快捷预设按钮组
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "快速预设",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PHOTO_COUNT_QUICK_PRESETS.forEach { count ->
                        val isCurrent = count == activeCount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isCurrent) Color(0xFF2563EB) else Color(0xFFF1F5F9)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem((count - 1).coerceIn(0, MAX_FRAME_COUNT - 1))
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${count}张",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. 实况矩阵预览卡片 (实时展现主页图库分布)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "图框分布矩阵",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "已启用 $activeCount / $MAX_FRAME_COUNT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2563EB)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 🌟 图框分布矩阵：从 1 到 MAX_FRAME_COUNT 槽位根据 activeCount 动态点亮（已激活高亮蓝色，未激活低透明度浅灰）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(MAX_FRAME_COUNT) { idx ->
                        val isActivated = idx < activeCount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isActivated) Color(0xFF3B82F6) else Color(0xFFE2E8F0).copy(alpha = 0.6f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActivated) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🎨 主页背景调色二级全屏面板：包含 4 个时段调色、全天色彩光晕分布条、三级专业调色盘及实时持久化保存
 */
@Composable
private fun AuraHomePaletteSubScreen(
    morningColor: Color,
    noonColor: Color,
    afternoonColor: Color,
    nightColor: Color,
    onSaveColors: (Color, Color, Color, Color) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMorning by remember(morningColor) { mutableStateOf(morningColor) }
    var currentNoon by remember(noonColor) { mutableStateOf(noonColor) }
    var currentAfternoon by remember(afternoonColor) { mutableStateOf(afternoonColor) }
    var currentNight by remember(nightColor) { mutableStateOf(nightColor) }

    var editingPeriod by remember { mutableStateOf<TimePeriod?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部返回与标题栏 (沉浸式下推安全区：极致极简留白，左右两端对齐)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 返回按钮 (极致极简留白：移除中间标题，左右两端对齐)
            AuraCapsuleBackButton(onBack = onBack)

            // 恢复默认按钮 (点击触发二次确认拦截弹窗，防止误触)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEE2E2).copy(alpha = 0.85f))
                    .clickable {
                        showResetConfirmDialog = true
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "恢复默认",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFDC2626)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 2. 4 个时段调色卡片容器 (1:1 对齐 V2 分组卡片风格)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                val periods = listOf(
                    Triple(TimePeriod.MORNING, "早上好", currentMorning to "07:00 - 11:59"),
                    Triple(TimePeriod.NOON, "中午好", currentNoon to "12:00 - 13:59"),
                    Triple(TimePeriod.AFTERNOON, "下午好", currentAfternoon to "14:00 - 19:59"),
                    Triple(TimePeriod.NIGHT, "晚上好", currentNight to "20:00 - 06:59")
                )

                periods.forEachIndexed { index, (period, title, colorAndTime) ->
                    PaletteTimePeriodRow(
                        title = title,
                        timeRange = colorAndTime.second,
                        color = colorAndTime.first,
                        onClick = { editingPeriod = period }
                    )

                    if (index < periods.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                            thickness = 0.5.dp,
                            color = Color(0xFFE2E8F0).copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. 实时背景色彩预览卡片
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "全天时段色谱分布",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                0.0f to currentMorning,
                                (12f / 24f) to currentNoon,       // 12:00 中午色标分界点 (12/24 = 50%)
                                (14f / 24f) to currentAfternoon,  // 14:00 下午色标分界点 (14/24 ≈ 58.3%)
                                (20f / 24f) to currentNight,      // 20:00 晚上色标分界点 (20/24 ≈ 83.3%)
                                1.0f to currentNight
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                )
            }
        }
    }

    // 🌟 三级专业调色盘弹窗挂载
    editingPeriod?.let { period ->
        val initialColor = when (period) {
            TimePeriod.MORNING -> currentMorning
            TimePeriod.NOON -> currentNoon
            TimePeriod.AFTERNOON -> currentAfternoon
            TimePeriod.NIGHT -> currentNight
        }
        val periodName = when (period) {
            TimePeriod.MORNING -> "早上好"
            TimePeriod.NOON -> "中午好"
            TimePeriod.AFTERNOON -> "下午好"
            TimePeriod.NIGHT -> "晚上好"
        }

        AuraColorPickerDialog(
            title = "「$periodName」",
            initialColor = initialColor,
            onColorSelected = { selectedColor ->
                var newMorning = currentMorning
                var newNoon = currentNoon
                var newAfternoon = currentAfternoon
                var newNight = currentNight

                when (period) {
                    TimePeriod.MORNING -> {
                        currentMorning = selectedColor
                        newMorning = selectedColor
                    }
                    TimePeriod.NOON -> {
                        currentNoon = selectedColor
                        newNoon = selectedColor
                    }
                    TimePeriod.AFTERNOON -> {
                        currentAfternoon = selectedColor
                        newAfternoon = selectedColor
                    }
                    TimePeriod.NIGHT -> {
                        currentNight = selectedColor
                        newNight = selectedColor
                    }
                }
                // 立即持久化并实时更新全局背景
                onSaveColors(newMorning, newNoon, newAfternoon, newNight)
                editingPeriod = null
                Toast.makeText(context, "「$periodName」色彩已生效", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { editingPeriod = null }
        )
    }

    // 🌟 恢复默认色彩二次确认弹窗 (防误触保护)
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = "恢复默认色彩",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.5.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "是否将清晨、中午、下午、傍晚所有时段背景光晕恢复为初始默认色彩？",
                    fontSize = 13.5.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentMorning = DEFAULT_COLOR_MORNING
                        currentNoon = DEFAULT_COLOR_NOON
                        currentAfternoon = DEFAULT_COLOR_AFTERNOON
                        currentNight = DEFAULT_COLOR_NIGHT
                        onSaveColors(DEFAULT_COLOR_MORNING, DEFAULT_COLOR_NOON, DEFAULT_COLOR_AFTERNOON, DEFAULT_COLOR_NIGHT)
                        showResetConfirmDialog = false
                        Toast.makeText(context, "已恢复默认背景色彩", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "确认恢复",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmDialog = false }
                ) {
                    Text(
                        text = "取消",
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

/**
 * 🌟 时段调色单行 Item
 */
@Composable
private fun PaletteTimePeriodRow(
    title: String,
    timeRange: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeRange,
                fontSize = 11.5.sp,
                color = Color(0xFF64748B)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // 色彩指示球 (双层描边高级质感)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, Color.White, CircleShape)
                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Edit Color",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// =========================================================================================
// 🎨 [专业 HSV 时态色彩调节器与极简滑块：AuraColorPickerDialog]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 19.0 独创的 HSV 空间光晕色彩调节弹窗与极简纤细滑块（AuraSlimSlider）系统。
// 2. 它能干什么？
//    - 将传入颜色转换为 HSV（色相 0-360°、饱和度 5-100%、柔和度/Alpha 15-85%）；
//    - 顶部实时呈现渐变长条预览与 Monospace 等宽十六进制色值（#AARRGGBB）；
//    - 提供“快速预设灵感”色彩圆珠一键选用；
//    - 点击【确定】以深邃曜石黑胶囊回传并保存至 DataStore。
// 3. 怎么调整参数？
//    - 预设色板：修改 PRESET_AURA_COLORS 列表中的 Color 定义。
//    - 滑块范围：修改 AuraSlimSlider 的 valueRange（如柔和度 0.15f..0.85f）。
//    - 弹窗外框：修改 RoundedCornerShape(22.dp) 与宽度 width(320.dp)。
// =========================================================================================

/**
 * 🎨 Aura 快速预设候选色彩常量集 (避免每次弹窗重新分配集合)
 */
private val PRESET_AURA_COLORS = listOf(
    Color(0xFFFFCC80), // 晨曦暖橘
    Color(0xFF80DEEA), // 清透浅青
    Color(0xFF8AB4F8), // 天空明蓝
    Color(0xFFB39DDB), // 梦幻星空紫
    Color(0xFFA7F3D0), // 极光薄荷绿
    Color(0xFFF8BBD0), // 樱花柔粉
    Color(0xFFFFAB91), // 珊瑚落日橘
    Color(0xFFE1BEE7)  // 丁香淡紫
)

/**
 * 🎨 Aura 专业色相/饱和度/光晕调节器弹窗
 */
@Composable
fun AuraColorPickerDialog(
    title: String,
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val safeInitialColor = if (initialColor.isSpecified) initialColor else DEFAULT_COLOR_MORNING
    val initialHsv = remember(safeInitialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(safeInitialColor.toArgb(), hsv)
        hsv
    }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    // 💡 明度下限固定约束为 0.7f，防止用户调出纯黑暗沉死色，保证背景光晕始终通透柔和
    var value by remember { mutableFloatStateOf(initialHsv[2].coerceAtLeast(0.7f)) }
    var alpha by remember { mutableFloatStateOf(safeInitialColor.alpha.coerceIn(0.15f, 0.85f)) }

    // 🌟 复用定长 FloatArray，彻底消除滑动拖拽高频计算时的内存对象分配 (零 GC 压力)
    val hsvArray = remember { FloatArray(3) }
    val currentColor = remember(hue, saturation, value, alpha) {
        hsvArray[0] = hue
        hsvArray[1] = saturation
        hsvArray[2] = value
        val alphaInt = (alpha * 255).roundToInt().coerceIn(0, 255)
        val colorInt = android.graphics.Color.HSVToColor(alphaInt, hsvArray)
        Color(colorInt)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.width(320.dp), // 💡 调整此处更改色彩调节弹窗宽度（默认 320.dp）
        shape = RoundedCornerShape(22.dp), // 💡 调整此处更改色彩调节弹窗外框圆角（默认 22.dp）
        containerColor = Color.White,      // 💡 调整此处更改色彩调节弹窗卡片背景色
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF0F172A)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. 实时色彩效果预览条 (恢复大色块背景填充样式)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp) // 💡 调整此处更改渐变色彩实时预览条高度（默认 52.dp）
                        .clip(RoundedCornerShape(12.dp)) // 💡 调整此处更改色彩预览条圆角（默认 12.dp）
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.White, currentColor)
                            )
                        )
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "#%08X", currentColor.toArgb()),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1F1F1F)
                    )
                }

                // 2. 快速预设灵感 (紧凑精致排版)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "快速预设灵感",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PRESET_AURA_COLORS.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp) // 💡 调整此处更改预设色彩圆珠直径（默认 24.dp）
                                    .clip(CircleShape)
                                    .background(preset)
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .border(0.5.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
                                    .clickable {
                                        android.graphics.Color.colorToHSV(preset.toArgb(), hsvArray)
                                        hue = hsvArray[0]
                                        saturation = hsvArray[1]
                                        value = hsvArray[2].coerceAtLeast(0.7f)
                                    }
                            )
                        }
                    }
                }

                // 3. 滑动条控制项 (纤细轨道、纯黑滑块与高定黑白对比)
                AuraSlimSlider(
                    label = "色相",
                    valueText = "${hue.roundToInt()}°",
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f // 💡 调整此处更改色相取值范围（0° - 360° 全色域）
                )

                AuraSlimSlider(
                    label = "色彩饱和度",
                    valueText = "${(saturation * 100).roundToInt()}%",
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0.05f..1f // 💡 调整此处更改饱和度范围（最小保留 5% 避免纯灰死白）
                )

                AuraSlimSlider(
                    label = "光晕柔和度",
                    valueText = "${(alpha * 100).roundToInt()}%",
                    value = alpha,
                    onValueChange = { alpha = it },
                    valueRange = 0.15f..0.85f // 💡 调整此处更改 Alpha 柔和度范围（0.15 - 0.85 保证光晕通透不泛白）
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorSelected(currentColor) },
                shape = RoundedCornerShape(12.dp), // 💡 调整此处更改确定按钮圆角（默认 12.dp）
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F172A), // 💡 调整此处更改确认按钮背景色（当前：曜石深灰黑 #0F172A）
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .defaultMinSize(minWidth = 100.dp) // 💡 调整此处更改确认按钮最小宽度（默认 100.dp）
                    .height(40.dp), // 💡 调整此处更改确认按钮高度（默认 40.dp）
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "确定",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(40.dp) // 💡 调整此处更改取消按钮高度（默认 40.dp 与确定对齐）
            ) {
                Text(
                    text = "取消",
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    )
}

/**
 * 🎚️ Aura 极简纤细滑动条组件 (黑白高定风格)
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AuraSlimSlider(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Text(
                text = valueText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF0F172A)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp), // 💡 调整此处更改纤细滑动条整体触控高度（默认 28.dp，过低会影响触摸面积）
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF0F172A),
                activeTrackColor = Color(0xFF0F172A),
                inactiveTrackColor = Color(0xFFE2E8F0)
            ),
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { MutableInteractionSource() },
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF0F172A)),
                    thumbSize = DpSize(14.dp, 14.dp) // 💡 调整此处更改滑动条圆点 Thumb 直径（默认 14.dp 极简小巧风格）
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(3.5.dp), // 💡 调整此处更改滑块轨道 Track 粗细（默认 3.5.dp 极细轨道，彰显极客审美）
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF0F172A),
                        activeTrackColor = Color(0xFF0F172A),
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    )
                )
            }
        )
    }
}

// =========================================================================================
// 🔒 [二级全屏面板：应用锁定与生物识别安全中心 - AuraAppLockSubScreen]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 19.0 的硬件级应用安全锁配置全屏二级页面。
// 2. 它能干什么？
//    - 控制应用私密锁总开关（调用系统 BiometricPrompt 校验合法性后开启/关闭）；
//    - 提供“离开后立即锁定”、“5 分钟”、“15 分钟”等阶梯超时自动锁定时长选项；
//    - 详细展示生物密钥在系统安全飞地（TEE/SE）中的防护原理说明，让用户清晰感知安全边界。
// 3. 怎么调整参数？
//    - 超时预设：修改 APP_LOCK_TIMEOUT_OPTIONS 列表中的分钟阶梯（0代表立即锁定）。
//    - 开关外观与卡片圆角：修改 RoundedCornerShape(16.dp) 与 SwitchDefaults.colors。
// =========================================================================================

/**
 * 隐私应用锁自动锁定时长选项数据模型与顶层静态预设列表
 */
private data class AuraLockTimeoutOption(
    val minutes: Int,
    val title: String,
    val description: String
)

private val APP_LOCK_TIMEOUT_OPTIONS = listOf(
    AuraLockTimeoutOption(0, "立即", "离开 立即锁定"),
    AuraLockTimeoutOption(5, "5 分钟后", "离开 5 分钟后"),
    AuraLockTimeoutOption(15, "15 分钟后", "离开 15 分钟后")
)

@Composable
private fun AuraAppLockSubScreen(
    isBiometricEnabled: Boolean,
    currentTimeout: Int,
    onToggleBiometric: (Boolean) -> Unit,
    onTimeoutChange: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部返回栏与状态指示徽章 (沉浸式下推安全区)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraCapsuleBackButton(onBack = onBack)
        }

        // 2. 主开关大卡片 (统一 16.dp 圆角，纯净微透父容器统一渲染)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "生物识别",
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBiometricEnabled) "启动应用/返回前台时验证身份" else "使用设备指纹/面容/密码",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { targetState ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleBiometric(targetState)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFFFFF),
                        checkedTrackColor = Color(0xFF05ACF7),
                        uncheckedThumbColor = Color(0x5DCBC9C9),
                        uncheckedTrackColor = Color(0x4CFFFFFF)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 自动锁定时间卡片 (由 isBiometricEnabled 动态控制交互与透明度)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isBiometricEnabled) 1f else 0.42f)
        ) {
            Text(
                text = "自动锁定时间",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.88f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    APP_LOCK_TIMEOUT_OPTIONS.forEachIndexed { index, option ->
                        val isSelected = (currentTimeout == option.minutes)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isBiometricEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onTimeoutChange(option.minutes)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = option.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                enabled = isBiometricEnabled,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onTimeoutChange(option.minutes)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF00B3FF),
                                    unselectedColor = Color(0x5DCBC9C9)
                                )
                            )
                        }

                        if (index < APP_LOCK_TIMEOUT_OPTIONS.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = Color(0xFFE2E8F0).copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================================
// 🧹 [二级全屏面板：清理图片缓存]
// 1:1 承载图片离线缓存实时扫描、一键极速深度释放及状态响应式刷新
// =========================================================================================

private const val CACHE_EFFECTIVE_THRESHOLD_BYTES = 1024L * 10L // 10KB 统一有效缓存阈值 (低于此值视为空整洁态，防止 Linux 目录项 4KB 导致按钮无法沉睡)
private const val APK_EXTENSION = ".apk"

private fun calculateAppCacheSizeBytes(context: Context): Long {
    fun getDirectorySize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            total += if (file.isDirectory) {
                getDirectorySize(file)
            } else {
                file.length()
            }
        }
        return total
    }

    var totalBytes = 0L
    try {
        totalBytes += getDirectorySize(context.cacheDir)
        totalBytes += getDirectorySize(context.externalCacheDir)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return totalBytes
}

private fun formatCacheSize(bytes: Long): Pair<String, String> {
    if (bytes <= CACHE_EFFECTIVE_THRESHOLD_BYTES) return "0" to "MB" // 小于 10KB 视为完全清空
    val mb = bytes / (1024.0 * 1024.0)
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f", gb) to "GB"
        else -> String.format(Locale.US, "%.2f", mb) to "MB"
    }
}

/**
 * 🌟 扫描下载目录中的所有 .apk 安装包文件大小，并返回 (字节大小 to MB)
 */
private fun calculateApkCacheSize(context: Context): Pair<Long, Float> {
    var totalBytes = 0L
    try {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (downloadDir != null && downloadDir.exists()) {
            downloadDir.listFiles { file -> file.isFile && file.name.endsWith(APK_EXTENSION, ignoreCase = true) }
                ?.forEach { apk ->
                    totalBytes += apk.length()
                }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val mb = (totalBytes.toDouble() / (1024.0 * 1024.0)).toFloat()
    return totalBytes to mb
}

/**
 * 🌟 一键粉碎删除下载目录下的所有 .apk 安装包文件
 */
private fun deleteApkFiles(context: Context) {
    try {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (downloadDir != null && downloadDir.exists()) {
            downloadDir.listFiles { file -> file.isFile && file.name.endsWith(APK_EXTENSION, ignoreCase = true) }
                ?.forEach { apk ->
                    try {
                        apk.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(coil.annotation.ExperimentalCoilApi::class)
@Composable
private fun AuraCacheCleanSubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var cacheSizeBytes by remember { mutableLongStateOf(0L) }
    var apkSizeBytes by remember { mutableLongStateOf(0L) }
    var apkSizeMB by remember { mutableFloatStateOf(0f) }
    var isApkSelected by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isCleaning by remember { mutableStateOf(false) }

    // 初始异步扫描本地图片及临时缓存大小，并同步扫描 APK 安装包大小
    LaunchedEffect(Unit) {
        isLoading = true
        withContext(Dispatchers.IO) {
            cacheSizeBytes = calculateAppCacheSizeBytes(context)
            val (bytes, mb) = calculateApkCacheSize(context)
            apkSizeBytes = bytes
            apkSizeMB = mb
            isApkSelected = bytes > 0L // 若发现残留安装包则默认勾选
        }
        isLoading = false
    }

    // 🌟 核心响应式联动：总大小 = 图片缓存大小 + (若 isApkSelected 为 true 则加上安装包大小)
    val totalEffectiveBytes = remember(cacheSizeBytes, apkSizeBytes, isApkSelected) {
        if (isApkSelected) {
            cacheSizeBytes + apkSizeBytes
        } else {
            cacheSizeBytes
        }
    }

    val hasEffectiveCache = totalEffectiveBytes > CACHE_EFFECTIVE_THRESHOLD_BYTES

    val (sizeValue, sizeUnit) = remember(totalEffectiveBytes, isLoading) {
        if (isLoading) {
            "..." to ""
        } else {
            formatCacheSize(totalEffectiveBytes)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部返回栏 (沉浸式下推安全区：极简留白，移除右上角存储空间管家标签)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraCapsuleBackButton(onBack = onBack)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 2. 中央 Hero 卡片：大尺寸展示当前缓存大小与状态 (极简瘦身版)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 26.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 核心数字高亮显示
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = sizeValue,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    if (sizeUnit.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sizeUnit,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (sizeValue == "0") "已清理完成" else "包含图片缓存与临时数据",
                    fontSize = 12.sp,
                    color = if (sizeValue == "0") Color(0xFF10B981) else Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 一键清理核心大按钮 (智能沉睡与唤醒：有缓存高亮可点，无缓存变暗进入已清理状态)
        val isButtonEnabled = !isCleaning && !isLoading && hasEffectiveCache
        Button(
            onClick = {
                if (!isCleaning && hasEffectiveCache) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        isCleaning = true
                        withContext(Dispatchers.IO) {
                            try {
                                context.imageLoader.diskCache?.clear()
                                context.imageLoader.memoryCache?.clear()

                                val clearDirectory: (File?) -> Unit = { dir ->
                                    dir?.listFiles()?.forEach { file ->
                                        try {
                                            file.deleteRecursively()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                clearDirectory(context.cacheDir)
                                clearDirectory(context.externalCacheDir)

                                // 2. 若勾选安装包，则一键粉碎下载目录下的所有 .apk 安装包
                                if (isApkSelected) {
                                    deleteApkFiles(context)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        cacheSizeBytes = withContext(Dispatchers.IO) {
                            calculateAppCacheSizeBytes(context)
                        }

                        // 3. 清理完成后，将 apkSizeMB 归零，并重置勾选状态
                        if (isApkSelected) {
                            apkSizeBytes = 0L
                            apkSizeMB = 0f
                            isApkSelected = false
                        } else {
                            val (bytes, mb) = calculateApkCacheSize(context)
                            apkSizeBytes = bytes
                            apkSizeMB = mb
                        }

                        isCleaning = false
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "缓存清理成功", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = isButtonEnabled,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF59E0B),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFF59E0B).copy(alpha = 0.45f),
                disabledContentColor = Color.White.copy(alpha = 0.8f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isCleaning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "正在深度清理中...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = if (hasEffectiveCache) "一键清理缓存" else "已是整洁状态",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. [新增] 更新安装包扫描清理卡片 (极简单行排版 + 动态明暗状态感应)
        val hasApk = apkSizeMB > 0f
        val cardAlpha by animateFloatAsState(
            targetValue = if (hasApk) 1f else 0.5f,
            animationSpec = tween(durationMillis = 250),
            label = "apkCardAlpha"
        )

        Surface(
            onClick = {
                if (hasApk) {
                    isApkSelected = !isApkSelected
                }
            },
            enabled = hasApk,
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.95f)),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(cardAlpha)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧安装包图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "APK Icon",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // 单行文字与体积信息
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "更新安装包",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                if (hasApk) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f MB", apkSizeMB),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasApk) Color(0xFFD97706) else Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }
                }

                // 右侧复选框 (绑定 isApkSelected)
                Checkbox(
                    checked = isApkSelected && hasApk,
                    onCheckedChange = { checked ->
                        if (hasApk) {
                            isApkSelected = checked
                        }
                    },
                    enabled = hasApk,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFF59E0B),
                        uncheckedColor = Color(0xFFCBD5E1),
                        checkmarkColor = Color.White
                    )
                )
            }
        }
    }
}

// =========================================================================================
// 📱 [二级全屏面板：分享二维码与双胶囊操作区 - AuraQRCodeSubScreen]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 19.0 的推广二维码沉浸式全屏展示与一键分享/保存子页面。
// 2. 它能干什么？
//    - 展示超高清晰度矢量/位图二维码卡片与 GitHub 下载链接快速复制；
//    - 底部提供双胶囊对称操作按钮：
//      * 左侧【系统分享】（OutlinedButton）：调用 Android 原生 Intent.ACTION_SEND 唤起系统应用分享弹窗；
//      * 右侧【保存到相册】（FilledButton）：通过 MediaStore 规范将二维码原图写入系统相册（Pictures/Aura）。
// 3. 怎么调整参数？
//    - 按钮顺序与样式：在底部的 Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) 中维护；
//    - 存储路径：修改 RELATIVE_PATH = Environment.DIRECTORY_PICTURES + "/Aura"。
//    - 按钮高度：修改 Modifier.height(48.dp) 与 RoundedCornerShape(14.dp)。
// =========================================================================================

private const val AURA_LATEST_RELEASE_URL = "https://github.com/Jeesemmy/Aura/releases/latest"
private val AURA_ELECTRIC_CYAN = Color(0xFF00E5FF) // Aura 荧光青标志主色
private val AURA_CYAN_PRIMARY = Color(0xFF06B6D4)  // Aura 电光青主色 (QR 二维码面板强调色)
private val AURA_CYAN_DARK = Color(0xFF0891B2)     // Aura 深青交互辅助色

private suspend fun saveQrCodeToGallery(context: Context) {
    withContext(Dispatchers.IO) {
        var insertedUri: Uri? = null
        val resolver = context.contentResolver
        try {
            // 1. 禁用密度自动缩放，锁定原始 1000x1000 像素精准解码 (内存占用硬核降至 ~3.8MB)
            val options = BitmapFactory.Options().apply {
                inScaled = false
            }
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_aura_download_qr, options)
            if (bitmap == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "无法读取二维码图片", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }

            val filename = "Aura_Download_QR_${System.currentTimeMillis()}.png"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Aura")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            insertedUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (insertedUri != null) {
                resolver.openOutputStream(insertedUri)?.use { outStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
                        throw java.io.IOException("Bitmap compression failed")
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(insertedUri, contentValues, null, null)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "已保存到相册！", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "保存失败，请检查存储权限！", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("AuraShare", "Save image failed", e)
            // 2. 异常回滚机制：清除由于未完成写入而在相册残留的 0 字节或损坏幽灵记录
            insertedUri?.let { uri ->
                try {
                    resolver.delete(uri, null, null)
                } catch (rollbackEx: Exception) {
                    Log.e("AuraShare", "Rollback failed", rollbackEx)
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "保存发生异常: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun shareAuraLink(context: Context) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "✨ 推荐一个App【Aura】\n" +
                        "🚀 最新发行版下载直达：\n" +
                        AURA_LATEST_RELEASE_URL
            )
            putExtra(Intent.EXTRA_SUBJECT, "Aura 应用")
        }
        val chooser = Intent.createChooser(shareIntent, "分享 Aura 给好友").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        Log.e("AuraShare", "Share link failed", e)
        Toast.makeText(context, "唤起系统分享失败", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun AuraQRCodeSubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val downloadUrl = AURA_LATEST_RELEASE_URL
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部返回栏 (沉浸式下推安全区：极简留白，移除右上角官方发行版标签)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraCapsuleBackButton(onBack = onBack)
        }

        // 2. 中央 Hero 卡片：高高清呈现二维码与下载链接 (极简留白：移除大标题及副标题)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)) // 💡 调整此处更改二维码中央 Hero 卡片圆角（默认 16.dp）
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 24.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 二维码纯白高对比度画布容器
                Box(
                    modifier = Modifier
                        .size(220.dp) // 💡 调整此处更改二维码纯白画布正方形边长（默认 220.dp）
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(14.dp), // 💡 调整此处更改二维码与外框内边距（默认 14.dp，留白保证扫码识别率）
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_aura_download_qr),
                        contentDescription = "Aura Download QR Code",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 下载链接展示与一键复制栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9).copy(alpha = 0.85f))
                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = downloadUrl,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            clipboardManager.setText(AnnotatedString(downloadUrl))
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                Toast.makeText(context, "链接已复制到剪贴板", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = AURA_CYAN_DARK,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 核心操作按钮组：系统分享 & 保存到相册
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp) // 💡 调整此处更改系统分享与保存到相册两按钮之间的横向间距（默认 12.dp）
        ) {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    shareAuraLink(context)
                },
                shape = RoundedCornerShape(14.dp), // 💡 调整此处更改系统分享按钮圆角（默认 14.dp）
                border = BorderStroke(1.dp, AURA_CYAN_PRIMARY), // 💡 调整此处更改系统分享按钮边框（1.dp #06B6D4 电光青）
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.85f),
                    contentColor = AURA_CYAN_DARK
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp) // 💡 调整此处更改系统分享按钮高度（默认 48.dp）
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "System Share",
                        tint = AURA_CYAN_DARK,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "分享链接",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = {
                    if (isSaving) return@Button
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isSaving = true
                    scope.launch {
                        try {
                            saveQrCodeToGallery(context)
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp), // 💡 调整此处更改保存到相册按钮圆角（默认 14.dp）
                colors = ButtonDefaults.buttonColors(
                    containerColor = AURA_CYAN_PRIMARY, // 💡 调整此处更改保存到相册按钮背景色（#06B6D4 电光青实心填充）
                    contentColor = Color.White,
                    disabledContainerColor = AURA_CYAN_PRIMARY.copy(alpha = 0.65f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp) // 💡 调整此处更改保存到相册按钮高度（默认 48.dp）
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Save to Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "保存到相册",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// =========================================================================================
// 🚀 [二级全屏面板：云端版本检测与更新日志 - AuraUpdateSubScreen]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 19.0 的版本在线检测与官方发行日志展示子页面。
// 2. 它能干什么？
//    - 对接 GitHub API（repos/Jeesemmy/Aura/releases/latest）获取官方最新 Release；
//    - 比对本地 VersionCode 与云端版本号，自动判定是否有新版本；
//    - 展示发行日志 Markdown、更新时间戳与资产文件列表；
//    - 触发系统 DownloadManager 后台下载 APK 并通过 FileProvider 拉起安全安装，支持备用系统浏览器跳转。
// 3. 怎么调整参数？
//    - 仓库源地址：在 openGitHubReleasePage 与 UpdateManager 中配置。
//    - 发行日志卡片圆角：修改 RoundedCornerShape(18.dp)。
// =========================================================================================

private fun openGitHubReleasePage(context: Context, targetUrl: String = AURA_LATEST_RELEASE_URL) {
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    } catch (e: Exception) {
        Log.e("AuraUpdate", "Failed to open browser", e)
        Toast.makeText(context, "无法唤起系统浏览器", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun AuraUpdateSubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val currentVersion = remember {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: UpdateManager.AURA_CURRENT_VERSION
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: UpdateManager.AURA_CURRENT_VERSION
            }
        } catch (e: Exception) {
            UpdateManager.AURA_CURRENT_VERSION
        }
    }

    var isChecking by remember { mutableStateOf(false) }
    var releaseInfo by remember { mutableStateOf(UpdateManager.latestReleaseInfo.value) }
    var checkError by remember { mutableStateOf<String?>(null) }
    var lastCheckedTime by remember { mutableStateOf<String?>(null) }

    // 🌟 [阶段二：OTA 真实网络流与系统安装] 应用内 OTA 下载状态
    var showDownloadDialog by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var isDownloaded by remember { mutableStateOf(false) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    // 应用专属下载目录目标文件 (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)/update.apk)
    val apkFile = remember {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "Download")
        File(downloadDir, "update.apk")
    }

    // 🚀 启动真实网络流式下载并收集进度 Flow (增加并发防抖)
    val startRealDownload: () -> Unit = {
        if (downloadJob?.isActive != true) {
            val downloadUrl = releaseInfo?.downloadUrl?.ifEmpty { null }
                ?: AURA_LATEST_RELEASE_URL

            downloadJob?.cancel()
            downloadProgress = 0f
            isDownloaded = false
            downloadError = null
            showDownloadDialog = true

            downloadJob = scope.launch {
                try {
                    UpdateManager.downloadApk(downloadUrl, apkFile)
                        .collect { progress ->
                            downloadProgress = progress
                            if (progress >= 1.0f) {
                                isDownloaded = true
                            }
                        }
                } catch (e: CancellationException) {
                    // 用户点击取消或关闭，清理未完成的残缺临时文件
                    try {
                        if (apkFile.exists() && !isDownloaded) {
                            apkFile.delete()
                        }
                    } catch (_: Exception) {}
                } catch (e: Exception) {
                    Log.e("AuraUpdate", "Download APK failed", e)
                    downloadError = e.localizedMessage ?: "下载失败，请检查网络"
                    Toast.makeText(context, "下载出错: $downloadError", Toast.LENGTH_SHORT).show()
                    try {
                        if (apkFile.exists() && !isDownloaded) {
                            apkFile.delete()
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // 安全网络检查逻辑：生命周期与 try-catch 严格把控，绝无崩溃与泄漏
    val performCheck: () -> Unit = {
        if (!isChecking) {
            isChecking = true
            checkError = null
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            scope.launch {
                try {
                    val result = UpdateManager.checkUpdate(currentVersion = currentVersion)
                    result.onSuccess { release ->
                        releaseInfo = release
                        checkError = null
                        lastCheckedTime = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
                        if (release.hasNewVersion) {
                            Toast.makeText(context, "发现新版本！", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "当前已是最新版本 (v$currentVersion)", Toast.LENGTH_SHORT).show()
                        }
                    }.onFailure { err ->
                        checkError = err.localizedMessage ?: "检查更新失败，请稍后重试"
                        Toast.makeText(context, "检查更新失败: $checkError", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: CancellationException) {
                    // 页面退回或销毁时正常取消协程，杜绝泄漏
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                    checkError = e.localizedMessage ?: "网络或数据解析异常"
                    Toast.makeText(context, "检查更新失败: $checkError", Toast.LENGTH_SHORT).show()
                } finally {
                    isChecking = false
                }
            }
        }
    }

    // 页面初次进入时自动安全检查一次
    LaunchedEffect(Unit) {
        performCheck()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部返回栏 (沉浸式下推安全区：极简留白，移除右上角状态标签)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraCapsuleBackButton(onBack = onBack)
        }

        // 2. 中央 Hero 卡片：双核心信息块【当前版本】与【最新版本】清晰对比
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {

                // 顶部说明
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column {
                        Text(
                            text = "检测新版本",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (lastCheckedTime != null) "上次检测时间: $lastCheckedTime" else "直连 Release 镜像",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 双核心版本对比模块
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 块 1：当前版本 (Current Version)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "当前版本",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "v$currentVersion",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "运行中",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 中间竖直分割线
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .width(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // 块 2：最新版本 (Latest Version)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "最新版本",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val latestDisplay = when {
                            isChecking -> "检查中..."
                            releaseInfo != null -> "v${releaseInfo!!.versionName}"
                            checkError != null -> "获取失败"
                            else -> "待检测"
                        }
                        Text(
                            text = latestDisplay,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (releaseInfo?.hasNewVersion == true) Color(0xFF4F46E5) else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val statusSubtitle = when {
                            isChecking -> "正在连接 GitHub"
                            releaseInfo?.hasNewVersion == true -> "🎉 有新版本可更新"
                            releaseInfo != null -> "已是最新版"
                            checkError != null -> "网络异常"
                            else -> "点击按钮检测"
                        }
                        Text(
                            text = statusSubtitle,
                            fontSize = 11.sp,
                            color = if (releaseInfo?.hasNewVersion == true) Color(0xFF6366F1) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 状态说明横幅
                if (releaseInfo?.hasNewVersion == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEEF2FF))
                            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🎉 发现了新版本!",
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF4338CA)
                        )
                    }
                } else if (releaseInfo != null && !releaseInfo!!.hasNewVersion) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFECFDF5))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "✨ 当前运行已是最新版本。",
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF047857)
                        )
                    }
                } else if (checkError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFFBEB))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ $checkError",
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 发行日志卡片 (如果已成功获取 ReleaseInfo 则优雅展示)
        if (releaseInfo != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.88f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "📋 更新内容 · ${releaseInfo!!.title}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = releaseInfo!!.changelog.trim().ifEmpty { "暂无详细发行说明" },
                        fontSize = 12.5.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. 核心主操作按钮组
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 主操作按钮：若有新版本则提供“下载最新版”，否则提供“立即检查更新”
            if (releaseInfo?.hasNewVersion == true) {
                val isDownloading = downloadJob?.isActive == true
                Button(
                    onClick = {
                        if (isDownloading) return@Button
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // 🌟 [阶段二] 启动真实网络流式下载并弹出 OTA 下载器
                        startRealDownload()
                    },
                    enabled = !isDownloading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF6366F1).copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDownloading) Icons.Default.Refresh else Icons.Default.Download,
                            contentDescription = "Download Release",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDownloading) "正在下载中..." else "下载最新版 (v${releaseInfo!!.versionName})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Button(
                onClick = { performCheck() },
                enabled = !isChecking,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (releaseInfo?.hasNewVersion == true) Color(0xFFF1F5F9) else Color(0xFF6366F1),
                    contentColor = if (releaseInfo?.hasNewVersion == true) Color(0xFF475569) else Color.White,
                    disabledContainerColor = Color(0xFF6366F1).copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "正在连接 GitHub 校验...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check Now",
                            tint = if (releaseInfo?.hasNewVersion == true) Color(0xFF475569) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (releaseInfo == null && checkError == null) "立即检查更新" else "重新检查更新",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // 🌟 [阶段二] 旗舰级应用内 OTA 下载三级弹窗 (真实网络流 + 防误触 + 居中圆润环形进度 + FileProvider 系统安装)
    if (showDownloadDialog) {
        AuraOtaDownloadDialog(
            versionName = releaseInfo?.versionName ?: UpdateManager.AURA_CURRENT_VERSION,
            downloadProgress = { downloadProgress },
            isDownloaded = isDownloaded,
            downloadError = downloadError,
            onDismissRequest = {
                // 🌟 取消对应的 Coroutine Job 并删除未完成的残缺文件
                downloadJob?.cancel()
                downloadJob = null
                try {
                    if (apkFile.exists() && !isDownloaded) {
                        apkFile.delete()
                    }
                } catch (_: Exception) {}
                showDownloadDialog = false
            },
            onRetryClick = {
                startRealDownload()
            },
            onInstallClick = {
                // 🌟 通过 FileProvider 唤起 Android 系统安装器
                UpdateManager.installApk(context, apkFile)
            }
        )
    }
}

// =========================================================================================
// 🚀 [三级弹窗：应用内 OTA 下载器 - AuraOtaDownloadDialog]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 19.0 的极简风格内置 OTA 下载模态对话框（阶段二：真实网络流与 FileProvider 安装）。
// 2. 它能干什么？
//    - 拦截跳出外部浏览器，在应用内原地呈现优雅的真实网络流下载体验；
//    - 强制配置 DialogProperties 杜绝误触外部遮罩与物理返回导致下载中断；
//    - 呈现 92.dp 大尺寸圆润环形进度指示器 (StrokeCap.Round) 与居中动态百分比；
//    - 操作按钮组严格将“安装”按钮绑定至 isDownloaded 状态，未完成下载时置灰，完成后高亮；
//    - 支持断网异常一键“重试 (Retry)”；
//    - 点击“安装”自动通过 FileProvider 安全调起系统包安装器。
// 3. 怎么调整参数？
//    - 弹窗外框圆角：修改 RoundedCornerShape(24.dp)（对标“主页背景调色”弹窗 24.dp）。
//    - 环形条尺寸：修改 size(92.dp) 与 strokeWidth = 8.dp。
// =========================================================================================
@Composable
private fun AuraOtaDownloadDialog(
    versionName: String,
    downloadProgress: () -> Float,
    isDownloaded: Boolean,
    downloadError: String? = null,
    onDismissRequest: () -> Unit,
    onRetryClick: () -> Unit = {},
    onInstallClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false, // 💡 强制防误触：禁止点击外部遮罩关闭弹窗
            dismissOnBackPress = false     // 💡 强制防误触：禁止物理返回键误关闭弹窗
        )
    ) {
        Surface(
            modifier = Modifier.width(320.dp), // 💡 对标“主页背景调色”弹窗标准宽度 320.dp
            shape = RoundedCornerShape(24.dp),  // 💡 24.dp 大圆角，视觉通透呼吸感
            color = Color.White,                // 💡 纯白极简底色
            border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.85f)),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 顶部标题与版本
                Text(
                    text = when {
                        downloadError != null -> "下载中断"
                        isDownloaded -> "新版本准备就绪"
                        else -> "正在下载最新版本..."
                    },
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (downloadError != null) downloadError else "Aura v$versionName",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (downloadError != null) Color(0xFFEF4444) else Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. 核心视觉：圆润大尺寸环形进度指示器 + 居中百分比 (通过 Lambda 延迟读取，避免父级重组)
                Box(
                    modifier = Modifier.size(92.dp), // 💡 80~100.dp 旗舰大尺寸环形条
                    contentAlignment = Alignment.Center
                ) {
                    // 底轨浅灰底衬 (全圆无缝底轨)
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(92.dp),
                        color = Color(0xFFF1F5F9), // 柔和底轨色
                        strokeWidth = 8.dp,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round
                    )
                    // 动态圆润进度高亮条 (直接绑定 Lambda，避免多余重组)
                    CircularProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier.size(92.dp),
                        color = when {
                            downloadError != null -> Color(0xFFEF4444) // 异常红
                            isDownloaded -> Color(0xFF10B981)          // 完成翡翠绿
                            else -> Color(0xFF6366F1)                  // 下载中深邃靛蓝
                        },
                        strokeWidth = 8.dp,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round
                    )
                    // 正中央实时百分比与状态文案
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (downloadError != null) "!" else "${(downloadProgress() * 100).toInt()}%",
                            fontSize = if (downloadError != null) 26.sp else 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (downloadError != null) Color(0xFFEF4444) else Color(0xFF0F172A)
                        )
                        Text(
                            text = when {
                                downloadError != null -> "异常"
                                isDownloaded -> "已完成"
                                else -> "下载中"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                downloadError != null -> Color(0xFFEF4444)
                                isDownloaded -> Color(0xFF10B981)
                                else -> Color(0xFF94A3B8)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. 操作按钮组 (Row 布局)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：“关闭 / 取消”按钮
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF64748B)
                        )
                    ) {
                        Text(
                            text = if (isDownloaded || downloadError != null) "关闭" else "取消",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // 右侧：“重试”或“安装”按钮
                    if (downloadError != null) {
                        Button(
                            onClick = onRetryClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "重试",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // 正常下载/安装链路
                        Button(
                            onClick = onInstallClick,
                            enabled = isDownloaded, // 💡 严格绑定：未完成下载时置灰，完成后恢复高亮强调色
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1), // 💡 下载完成后高亮强调色
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFF1F5F9), // 💡 未完成时置灰背景
                                disabledContentColor = Color(0xFF94A3B8)    // 💡 未完成时置灰文字
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "安装",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
