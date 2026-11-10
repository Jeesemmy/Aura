package com.example.wxxx

/**
 * =========================================================================================
 * 🌌 Aura 18.0 核心主控工程架构与 UI 运行时 (MainActivity.kt)
 * =========================================================================================
 * 【工程定位】：
 * 本文件为 Aura 应用的核心逻辑与界面枢纽，集成 Jetpack Compose 现代化声明式 UI、
 * Edge-to-Edge 无界全屏沉浸视效、时态光晕背景计算引擎、液态玻璃悬浮底栏、
 * 以及企业级安全生物识别拦截机制。
 *
 * 【核心系统一览】：
 * 1. DataStore Preferences 本地持久化中枢（画框路径、安全锁、时态光晕）
 * 2. 顶级路由状态机与二级页面底栏智能滑出隐藏机制 (showBottomBar)
 * 3. 动态时态视觉引擎（早/中/晚/夜时态色彩插值与光晕背景）
 * 4. Fading Edge 渐变消散上下边缘模糊画框展示流
 * 5. 光感浮岛首屏公告弹窗与曜石黑极客风格副标语修改弹窗
 * 6. Z 轴 9f 物理隔离防穿模白盾（彻底解决跨页面切换闪烁穿模）
 * 7. 液态玻璃灵动悬浮底栏 ExpandableFab（双层磨砂微白通透底板 + 弹簧滑块）
 * 8. 私密空间生物锁屏覆盖层 BiometricLockOverlay 与毫秒级驻留监控
 * =========================================================================================
 */

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.wxxx.ui.theme.AuraTheme
import com.example.wxxx.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

// =========================================================================================
// 💡 [底层数据存储配置区 - DataStore Preferences]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 18.0 应用的全局持久化存储中枢（Jetpack DataStore Preferences）。
//    - 集中管理应用设置、安全策略、时态光晕以及自定义画框的所有配置键名（Key）。
// 2. 它能干什么？
//    - 负责把用户的个人设定持久化保存在本地沙盒中，即使关闭 App 或重启手机，设置依然完好无损。
//    - 核心存储项包括：
//      * SAVED_URIS_KEY: 存储画框选取的图片绝对 Uri 路径（以 "|" 分隔的多选列表）
//      * BIOMETRIC_LOCK_KEY / TIMEOUT: 应用私密锁开关与后台切出超时自动锁定时长（毫秒级判断）
//      * SHOW_ANNOUNCEMENT_KEY: 首屏光感浮岛更新公告弹窗的展示标记（勾选“不再显示”后置为 false）
//      * PHOTO_COUNT_KEY: 主页画框数量（1 - 10 动态调整，默认 7）
//      * SUBTITLE_KEY: 用户主页副标语内容（支持长按弹窗自定义输入，最多 12 个字限制）
//      * COLOR_*_KEY: 早、中、下、晚四时段底部光晕动态调色板的 ARGB 整数值
// 3. 怎么调整参数？
//    - 若需增加新的持久化字段：使用 stringPreferencesKey() / booleanPreferencesKey() / intPreferencesKey() 声明新 Key。
//    - 若需调整默认四时段光晕色相与透明度：直接修改下方的 DEFAULT_COLOR_MORNING / NOON / AFTERNOON / NIGHT 常量。
// =========================================================================================
private val Context.dataStore by preferencesDataStore(name = "photo_settings")
private val SAVED_URIS_KEY = stringPreferencesKey("saved_uris")
private val BIOMETRIC_LOCK_KEY = booleanPreferencesKey("biometric_lock_enabled")
private val BIOMETRIC_LOCK_TIMEOUT_KEY = intPreferencesKey("biometric_lock_timeout")
private val SHOW_ANNOUNCEMENT_KEY = booleanPreferencesKey("show_announcement")
private val PHOTO_COUNT_KEY = stringPreferencesKey("photo_count") // 存储图框数量（1-10）
private val SUBTITLE_KEY = stringPreferencesKey("custom_subtitle")
private val COLOR_MORNING_KEY = intPreferencesKey("color_morning")
private val COLOR_NOON_KEY = intPreferencesKey("color_noon")
private val COLOR_AFTERNOON_KEY = intPreferencesKey("color_afternoon")
private val COLOR_NIGHT_KEY = intPreferencesKey("color_night")
val KEY_APP_THEME_MODE = intPreferencesKey("key_app_theme_mode")

const val MAX_FRAME_COUNT = 10 // 🌟 全局最大图框展示数量统一规范常量

// 早晨颜色 (07:00 - 11:59)
val DEFAULT_COLOR_MORNING = Color(0xFFFFCC80).copy(alpha = 0.4f)   // 🌅 早上：Gemini风 晨曦粉橘 (Orange 200)

// 中午颜色 (12:00 - 13:59)
val DEFAULT_COLOR_NOON = Color(0xFF8AB4F8).copy(alpha = 0.4f)      // ☀️ 中午：Gemini本尊 天空蓝 (Blue 300)

// 下午颜色 (14:00 - 19:59)
val DEFAULT_COLOR_AFTERNOON = Color(0xFFF8EB8A).copy(alpha = 0.45f)// ⛅ 下午：Gemini风 清透浅青 (Amber/Yellow 200)

// 晚上颜色 (20:00 - 06:59)
val DEFAULT_COLOR_NIGHT = Color(0xFFB39DDB).copy(alpha = 0.45f)     // 🌃 晚上：Gemini风 梦幻星空紫 (Deep Purple 200)

/**
 * 🌌 Aura 底部二次方 Ease-In 渐变渲染修饰符 (统一视觉标准与性能隔离)
 * 将 glowFraction 读取严格后移至 Draw 阶段执行，彻底消除无效全局重组，并按基准 Alpha 动态联动。
 */
fun Modifier.auraBottomGradient(
    color: Color,
    glowFractionProvider: () -> Float
): Modifier = this.drawBehind {
    val fraction = glowFractionProvider()
    val baseAlpha = color.alpha
    val brush = Brush.verticalGradient(
        0.0f to color.copy(alpha = 0.0f),
        fraction to color.copy(alpha = 0.0f),
        fraction + (1f - fraction) * 0.25f to color.copy(alpha = baseAlpha * 0.0625f),
        fraction + (1f - fraction) * 0.50f to color.copy(alpha = baseAlpha * 0.25f),
        fraction + (1f - fraction) * 0.75f to color.copy(alpha = baseAlpha * 0.5625f),
        1.0f to color
    )
    drawRect(brush = brush)
}

fun Modifier.auraBottomGradient(
    color: Color,
    glowFraction: Float
): Modifier = auraBottomGradient(color = color, glowFractionProvider = { glowFraction })

enum class TimePeriod { MORNING, NOON, AFTERNOON, NIGHT }

// 左滑删除的状态枚举：Settled(静止/复位) | Dismissed(滑出删除) | Open(兼容保留)
enum class DragValue { Settled, Dismissed, Open }

// =========================================================================================
// 💡 [App 全局入口：MainActivity]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 18.0 Android 应用的单 Activity 宿主入口（继承自 FragmentActivity 以支持硬件级生物认证）。
// 2. 它能干什么？
//    - 安装 Android 12+ 标准启动闪屏（installSplashScreen）。
//    - 全面开启无边界沉浸式体验（Edge-to-Edge），将状态栏和导航栏彻底变为全透明，消除系统生硬边框。
//    - 在主线程以 runBlocking 短暂读取本地 DataStore，决定冷启动阶段是否直接拉起安全锁屏拦截层。
//    - 挂载根 Compose 布局树（AuraTheme -> MainScreen）。
// 3. 怎么调整参数？
//    - 若需修改系统栏前景色/行为：调整 enableEdgeToEdge(statusBarStyle = ..., navigationBarStyle = ...)。
//    - 若需修改冷启动应用锁判定：调整 initialLockEnabled 逻辑。
// =========================================================================================

class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()// 启用原生冷启动闪屏页 (12.0+ 标准)
        val splashStartTime = SystemClock.uptimeMillis()
        splashScreen.setKeepOnScreenCondition {
            SystemClock.uptimeMillis() - splashStartTime < 950L // 维持 950ms 完整播放 avd_aura_logo 矢量动效，杜绝一闪而过
        }
        super.onCreate(savedInstanceState)

        // 开启全面屏沉浸式体验 (Edge-to-Edge)，把状态栏和底部导航栏彻底变透明
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // 🚀 [120Hz 极客锁帧] 显式申请系统最高刷新率模式，突破国产 ROM（HyperOS / OriginOS 等）默认 60Hz 限制
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }
            val modes = display?.supportedModes ?: emptyArray()
            // 筛选支持的最高刷新率模式（优先锁定 120Hz / 最高帧率）
            val maxMode = modes.maxByOrNull { it.refreshRate }
            maxMode?.let {
                val params = window.attributes
                params.preferredDisplayModeId = it.modeId
                window.attributes = params
            }
        }

        // 🌟 [阅后即焚] 应用启动时在后台静默抹除上一版本遗留的 OTA 安装包残余，深藏功与名
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (downloadDir != null && downloadDir.exists()) {
                    val updateApk = File(downloadDir, "update.apk")
                    if (updateApk.exists()) {
                        updateApk.delete()
                    }
                    // 同时防御性扫描并清理该目录下可能残留的其他 .apk 安装包
                    downloadDir.listFiles { file -> file.isFile && file.name.endsWith(".apk", ignoreCase = true) }
                        ?.forEach { apk ->
                            try { apk.delete() } catch (_: Exception) {}
                        }
                }
            } catch (_: Exception) {}
        }

        // 挂载 Compose UI 主界面 (主线程零阻塞，全局应用主题与深色模式毫秒级响应)
        setContent {
            val context = LocalContext.current
            val themeModeInt by remember(context) {
                context.dataStore.data
                    .map { it[KEY_APP_THEME_MODE] ?: AppThemeMode.MODE_LIGHT.value }
                    .distinctUntilChanged()
            }.collectAsState(initial = AppThemeMode.MODE_LIGHT.value)
            val currentThemeMode = remember(themeModeInt) { AppThemeMode.fromValue(themeModeInt) }

            AuraTheme(themeMode = currentThemeMode) {
                MainScreen()
            }
        }
    }
}

// =========================================================================================
// 💡 [核心主屏幕：MainScreen]
// 这是 Aura App 的心脏，所有的主页 UI、状态管理、动画都聚集在这里。
// ==============================================================
@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(initialLocked: Boolean = false) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current// 震动马达控制器8.0
    val scope = rememberCoroutineScope()// 协程作用域，用于执行异步任务（如存数据、播动画）
    val activity = context as? FragmentActivity

    var isLocked by rememberSaveable { mutableStateOf(initialLocked) } // 🛡️ 严格约束：默认为未锁定，由 DataStore 开关真实状态驱动
    var backgroundTimestamp by rememberSaveable { mutableLongStateOf(0L) }
    var biometricPromptTrigger by remember { mutableLongStateOf(0L) }
    var needsPromptOnResume by rememberSaveable { mutableStateOf(false) } // 🌟 记录切后台导致弹窗被系统取消的补偿标记

    // 👇 把申请通知权限的代码加在这里
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 用户同意了
        }
    }

    LaunchedEffect(key1 = Unit) {
        // 请求通知权限 (Android 13+ 需要)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 注册 WorkManager 后台任务，实现“每 24 小时发送一次通知提醒”
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<DailyNotificationWorker>(
            1, java.util.concurrent.TimeUnit.DAYS
        ).build()

        // 3. 把任务交给系统底层
        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "AuraDailyTask", // 给这个任务起个专属代号
            androidx.work.ExistingPeriodicWorkPolicy.KEEP, // 保证任务不重复创建
            workRequest
        )
    }
    // 👆 权限代码结束


    // ----------------------------------------------------------------------
    // 📊 [全局状态管理区 (State Management)]
    // 这里定义了 UI 需要记住的所有数据，数据一变，UI 自动刷新。
    // ----------------------------------------------------------------------
    var displayCount by remember { mutableIntStateOf(7) }// 当前显示的画框数量
    val selectedImageUris = remember { mutableStateListOf<Uri?>().apply { repeat(MAX_FRAME_COUNT) { add(null) } } }// 存放最多 MAX_FRAME_COUNT 张图的真实地址
    val itemOrder = remember { mutableStateListOf<Int>().apply { addAll(0 until MAX_FRAME_COUNT) } }// 图片视觉排序的索引清单
    var targetSlotIndex by remember { mutableStateOf<Int?>(null) } // 🎯 记录用户点击选择图片的具体画框真实序号
    var savePhotoCountJob by remember { mutableStateOf<Job?>(null) } // 🌟 画框展示数量持久化防抖 Job (300ms 防抖消除滚轮快速滑动的并发写盘 I/O 阻塞)
    var saveThemeJob by remember { mutableStateOf<Job?>(null) } // 🌟 主题模式持久化协程 Job，取消前序未决任务防高频重叠写盘

    // 各种弹窗和面板的开关控制
    var isFabExpanded by remember { mutableStateOf(false) }
    var customSubtitle by remember { mutableStateOf("今天也要开心呀🥰") }
    var showEditSubtitleDialog by remember { mutableStateOf(false) }
    var tempSubtitleInput by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAlarmDialog by rememberSaveable { mutableStateOf(false) } // 闹钟大房子开关
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var isSettingsSubScreenOpen by rememberSaveable { mutableStateOf(false) } // 🌟 二级设置全屏页状态
    var showSortDialog by remember { mutableStateOf(false) } // 🌟 新增：控制独立排序弹窗的显示与隐藏
    var showAnnouncementDialog by rememberSaveable { mutableStateOf(false) }
    var previewUri by rememberSaveable { mutableStateOf<Uri?>(null) }// 用于大图预览，如果非空就弹起全屏大图
    var titleVisible by remember { mutableStateOf(false) }

    // ----------------------------------------------------------------------
    // 📜 [列表滑动联动动画]
    // 监听滚轮状态，控制顶部标题和底部胶囊的自动隐藏/浮现
    // ----------------------------------------------------------------------
    val lazyListState = rememberLazyListState()
    val helpScrollState = rememberScrollState()
    val settingsLazyListState = rememberLazyListState() // 🌟 V2 设置页的独立滚动状态
    val alarmScrollState = rememberScrollState() // 闹钟页面的独立滚轮

    val isMainScrollingUp = lazyListState.isScrollingUp()
    val isHelpScrollingUp = helpScrollState.isScrollingUp()
    val isSettingsScrollingUp = settingsLazyListState.isScrollingUp()
    val isAlarmScrollingUp = alarmScrollState.isScrollingUp()

    // 🌟 动态路由：根据当前打开的页面，决定胶囊听谁的指挥
    val isScrollingUp = when {
        showHelpDialog -> isHelpScrollingUp
        showAlarmDialog -> isAlarmScrollingUp // 🌟 新增路由接管
        isSettingsOpen -> isSettingsScrollingUp
        else -> isMainScrollingUp
    }

    val topBarOffset by animateDpAsState(
        targetValue = if (isScrollingUp) 0.dp else (-170).dp,// 往下滑时把标题收上去
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "TopBar"
    )

    val bottomBarOffset by animateDpAsState(
        targetValue = if (isScrollingUp) 0.dp else 100.dp,// 往下滑时把底部胶囊藏起来
        animationSpec = tween(durationMillis = 300),
        label = "BottomBar"
    )

    // =========================================================================================
    // 🧭 [顶级主 Tab 路由状态机与沉浸式二级页面底栏智能显隐]
    // 【灵魂三问】：
    // 1. 这是什么？
    //    - 这是 Aura 18.0 全局导航路由状态机与底部悬浮导航栏的显示控制判定中心。
    // 2. 它能干什么？
    //    - 实时监听多层状态组合，解析出当前处于哪个具体页面（如 preview、settings_detail、settings、time、calendar、home）。
    //    - 维护 4 个主 Tab 路由白名单（topLevelRoutes）；只要用户在 4 个主级功能间切换，底栏稳定常驻；
    //    - 一旦用户点击进入任何深层二级全屏子页面（如设置子项、大图全屏预览），立刻触发 showBottomBar = false，
    //      通知外部 AnimatedVisibility 联动滑出隐藏，提供极致的 Edge-to-Edge 全屏沉浸体验。
    // 3. 怎么调整参数？
    //    - 若未来新增了第 5 个主 Tab：将新 Tab 的路由标识字符串追加到 topLevelRoutes 的 listOf(...) 中。
    //    - 若希望某个二级页面在特定条件下不隐藏底栏：修改 val showBottomBar = ... 的条件表达式。
    // =========================================================================================
    val currentRoute = when {
        previewUri != null -> "preview"
        isSettingsOpen && isSettingsSubScreenOpen -> "settings_detail" // 二级设置全屏页
        isSettingsOpen -> "settings"                                   // 第 4 个主 Tab：设置
        showAlarmDialog -> "time"                                      // 第 3 个主 Tab：版本时间线
        showHelpDialog -> "calendar"                                   // 第 2 个主 Tab：实验性功能/探索空间占位
        else -> "home"                                                 // 第 1 个主 Tab：主页
    }

    // 🌟 4 个有效主 Tab 路由白名单：确保在主 Tab 间任意切换时底栏常驻显示（净化历史死代码路由）
    val topLevelRoutes = remember {
        listOf(
            "home",
            "calendar",
            "time",
            "settings"
        )
    }
    val showBottomBar = currentRoute in topLevelRoutes

    // 顶部问候语的轻微左右摇摆动画 (Jelly Effect)
    val infiniteTransition = rememberInfiniteTransition(label = "jelly")
    val angle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "angle"
    )

    // ----------------------------------------------------------------------
    // 💾 [数据持久化存取]
    // ----------------------------------------------------------------------
    // 保存排序后的图片地址到手机本地存储
    val saveUrisToDisk = {
        scope.launch {
            // 🛡️ 核心微调：按照拖动后的【视觉顺序 itemOrder】重新组合数据并存储
            val data = itemOrder.map { selectedImageUris[it]?.toString() ?: "" }.joinToString("|")
            context.dataStore.edit { it[SAVED_URIS_KEY] = data }
        }
    }

    // App 冷启动时，读取本地保存的数据 (数量、图片地址、标语)
    LaunchedEffect(Unit) {
        titleVisible = true
        val prefs = context.dataStore.data.first()
        // 🛡️ 注入极值防爆兜底：严防非法值、0 或负数进入系统状态，杜绝大图预览 Pager 抛出 IllegalArgumentException
        displayCount = (prefs[PHOTO_COUNT_KEY]?.toIntOrNull() ?: 7).coerceIn(1, MAX_FRAME_COUNT)

        // 🌟 副标语在这里
        customSubtitle = prefs[SUBTITLE_KEY] ?: "长按可修改🗽"

        val savedString = prefs[SAVED_URIS_KEY] ?: ""
        if (savedString.isNotEmpty()) {
            savedString.split("|").forEachIndexed { index, path ->
                if (path.isNotEmpty() && index < MAX_FRAME_COUNT) {
                    selectedImageUris[index] = Uri.parse(path)
                }
            }
        }

        // 🛡️ 严正约束：仅在用户明确开启应用锁 (BIOMETRIC_LOCK_KEY == true) 时才派发锁定与鉴权
        val lockEnabled = prefs[BIOMETRIC_LOCK_KEY] ?: false
        if (lockEnabled) {
            isLocked = true
            biometricPromptTrigger = SystemClock.elapsedRealtime()
        } else {
            isLocked = false
        }
    }

    // 判断是否需要弹出版本公告
    var hasCheckedAnnouncement by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isLocked) {
        if (!isLocked && !hasCheckedAnnouncement) {
            val prefs = context.dataStore.data.first()
            if (prefs[SHOW_ANNOUNCEMENT_KEY] ?: true) {
                showAnnouncementDialog = true
            }
            hasCheckedAnnouncement = true
        }
    }

    // 5.2 升级：支持多选图片并优化填充逻辑
    // 相册多选图选择器（私有沙盒安全落盘 + 槽位精准映射）
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_FRAME_COUNT)
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val currentDisplayCount = displayCount
                val visibleSlots = itemOrder.take(currentDisplayCount)

                // 目标槽位：优先取记录的 targetSlotIndex，若未指定则取当前可见的第一个空槽位
                val targetSlot = targetSlotIndex
                    ?: visibleSlots.firstOrNull { selectedImageUris[it] == null }
                    ?: visibleSlots.firstOrNull()
                    ?: 0

                // 其余空槽位（排除已作为目标槽位的 targetSlot）
                val remainingEmptySlots = visibleSlots.filter { it != targetSlot && selectedImageUris[it] == null }

                // 槽位分配：第一个图片填入目标槽位，后续图片按序填入空余槽位
                val slotAssignments = mutableListOf<Pair<Int, Uri>>()
                slotAssignments.add(targetSlot to uris[0])

                val remainingUris = uris.drop(1)
                for (i in 0 until minOf(remainingUris.size, remainingEmptySlots.size)) {
                    slotAssignments.add(remainingEmptySlots[i] to remainingUris[i])
                }

                // 超选友好 Toast 提示（当用户挑选数量大于可填入的槽位数时）
                if (uris.size > slotAssignments.size) {
                    val filledCount = slotAssignments.size
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "已填入 ${filledCount} 张，超出数量已忽略", Toast.LENGTH_SHORT).show()
                    }
                }

                // 在 Dispatchers.IO 协程中执行沙盒安全拷贝、防爆下采样落盘与旧文件清理
                for ((slot, sourceUri) in slotAssignments) {
                    // 若原槽位已有沙盒文件，安全清理旧文件防止无用占用
                    val oldUri = selectedImageUris.getOrNull(slot)
                    if (oldUri?.scheme == "file") {
                        try {
                            File(oldUri.path ?: "").delete()
                        } catch (_: Exception) {}
                    }

                    val sandboxUri = saveImageToSandbox(context, sourceUri, slot)
                    selectedImageUris[slot] = sandboxUri
                }

                saveUrisToDisk()
                targetSlotIndex = null
            }
        } else {
            targetSlotIndex = null
        }
    }

    // ----------------------------------------------------------------------
    // 🎨 [时态视觉引擎 (Gemini Style)]
    // 根据当前 24 小时制时间，动态计算问候语和底部光晕颜色
    // 时段划分业务规范：
    // - 清晨/早晨 (07:00 ~ 11:59)："早上好"，对应晨曦柔和光晕 (morningColor)
    // - 正午/午间 (12:00 ~ 13:59)："中午好"，对应晴空明亮光晕 (noonColor)
    // - 午后/傍晚 (14:00 ~ 19:59)："下午好"，对应落日余晖光晕 (afternoonColor)
    // - 静夜/深夜 (20:00 ~ 次日 06:59)："晚上好"，对应静谧星夜光晕 (nightColor)
    // ----------------------------------------------------------------------
    var hour by remember { mutableIntStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) }

    val greetingText = when (hour) {
        in 7..11 -> "早上好"   // 早晨时段 (07:00 ~ 11:59)
        in 12..13 -> "中午好"  // 午间时段 (12:00 ~ 13:59)
        in 14..19 -> "下午好"  // 傍晚时段 (14:00 ~ 19:59)
        else -> "晚上好"       // 夜间时段 (20:00 ~ 次日 06:59)
    }

    // 2. 统一计算底部光晕颜色（支持从 DataStore 动态响应与平滑过渡）
    val isDarkTheme = LocalIsDarkTheme.current
    val preferencesState by context.dataStore.data.collectAsState(initial = null)
    val isBiometricLockEnabled = preferencesState?.get(BIOMETRIC_LOCK_KEY) ?: false
    val timeoutMinutes = preferencesState?.get(BIOMETRIC_LOCK_TIMEOUT_KEY) ?: 0
    val themeModeInt = preferencesState?.get(KEY_APP_THEME_MODE) ?: AppThemeMode.MODE_LIGHT.value
    val currentThemeMode = remember(themeModeInt) { AppThemeMode.fromValue(themeModeInt) }

    // 监听前后台生命周期切换 (支持时间延迟锁定)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // 若非生物识别鉴权弹窗本身引起的失焦，才记录离开前台的高精度系统运行时间戳
                    if (!isBiometricPromptActive) {
                        backgroundTimestamp = SystemClock.elapsedRealtime()
                    } else if (isLocked) {
                        // 正在弹出生物识别时切后台，系统会取消弹窗，标记恢复时补偿弹窗
                        needsPromptOnResume = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    // 🌟 返回前台时重新校准当前系统小时数，确保时态问候语与光晕精准对应
                    hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    if (!isBiometricPromptActive && backgroundTimestamp > 0L && isBiometricLockEnabled) {
                        // 短路判断：如果 timeoutMinutes == 0（即选择了立即锁定），则无视后台驻留时间差，返回前台 100% 立即触发锁定
                        val shouldLock = if (timeoutMinutes == 0) {
                            true
                        } else {
                            val elapsedMillis = SystemClock.elapsedRealtime() - backgroundTimestamp
                            val timeoutMillis = timeoutMinutes * 60 * 1000L
                            elapsedMillis >= timeoutMillis
                        }

                        if (shouldLock) {
                            isLocked = true
                            isSettingsOpen = false
                            previewUri = null
                            showHelpDialog = false
                            showAlarmDialog = false
                            biometricPromptTrigger = SystemClock.elapsedRealtime()
                        }
                    }
                    backgroundTimestamp = 0L

                    // 🌟 前后台切回弹窗补偿：若功能开启且仍处于锁定状态且由于切后台被系统取消，自动触发补弹，消除“多点一次屏幕”
                    if (isBiometricLockEnabled && isLocked && !isBiometricPromptActive && needsPromptOnResume) {
                        needsPromptOnResume = false
                        biometricPromptTrigger = SystemClock.elapsedRealtime()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 锁定状态下自动唤起生物识别 (严正门控：仅在功能开启且处于锁定态时执行)
    LaunchedEffect(isLocked, biometricPromptTrigger, isBiometricLockEnabled) {
        if (isBiometricLockEnabled && isLocked && activity != null && !isBiometricPromptActive) {
            showBiometricPrompt(
                activity = activity,
                onSuccess = {
                    isLocked = false
                    needsPromptOnResume = false
                },
                onError = { errorCode, errString ->
                    // 1. 用户按 Home 键切后台或系统取消验证时，记录补偿标记，返回前台时自动补弹
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_CANCELED) {
                        needsPromptOnResume = true
                    } else if (errorCode == BiometricPrompt.ERROR_LOCKOUT || errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT) {
                        // 2. 补充连续输错冻结的用户反馈 Toast，消除用户指纹被冻结时的假死感
                        Toast.makeText(context, errString.ifBlank { "尝试次数过多，请稍后再试" }, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // 丝滑迷雾消散动画参数 (受 isBiometricLockEnabled 严格约束)
    val animatedBlurRadius by animateDpAsState(
        targetValue = if (isBiometricLockEnabled && isLocked) 35.dp else 0.dp,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "blurRadius"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isBiometricLockEnabled && isLocked) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "overlayAlpha"
    )

    // 🌟 主页专属入场微缩放控制器 (0.96f -> 1.0f)
    val homeScale by animateFloatAsState(
        targetValue = if (isBiometricLockEnabled && isLocked) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "homeScale"
    )

    // 🌟 主页专属入场透明度控制器 (0f -> 1.0f)
    val homeAlpha by animateFloatAsState(
        targetValue = if (isBiometricLockEnabled && isLocked) 0f else 1.0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "homeAlpha"
    )
    val morningColor = remember(preferencesState) {
        preferencesState?.get(COLOR_MORNING_KEY)?.let { Color(it) } ?: DEFAULT_COLOR_MORNING
    }
    val noonColor = remember(preferencesState) {
        preferencesState?.get(COLOR_NOON_KEY)?.let { Color(it) } ?: DEFAULT_COLOR_NOON
    }
    val afternoonColor = remember(preferencesState) {
        preferencesState?.get(COLOR_AFTERNOON_KEY)?.let { Color(it) } ?: DEFAULT_COLOR_AFTERNOON
    }
    val nightColor = remember(preferencesState) {
        preferencesState?.get(COLOR_NIGHT_KEY)?.let { Color(it) } ?: DEFAULT_COLOR_NIGHT
    }

    val targetBottomGradientColor = when (hour) {
        in 7..11 -> morningColor
        in 12..13 -> noonColor
        in 14..19 -> afternoonColor
        else -> nightColor
    }

    val bottomGradientColor by animateColorAsState(
        targetValue = targetBottomGradientColor,
        animationSpec = tween(durationMillis = 800),
        label = "bottomGradientColor"
    )
    // 极光呼吸动效引擎：控制底部光晕在屏幕高度间缓慢往复起伏
    val breathingTransition = rememberInfiniteTransition(label = "breathing")
    val glowFraction by breathingTransition.animateFloat(
        initialValue = 0.75f,  // 状态 A：光晕收缩（压在屏幕底部 75% 处）
        targetValue = 0.20f,   // 状态 B：光晕扩散（向上蔓延到屏幕 20% 处）
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing), // 4秒一次深呼吸的缓慢流体节奏
            repeatMode = RepeatMode.Reverse // 来回往复，生生不息
        ),
        label = "glowFraction"
    )

    // =========================================================================================
    // 🖼️ [页面主骨架：Scaffold & UI 树]
    // ==============================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF121212) else MaterialTheme.colorScheme.background)
    ) {
        // 🌟 视觉定格与丝滑展开：锁定态不露痕迹；解锁瞬间主页从 0.96 微缩状态优雅浮现展开
        if (!isBiometricLockEnabled || !isLocked || overlayAlpha < 1f) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = homeAlpha
                        scaleX = homeScale
                        scaleY = homeScale
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                    },
                containerColor = if (isDarkTheme) Color(0xFF121212) else MaterialTheme.colorScheme.background,
                floatingActionButtonPosition = FabPosition.Center,
                // =========================================================================================
                // 💊 [底部悬浮导航栏动态入场/离场系统 - AnimatedVisibility & Z 轴 20f 制空权]
                // 【灵魂三问】：
                // 1. 这是什么？
                //    - 这是 Aura 18.0 底部悬浮灵动导航栏的入场、离场动效与最高 Z 轴层级保障容器。
                // 2. 它能干什么？
                //    - 监听 showBottomBar 状态，在主 Tab 切换时以 350ms 优雅的垂直滑入 (slideInVertically) + 淡入 (fadeIn) 显现；
                //    - 进入二级全屏页面时，通过 slideOutVertically 向下滑出屏幕并淡出消失；
                //    - 赋予 Modifier.zIndex(20f) 最高层级，彻底解决跨 Tab 切换时 10f 的二级全屏页面将底栏遮挡或穿模的顽疾。
                // 3. 怎么调整参数？
                //    - 若需调整底栏滑入滑出速度：修改 tween(durationMillis = 350, easing = FastOutSlowInEasing)。
                //    - 若需调整滑出方向或初末偏移量：修改 initialOffsetY / targetOffsetY 的 Lambda 返回值。
                //    - 必须严守 zIndex(20f) > 页面 zIndex(10f) > 白盾 zIndex(9f) 的层级铁律，切勿调小。
                // =========================================================================================
                floatingActionButton = {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showBottomBar,
                        modifier = Modifier.zIndex(20f), // 💡 调整此处更改底栏胶囊层级（必须保持 20f > 二级全屏页 10f > 防穿模白盾 9f，防止层级错乱）
                        enter = slideInVertically(
                            initialOffsetY = { it }, // 💡 调整此处更改底栏滑入初始偏移（{ it } 代表从完全屏幕底部向上滑入）
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing) // 💡 调整此处更改底栏滑入时长（默认 350ms）
                        ) + fadeIn(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)),
                        exit = slideOutVertically(
                            targetOffsetY = { it }, // 💡 调整此处更改底栏退场目标偏移（{ it } 代表完全向下滑出屏幕之外）
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing) // 💡 调整此处更改底栏滑出时长（默认 350ms）
                        ) + fadeOut(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
                    ) {
                        Box(
                            modifier = Modifier
                                .offset { androidx.compose.ui.unit.IntOffset(0, bottomBarOffset.roundToPx()) }
                                .zIndex(20f) // 💡 调整此处保证容器 Box 与外层 AnimatedVisibility 共享 20f 最高层级
                        ) {
                            ExpandableFab(
                                // 智能识别：告诉胶囊现在该滑到哪个图标下面
                                selectedIndex = when {
                                    isSettingsOpen -> 3      // 对应右侧的“设置”按钮
                                    showAlarmDialog -> 2     // 对应第 3 个 Tab：时间线/纸飞机
                                    showHelpDialog -> 1      // 对应第 2 个 Tab：Tab 2 实验性功能/探索空间占位弹窗
                                    else -> 0                // 默认主页
                                },
                                onHomeClick = {
                                    isSettingsOpen = false
                                    isSettingsSubScreenOpen = false
                                    showHelpDialog = false
                                    showAlarmDialog = false  // 🌟 关闭闹钟
                                },
                                onTab2Click = {
                                    isSettingsOpen = false
                                    isSettingsSubScreenOpen = false
                                    showHelpDialog = true
                                    showAlarmDialog = false  // 🌟 关闭闹钟
                                },
                                onAlarmClick = {
                                    isSettingsOpen = false
                                    isSettingsSubScreenOpen = false
                                    showHelpDialog = false
                                    showAlarmDialog = true   // 🌟 独占打开闹钟
                                },
                                onSettingsClick = {
                                    isSettingsOpen = true
                                    isSettingsSubScreenOpen = false
                                    showHelpDialog = false
                                    showAlarmDialog = false  // 🌟 关闭闹钟
                                }
                            )
                        }
                    }
                }

            ) { innerPadding ->
                // 背景光晕渲染层
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 1. 第一层（打底）：纯净不透明的 App 主题底色，深浅自适应隔绝系统底层 Window 色差干扰
                        .background(if (isDarkTheme) Color(0xFF121212) else Color.White)
                        // 2. 第二层（光晕）：二次方缓动锚点 (Ease-In)，彻底磨平斜率突变，并在 Draw 阶段读取状态消除重组
                        .auraBottomGradient(
                            color = bottomGradientColor,
                            glowFractionProvider = { glowFraction }
                        )
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // ----------------------------------------------------------------------
                        // ✨ 顶部标题栏区域 (AnimatedVisibility 负责滑动隐藏/展示动画)
                        // --------------------------------------------------------
                        AnimatedVisibility(
                            visible = titleVisible,
                            modifier = Modifier
                                .offset {
                                    androidx.compose.ui.unit.IntOffset(
                                        0,
                                        topBarOffset.roundToPx()
                                    )
                                }
                                .zIndex(1f),
                            enter = fadeIn(animationSpec = tween(800)) +
                                    slideInVertically(
                                        initialOffsetY = { 50 },
                                        animationSpec = tween(600)
                                    )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth() // 👈 2. 撑满宽度
                                    // 👇 3. 核心修复：加一层半透明背景底板，完美隔离底部滚动的图片！
                                    .background(color = if (isDarkTheme) Color(0xFF121212).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.5f))// 半透明遮罩隔离底部滑动的图片
                                    // 👇 2. 新增这行！在背景之后加状态栏 padding，把文字安全地挤下来
                                    .statusBarsPadding() // 👈 这里保留，保证“你好”文字在状态栏下面，不被摄像头遮挡

                                    // 👇 加上这一行：单纯把整行元素在视觉上往下平移
                                    .offset(y = 20.dp)
                                    // 👇 4. 底部增加一点 padding，把背景板撑开，看起来更像原生的 TopBar
                                    .padding(top = 14.dp, bottom = 16.dp, start = 24.dp)
                            ) {

                                // 👇 2. 修改 Text，让它使用我们刚刚计算出来的 greetingText
                                Text(
                                    text = greetingText, // 👈 替换掉写死的 "你好"
                                    color = if (isDarkTheme) Color(0xFFF1F5F9) else Color.Black,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.5.sp,
                                    modifier = Modifier.graphicsLayer { rotationZ = angle }// 摇摆特效
                                )

                                Spacer(modifier = Modifier.width(12.dp))
                                // 🌟 改造右侧副标题：支持长按弹出自定义输入框
                                Text(
                                    text = "       $customSubtitle\n",
                                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color.Gray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier
                                        .offset(y = 12.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                // 长按修改副标语
                                                onLongPress = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    tempSubtitleInput = customSubtitle
                                                    showEditSubtitleDialog = true
                                                }
                                            )
                                        }
                                )
                            }
                        }

                        // =========================================================================================
                        // 📱 [画框主列表：LazyColumn 极简清晰实体呈现]
                        // 【灵魂三问】：
                        // 1. 这是什么？
                        //    - 这是 Aura 主页画框卡片的动态滚动列表，移除上下边缘渐变遮罩，使相框在滚动至边缘时保持原本清晰的实体呈现。
                        // 2. 它能干什么？
                        //    - 展示最多 10 个可交互相框卡片（点击选图/预览大图，长按触发独立排序弹窗）；
                        //    - 保持顶部与底部安全避让间距（top = 140.dp, bottom = 100.dp），防止列表内容被顶栏或底栏截断或穿模。
                        // 3. 怎么调整参数？
                        //    - 若需调整卡片垂直间距：修改 verticalArrangement = Arrangement.spacedBy(16.dp)。
                        //    - 若需调整列表上下安全避让：修改 contentPadding 的 top = 140.dp 与 bottom = 100.dp。
                        // =========================================================================================
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(enabled = isFabExpanded) { isFabExpanded = false },
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 140.dp,
                                bottom = 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = itemOrder.take(displayCount),
                                key = { originalIndex -> "slot_$originalIndex" },
                                contentType = { "photo_card" }
                            ) { originalIndex ->
                                Box(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                        placementSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                ) {
                                    AppListItem(
                                        uri = selectedImageUris.getOrNull(originalIndex),
                                        onSelect = {
                                            targetSlotIndex = originalIndex
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        onPreview = { uri ->
                                            previewUri = uri
                                        },
                                        onDelete = {
                                            if (originalIndex in selectedImageUris.indices) {
                                                val oldUri = selectedImageUris[originalIndex]
                                                if (oldUri?.scheme == "file") {
                                                    try {
                                                        File(oldUri.path ?: "").delete()
                                                    } catch (_: Exception) {}
                                                }
                                                selectedImageUris[originalIndex] = null
                                            }
                                            saveUrisToDisk()
                                        },
                                        onLongPress = {
                                            showSortDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // =========================================================================================
                // 🪟 [全家桶弹窗控制区]
                // 这里拦截并渲染 App 内的各种 Dialog 面板
                // =========================================================================================

                // =========================================================================================
                // 🪟 [1. 版本公告弹窗 - 光感浮岛旗舰级升级]
                // 【灵魂三问】：
                // 1. 这是什么？
                //    - 这是 Aura 18.0 全新重构的光感浮岛版本公告对话框（showAnnouncementDialog）。
                // 2. 它能干什么？
                //    - 冷启动时根据 DataStore 中的 SHOW_ANNOUNCEMENT_KEY 自动判断拉起；
                //    - 以双色光感渐变标题（✨ Aura 最新动态）展现版本特性；
                //    - 提供支持垂直滚动的呼吸感特性列表及 Google / 骁龙 / 天玑芯片合作伙伴无背景微标；
                //    - 底部提供【我知道了】（曜石黑胶囊流体按钮）与【不再显示】（持久化标记置灰关闭）主次双操作。
                // 3. 怎么调整参数？
                //    - 浮岛圆角：修改 RoundedCornerShape(32.dp)；浮岛高度：修改 tonalElevation = 8.dp。
                //    - 边框描边：修改 dialogBorder 的 1.dp 半透明高光色。
                //    - 标题渐变：修改 Brush.linearGradient 的色彩清单（日间：Indigo -> Cyan；夜间：亮紫 -> 青蓝）。
                //    - 列表间距：修改 verticalArrangement = Arrangement.spacedBy(14.dp)。
                //    - 主按钮样式：修改【我知道了】的 containerColor（默认曜石黑 0xFF2C2C2C / 0xFF383D47）与 RoundedCornerShape(50)。
                // =========================================================================================
                if (showAnnouncementDialog) {
                    val dialogBg = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
                    val dialogBorder = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f)
                    val sectionHeaderColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1F2937)
                    val itemTextColor = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF334155)
                    val dismissTextColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                    val maxDialogHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.72f).dp

                    AlertDialog(
                        modifier = Modifier
                            .widthIn(min = 320.dp, max = 350.dp) // 💡 调整此处更改公告弹窗宽度范围（默认 min=320.dp, max=350.dp）
                            .heightIn(max = maxDialogHeight) // 💡 调整此处更改弹窗最大高度限制（默认屏幕高 72%）
                            .border(
                                width = 1.dp, // 💡 调整此处更改外层极细质感描边粗细（默认 1.dp）
                                color = dialogBorder,
                                shape = RoundedCornerShape(32.dp) // 💡 调整此处更改描边圆角弧度（需与下行 shape 严格同步）
                            ),
                        shape = RoundedCornerShape(32.dp), // 💡 调整此处更改光感浮岛外层超大圆角（默认 32.dp 旗舰圆角，建议 28-36.dp）
                        containerColor = dialogBg, // 💡 调整此处更改弹窗底色（深色模式：0xFF1E1E1E；浅色模式：纯白）
                        tonalElevation = 0.dp, // 💡 彻底设为 0.dp，消除 Material 3 浅色模式下的主题淡绿染色
                        onDismissRequest = { showAnnouncementDialog = false },
                        title = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "✨ 最新动态",
                                    fontSize = 20.sp, // 💡 调整此处更改大标题文字字号（默认 20.sp，加粗）
                                    fontWeight = FontWeight.Bold,
                                    style = androidx.compose.ui.text.TextStyle(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = if (isDarkTheme) listOf(Color(0xFF38BDF8), Color(0xFF22D3EE)) // 💡 深色模式高饱和亮蓝 -> 亮青
                                                     else listOf(Color(0xFF4F46E5), Color(0xFF0891B2)) // 💡 浅色模式渐变色：极客靛蓝 -> 深湖青
                                        )
                                    ),
                                    letterSpacing = (-0.3).sp // 💡 调整此处更改标题字间距（-0.3.sp 营造现代紧凑高级感）
                                )

                                Spacer(modifier = Modifier.height(12.dp)) // 💡 调整此处更改标题与分割线之间的上下留白（默认 12.dp）

                                androidx.compose.material3.HorizontalDivider(
                                    color = if (isDarkTheme) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f), // 💡 调整此处更改极浅分割线透明度
                                    thickness = 0.8.dp // 💡 调整此处更改极浅分割线厚度（默认 0.8.dp 纤细分割）
                                )
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(top = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp) // 💡 调整此处更改更新条目之间的垂直行间距（默认 14.dp 充足呼吸感）
                            ) {
                                Text(
                                    text = "本次更新内容：",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = sectionHeaderColor,
                                    letterSpacing = 0.2.sp
                                )

                                // 条目 1
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "1. ",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = itemTextColor
                                    )
                                    Text(
                                        text = "适配了深色模式。",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 20.sp,
                                        color = itemTextColor
                                    )
                                }

                                // 条目 2
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "2. ",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = itemTextColor
                                    )
                                    Text(
                                        text = "带来了实验性功能-性能监测。",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 20.sp,
                                        color = itemTextColor
                                    )
                                }

                                // 条目 3（水平排列文字与双芯徽章）
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "3. ",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = itemTextColor
                                    )
                                    Text(
                                        text = "深度适配了120Hz刷新率。",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = itemTextColor
                                    )
                                }
                            }
                        },

                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = { showAnnouncementDialog = false },
                                shape = RoundedCornerShape(50), // 💡 调整此处更改【我知道了】主按钮胶囊流体圆角（50 为全胶囊）
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkTheme) Color(0xFF10B981) else Color(0xFF2C2C2C), // 💡 深色模式下使用品牌主题绿底，浅色模式保持曜石黑
                                    contentColor = Color.White // 💡 调整此处更改主按钮字体颜色（默认纯白高对比度）
                                ),
                                elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp, // 💡 调整此处更改按钮静止默认阴影高度（默认 2.dp 浮动触感）
                                    pressedElevation = 4.dp // 💡 调整此处更改按钮按下时的投影高度（默认 4.dp）
                                ),
                                modifier = Modifier
                                    .height(30.dp) // 💡 调整此处更改主按钮高度（默认 36.dp，精致小巧）
                                    .padding(start = 4.dp), // 💡 调整此处更改主次按钮防误触横向间距
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "我知道了",
                                    fontSize = 13.sp, // 💡 调整此处更改主按钮字号（默认 13.sp，精致协调）
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    scope.launch {
                                        context.dataStore.edit { it[SHOW_ANNOUNCEMENT_KEY] = false }
                                    }
                                    showAnnouncementDialog = false
                                },
                                shape = RoundedCornerShape(50), // 💡 调整此处更改【不再显示】次按钮胶囊圆角
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = dismissTextColor // 💡 调整此处更改次级按钮低调文字颜色
                                ),
                                modifier = Modifier
                                    .height(30.dp) // 💡 调整此处更改次按钮高度（与主按钮保持 36.dp 高度一致）
                                    .padding(end = 4.dp), // 💡 调整此处更改次按钮右侧边距
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "不再显示",
                                    fontSize = 12.5.sp, // 💡 调整此处更改次按钮字号（默认 12.5.sp，略小以拉开主次层次）
                                    fontWeight = FontWeight.Normal,
                                    color = dismissTextColor
                                )
                            }
                        }
                    )
                }

                // =========================================================================================
                // 🪟 [2. 自定义副标语修改弹窗 - 旗舰级 UI 升级]
                // 【灵魂三问】：
                // 1. 这是什么？
                //    - 这是 Aura 18.0 主页副标语的长按自定义修改模态对话框（showEditSubtitleDialog）。
                // 2. 它能干什么？
                //    - 用户在主页长按标语文字时触发，提供单行文本输入框；
                //    - 严格限制最多 12 个中英文字符（it.length <= 12）；
                //    - 输入框底缘内置 supportingText 靠右显示实时字数计数（满 12 字变为警示红）；
                //    - 点击【保存】以曜石黑（Obsidian Black）胶囊风格将新标语实时持久化写回 DataStore（SUBTITLE_KEY）。
                // 3. 怎么调整参数？
                //    - 弹窗外框：修改 RoundedCornerShape(28.dp) 与 dialogBorder。
                //    - 输入框：修改 OutlinedTextField 的 RoundedCornerShape(16.dp) 与 OutlinedTextFieldDefaults.colors。
                //    - 字数上限：若需允许更长标语，修改 if (it.length <= 12) 的数字以及 supportingText 里的分子分母。
                //    - 保存按钮颜色：修改 containerColor = if (isDark) Color(0xFF383D47) else Color(0xFF2C2C2C)。
                // =========================================================================================
                if (showEditSubtitleDialog) {
                    val dialogBg = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
                    val dialogBorder = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f)
                    val textPrimary = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

                    AlertDialog(
                        onDismissRequest = { showEditSubtitleDialog = false },
                        modifier = Modifier
                            .widthIn(min = 300.dp, max = 340.dp) // 💡 调整此处更改副标语弹窗宽度（默认 300-340.dp）
                            .border(
                                width = 1.dp, // 💡 调整此处更改外层极细高定边框粗细
                                color = dialogBorder,
                                shape = RoundedCornerShape(28.dp) // 💡 调整此处更改描边圆角（需与下行 shape 同步）
                            ),
                        shape = RoundedCornerShape(28.dp), // 💡 调整此处更改弹窗大圆角（默认 28.dp）
                        containerColor = dialogBg, // 💡 调整此处更改弹窗背景色（深色模式：0xFF1E1E1E；浅色模式：纯白）
                        tonalElevation = 0.dp, // 💡 设为 0.dp 消除浅色模式淡绿染色
                        title = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp) // 💡 调整此处更改标题与说明文字的垂直间距（默认 6.dp 紧凑优雅）
                            ) {
                                Text(
                                    text = "副标语",
                                    fontSize = 20.sp, // 💡 调整此处更改标题字号（默认 20.sp，粗体）
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "输入你想对自己说的话（最多 12 个字）",
                                    fontSize = 13.sp, // 💡 调整此处更改副标题说明文字字号（默认 13.sp）
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 18.sp,
                                    color = textSecondary
                                )
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                            ) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = tempSubtitleInput,
                                    onValueChange = {
                                        if (it.length <= 12) { // 💡 调整此处更改副标语允许的最大字符数限制（当前：最多 12 字）
                                            tempSubtitleInput = it
                                        }
                                    },
                                    singleLine = true,
                                    maxLines = 1,
                                    shape = RoundedCornerShape(16.dp), // 💡 调整此处更改输入框圆角（默认 16.dp 柔和内凹）
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 15.sp, // 💡 调整此处更改输入框内文字大小（默认 15.sp）
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    ),
                                    placeholder = {
                                        Text(
                                            text = "例如：你终于发现这里了",
                                            fontSize = 14.sp,
                                            color = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
                                        )
                                    },
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary, // 💡 调整此处更改输入框激活时的焦点高亮边框色
                                        unfocusedBorderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0), // 💡 调整此处更改失焦时的浅板岩灰边框
                                        focusedContainerColor = if (isDarkTheme) Color(0xFF262C36) else Color(0xFFF8FAFC), // 💡 调整此处更改激活时的内凹微底色
                                        unfocusedContainerColor = if (isDarkTheme) Color(0xFF262C36) else Color(0xFFF8FAFC), // 💡 调整此处更改未激活时的内凹微底色
                                        cursorColor = MaterialTheme.colorScheme.primary // 💡 调整此处更改输入光标颜色
                                    ),
                                    supportingText = {
                                        Text(
                                            text = "${tempSubtitleInput.length}/12",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                            fontSize = 12.sp, // 💡 调整此处更改右下角字数统计字号（默认 12.sp）
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (tempSubtitleInput.length == 12) Color(0xFFEF4444) else (if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)) // 💡 调整此处更改满额警示红（0xFFEF4444）与平时灰
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = {
                                    if (tempSubtitleInput.isNotBlank()) {
                                        customSubtitle = tempSubtitleInput.safeTakeChars(12)
                                        scope.launch {
                                            context.dataStore.edit { it[SUBTITLE_KEY] = customSubtitle }
                                        }
                                    }
                                    showEditSubtitleDialog = false
                                },
                                shape = RoundedCornerShape(14.dp), // 💡 调整此处更改【保存】按钮圆角（默认 14.dp）
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkTheme) Color(0xFF10B981) else Color(0xFF2C2C2C), // 💡 调整此处更改【保存】按钮背景色
                                    contentColor = Color.White // 💡 调整此处更改【保存】按钮文字颜色（纯白）
                                ),
                                elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp, // 💡 调整此处更改默认阴影高度
                                    pressedElevation = 2.dp // 💡 调整此处更改按压触感阴影
                                ),
                                modifier = Modifier
                                    .height(42.dp) // 💡 调整此处更改【保存】按钮高度（默认 42.dp）
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "保存",
                                    fontSize = 14.5.sp, // 💡 调整此处更改【保存】字号（默认 14.5.sp，SemiBold）
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(
                                onClick = { showEditSubtitleDialog = false },
                                shape = RoundedCornerShape(14.dp), // 💡 调整此处更改【取消】按钮圆角（与保存按钮同步）
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = textSecondary // 💡 调整此处更改【取消】按钮文字颜色（柔和次级灰）
                                ),
                                modifier = Modifier
                                    .height(42.dp) // 💡 调整此处更改【取消】按钮高度（与保存按钮同步 42.dp）
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "取消",
                                    fontSize = 14.5.sp, // 💡 调整此处更改【取消】字号
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    )
                }

                // =========================================================================================
                // 🛡️ [跨页面切换防穿模白盾：White Shield Isolation]
                // 【灵魂三问】：
                // 1. 这是什么？
                //    - 这是 Aura 18.0 独创的物理层级防穿模保护层（处于 Z 轴 9f）。
                // 2. 它能干什么？
                //    - 当任意二级全屏 Dialog（探索空间占位、时间看板、设置中心）开启时同步淡入；
                //    - 严格垫在 10f 的二级全屏大窗口正下方、0f 的主页元素正上方；
                //    - 彻底遮蔽底层主页的标题与 "+" 号，根除跨页面切换时由于透明度叠加或过渡插值造成的主页元素闪烁漏出。
                // 3. 怎么调整参数？
                //    - 必须严守 Z 轴排序：底栏(20f) > 二级全屏页(10f) > 白盾(9f) > 主页(0f)，不可随意调换。
                //    - 渐隐渐显速度：修改 tween(250) 和 tween(200)。
                // =========================================================================================
                val isAnyPageOpen = showHelpDialog || showAlarmDialog || isSettingsOpen
                androidx.compose.animation.AnimatedVisibility(
                    visible = isAnyPageOpen,
                    modifier = androidx.compose.ui.Modifier.zIndex(9f), // 💡 调整此处更改防穿模白盾层级（严格维持 9f，垫在二级全屏页 10f 之下、主页 0f 之上）
                    enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(250)), // 💡 调整此处更改白盾淡入动画时长（默认 250ms）
                    exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))  // 💡 调整此处更改白盾淡出动画时长（默认 200ms）
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxSize()
                            .background(if (isDarkTheme) Color(0xFF121212) else Color.White) // 💡 适配深浅色防穿模隔离盾，彻底杜绝深色下白屏闪烁
                    )
                }

                // 3. 底栏第 2 个 Tab：实验性功能/探索空间占位页面 (创意孵化中)
                androidx.compose.animation.AnimatedVisibility(
                    visible = showHelpDialog,
                    modifier = androidx.compose.ui.Modifier.zIndex(10f), // 🌟 核心修复：提升 Z 轴层级
                    enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(250)),
                    exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
                ) {
                    BackHandler { showHelpDialog = false }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isDarkTheme) Color(0xFF121212) else Color.White)
                            .auraBottomGradient(
                                color = bottomGradientColor,
                                glowFractionProvider = { glowFraction }
                            )
                            .pointerInput(Unit) { detectTapGestures { } },
                        contentAlignment = Alignment.Center
                    ) {
                        // 🌟 极简预留占位文案 (水平和垂直居中，Aura 极客留白感)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        ) {
                            Text(
                                text = "// 创意孵化中",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = if (isDarkTheme) Color(0xFFF1F5F9).copy(alpha = 0.85f) else Color(0xFF1E293B).copy(alpha = 0.75f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "// New Module Coming Soon...",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = if (isDarkTheme) Color(0xFF94A3B8).copy(alpha = 0.75f) else Color(0xFF64748B).copy(alpha = 0.65f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }

                // 🌟 预留的闹钟/便签全屏沉浸式页面 (大房子毛坯房)
                androidx.compose.animation.AnimatedVisibility(
                    visible = showAlarmDialog,
                    modifier = androidx.compose.ui.Modifier.zIndex(10f), // 🌟 核心修复：提升 Z 轴层级
                    enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(250)),
                    exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
                ) {
                    var selectedAuraVersion by remember { mutableStateOf<AuraVersionInfo?>(null) }
                    var displayedAuraVersion by remember { mutableStateOf<AuraVersionInfo?>(null) }
                    if (selectedAuraVersion != null) {
                        displayedAuraVersion = selectedAuraVersion
                    }

                    BackHandler {
                        if (selectedAuraVersion != null) {
                            selectedAuraVersion = null
                        } else {
                            showAlarmDialog = false
                        }
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isDarkTheme) Color(0xFF121212) else Color.White)
                            .auraBottomGradient(
                                color = bottomGradientColor,
                                glowFractionProvider = { glowFraction }
                            )
                            .pointerInput(Unit) { detectTapGestures { } } // 拦截穿透点击
                    ) {
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        val maxCollapsePx = remember(density) { with(density) { 140.dp.toPx() } }

                        // 🌟 精准消除父容器多余滚动距离 (Over-scroll Gap)：
                        // 卡片总高为 636.dp (216.dp 顶部留白 + 420.dp 卡片)，目标最大滚动阈值恰好为 140.dp (头像完全收缩点)
                        // 动态计算底部留白高度，使父容器滚动到底时 maxValue 恰好等于 140.dp (maxHeight - 496.dp)
                        val bottomSpacerHeight = (maxHeight - 496.dp).coerceAtLeast(0.dp)

                        // 🌟 物理硬顶拦截器：即使发生极端 Fling，一旦头像收缩完毕 (达到 140.dp) 绝不允许向上多推 1 像素
                        val parentClampConnection = remember(maxCollapsePx) {
                            object : NestedScrollConnection {
                                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                    if (available.y < 0f && alarmScrollState.value >= maxCollapsePx.roundToInt()) {
                                        return available
                                    }
                                    if (available.y < 0f) {
                                        val current = alarmScrollState.value
                                        val maxAllowed = maxCollapsePx.roundToInt()
                                        val delta = -available.y
                                        if (current + delta > maxAllowed) {
                                            val allowed = (maxAllowed - current).coerceAtLeast(0)
                                            val excess = delta - allowed
                                            return Offset(0f, -excess)
                                        }
                                    }
                                    return Offset.Zero
                                }

                                override suspend fun onPreFling(available: Velocity): Velocity {
                                    if (available.y < 0f && alarmScrollState.value >= maxCollapsePx.roundToInt()) {
                                        return available
                                    }
                                    return Velocity.Zero
                                }
                            }
                        }

                        // 🌟 红区回弹平移动画增强 (Card List Spring Bounce Animation)：
                        // 捕获 parentScrollState 的滚动位移，使用 Spring 弹簧规格产生物理回弹阻尼感
                        val rawScrollY = alarmScrollState.value.toFloat()
                        val animatedScrollY by animateFloatAsState(
                            targetValue = rawScrollY,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy, // 中等回弹
                                stiffness = Spring.StiffnessMedium // 提高到中等刚度，敏捷紧凑跟手，大幅减少位移时间差并保留停靠回弹
                            ),
                            label = "CardListBounceAnimation"
                        )
                        val animatedOffsetY = rawScrollY - animatedScrollY

                        // 🌟 头像视觉真实中心与物理半径状态容器 (仅由头像自身圆形控件在 onGloballyPositioned 中动态上报)
                        var avatarPositionState by remember { mutableStateOf<Pair<Offset, Float>?>(null) }

                        // 0. 最底层沉浸式全屏背景：Aura 基因果冻方块游乐场 (全屏独立悬浮，仅保留头像自身力场，彻底清除上方虚位力场)
                        AuraJellyPlayground(
                            scrollState = alarmScrollState,
                            avatarPositionProvider = { avatarPositionState },
                            modifier = Modifier.fillMaxSize()
                        )

                        // 1. 中层：可滚动内容区（蓝区全局滚动：在顶部头像旁或底部留白滑动，控制父容器滚动及头像丝滑收缩）
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f) // 🌟 视差层级保护：卡片内容区悬浮于头像图层之上，彻底杜绝快速下拉时头像遮挡卡片
                                .nestedScroll(parentClampConnection)
                                .verticalScroll(alarmScrollState)
                        ) {
                            // 顶部留白：为初始状态居中大头像留出完整展示空间 (适配 startY = 140.dp，保持与卡片 30.dp 和谐间距)
                            Spacer(modifier = Modifier.height(216.dp))

                            // 🌟 极简圆角大卡片与 1:1 等大二级版本更新弹窗容器（红区：严格约束高度 420.dp，注入物理回弹动画）
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .height(420.dp)
                                    .zIndex(1f) // 🌟 红区视差保护
                                    .graphicsLayer { translationY = animatedOffsetY }
                            ) {
                                // 1. 常驻底层大卡片（极客发光灯牌 + 历代版本时间线）
                                AuraMainDashboardCard(
                                    onSelectVersion = { version -> selectedAuraVersion = version },
                                    modifier = Modifier.fillMaxSize()
                                )

                                // 2. 1:1 等大重合的二级版本更新弹窗 (无缝覆盖层，入场与退场对称 120Hz 极简微动效)
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = selectedAuraVersion != null,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
                                            scaleIn(initialScale = 0.95f, animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)) + // 💡 调整此处更改弹窗退出淡出耗时（默认 200ms）
                                           scaleOut(targetScale = 0.95f, animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)), // 💡 调整此处更改弹窗退出缩放比例与耗时（默认 0.95f，200ms）
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    displayedAuraVersion?.let { version ->
                                        AuraReleaseNotesModal(
                                            versionInfo = version,
                                            onClose = { selectedAuraVersion = null },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            // 底部留白（蓝区）：动态精确切除多余距离，确保触底后卡片稳如泰山
                            Spacer(modifier = Modifier.height(bottomSpacerHeight))
                        }

                        // 2. 顶层：Telegram 液态水滴头像吸入摄像头动效层 (直达屏幕物理最顶部，由头像自身精准上报真实视觉中心)
                        TelegramAvatarHeader(
                            scrollState = alarmScrollState,
                            onAvatarPositioned = { center, radius ->
                                avatarPositionState = center to radius
                            }
                        )
                    }
                }

                // 4. 图片自由排序弹窗
                if (showSortDialog) {
                    ImageSortDialog(
                        currentOrder = itemOrder,
                        uris = selectedImageUris,
                        displayCount = displayCount,
                        onDismiss = { showSortDialog = false },
                        onSave = { newOrder ->
                            itemOrder.clear()
                            itemOrder.addAll(newOrder)
                            saveUrisToDisk() // 自动持久化保存到 DataStore
                        }
                    )
                }

                // 5. 大图全屏预览页面 (带有 3D 翻页和丝滑双指缩放)
                if (previewUri != null) {
                    val safeInitialPage = remember(previewUri, displayCount) {
                        val rawIndex = selectedImageUris.indexOf(previewUri)
                        val rawPage = itemOrder.indexOf(rawIndex).coerceAtLeast(0)
                        rawPage.coerceIn(0, (displayCount - 1).coerceAtLeast(0))
                    }
                    val pagerState = rememberPagerState(initialPage = safeInitialPage, pageCount = { displayCount })
                    var isCurrentPageZoomed by remember { mutableStateOf(false) }

                    FullScreenDialog(
                        onDismiss = { previewUri = null },
                        isZoomed = isCurrentPageZoomed
                    ) { pagerModifier, bgAlpha, onTriggerExit ->
                        val currentBgIndex =
                            itemOrder.getOrNull(pagerState.currentPage) ?: pagerState.currentPage
                        val currentBgUri = selectedImageUris.getOrNull(currentBgIndex)

                        Box(modifier = Modifier.fillMaxSize()) {
                            // 🌟 纯黑底色 + 高斯模糊流光背景（铺满全屏，不缩放，随 bgAlpha 从 0 平滑淡入，第0帧绝对透明零闪黑）
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = bgAlpha }
                                    .background(Color(0xFF0F1115))
                            ) {
                                if (currentBgUri != null) {
                                    val bgImageRequest = remember(currentBgUri) {
                                        ImageRequest.Builder(context)
                                            .data(currentBgUri)
                                            .size(coil.size.Size(100, 100))
                                            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                                            .allowHardware(true)
                                            .crossfade(true)
                                            .build()
                                    }
                                    AsyncImage(
                                        model = bgImageRequest,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(45.dp), // 高斯模糊营造氛围光晕
                                        contentScale = ContentScale.Crop
                                    )
                                    // 叠加一层渐变暗角遮罩，让前景图片更突出
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.65f),
                                                        Color.Black.copy(alpha = 0.45f),
                                                        Color.Black.copy(alpha = 0.75f)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }

                            // 支持 3D 翻页过渡与丝滑缩放的主画廊
                            // 核心 Pager (主体图片滑动区：绑定专属呼吸放大与下拉缩小动效)
                            HorizontalPager(
                                state = pagerState,
                                modifier = pagerModifier,
                                userScrollEnabled = !isCurrentPageZoomed,
                                beyondViewportPageCount = 1
                            ) { page ->
                                val realIndex = itemOrder.getOrNull(page) ?: page
                                val currentUri = selectedImageUris.getOrNull(realIndex)

                                if (currentUri != null) {
                                    var scale by rememberSaveable(page) { mutableFloatStateOf(1f) }
                                    var offsetX by rememberSaveable(page) { mutableFloatStateOf(0f) }
                                    var offsetY by rememberSaveable(page) { mutableFloatStateOf(0f) }

                                    val isZoomed = scale > 1.05f
                                    LaunchedEffect(isZoomed, pagerState.currentPage) {
                                        if (pagerState.currentPage == page) {
                                            isCurrentPageZoomed = isZoomed
                                        }
                                    }

                                    // 当切走页面时，自动将上一页的缩放复位
                                    LaunchedEffect(pagerState.currentPage) {
                                        if (pagerState.currentPage != page) {
                                            scale = 1f; offsetX = 0f; offsetY = 0f
                                        }
                                    }

                                    // 丝滑弹簧动画驱动缩放与位移
                                    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
                                        targetValue = scale,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        label = "zoomScale"
                                    )
                                    val animatedOffsetX by androidx.compose.animation.core.animateFloatAsState(
                                        targetValue = offsetX,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        label = "offsetX"
                                    )
                                    val animatedOffsetY by androidx.compose.animation.core.animateFloatAsState(
                                        targetValue = offsetY,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        label = "offsetY"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                val pageOffset = kotlin.math.abs((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
                                                scaleX = 1f - (pageOffset * 0.12f)
                                                scaleY = 1f - (pageOffset * 0.12f)
                                                alpha = 1f - (pageOffset * 0.4f)
                                            }
                                            .pointerInput(page) {
                                                // 专属双击放大缩小手势
                                                detectTapGestures(
                                                    onDoubleTap = { offset ->
                                                        if (scale > 1f) {
                                                            // 已经是放大状态，双击恢复原图
                                                            scale = 1f
                                                            offsetX = 0f
                                                            offsetY = 0f
                                                        } else {
                                                            // 原图状态，双击放大 2.5 倍
                                                            scale = 2.5f

                                                            // 计算放大后，点击的位置应该如何平移，才能让点击的点在屏幕中心
                                                            val maxOffsetX = (size.width * (scale - 1f) / 2f) / scale
                                                            val maxOffsetY = (size.height * (scale - 1f) / 2f) / scale

                                                            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                                                            val targetOffsetX = (center.x - offset.x) * (scale - 1f) / scale
                                                            val targetOffsetY = (center.y - offset.y) * (scale - 1f) / scale

                                                            offsetX = targetOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                                            offsetY = targetOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
                                                        }
                                                    }
                                                )
                                            }

                                            .pointerInput(page) {
                                                // 经典双指拖动与放大缩小算法
                                                awaitEachGesture {
                                                    awaitFirstDown(requireUnconsumed = false)
                                                    do {
                                                        val event = awaitPointerEvent()
                                                        val zoom = event.calculateZoom()
                                                        val pan = event.calculatePan()
                                                        val isMultiTouch = event.changes.size > 1
                                                        val deltaZoom = 1f + (zoom - 1f) * 0.85f
                                                        val newScale = (scale * deltaZoom).coerceIn(1f, 5f)

                                                        if (newScale > 1.02f || isMultiTouch) {
                                                            if (newScale > 1.02f) {

                                                                val maxOffsetX = (size.width * (newScale - 1f) / 2f) / newScale
                                                                val maxOffsetY = (size.height * (newScale - 1f) / 2f) / newScale
                                                                val nextOffsetX = offsetX + (pan.x / newScale)
                                                                if (nextOffsetX in -maxOffsetX..maxOffsetX) {
                                                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                                                }

                                                                offsetX = nextOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                                                offsetY = (offsetY + (pan.y / newScale)).coerceIn(-maxOffsetY, maxOffsetY)

                                                                scale = newScale
                                                            } else {
                                                                scale = 1f; offsetX = 0f; offsetY = 0f
                                                            }
                                                        }
                                                    } while (event.changes.any { it.pressed })

                                                    if (scale < 1.08f) {
                                                        scale = 1f; offsetX = 0f; offsetY = 0f
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val displayMetrics = context.resources.displayMetrics
                                        val painter = rememberAsyncImagePainter(
                                            model = ImageRequest.Builder(context)
                                                .data(currentUri)
                                                .size(coil.size.Size(displayMetrics.widthPixels, displayMetrics.heightPixels))
                                                .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                                                .allowHardware(true)
                                                .crossfade(true)
                                                .build()
                                        )
                                        val intrinsicSize = painter.intrinsicSize
                                        val imageRatio = if (intrinsicSize != androidx.compose.ui.geometry.Size.Unspecified && intrinsicSize.height > 0f && intrinsicSize.width > 0f) {
                                            intrinsicSize.width / intrinsicSize.height
                                        } else {
                                            val successDrawable = (painter.state as? coil.compose.AsyncImagePainter.State.Success)?.result?.drawable
                                            if (successDrawable != null && successDrawable.intrinsicHeight > 0 && successDrawable.intrinsicWidth > 0) {
                                                successDrawable.intrinsicWidth.toFloat() / successDrawable.intrinsicHeight.toFloat()
                                            } else {
                                                null
                                            }
                                        }

                                        Image(
                                            painter = painter,
                                            contentDescription = "预览图片",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = if (scale <= 1.05f) 12.dp else 0.dp)
                                                .wrapContentSize(Alignment.Center)
                                                .then(
                                                    if (imageRatio != null) Modifier.aspectRatio(imageRatio) else Modifier.fillMaxWidth()
                                                )
                                                .graphicsLayer(
                                                    scaleX = animatedScale,
                                                    scaleY = animatedScale,
                                                    translationX = animatedOffsetX,
                                                    translationY = animatedOffsetY
                                                )
                                                .clip(RoundedCornerShape(if (scale <= 1.05f) 16.dp else 0.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                } else {
                                    // 升级版轻量空画框提示 (纯净玻璃框)
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color.White.copy(alpha = 0.06f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                0.5.dp,
                                                Color.White.copy(alpha = 0.12f)
                                            )
                                        ) {
                                            Text(
                                                text = "这个画框还是空的哦 ~",
                                                color = Color.White.copy(alpha = 0.65f),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(
                                                    horizontal = 24.dp,
                                                    vertical = 14.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // 顶部悬浮胶囊栏
                            // 悬浮顶部操作栏 (页码展示和关闭按钮)
                            Row(
                                modifier = Modifier
                                    .graphicsLayer { alpha = bgAlpha }
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 左侧：页码胶囊（如 2 / 7）
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Black.copy(alpha = 0.45f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        0.5.dp,
                                        Color.White.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Text(
                                        text = "${pagerState.currentPage + 1} / $displayCount",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }

                                // 右侧：独立圆形关闭按钮
                                Surface(
                                    onClick = onTriggerExit,
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.45f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        0.5.dp,
                                        Color.White.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "✕",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // 🌟 核心升级 4：底部灵动圆点指示器 (动态避让系统三键导航栏)
                            Row(
                                modifier = Modifier
                                    .graphicsLayer { alpha = bgAlpha }
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 24.dp)
                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(displayCount) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    val dotWidth by animateDpAsState(
                                        targetValue = if (isSelected) 18.dp else 6.dp,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        label = "dotWidth"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(6.dp)
                                            .width(dotWidth)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color.White else Color.White.copy(alpha = 0.35f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. 系统设置大面板 (已切流至 V2 ChatGPT 风格分组大卡片)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSettingsOpen,
                    modifier = androidx.compose.ui.Modifier.zIndex(10f), // 🌟 核心修复：提升 Z 轴层级
                    enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(250)),
                    exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
                ) {
                    AuraSettingsScreen(
                        currentCount = displayCount,
                        onCountChange = { newCount ->
                            // 1. 内存状态实时更新：确保滚轮滑动、大字号与图框矩阵点亮绝对丝滑跟手
                            val safeCount = newCount.coerceIn(1, MAX_FRAME_COUNT)
                            displayCount = safeCount
                            // 2. DataStore 磁盘写入 300ms 防抖：杜绝快速滑动滚轮时并发触发写盘事务导致的 I/O 阻塞
                            savePhotoCountJob?.cancel()
                            savePhotoCountJob = scope.launch {
                                delay(300L)
                                context.dataStore.edit {
                                    it[PHOTO_COUNT_KEY] = safeCount.toString()
                                }
                            }
                        },
                        themeMode = currentThemeMode,
                        onThemeChange = { newMode ->
                            saveThemeJob?.cancel()
                            saveThemeJob = scope.launch {
                                context.dataStore.edit { prefs ->
                                    prefs[KEY_APP_THEME_MODE] = newMode.value
                                }
                            }
                        },
                        morningColor = morningColor,
                        noonColor = noonColor,
                        afternoonColor = afternoonColor,
                        nightColor = nightColor,
                        onSaveColors = { m, n, a, ni ->
                            scope.launch {
                                context.dataStore.edit { prefs ->
                                    prefs[COLOR_MORNING_KEY] = m.toArgb()
                                    prefs[COLOR_NOON_KEY] = n.toArgb()
                                    prefs[COLOR_AFTERNOON_KEY] = a.toArgb()
                                    prefs[COLOR_NIGHT_KEY] = ni.toArgb()
                                }
                            }
                        },
                        isBiometricLockEnabled = isBiometricLockEnabled,
                        lockTimeoutMinutes = timeoutMinutes,
                        onToggleBiometricLock = { targetState ->
                            val biometricManager = BiometricManager.from(context)
                            val canAuth = biometricManager.canAuthenticate(
                                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                            )
                            if (canAuth != BiometricManager.BIOMETRIC_SUCCESS &&
                                canAuth != BiometricManager.BIOMETRIC_STATUS_UNKNOWN
                            ) {
                                Toast.makeText(
                                    context,
                                    "设备未设置指纹/面容或密码，请先在系统设置中录入",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val act = context as? FragmentActivity
                                if (act != null) {
                                    showBiometricPrompt(
                                        activity = act,
                                        title = if (targetState) "开启应用锁" else "关闭应用锁",
                                        subtitle = "需要身份验证",
                                        onSuccess = {
                                            scope.launch {
                                                context.dataStore.edit { prefs ->
                                                    prefs[BIOMETRIC_LOCK_KEY] = targetState
                                                }
                                            }
                                            Toast.makeText(
                                                context,
                                                if (targetState) "应用锁已开启" else "应用锁已关闭",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { errorCode, errString ->
                                            if (errorCode == BiometricPrompt.ERROR_LOCKOUT || errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT) {
                                                Toast.makeText(context, errString.ifBlank { "尝试次数过多，请稍后再试" }, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        onTimeoutChange = { minutes ->
                            scope.launch {
                                context.dataStore.edit { prefs ->
                                    prefs[BIOMETRIC_LOCK_TIMEOUT_KEY] = minutes
                                }
                            }
                        },
                        bottomGradientColor = bottomGradientColor,
                        glowFraction = glowFraction,
                        listState = settingsLazyListState,
                        onSubScreenChange = { isOpen -> isSettingsSubScreenOpen = isOpen },
                        onBack = {
                            isSettingsSubScreenOpen = false
                            isSettingsOpen = false
                        }
                    )
                }

                // 🌟 设置面板收起时：若仍有未落盘的防抖写入任务，立即兜底刷盘，确保零数据丢失
                LaunchedEffect(isSettingsOpen) {
                    if (!isSettingsOpen && savePhotoCountJob?.isActive == true) {
                        savePhotoCountJob?.cancel()
                        context.dataStore.edit {
                            it[PHOTO_COUNT_KEY] = displayCount.toString()
                        }
                    }
                }
            }
        }

        // 🌟 私密空间锁拦截层 (仅在应用锁功能开启且处于锁定状态时展示)
        if (isBiometricLockEnabled && overlayAlpha > 0f) {
            BiometricLockOverlay(
                alpha = overlayAlpha,
                isLocked = isLocked,
                onTriggerUnlock = {
                    if (activity != null && !isBiometricPromptActive) {
                        showBiometricPrompt(
                            activity = activity,
                            onSuccess = {
                                isLocked = false
                                needsPromptOnResume = false
                            },
                            onError = { errorCode, errString ->
                                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_CANCELED) {
                                    needsPromptOnResume = true
                                } else if (errorCode == BiometricPrompt.ERROR_LOCKOUT || errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT) {
                                    Toast.makeText(context, errString.ifBlank { "尝试次数过多，请稍后再试" }, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            )
        }

        // 拦截系统返回键：仅在功能开启且锁定状态下按返回键直接退出应用，防止绕过锁屏
        BackHandler(enabled = isBiometricLockEnabled && isLocked) {
            activity?.finish()
        }
    }
}

// =========================================================================================
// 💊 [底部灵动悬浮胶囊导航栏：ExpandableFab (Liquid Glass Design)]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 18.0 的标志性底部灵动胶囊导航系统（分段式静态图标 + 底部物理弹簧果冻滑块）。
// 2. 它能干什么？
//    - 承载主页（首页）、探索空间（Tab 2 实验性功能）、时间线（版本看板）、设置（偏好中心）四个分段 Tab；
//    - 采用 iOS 旗舰级“液态玻璃（Liquid Glass）”设计：微白通透底板（40% Alpha）+ 渐变高光边框 + 弥散悬浮阴影；
//    - 内部拥有物理弹性指示器（indicatorOffset），在 Tab 切换时伴随阻尼回弹优雅滑移；
//    - 设置齿轮集成弹簧阻尼旋转物理反馈（settingsGearRotation）。
// 3. 怎么调整参数？
//    - 尺寸比例：调整 containerHeight（默认 68.dp）、indicatorHeight（默认 56.dp）、itemWidth（默认 76.dp）。
//    - 胶囊圆角：调整 containerShape = RoundedCornerShape(28.dp) 与 indicatorShape = RoundedCornerShape(20.dp)。
//    - 滑块回弹手感：调整 indicatorOffset 动画规范中的 spring(dampingRatio = 0.6f, stiffness = 400f)。
//    - 玻璃通透感：修改 Surface 的 color = Color.White.copy(alpha = 0.40f) 与边框高光 Gradient。
// =========================================================================================
@Composable
fun ExpandableFab(
    selectedIndex: Int,
    onHomeClick: () -> Unit,
    onTab2Click: () -> Unit,
    onAlarmClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // 🌟 1. 尺寸与比例参数（你想调整高矮胖瘦在这里改）
    val containerHeight = 68.dp // 💡 调整此处更改外层大胶囊高度（默认 68.dp，过小会导致内部滑块被挤压）
    val indicatorHeight = 56.dp // 💡 调整此处更改内部果冻滑块高度（默认 56.dp，与外层保持 12.dp 差值营造双层液态质感）
    val itemWidth = 76.dp       // 💡 调整此处更改每个 Tab 槽位宽度（决定滑块宽度与底栏总宽度，默认 76.dp）

    val isDarkTheme = LocalIsDarkTheme.current
    val brandGreen = androidx.compose.ui.graphics.Color(0xFFABEE4F) // 💡 调整此处更改 Aura 品牌荧光绿高亮主色
    val inactiveColor = if (isDarkTheme) Color(0xFFE2E8F0) else androidx.compose.ui.graphics.Color(0xFF080808) // 💡 调整此处更改底栏未选中图标与文字颜色
    // 🌟 修改为半透明亮白，打造“双层液态玻璃”的通透质感
    val indicatorBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.30f) // 💡 调整此处更改滑块基底透明度

    val indicatorOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = (selectedIndex * itemWidth.value).dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.6f, // 💡 调整此处更改果冻滑块弹簧阻尼比（0.6f 为中度回弹，数值越小越弹）
            stiffness = 400f     // 💡 调整此处更改果冻滑块弹簧刚度/回弹速度（400f 干脆且富有韧性质感）
        ),
        label = "indicatorOffset"
    )

    val containerShape = RoundedCornerShape(28.dp) // 💡 调整此处更改外层大胶囊圆角（默认 28.dp，匹配 68.dp 高度）
    val indicatorShape = RoundedCornerShape(20.dp) // 💡 调整此处更改内部果冻滑块圆角（默认 20.dp，与外层大圆角同心呼应）

    // 🌟 触觉微反馈与 300ms 防抖判定（拦截高频连击动画打断，彻底消除指示器坐标跳跃与闪烁）
    val haptic = LocalHapticFeedback.current
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val handleTabClick: (() -> Unit) -> Unit = { action ->
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime >= 300L) {
            lastClickTime = currentTime
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            action()
        }
    }

    // 🌟 设置齿轮弹簧阻尼旋转动效 (Bouncy Spring Physics)
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, // 💡 调整此处更改设置齿轮点击旋转阻尼比
            stiffness = Spring.StiffnessLow                 // 💡 调整此处更改设置齿轮点击旋转回弹速度
        ),
        label = "settingsGearRotation"
    )

    // 🌟 纸飞机起飞与归位动效状态机 (Paper Plane Flight Animation)
    val fabScope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val flyDistanceXPx = with(density) { 40.dp.toPx() }   // 💡 调整此处更改飞机起飞的水平位移距离（默认 +40.dp 向右）
    val flyDistanceYPx = with(density) { (-40).dp.toPx() } // 💡 调整此处更改飞机起飞的垂直位移距离（默认 -40.dp 向上）
    val planeOffsetX = remember { Animatable(0f) }
    val planeOffsetY = remember { Animatable(0f) }
    val planeScale = remember { Animatable(1.0f) }
    val planeAlpha = remember { Animatable(1.0f) }
    var planeFlightJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // 🌟 第 2 Tab (爱心/志愿) Q 弹缩放动效状态机
    val tab2Scale = remember { Animatable(1.0f) }
    var tab2BounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // 🌟 iOS 级“液态玻璃 (Liquid Glass)”材质：极度通透微白基底 + 折射渐变高光边框 + 柔和弥散悬浮阴影
    androidx.compose.material3.Surface(
        modifier = androidx.compose.ui.Modifier
            .height(containerHeight)
            .shadow(
                elevation = 16.dp, // 💡 调整此处更改液态玻璃底栏悬浮阴影高度（默认 16.dp）
                shape = containerShape,
                spotColor = Color.Black.copy(alpha = 0.08f),
                ambientColor = Color.Black.copy(alpha = 0.04f)
            ),
        shape = containerShape,
        color = if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.40f), // 💡 调整此处更改液态玻璃通透基底透明度
        shadowElevation = 0.dp,                  // 避免生硬的原生纯黑阴影
        border = androidx.compose.foundation.BorderStroke(
            width = 0.8.dp, // 💡 调整此处更改液态玻璃外层折射高光边框粗细（默认 0.8.dp）
            brush = Brush.linearGradient(
                colors = if (isDarkTheme) {
                    listOf(
                        Color.White.copy(alpha = 0.30f),
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.02f)
                    )
                } else {
                    listOf(
                        Color.White.copy(alpha = 0.60f),
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.05f)
                    )
                }
            )
        )
    ) {

        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxHeight()
                .padding(horizontal = 6.dp), // 收紧左右边距，增强包裹感
            contentAlignment = androidx.compose.ui.Alignment.CenterStart
        ) {
            // 滑动指示器 (液态高光水珠)
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .offset(x = indicatorOffset)
                    .size(width = itemWidth, height = indicatorHeight)
                    .shadow(
                        elevation = 2.dp,
                        shape = indicatorShape,
                        spotColor = Color.Black.copy(alpha = 0.04f),
                        ambientColor = Color.Black.copy(alpha = 0.02f)
                    )
                    .clip(indicatorShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f), // 顶部水珠聚光高光
                                Color.White.copy(alpha = 0.15f)  // 底部柔和透亮过渡
                            )
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.10f)
                            )
                        ),
                        shape = indicatorShape
                    )
            )

            // 顶层图标矩阵
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Home 槽位
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(width = itemWidth, height = containerHeight)
                        .clickable(
                            interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                handleTabClick {
                                    onHomeClick()
                                }
                            }
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_aura_logo),
                        contentDescription = "Home",
                        tint = if (selectedIndex == 0) brandGreen else inactiveColor,
                        modifier = androidx.compose.ui.Modifier.size(32.dp)
                    )
                }

                // 🌟 第 2 Tab：实验性功能/探索空间槽位 (点击触发专属 Q 弹缩放微动效)
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(width = itemWidth, height = containerHeight)
                        .clickable(
                            interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                handleTabClick {
                                    tab2BounceJob?.cancel()
                                    tab2BounceJob = fabScope.launch {
                                        // 第一阶段（按下收缩）：图标迅速缩小至 0.7f，营造干脆的按压阻尼感
                                        tab2Scale.animateTo(
                                            targetValue = 0.7f, // 💡 调整此处更改按下缩放大小（默认 0.7f，营造干脆的按压阻尼感）
                                            animationSpec = tween(durationMillis = 80) // 💡 调整此处更改按下收缩耗时（建议 50ms-100ms）
                                        )
                                        // 第二阶段（Q 弹跃出）：回弹恢复至 1.0f，带物理弹性果冻感
                                        tab2Scale.animateTo(
                                            targetValue = 1.0f, // 💡 调整此处更改回弹终值（恢复原尺寸 1.0f）
                                            animationSpec = spring(
                                                dampingRatio = 0.5f,               // 💡 调整此处更改回弹阻尼（0.5f 具有果冻般的 Q 弹感）
                                                stiffness = Spring.StiffnessMedium // 💡 调整此处更改回弹刚度（StiffnessMedium 中等弹性）
                                            )
                                        )
                                    }
                                    onTab2Click() // 🌟 路由非阻塞：即时触发原有页面交互
                                }
                            }
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_volunteer_activism),
                        contentDescription = "Explore",
                        tint = inactiveColor,
                        modifier = androidx.compose.ui.Modifier
                            .size(26.dp)//修改图标大小
                            .graphicsLayer {
                                scaleX = tab2Scale.value
                                scaleY = tab2Scale.value
                            }
                    )
                }

                // 🌟 第 3 Tab：纸飞机起飞槽位 (点击触发向右上起飞冲刺 + 幽灵归位淡入微动效)
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(width = itemWidth, height = containerHeight)
                        .clickable(
                            interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                handleTabClick {
                                    planeFlightJob?.cancel()
                                    planeFlightJob = fabScope.launch {
                                        // 阶段一：向右上起飞冲刺 (Fly Away) + 缩小至 0.2f + 渐隐至 0.0 (原封不动保留用户 600ms 定制参数)
                                        launch {
                                            kotlinx.coroutines.coroutineScope {
                                                launch {
                                                    planeOffsetX.animateTo(
                                                        targetValue = flyDistanceXPx, // 💡 调整此处更改飞机起飞水平位移终值（默认 +40.dp 向右）
                                                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing) // 💡 调整此处更改飞机起飞耗时（当前 600ms）
                                                    )
                                                }
                                                launch {
                                                    planeOffsetY.animateTo(
                                                        targetValue = flyDistanceYPx, // 💡 调整此处更改飞机起飞垂直位移终值（默认 -40.dp 向上）
                                                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing) // 💡 调整此处更改飞机起飞耗时（当前 600ms）
                                                    )
                                                }
                                                launch {
                                                    planeScale.animateTo(
                                                        targetValue = 0.2f, // 💡 调整此处更改飞机起飞缩放比例（当前 0.2f）
                                                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing) // 💡 调整此处更改飞机缩放耗时（当前 600ms）
                                                    )
                                                }
                                                launch {
                                                    planeAlpha.animateTo(
                                                        targetValue = 0.0f, // 💡 调整此处更改飞机起飞透明度终值（渐隐至 0.0）
                                                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing) // 💡 调整此处更改飞机渐隐耗时（当前 600ms）
                                                    )
                                                }
                                            }
                                        }

                                        // 等待飞出完全跑完（650ms 缓冲，确保 600ms 起飞动画彻底播完）
                                        delay(650) // 💡 调整此处更改飞出等待时间（默认 650ms）

                                        // 阶段二：重置初始状态（隐身 + 缩至最小 0.0f，为后续弹起做准备）
                                        planeOffsetX.snapTo(0f) // 💡 调整此处瞬间重置飞机 X 轴初始位置
                                        planeOffsetY.snapTo(0f) // 💡 调整此处瞬间重置飞机 Y 轴初始位置
                                        planeAlpha.snapTo(0f)   // 💡 调整此处瞬间重置飞机透明度为 0（隐身）
                                        planeScale.snapTo(0.0f) // 💡 调整此处瞬间重置飞机缩放比例为 0（为后续弹起做准备）

                                        // 阶段三：Q 弹淡入归位 (并发启动淡入 300ms + Spring Q 弹跃出)
                                        launch {
                                            planeAlpha.animateTo(
                                                targetValue = 1.0f, // 💡 调整此处更改飞机归位淡入透明度终值 (1.0 完全显现)
                                                animationSpec = tween(durationMillis = 300) // 💡 调整此处更改飞机归位淡入耗时（默认 300ms）
                                            )
                                        }
                                        launch {
                                            planeScale.animateTo(
                                                targetValue = 1.0f, // 💡 调整此处更改飞机归位弹起缩放终值 (1.0 恢复原尺寸)
                                                animationSpec = spring(
                                                    dampingRatio = 0.5f,               // 💡 调整此处更改飞机弹起阻尼比（0.5f 具有充满活力的回弹感）
                                                    stiffness = Spring.StiffnessMedium // 💡 调整此处更改飞机弹起刚度（StiffnessMedium 中等弹性）
                                                )
                                            )
                                        }
                                    }
                                    onAlarmClick() // 🌟 路由非阻塞：即时触发原有页面交互
                                }
                            }
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_tab_custom), // 纸飞机图标
                        contentDescription = "Paper Plane",
                        tint = inactiveColor, // 占据 Index 2
                        modifier = androidx.compose.ui.Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                translationX = planeOffsetX.value
                                translationY = planeOffsetY.value
                                scaleX = planeScale.value
                                scaleY = planeScale.value
                                alpha = planeAlpha.value
                            }
                    )
                }

                // Settings 槽位 (带弹簧阻尼旋转动效)
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(width = itemWidth, height = containerHeight)
                        .clickable(
                            interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                handleTabClick {
                                    rotationAngle += 90f
                                    onSettingsClick()
                                }
                            }
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_custom_settings),
                        contentDescription = "Settings",
                        tint = inactiveColor,
                        modifier = androidx.compose.ui.Modifier
                            .size(26.dp)
                            .rotate(animatedRotation)
                    )
                }
            }
        }
    }
}

// =========================================================================================
// 🪟 [工具类] 大图预览用全屏蒙版（支持 Telegram 级跟手下滑缩小 & 侧滑返回手势）
// ======================================================
@Composable
fun FullScreenDialog(
    onDismiss: () -> Unit,
    isZoomed: Boolean = false,
    content: @Composable (pagerModifier: Modifier, bgAlpha: Float, onTriggerExit: () -> Unit) -> Unit
) {
    val view = androidx.compose.ui.platform.LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 进入预览时将顶部时间/电量图标变为纯白色，退出预览时自动恢复原有深浅模式
    androidx.compose.runtime.DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val controller =
            window?.let { androidx.core.view.WindowCompat.getInsetsController(it, view) }
        val prevLight = controller?.isAppearanceLightStatusBars ?: true
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = prevLight
        }
    }

    // 核心物理动效控制器：缩放、位移Y、圆角半径、底层遮罩透明度、卡片内容透明度
    // 🌟 初始第0帧状态：alpha=0f（彻底杜绝首帧黑屏闪现），主体卡片 scale=0.9f，radius=20dp
    val animScale = remember { androidx.compose.animation.core.Animatable(0.9f) }
    val animOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val animRadius = remember { androidx.compose.animation.core.Animatable(20f) }
    val animScrimAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val animCardAlpha = remember { androidx.compose.animation.core.Animatable(0f) }

    var isDismissing by remember { mutableStateOf(false) }

    // 屏幕内直接下滑拖拽手势 (Drag-to-Dismiss)
    var dragAccumulatedY by remember { mutableFloatStateOf(0f) }
    val dismissThresholdPx = with(density) { 120.dp.toPx() }

    // 统一的多轨惯性退场动画闭环：下拉释放与右上角关闭共用
    val triggerExit: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            scope.launch {
                val exitOffsetY = animOffsetY.value + dragAccumulatedY + with(density) { 360.dp.toPx() }
                val targetScale = (animScale.value * 0.85f).coerceAtLeast(0.65f)

                val jobY = launch {
                    animOffsetY.animateTo(
                        targetValue = exitOffsetY,
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = FastOutLinearInEasing
                        )
                    )
                }
                val jobScale = launch {
                    animScale.animateTo(
                        targetValue = targetScale,
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = FastOutLinearInEasing
                        )
                    )
                }
                val jobCardAlpha = launch {
                    animCardAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 240,
                            easing = LinearOutSlowInEasing
                        )
                    )
                }
                val jobScrim = launch {
                    animScrimAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                // 等待渐隐动画完全演进完毕后再闭环退出
                jobCardAlpha.join()
                jobScrim.join()
                jobY.join()
                jobScale.join()
                onDismiss()
            }
        }
    }

    // 安全系统返回拦截（全机型兼容，优雅退场）
    androidx.activity.compose.BackHandler(enabled = true, onBack = triggerExit)

    // 开场柔和呼吸感入场动效（恢复经典 Q 弹回弹，MediumBouncy + StiffnessMediumLow）
    LaunchedEffect(Unit) {
        launch {
            animScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            animCardAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )
        }
        launch {
            animRadius.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            animScrimAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
    }

    // 统一复位回弹动画（抽取私有 Lambda 消除重复代码）
    val resetDrag: () -> Unit = {
        if (!isDismissing && dragAccumulatedY > 0f) {
            scope.launch {
                val startY = dragAccumulatedY
                androidx.compose.animation.core.Animatable(startY).animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) {
                    dragAccumulatedY = value
                }
            }
        }
    }

    // 轻量状态直驱：从 dragAccumulatedY 计算手势插值（0 协程开销）
    val dragRatio = (dragAccumulatedY / 500f).coerceIn(0f, 1f)
    val dragScale = (1f - dragRatio * 0.22f).coerceIn(0.78f, 1f)
    val dragRadius = (dragRatio * 28f).coerceIn(0f, 28f)
    val dragScrimAlpha = (1f - dragRatio * 0.75f).coerceIn(0f, 1f)
    val dragCardAlpha = (1f - dragRatio * 0.15f).coerceIn(0.6f, 1f)

    val currentCardAlpha = (animCardAlpha.value * dragCardAlpha).coerceIn(0f, 1f)
    val currentBgAlpha = (animScrimAlpha.value * dragScrimAlpha).coerceIn(0f, 1f)
    val currentRadius = if (dragAccumulatedY > 0f) dragRadius else animRadius.value.coerceAtLeast(0f)

    // 🌟 单独赋给内部 HorizontalPager 的专属修饰符：承担呼吸缩放、下滑位移与圆角，绝不影响外层铺满的黑底
    val pagerModifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
            val scale = (animScale.value * dragScale).coerceAtLeast(0.5f)
            scaleX = scale
            scaleY = scale
            translationY = animOffsetY.value + dragAccumulatedY
            alpha = currentCardAlpha
            clip = currentRadius > 0.1f
            shape = RoundedCornerShape(currentRadius.dp)
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f)
            .pointerInput(Unit) { detectTapGestures { } } // 拦截穿透点击
            .pointerInput(isZoomed) {
                detectVerticalDragGestures(
                    onDragStart = {
                        if (!isDismissing && !isZoomed) {
                            dragAccumulatedY = 0f
                        }
                    },
                    onDragEnd = {
                        if (!isDismissing && !isZoomed) {
                            if (dragAccumulatedY > dismissThresholdPx) {
                                // 🌟 超过阈值：多轨惯性协同退场，卡片与背景如薄雾般柔和消散，彻底告别硬切
                                triggerExit()
                            } else {
                                // 未达阈值：物理弹簧柔和归位
                                resetDrag()
                            }
                        }
                    },
                    onDragCancel = {
                        if (!isDismissing && !isZoomed) {
                            resetDrag()
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (!isDismissing && !isZoomed) {
                            dragAccumulatedY = (dragAccumulatedY + dragAmount).coerceAtLeast(0f)
                            if (dragAccumulatedY > 0f) {
                                change.consume()
                            }
                        }
                    }
                )
            }
    ) {
        content(pagerModifier, currentBgAlpha, triggerExit)
    }
}

// =========================================================================================
// 🖼️ [单张图框组件：AppListItem]
// 支持：长按呼出排序、左滑呼出删除按钮并触发弹簧效果
// ======================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    uri: Uri?,
    onSelect: () -> Unit,
    onPreview: (Uri) -> Unit,
    onDelete: () -> Unit,
    onLongPress: () -> Unit = {} // 🌟 新增：长按的回调参数
) {
    val haptic = LocalHapticFeedback.current
    var isLoadError by remember(uri) { mutableStateOf(false) }

    if (uri != null && !isLoadError) {
        // =====================================================================
        // 🌟 1. 有图片状态：完整的 SwipeToDismiss 滑动删除与带 8.dp 阴影的 Card 容器
        // =====================================================================
        val decayAnimationSpec = rememberSplineBasedDecay<Float>()
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val fallbackWidthPx = with(density) { (configuration.screenWidthDp - 32).dp.toPx() }
        var cardWidthPx by remember(uri) { mutableFloatStateOf(fallbackWidthPx) }

        // 🌟 40% 安全滑动判定阈值
        val thresholdPx = cardWidthPx * 0.4f

        // 追踪左滑删除组件的物理锚点和当前位置 (支持滑出全宽)
        val state = remember(uri) {
            AnchoredDraggableState(
                initialValue = DragValue.Settled,
                positionalThreshold = { distance -> distance * 0.4f }, // 🌟 超过 40% 判定为滑出删除
                velocityThreshold = { with(density) { 125.dp.toPx() } },
                snapAnimationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                decayAnimationSpec = decayAnimationSpec
            )
        }

        // 🌟 临界点机械震动反馈：拖拽首次越过 40% 阈值时触发一次振动，回退再滑入可重复触发
        var hasVibratedThreshold by remember(uri) { mutableStateOf(false) }
        var isDeleted by remember(uri) { mutableStateOf(false) }

        LaunchedEffect(state, thresholdPx) {
            snapshotFlow { state.offset }
                .collect { offset ->
                    if (!offset.isNaN() && thresholdPx > 0f) {
                        val isPastThreshold = offset < -thresholdPx
                        if (isPastThreshold && !hasVibratedThreshold) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            hasVibratedThreshold = true
                        } else if (!isPastThreshold && hasVibratedThreshold) {
                            hasVibratedThreshold = false
                        }
                    }
                }
        }

        // 🌟 松手即删：卡片完全滑出屏幕到达 Dismissed 锚点时，执行删除回调并彻底归零状态
        LaunchedEffect(state) {
            snapshotFlow { state.settledValue }
                .distinctUntilChanged()
                .collect { settled ->
                    if (settled == DragValue.Dismissed) {
                        isDeleted = true
                        onDelete()
                        // 🌟 强制将锚点偏移重置归零，彻底消灭删除后的红底残留 Bug
                        state.snapTo(DragValue.Settled)
                    }
                }
        }

        SideEffect {
            state.updateAnchors(newAnchors = DraggableAnchors {
                DragValue.Settled at 0f
                DragValue.Dismissed at -cardWidthPx // 扩宽至整卡宽度，支持完全滑出屏幕
            })
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .onSizeChanged { size ->
                    if (size.width > 0) {
                        cardWidthPx = size.width.toFloat()
                    }
                }
        ) {
            val currentOffset = if (isDeleted || state.offset.isNaN() || state.offset >= 0f) 0f else state.offset
            val isOverThreshold = currentOffset < -thresholdPx

            // 🌟 慢速渐变平滑晕染：拉长过渡区间至整卡宽度的 65%，避免“刚一拉就瞬间爆红”
            val bgAlpha = if (isDeleted || currentOffset >= -1f) {
                0f
            } else {
                val ratio = ((-currentOffset) / (cardWidthPx * 0.65f)).coerceIn(0f, 1f)
                ratio * 0.92f
            }

            // 垃圾桶图标透明度与缩放
            val iconAlpha = if (isDeleted || currentOffset >= -1f) {
                0f
            } else {
                ((-currentOffset) / (thresholdPx * 0.85f)).coerceIn(0f, 1f)
            }

            // 底层高级珊瑚红背景与居中/右侧跟随的垃圾桶图标
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = bgAlpha
                    }
                    .background(Color(0xFFF05252)), // 🌟 升级为柔和优雅的浅珊瑚红 (Soft Coral Red)，告别刺眼生硬
                contentAlignment = Alignment.CenterEnd
            ) {
                val iconScale by animateFloatAsState(
                    targetValue = if (isOverThreshold) 1.25f else (0.75f + 0.25f * iconAlpha),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "trashIconScale"
                )

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 28.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            alpha = iconAlpha
                        }
                )
            }

            // 顶层主图框展示区域 (Card 容器，带圆角与 8.dp 阴影)
            Card(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = if (isDeleted) 0 else state.offset.let { if (it.isNaN()) 0 else it.roundToInt() },
                            y = 0
                        )
                    }
                    .anchoredDraggable(state, Orientation.Horizontal)
                    .fillMaxSize()
                    .pointerInput(uri) {
                        detectTapGestures(
                            onTap = { onPreview(uri) },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPress()
                            }
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                val context = LocalContext.current
                val imageRequest = remember(uri) {
                    ImageRequest.Builder(context)
                        .data(uri)
                        .size(coil.size.Size(1080, 600))
                        .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                        .allowHardware(true)
                        .crossfade(true)
                        .build()
                }

                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = {
                        isLoadError = true
                    }
                )
            }
        }
    } else {
        // =====================================================================
        // 🌟 2. 无图片空白占位状态：水印级相框引导图标 + 微光呼吸微动效（hint 点击选图）
        // =====================================================================
        val infiniteTransition = rememberInfiniteTransition(label = "PlaceholderBreathTransition")
        val breathAlpha by infiniteTransition.animateFloat(
            initialValue = 0.2f, // 💡 调整此处更改呼吸动效最小透明度（默认 0.2f）
            targetValue = 0.5f,  // 💡 调整此处更改呼吸动效最大透明度（默认 0.5f）
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing), // 💡 调整此处更改单次呼吸过渡周期（建议 1500ms ~ 2000ms）
                repeatMode = RepeatMode.Reverse
            ),
            label = "PlaceholderAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onSelect() },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_photo),
                contentDescription = "添加图片",
                modifier = Modifier
                    .size(30.dp) // 💡 水印级紧凑相框引导图标大小 (30.dp)
                    .graphicsLayer { alpha = breathAlpha },
                tint = MaterialTheme.colorScheme.onSurface // 💡 调整此处更改占位图标基础颜色（半透明水印质感）
            )
        }
    }
}

// =========================================================================================
// 🧰 [拓展工具与 Worker 任务区]
// =========================================================================================
// 🚀 [120Hz 极客优化] 纯函数式滑动方向追踪：基于 snapshotFlow 在协程上下文中比对位移差
// 彻底移除 derivedStateOf 闭包内修改变量的 Snapshot 快照写入副作用，从根源杜绝高刷微抖动
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var isUp by remember(this) { mutableStateOf(true) }
    LaunchedEffect(this) {
        var previousIndex = firstVisibleItemIndex
        var previousScrollOffset = firstVisibleItemScrollOffset
        snapshotFlow { firstVisibleItemIndex to firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentOffset) ->
                if (currentIndex == 0 && currentOffset == 0) {
                    isUp = true
                } else if (currentIndex != previousIndex) {
                    isUp = currentIndex < previousIndex
                } else if (currentOffset != previousScrollOffset) {
                    isUp = currentOffset < previousScrollOffset
                }
                previousIndex = currentIndex
                previousScrollOffset = currentOffset
            }
    }
    return isUp
}

@Composable
fun androidx.compose.foundation.ScrollState.isScrollingUp(): Boolean {
    var isUp by androidx.compose.runtime.remember(this) { androidx.compose.runtime.mutableStateOf(true) }
    androidx.compose.runtime.LaunchedEffect(this) {
        var previousValue = value
        androidx.compose.runtime.snapshotFlow { value }
            .collect { currentValue ->
                if (currentValue == 0) {
                    isUp = true
                } else if (currentValue != previousValue) {
                    isUp = currentValue < previousValue
                }
                previousValue = currentValue
            }
    }
    return isUp
}

// 模拟发送系统通知
@SuppressLint("ServiceCast")
fun sendTestNotification(context: Context) {
    val channelId = "aura_daily_channel"
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 1. 创建通知渠道 (Android 8.0 及以上必须有渠道)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "每日提醒", // 用户在系统设置里看到的渠道名称
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "用于发送 Aura 的每日备忘提醒"
        }
        notificationManager.createNotificationChannel(channel)
    }

    // 2. 构建点击跳转 PendingIntent (回跳主页，支持 Android 12+ 强安全性 FLAG_IMMUTABLE)
    val notifyIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        notifyIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // 3. 构建通知内容 (注入有效 PendingIntent 闭环点击唤醒交互)
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.my_icon)
        .setContentTitle("Aura 提醒")
        .setContentText("Hello，快想想还有什么重要的事情没做呢？💫")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true) // 点击后自动消失
        .build()

    // 4. 发送通知 (Android 13+ 权限安全兜底)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        notificationManager.notify(1001, notification)
    }
}

/**
 * 🛡️ [Unicode CodePoint 安全截断]
 * 避免因直接按 UTF-16 字符 (char) 截断导致双字符 Emoji (Surrogate Pair) 变成乱码或高低位残损
 * @param maxCount 最大允许的完整码点 (CodePoint) 数量
 */
private fun String.safeTakeChars(maxCount: Int): String {
    val trimmed = this.trim()
    var charCount = 0
    var byteOffset = 0
    while (byteOffset < trimmed.length && charCount < maxCount) {
        val cp = trimmed.codePointAt(byteOffset)
        byteOffset += Character.charCount(cp)
        charCount++
    }
    return trimmed.substring(0, byteOffset)
}

/**
 * 🛡️ [私有沙盒安全落盘与大图防爆下采样]
 * 将外部 Uri (如 PhotoPicker 的临时 content:// Uri) 安全保存至内部私有目录 files/frames。
 * 针对几千万像素/数十 MB 的超大单反原图进行长边上限采样 (maxAllowed = 2560px)，彻底阻断 OOM。
 * 返回内部沙盒文件的持久化 file:// Uri。
 */
private suspend fun saveImageToSandbox(
    context: Context,
    sourceUri: Uri,
    slotIndex: Int
): Uri = withContext(Dispatchers.IO) {
    try {
        // 若来源已是沙盒内的本地文件且有效存在，直接复用
        if (sourceUri.scheme == "file") {
            val existingFile = File(sourceUri.path ?: "")
            if (existingFile.exists() && existingFile.length() > 0) {
                return@withContext sourceUri
            }
        }

        val framesDir = File(context.filesDir, "frames").apply {
            if (!exists()) mkdirs()
        }
        val targetFile = File(
            framesDir,
            "frame_${slotIndex}_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
        )

        // 1. 仅解析图片尺寸，判断是否需要下采样 (最大长边限制 2560px)
        var inSampleSize = 1
        var outMimeType: String? = null
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(input, null, options)
            outMimeType = options.outMimeType
            val maxDimension = maxOf(options.outWidth, options.outHeight)
            val maxAllowedDimension = 2560
            if (maxDimension > maxAllowedDimension) {
                var sample = 1
                while ((maxDimension / (sample * 2)) >= maxAllowedDimension) {
                    sample *= 2
                }
                inSampleSize = sample
            }
        }

        // 2. 根据尺寸决定是采样压缩还是流式直存
        if (inSampleSize > 1) {
            // 超大图：按采样比例安全解码并压缩落盘，阻断 OOM
            val isPng = outMimeType?.contains("png", ignoreCase = true) == true
            val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            val decodeConfig = if (isPng) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = decodeConfig
                }
                val bitmap = BitmapFactory.decodeStream(input, null, decodeOptions)
                if (bitmap != null) {
                    FileOutputStream(targetFile).use { out ->
                        bitmap.compress(compressFormat, 90, out)
                        out.flush()
                    }
                    bitmap.recycle()
                }
            }
        } else {
            // 常规尺寸：采用高性能流式边读边写直接落盘，零内存膨胀
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
        }

        if (targetFile.exists() && targetFile.length() > 0) {
            Uri.fromFile(targetFile)
        } else {
            sourceUri
        }
    } catch (e: Exception) {
        e.printStackTrace()
        sourceUri
    }
}

// Worker：被系统托管的后台 24 小时唤醒执行机制
class DailyNotificationWorker(
    context: android.content.Context,
    workerParams: androidx.work.WorkerParameters
) : androidx.work.Worker(context, workerParams) {

    override fun doWork(): androidx.work.ListenableWorker.Result {
        // 后台一到时间，就会偷偷执行你写好的发通知函数
        sendTestNotification(applicationContext)
        return androidx.work.ListenableWorker.Result.success()
    }
}

// =========================================================================================
// 🔄 [画框卡片顺序动态重排弹窗：ImageSortDialog]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 18.0 的多画框拖拽排序模态窗口组件。
// 2. 它能干什么？
//    - 展示当前已启用槽位的所有图片缩略图列表；
//    - 支持手势上下长按拖拽，拖动时卡片提升至最高 Z 轴（zIndex = 99f），并实时渲染 translationY 位移；
//    - 用户松手确认后，将新视觉索引列表回传（onSave），无损刷新主列表并同步持久化到 DataStore（SAVED_URIS_KEY）。
// 3. 怎么调整参数？
//    - 弹窗尺寸：修改 height(400.dp) 与 RoundedCornerShape(28.dp)。
//    - 拖拽卡片尺寸：修改 height(110.dp) 与 RoundedCornerShape(16.dp)。
//    - 排序拖拽灵敏度与防误触阈值：修改 PointerInput 手势监听内的判定常量。
// =========================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSortDialog(
    currentOrder: List<Int>,
    uris: List<Uri?>,
    displayCount: Int,
    onDismiss: () -> Unit,
    onSave: (List<Int>) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val dialogBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val dialogBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f)
    val tempOrder = remember { mutableStateListOf<Int>().apply { addAll(currentOrder) } }
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    // --- 新增：在这里提前算好几个关键的物理高度尺寸 ---
    val cardHeightPx = with(density) { (110 + 12).dp.toPx() }// 卡片自身高度 + 间距补偿
    val viewportHeightPx = with(density) { 400.dp.toPx() }// 弹窗可见高度
    val edgeZonePx = with(density) { 80.dp.toPx() }// 上下滚动的敏感边缘距离
    val autoScrollSpeed = remember { mutableFloatStateOf(0f) }

    // 核心物理引擎：计算当前拖拽的位置是否跨越了临界点，从而执行列表内的卡片交换 (仅内存重排，不触发写盘)
    val updatePhysics = {
        val curr = draggedIndex
        if (curr != null) {
            val lastIdx = (minOf(displayCount, tempOrder.size) - 1).coerceAtLeast(0)

            // 下划交换判定
            if (dragOffsetY > cardHeightPx * 0.5f && curr < lastIdx) {
                val item = tempOrder.removeAt(curr)
                tempOrder.add(curr + 1, item)
                draggedIndex = curr + 1
                dragOffsetY -= cardHeightPx
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                // 上划交换判定
            } else if (dragOffsetY < -cardHeightPx * 0.5f && curr > 0) {
                val item = tempOrder.removeAt(curr)
                tempOrder.add(curr - 1, item)
                draggedIndex = curr - 1
                dragOffsetY += cardHeightPx
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }

            // 边缘自动滚动测速逻辑：越靠近边缘滑动得越快
            val currentVisualY = (curr * cardHeightPx) - scrollState.value + dragOffsetY
            val maxSpd = 16f
            autoScrollSpeed.floatValue = when {
                currentVisualY < edgeZonePx -> -maxSpd * (1f - currentVisualY / edgeZonePx).coerceIn(
                    0f,
                    1f
                )

                currentVisualY + cardHeightPx > viewportHeightPx - edgeZonePx -> {
                    val bottomDist =
                        (currentVisualY + cardHeightPx) - (viewportHeightPx - edgeZonePx)
                    maxSpd * (bottomDist / edgeZonePx).coerceIn(0f, 1f)
                }
                else -> 0f
            }
        }
    }

    // 挂载一个死循环马达，每隔 16 毫秒扫描一次是否需要滚动屏幕
    LaunchedEffect(draggedIndex) {
        if (draggedIndex == null) return@LaunchedEffect
        while (true) {
            val speed = autoScrollSpeed.floatValue
            if (speed != 0f) {
                val consumed = scrollState.scrollBy(speed)
                dragOffsetY += consumed // 补偿手指位移
                updatePhysics() // 🌟 新增：列表自动滚动后，立刻进行物理结算！防止卡片脱手！
            }
            kotlinx.coroutines.delay(16)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .widthIn(min = 320.dp, max = 360.dp)
            .border(
                width = 1.dp,
                color = dialogBorder,
                shape = RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        containerColor = dialogBg,
        tonalElevation = 0.dp,
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                Text(
                    text = "调整图片顺序",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = if (isDark) listOf(Color(0xFF818CF8), Color(0xFF22D3EE))
                                     else listOf(Color(0xFF4F46E5), Color(0xFF0891B2))
                        )
                    ),
                    letterSpacing = (-0.3).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "长按右侧划块，上下拖动即可调整顺序",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 18.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.HorizontalDivider(
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                    thickness = 0.8.dp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tempOrder.take(displayCount).forEachIndexed { index, originalIndex ->
                    val uri = uris.getOrNull(originalIndex)
                    val isDragging = draggedIndex == index

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .zIndex(if (isDragging) 99f else 1f) // 正在拖拽的卡片提高 Z 轴，浮在最表面
                            .graphicsLayer {
                                translationY = if (isDragging) dragOffsetY else 0f
                                if (isDragging) {
                                    scaleX = 1.02f
                                    scaleY = 1.02f
                                }
                            }
                            .shadow(
                                elevation = if (isDragging) 8.dp else 0.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .border(
                                width = if (isDragging) 1.5.dp else 1.dp,
                                color = if (isDragging) (if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1))
                                        else (if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .background(if (isDark) Color(0xFF262C36) else Color(0xFFF8FAFC))
                    ) {
                        // 背景图片与遮罩
                        if (uri != null) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.52f),
                                                Color.Black.copy(alpha = 0.12f),
                                                Color.Black.copy(alpha = 0.52f)
                                            )
                                        )
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (isDark) Color(0xFF262C36) else Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "这个画框还是空的哦 ~",
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // 🌟 浮岛化卡片内层控件排版 (去生硬感，圆角精致微浮雕)
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 左上角浮动胶囊数字标号 (去生硬感，精致圆角毛玻璃微卡片)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 10.dp, top = 10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (uri != null) Color.Black.copy(alpha = 0.48f)
                                        else (if (isDark) Color(0xFF1E222B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.92f))
                                    )
                                    .border(
                                        width = 0.5.dp,
                                        color = if (uri != null) Color.White.copy(alpha = 0.22f)
                                                else (if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (index + 1 < 10) "0${index + 1}" else "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (uri != null) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                                )
                            }

                            // 右侧拖动手柄 (长按滑动交互区，外层大范围触控，内层精致现代把手)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                                    .width(56.dp)
                                    .pointerInput(displayCount) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggedIndex = index
                                                dragOffsetY = 0f
                                                autoScrollSpeed.floatValue = 0f
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                updatePhysics() // 物理碰撞与顺序重排
                                            },
                                            onDragEnd = {
                                                draggedIndex = null
                                                dragOffsetY = 0f
                                                autoScrollSpeed.floatValue = 0f
                                            },
                                            onDragCancel = {
                                                draggedIndex = null
                                                dragOffsetY = 0f
                                                autoScrollSpeed.floatValue = 0f
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (uri != null) Color.Black.copy(alpha = 0.48f)
                                            else (if (isDark) Color(0xFF1E222B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.92f))
                                        )
                                        .border(
                                            width = 0.5.dp,
                                            color = if (uri != null) Color.White.copy(alpha = 0.22f)
                                                    else (if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "拖拽排序",
                                        tint = if (uri != null) Color.White.copy(alpha = 0.92f)
                                               else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSave(tempOrder.toList())
                    onDismiss()
                },
                shape = RoundedCornerShape(50),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF383D47) else Color(0xFF2C2C2C),
                    contentColor = Color.White
                ),
                elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                ),
                modifier = Modifier
                    .height(32.dp)
                    .padding(end = 4.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "完成",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    )
}


// =========================================================================================
// 🔒 [私密空间安全锁屏全屏拦截层：BiometricLockOverlay]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 18.0 的硬件级私密锁屏全屏遮罩组件与认证调度中枢。
// 2. 它能干什么？
//    - 在应用冷启动、或切到后台驻留超过设定超时时间（timeoutMinutes）返回前台时立即拦截；
//    - 呈现定格的纯净开屏视觉（曜黑/冷白高对比基底 + Aura 官方品牌绿 Logo）；
//    - 彻底拦截系统物理返回键（BackHandler），防止恶意绕过；
//    - 认证成功后触发 650ms 丝滑渐隐，Logo 伴随 1.0f -> 1.12f 微量扩张优雅退场。
// 3. 怎么调整参数？
//    - 品牌主色：修改 brandGreen = Color(0xFF63B939)。
//    - 解锁动效曲线：修改 logoScale 动画规范 tween(durationMillis = 650, easing = FastOutSlowInEasing)。
//    - 底色主题：修改 backgroundColor（深色：0xFF111317；浅色：0xFFFFFFFF）。
// =========================================================================================
@Composable
fun BiometricLockOverlay(
    alpha: Float,
    isLocked: Boolean,
    onTriggerUnlock: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current

    // 经典开屏页基底色（深色模式：深邃曜黑；浅色模式：纯净冷白）
    val backgroundColor = if (isDark) Color(0xFF111317) else Color(0xFFFFFFFF)
    val brandGreen = Color(0xFF63B939) // Aura 官方经典品牌绿
    val textPrimary = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // 解锁动效：Logo 在解锁瞬间从 1.0f 微微扩张至 1.12f 伴随淡出
    val logoScale by animateFloatAsState(
        targetValue = if (isLocked) 1f else 1.12f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f)
            .graphicsLayer { this.alpha = alpha }
            // 锁定状态拦截底层所有手势防穿透，点击全屏任意位置可直接唤起指纹/面容
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isLocked) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTriggerUnlock()
                }
            }
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // 🌟 1. 经典绿色 Aura Logo（带有微缩放与微光环境层）
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                    }
            ) {
                // 柔和的环境光晕（营造高级私密空间感）
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    brandGreen.copy(alpha = if (isDark) 0.16f else 0.10f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // 官方矢量 Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_aura_logo),
                    contentDescription = "Aura Logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🌟 2. 标题与状态提示文案
            Text(
                text = "解锁 Aura",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🌟 3. 极简轻触验证按钮
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTriggerUnlock()
                },
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF1E222B) else Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = brandGreen.copy(alpha = 0.35f)
                ),
                tonalElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "指纹验证",
                        modifier = Modifier.size(20.dp),
                        tint = brandGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "使用生物识别",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                }
            }
        }
    }
}

// 标记生物识别原生弹窗是否处于活跃展示状态，避免弹窗抢焦点触发 PAUSE/RESUME 导致误重复判定锁定
private var isBiometricPromptActive: Boolean = false

// 唤起系统原生 BiometricPrompt
fun showBiometricPrompt(
    activity: FragmentActivity,
    title: String = "需要身份验证",
    subtitle: String = "使用生物识别解锁应用",
    onSuccess: () -> Unit,
    onError: (errorCode: Int, errString: String) -> Unit = { _, _ -> }
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricManager = BiometricManager.from(activity)

    // 判断设备是否支持 PIN/图案/密码等锁屏凭据与生物识别组合
    val canDeviceCredential = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ) == BiometricManager.BIOMETRIC_SUCCESS

    val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)

    if (canDeviceCredential) {
        promptInfoBuilder.setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    } else {
        promptInfoBuilder.setNegativeButtonText("取消")
        promptInfoBuilder.setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
    }

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isBiometricPromptActive = false
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                isBiometricPromptActive = false
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }
    )

    try {
        isBiometricPromptActive = true
        biometricPrompt.authenticate(promptInfoBuilder.build())
    } catch (e: Exception) {
        isBiometricPromptActive = false
        e.printStackTrace()
        onError(-1, e.message ?: "启动生物识别异常")
    }
}

// =========================================================================================
// 🎨 [Aura 物理果冻动力学实验场：AuraJellyPlayground]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 18.0 的 2D 物理粒子与触控冲击波交互游乐场组件。
// 2. 它能干什么？
//    - 模拟具备弹性形变、速度衰减、边界碰撞反弹、以及手势轻触冲击波扩散的果冻微粒；
//    - 支持粒子尾迹拖影与排斥力场计算，营造极客动感视觉。
// 3. 怎么调整参数？
//    - 物理参数统一收敛于 AuraPlaygroundPhysicsConfig 常量配置中心。
// =========================================================================================

/**
 * 🌌 [Aura 物理果冻动力学常量配置中心]
 * 集中归拢粒子游乐场的弹性形变、力场排斥、阻尼与边界碰撞等所有物理参数。
 */
private object AuraPlaygroundPhysicsConfig {
    // 1. 弹性形变 (胡克定律弹簧模型)
    const val SPRING_STIFFNESS = 160f        // 弹簧劲度系数 (k)
    const val SPRING_DAMPING = 14f           // 阻尼系数 (c)
    const val SQUASH_SCALE_COMPRESS = 0.52f  // 碰壁挤压时的压缩尺度
    const val SQUASH_SCALE_STRETCH = 1.48f   // 碰壁挤压时的拉伸尺度
    const val SQUASH_VELOCITY = 4f           // 挤压形变回弹速度

    // 2. 斥力与切向导流力场（严格仅保留头像自身同心圆，无任何上方虚位力场）
    const val AVATAR_REPEL_ACCEL = 2600f     // 头像自身圆形边界最大排斥加速度
    const val AVATAR_TANGENT_FACTOR = 0.75f  // 流体力学切向导流加速度系数 (顺滑绕流避让)
    const val AVATAR_RESTITUTION = 0.5f      // 头像硬边界法向弹性反弹系数 (杜绝表面死平衡堆积)

    // 3. 摩擦阻尼与保活
    const val VELOCITY_FRICTION = 0.996f     // 粒子自然漂移空气阻尼
    const val MIN_SPEED_DP = 20f             // 微动保活最低速度 (dp/s)

    // 4. 边界碰撞与可视活动边界
    const val WALL_RESTITUTION = 0.88f       // 边框碰撞动能保留系数 (反弹衰减)
    const val MAX_ACTIVE_HEIGHT_DP = 280f    // 物理与渲染最大有效活动窗口 (dp)

    // 5. 残影拖影 (Motion Trail)
    const val TRAIL_MIN_DIST_SQ = 4f         // 触发新残影记录的位移平方差阈值 (px^2)
    const val MAX_TRAIL_COUNT = 4            // 历史残影最大留存数量
    const val TRAIL_ALPHA_FACTOR = 0.35f     // 残影基础透明度衰减系数
    const val TRAIL_SCALE_START = 0.65f      // 残影最小初始缩放比例
    const val TRAIL_SCALE_STEP = 0.35f       // 残影梯级缩放增量

    // 6. 尺寸与圆角安全限制
    const val SCALE_MIN = 0.35f              // 最小形变缩放比
    const val SCALE_MAX = 2.2f               // 最大形变缩放比
    const val CORNER_RATIO_MIN = 0.4f        // 圆角最小自适应比
    const val CORNER_RATIO_MAX = 1.8f        // 圆角最大自适应比

    // 7. 几何间隙配置 (dp)
    const val FORCEFIELD_MARGIN_DP = 36f     // 头像周围同心圆缓冲外圈 默认32(dp)
    const val HARD_MARGIN_DP = 8f            // 物理硬边界紧贴间距 (dp)
}

/**
 * 🌟 [Aura 19.0 粒子系统核心类型定义]
 * 涵盖 Aura 基础基因果冻方块与 4 位专属伙伴彩蛋：Google、Gemini、Antigravity、GitHub
 */
enum class ParticleType {
    REGULAR,
    GOOGLE,
    GEMINI,
    ANTIGRAVITY,
    GITHUB
}

private class AuraJellyParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val sizePx: Float,
    val cornerRadiusPx: Float,
    val color: Color,
    val baseAlpha: Float,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var scaleVelX: Float = 0f,
    var scaleVelY: Float = 0f,
    val particleType: ParticleType = ParticleType.REGULAR
) {
    // 🌟 定长 FloatArray 复用残影历史坐标，彻底消灭每秒上万次 Offset 临时小对象分配
    val historyX = FloatArray(AuraPlaygroundPhysicsConfig.MAX_TRAIL_COUNT)
    val historyY = FloatArray(AuraPlaygroundPhysicsConfig.MAX_TRAIL_COUNT)
    var historyCount: Int = 0

    fun addHistory(newX: Float, newY: Float) {
        val max = AuraPlaygroundPhysicsConfig.MAX_TRAIL_COUNT
        if (historyCount < max) {
            historyX[historyCount] = newX
            historyY[historyCount] = newY
            historyCount++
        } else {
            for (i in 0 until max - 1) {
                historyX[i] = historyX[i + 1]
                historyY[i] = historyY[i + 1]
            }
            historyX[max - 1] = newX
            historyY[max - 1] = newY
        }
    }
}

@Composable
fun AuraJellyPlayground(
    scrollState: androidx.compose.foundation.ScrollState,
    avatarPositionProvider: (() -> Pair<Offset, Float>?)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val isDarkTheme = LocalIsDarkTheme.current
    val collapseRangePx = with(density) { 140.dp.toPx() } // 与 TelegramAvatarHeader 保持 100% 几何一致
    val cameraYPx = with(density) { 20.dp.toPx() }
    val cameraRadiusPx = with(density) { 11.dp.toPx() }
    val startYPx = with(density) { 140.dp.toPx() }
    val startRadiusPx = with(density) { 46.dp.toPx() }

    // 🌟 提前预缓存密度转换像素值，杜绝物理帧内高频调用 dp.toPx()
    val forcefieldMargin = with(density) { AuraPlaygroundPhysicsConfig.FORCEFIELD_MARGIN_DP.dp.toPx() }
    val minSpeed = with(density) { AuraPlaygroundPhysicsConfig.MIN_SPEED_DP.dp.toPx() }
    val hardMarginPx = with(density) { AuraPlaygroundPhysicsConfig.HARD_MARGIN_DP.dp.toPx() }
    val maxActiveHeightPx = with(density) { AuraPlaygroundPhysicsConfig.MAX_ACTIVE_HEIGHT_DP.dp.toPx() }

    // 🌟 提取 4 位专属伙伴图标（Google、Gemini、Antigravity、GitHub）
    val googleBitmap = ImageBitmap.imageResource(id = R.drawable.google_logo)
    val geminiBitmap = ImageBitmap.imageResource(id = R.drawable.ic_gemini_color)
    val antigravityBitmap = ImageBitmap.imageResource(id = R.drawable.ic_antigravity)

    // 🚀 [120Hz 极限性能防线] GitHub 矢量图标在进入时预渲染为 96x96 纯白 Alpha 遮罩位图，在帧循环中直接执行 GPU 纯色 Tint，0 每帧开销
    val githubBitmap = remember(context) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_particle_github)
        if (drawable != null) {
            val sizePx = 96
            val bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, sizePx, sizePx)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } else null
    }

    // 浅色日间模式：质感沉稳深灰 Color(0xFF24292F)；深色沉浸模式：浅银灰纯白 Color(0xFFF0F6FC)
    val githubTintColor = if (isDarkTheme) Color(0xFFF0F6FC) else Color(0xFF24292F)
    val githubColorFilter = remember(githubTintColor) { ColorFilter.tint(githubTintColor) }
    val githubAlpha = if (isDarkTheme) 0.55f else 0.38f // 浅色 0.38f 低调通透，深色 0.55f 银辉漫游

    // 🌟 将 scrollState.value 依赖封装为按需计算 Lambda，彻底杜绝列表滚动时 AuraJellyPlayground 发生无效高频重组
    val calculateAvatarGeometry = {
        val scrollVal = scrollState.value.toFloat()
        val progress = (scrollVal / collapseRangePx).coerceIn(0f, 1f)
        val yProgress = progress.pow(1.75f)
        val centerY = startYPx + (cameraYPx - startYPx) * yProgress
        val radius = startRadiusPx + (cameraRadiusPx - startRadiusPx) * progress
        centerY to radius
    }

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // 🌟 挂接 Lifecycle 生命周期感知：退后台/锁屏自动暂停物理帧循环，杜绝耗电发热
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var isResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isResumed = true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> isResumed = false
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 初始化微粒矩阵：常规 12 个 Aura 基因方块 + 必定生成且仅生成 4 个专属伙伴彩蛋 (Google, Gemini, Antigravity, GitHub)
    val particles = remember {
        val colors = listOf(
            Color(0xFF34C759), // Aura 绿
            Color(0xFF007AFF), // Aura 蓝
            Color(0xFFFFFFFF), // 纯净白
            Color(0xFFE2E8F0)  // 浅灰
        )
        // 1. 常规 12 个圆角方块
        val regularList = List(12) { index ->
            val sizeDp = 11f + (index * 7 % 14) // 11dp ~ 24dp
            val sizePx = with(density) { sizeDp.dp.toPx() }
            val color = colors[index % colors.size]
            val baseAlpha = 0.62f + (index * 13 % 17) / 100f // 0.62f ~ 0.78f
            // 初始自然漂移速度 (30dp/s ~ 65dp/s)
            val angle = (index * 27.7f) * (kotlin.math.PI.toFloat() / 180f)
            val speed = with(density) { (30f + (index * 9 % 35)).dp.toPx() }
            AuraJellyParticle(
                x = 0f,
                y = 0f,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                sizePx = sizePx,
                cornerRadiusPx = sizePx * 0.28f,
                color = color,
                baseAlpha = baseAlpha,
                particleType = ParticleType.REGULAR
            )
        }

        // 2. 🌟 必定生成且仅生成 4 个特殊的专属伙伴彩蛋粒子 (Google, Gemini, Antigravity, GitHub 各一个)
        val easterEggTypes = listOf(
            ParticleType.GOOGLE,
            ParticleType.GEMINI,
            ParticleType.ANTIGRAVITY,
            ParticleType.GITHUB
        )

        val easterEggList = easterEggTypes.mapIndexed { index, type ->
            val sizeDp = 27f
            val sizePx = with(density) { sizeDp.dp.toPx() }
            val baseAlpha = if (type == ParticleType.GITHUB) 0.40f else 0.82f
            val angle = ((index + 1) * 88.5f) * (kotlin.math.PI.toFloat() / 180f)
            val speed = with(density) { 36.dp.toPx() }
            AuraJellyParticle(
                x = 0f,
                y = 0f,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                sizePx = sizePx,
                cornerRadiusPx = sizePx * 0.28f,
                color = Color.White,
                baseAlpha = baseAlpha,
                particleType = type
            )
        }

        regularList + easterEggList
    }

    var initialized by remember { mutableStateOf(false) }
    var frameTick by remember { mutableLongStateOf(0L) }

    // 独立 60fps/120fps 物理帧动画协程（绑定 isResumed 生命周期，退后台即刻休眠）
    LaunchedEffect(canvasSize, isResumed) {
        if (!isResumed) return@LaunchedEffect
        if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@LaunchedEffect

        val w = canvasSize.width
        val h = canvasSize.height
        val effectiveHeight = minOf(h, maxActiveHeightPx)
        val centerX = w / 2f

        // 初次分布在四周，避开正中央力场并严格限制在顶部可视窗口内
        if (!initialized) {
            val (initAvatarCenterY, _) = calculateAvatarGeometry()
            particles.forEachIndexed { i, p ->
                val angle = (i.toFloat() / particles.size) * 2f * kotlin.math.PI.toFloat()
                val radius = (w / 2f - p.sizePx) * (0.35f + (i % 5) * 0.12f)
                p.x = (centerX + kotlin.math.cos(angle) * radius).coerceIn(p.sizePx, w - p.sizePx)
                p.y = (initAvatarCenterY + kotlin.math.sin(angle) * radius).coerceIn(p.sizePx, effectiveHeight - p.sizePx)
            }
            initialized = true
        }

        var lastFrameNanos = 0L

        while (isResumed) {
            withFrameNanos { now ->
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = now
                    return@withFrameNanos
                }
                val rawDt = (now - lastFrameNanos) / 1_000_000_000f
                lastFrameNanos = now
                val dt = rawDt.coerceIn(0.001f, 0.033f)

                // 物理帧内部优先采用头像自身在 onGloballyPositioned 中上报的真实视觉中心与物理半径，杜绝人为重心偏移
                val avatarReported = avatarPositionProvider?.invoke()
                val currentAvatarCenterX: Float
                val currentAvatarCenterY: Float
                val currentAvatarRadius: Float

                if (avatarReported != null && avatarReported.second > 0f) {
                    currentAvatarCenterX = avatarReported.first.x
                    currentAvatarCenterY = avatarReported.first.y
                    currentAvatarRadius = avatarReported.second
                } else {
                    val (geomY, geomRadius) = calculateAvatarGeometry()
                    currentAvatarCenterX = centerX
                    currentAvatarCenterY = geomY
                    currentAvatarRadius = geomRadius
                }

                val currentForcefieldRadius = currentAvatarRadius + forcefieldMargin
                val hardBoundary = currentAvatarRadius + hardMarginPx
                val avatarRepelAccel = AuraPlaygroundPhysicsConfig.AVATAR_REPEL_ACCEL
                val tangentFactor = AuraPlaygroundPhysicsConfig.AVATAR_TANGENT_FACTOR
                val restitutionFactor = 1f + AuraPlaygroundPhysicsConfig.AVATAR_RESTITUTION

                for (p in particles) {
                    // 1. 弹簧 Q 弹恢复力 (Squash & Stretch Restoring Force)
                    val dScaleX = -AuraPlaygroundPhysicsConfig.SPRING_STIFFNESS * (p.scaleX - 1f) - AuraPlaygroundPhysicsConfig.SPRING_DAMPING * p.scaleVelX
                    val dScaleY = -AuraPlaygroundPhysicsConfig.SPRING_STIFFNESS * (p.scaleY - 1f) - AuraPlaygroundPhysicsConfig.SPRING_DAMPING * p.scaleVelY
                    p.scaleVelX += dScaleX * dt
                    p.scaleVelY += dScaleY * dt
                    p.scaleX += p.scaleVelX * dt
                    p.scaleY += p.scaleVelY * dt

                    // 2. 真实圆形头像自身排斥力场 (严格以纯同心圆欧几里得距离为基准，保持上方空白与前摄通道 100% 自由运动)
                    val dxAvatar = p.x - currentAvatarCenterX
                    val dyAvatar = p.y - currentAvatarCenterY
                    val distance = kotlin.math.hypot(dxAvatar, dyAvatar).coerceAtLeast(0.1f)

                    if (distance < currentForcefieldRadius) {
                        val penetration = 1f - distance / currentForcefieldRadius
                        val repelAccel = avatarRepelAccel * penetration * penetration
                        val nx = dxAvatar / distance
                        val ny = dyAvatar / distance

                        // 🌟 切向导流：打破头顶死平衡，引导粒子沿圆周切线优雅绕流滑开
                        val tangentSign = if (dxAvatar >= 0f) 1f else -1f
                        val tx = -ny * tangentSign
                        val ty = nx * tangentSign
                        val dtAccel = repelAccel * dt

                        p.vx += (nx + tx * tangentFactor) * dtAccel
                        p.vy += (ny + ty * tangentFactor) * dtAccel

                        // 强推侵入边界 (确保视觉绝对纯净) 并施加物理法向弹性反弹 (Restitution)
                        if (distance < hardBoundary) {
                            p.x = currentAvatarCenterX + nx * hardBoundary
                            p.y = currentAvatarCenterY + ny * hardBoundary

                            // 计算粒子朝向圆心的法向相对速度，向外弹性反射，彻底消除头顶滞留与表面卡死
                            val vNormal = p.vx * nx + p.vy * ny
                            if (vNormal < 0f) { // 仅当粒子正向圆心运动时进行反弹
                                p.vx -= restitutionFactor * vNormal * nx
                                p.vy -= restitutionFactor * vNormal * ny
                            }
                        }
                    }

                    // 3. 自然阻尼与微动保活
                    p.vx *= AuraPlaygroundPhysicsConfig.VELOCITY_FRICTION
                    p.vy *= AuraPlaygroundPhysicsConfig.VELOCITY_FRICTION
                    val currentSpeed = kotlin.math.sqrt(p.vx * p.vx + p.vy * p.vy)
                    if (currentSpeed < minSpeed) {
                        val boost = minSpeed / currentSpeed.coerceAtLeast(1f)
                        p.vx *= boost
                        p.vy *= boost
                    }

                    // 4. 坐标更新
                    p.x += p.vx * dt
                    p.y += p.vy * dt

                    // 6. 四周边框碰撞反弹与果冻挤压 (严格约束在 effectiveHeight 顶部活动窗口内)
                    val clampedScaleX = p.scaleX.coerceIn(AuraPlaygroundPhysicsConfig.SCALE_MIN, AuraPlaygroundPhysicsConfig.SCALE_MAX)
                    val clampedScaleY = p.scaleY.coerceIn(AuraPlaygroundPhysicsConfig.SCALE_MIN, AuraPlaygroundPhysicsConfig.SCALE_MAX)
                    val halfW = (p.sizePx * clampedScaleX) / 2f
                    val halfH = (p.sizePx * clampedScaleY) / 2f

                    // 左右边框反弹与挤压
                    if (p.x - halfW < 0f) {
                        p.x = halfW
                        p.vx = -p.vx * AuraPlaygroundPhysicsConfig.WALL_RESTITUTION
                        p.scaleX = AuraPlaygroundPhysicsConfig.SQUASH_SCALE_COMPRESS
                        p.scaleY = AuraPlaygroundPhysicsConfig.SQUASH_SCALE_STRETCH
                        p.scaleVelX = AuraPlaygroundPhysicsConfig.SQUASH_VELOCITY
                        p.scaleVelY = -AuraPlaygroundPhysicsConfig.SQUASH_VELOCITY
                    } else if (p.x + halfW > w) {
                        p.x = w - halfW
                        p.vx = -p.vx * AuraPlaygroundPhysicsConfig.WALL_RESTITUTION
                        p.scaleX = AuraPlaygroundPhysicsConfig.SQUASH_SCALE_COMPRESS
                        p.scaleY = AuraPlaygroundPhysicsConfig.SQUASH_SCALE_STRETCH
                        p.scaleVelX = AuraPlaygroundPhysicsConfig.SQUASH_VELOCITY
                        p.scaleVelY = -AuraPlaygroundPhysicsConfig.SQUASH_VELOCITY
                    }

                    // 上下边框反弹与挤压 (下边界严格限制在 effectiveHeight)
                    if (p.y - halfH < 0f) {
                        p.y = halfH
                        p.vy = -p.vy * AuraPlaygroundPhysicsConfig.WALL_RESTITUTION
                        p.scaleY = AuraPlaygroundPhysicsConfig.SQUASH_SCALE_COMPRESS
                        p.scaleX = AuraPlaygroundPhysicsConfig.SQUASH_SCALE_STRETCH
                        p.scaleVelY = AuraPlaygroundPhysicsConfig.SQUASH_VELOCITY
                        p.scaleVelX = -AuraPlaygroundPhysicsConfig.SQUASH_VELOCITY
                    } else if (p.y + halfH > effectiveHeight) {
                        p.y = effectiveHeight - halfH
                        p.vy = -p.vy * AuraPlaygroundPhysicsConfig.WALL_RESTITUTION
                        p.scaleY = AuraPlaygroundPhysicsConfig.SQUASH_SCALE_COMPRESS
                        p.scaleX = AuraPlaygroundPhysicsConfig.SQUASH_SCALE_STRETCH
                        p.scaleVelY = AuraPlaygroundPhysicsConfig.SQUASH_VELOCITY
                        p.scaleVelX = -AuraPlaygroundPhysicsConfig.SQUASH_VELOCITY
                    }

                    // 7. 丝滑残影历史队列记录 (Motion Trail, 零对象分配)
                    if (p.historyCount == 0) {
                        p.addHistory(p.x, p.y)
                    } else {
                        val lastX = p.historyX[p.historyCount - 1]
                        val lastY = p.historyY[p.historyCount - 1]
                        val moveDistSq = (p.x - lastX) * (p.x - lastX) + (p.y - lastY) * (p.y - lastY)
                        if (moveDistSq >= AuraPlaygroundPhysicsConfig.TRAIL_MIN_DIST_SQ) {
                            p.addHistory(p.x, p.y)
                        }
                    }
                }

                frameTick++
            }
        }
    }

    // 纯渲染画布：完全无任何 pointerInput 或交互监听，对触控绝对透明（Zero-Interference）
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                canvasSize = Size(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@Canvas
        val _tick = frameTick
        val effectiveHeight = minOf(canvasSize.height, maxActiveHeightPx)

        // 绘制方块残影与果冻实体 (全屏独立悬浮，永远自由跳动)
        for (p in particles) {
            val iconBitmap = when (p.particleType) {
                ParticleType.GOOGLE -> googleBitmap
                ParticleType.GEMINI -> geminiBitmap
                ParticleType.ANTIGRAVITY -> antigravityBitmap
                ParticleType.GITHUB -> githubBitmap
                ParticleType.REGULAR -> null
            }

            val isGithub = p.particleType == ParticleType.GITHUB
            val activeFilter = if (isGithub) githubColorFilter else null
            val effectiveAlpha = if (isGithub) githubAlpha else p.baseAlpha

            // 绘制 Alpha 渐减的运动历史残影 (Motion Trail, 0 内存对象分配)
            val hCount = p.historyCount
            for (idx in 0 until hCount) {
                val trailX = p.historyX[idx]
                val trailY = p.historyY[idx]

                // 🌟 越入下方不透明卡片区域，安全跳过绘制以消除 Overdraw
                if (trailY > effectiveHeight) continue

                val trailFactor = (idx + 1).toFloat() / (hCount + 1)
                val trailAlpha = effectiveAlpha * trailFactor * AuraPlaygroundPhysicsConfig.TRAIL_ALPHA_FACTOR
                val trailScale = AuraPlaygroundPhysicsConfig.TRAIL_SCALE_START + AuraPlaygroundPhysicsConfig.TRAIL_SCALE_STEP * trailFactor
                val trailSize = p.sizePx * trailScale
                val trailCorner = p.cornerRadiusPx * trailScale

                val halfSize = trailSize / 2f
                val drawLeft = trailX - halfSize
                val drawTop = trailY - halfSize

                if (iconBitmap != null) {
                    drawImage(
                        image = iconBitmap,
                        dstOffset = IntOffset(drawLeft.roundToInt(), drawTop.roundToInt()),
                        dstSize = IntSize(trailSize.roundToInt().coerceAtLeast(1), trailSize.roundToInt().coerceAtLeast(1)),
                        alpha = trailAlpha,
                        colorFilter = activeFilter
                    )
                } else {
                    drawRoundRect(
                        color = p.color,
                        topLeft = Offset(drawLeft, drawTop),
                        size = Size(trailSize, trailSize),
                        cornerRadius = CornerRadius(trailCorner, trailCorner),
                        alpha = trailAlpha
                    )
                }
            }

            // 绘制当前果冻实体 (Squash & Stretch 形变)
            val clampedScaleX = p.scaleX.coerceIn(AuraPlaygroundPhysicsConfig.SCALE_MIN, AuraPlaygroundPhysicsConfig.SCALE_MAX)
            val clampedScaleY = p.scaleY.coerceIn(AuraPlaygroundPhysicsConfig.SCALE_MIN, AuraPlaygroundPhysicsConfig.SCALE_MAX)
            val currentW = p.sizePx * clampedScaleX
            val currentH = p.sizePx * clampedScaleY
            val corner = p.cornerRadiusPx * ((clampedScaleX + clampedScaleY) / 2f).coerceIn(
                AuraPlaygroundPhysicsConfig.CORNER_RATIO_MIN,
                AuraPlaygroundPhysicsConfig.CORNER_RATIO_MAX
            )

            val entityLeft = p.x - currentW / 2f
            val entityTop = p.y - currentH / 2f

            // 🌟 越入下方不透明卡片区域，安全跳过绘制以消除 Overdraw
            if (entityTop > effectiveHeight) continue

            if (iconBitmap != null) {
                // 🌟 绘制彩蛋图标实体（完美融入 Squash & Stretch 物理果冻挤压形变）
                drawImage(
                    image = iconBitmap,
                    dstOffset = IntOffset(entityLeft.roundToInt(), entityTop.roundToInt()),
                    dstSize = IntSize(currentW.roundToInt().coerceAtLeast(1), currentH.roundToInt().coerceAtLeast(1)),
                    alpha = effectiveAlpha,
                    colorFilter = activeFilter
                )
            } else {
                drawRoundRect(
                    color = p.color,
                    topLeft = Offset(entityLeft, entityTop),
                    size = Size(currentW, currentH),
                    cornerRadius = CornerRadius(corner, corner),
                    alpha = p.baseAlpha
                )
            }
        }
    }
}

// =========================================================================================
// 🌟 [空间 3 专属] Aura 极客灯牌、版本时间线与等大 1:1 二级版本更新弹窗组件
// =========================================================================================
@Immutable
data class AuraVersionInfo(
    val version: String,
    val title: String,
    val date: String,
    val isLatest: Boolean,
    val accentColor: Color,
    val highlights: List<String>
)

val auraMilestones = listOf(
    AuraVersionInfo(
        version = "v19.0",
        title = "实验室",
        date = "2026.11",
        isLatest = true,            //最新版
        accentColor = Color(0xFF8445F2),
        highlights = listOf(
            "适配了深色模式。",
            "带来了实验性功能-性能监测。",
            "深度适配了120Hz刷新率。",
        )
    ),
    AuraVersionInfo(
        version = "v18.0",
        title = "全新视觉",
        date = "2026.11",
        isLatest = false,
        accentColor = Color(0xFF34C759), // Aura 绿
        highlights = listOf(
            "本次更新带来了许多新功能，一起来体验吧QAQ",
            "新增应用分享，扫描二维码跳转。",
            "新增应用锁功能，保护您的隐私。",
            "全新的底部导航栏，提升视觉效果。",
            "全新版本日志时间线，提升查阅体验。",
            "页面美术升级，提升视觉效果。",
            "升级了清理缓存空间功能。",
            "预留了实验室功能页面。",
            "预留了Tab 2卡片页面。",
            "使用Agent对应用进行了17次专项体检。",
        )
    ),
    AuraVersionInfo(
        version = "v17.0",
        title = "光晕调色",
        date = "2026.10",
        isLatest = false,
        accentColor = Color(0xFF007AFF), // Aura 蓝
        highlights = listOf(
            "新增光晕调色功能。",
            "深度优化了部分视觉效果，质感再提升。"
        )
    ),
    AuraVersionInfo(
        version = "v16.0",
        title = "大图动画",
        date = "2026.10",
        isLatest = false,
        accentColor = Color(0x700284C7), // 天空蓝
        highlights = listOf(
            "全面优化大图预览动画与视觉效果，转场更丝滑。"
        )
    ),
    AuraVersionInfo(
        version = "v15.0",
        title = "交互进化",
        date = "2026.10",
        isLatest = false,
        accentColor = Color(0xFFF5A3BD), // 浅玫瑰粉色 (Light Pink)
        highlights = listOf(
            "大图预览新增下拉跟手返回，回弹更加 Q 弹自然。",
            "替换全新功能扩展图标，修复多项历史版本 UI 细节。",
            "全新联网检测，新增专属进度条实时弹窗。",
            "清理缓存功能进阶，彻底释放无用临时文件。"
        )
    ),
    AuraVersionInfo(
        version = "v14.0",
        title = "视觉底栏",
        date = "2026.10",
        isLatest = false,
        accentColor = Color(0xFFD0DC51), // 橄榄黄绿 (Lime / Yellow-Green)
        highlights = listOf(
            "优化主页背景色彩随时间流转的视觉体验。",
            "深度优化底栏视觉表现，更沉浸更优雅。",
            "优化长按自定义排序逻辑，拖拽更跟手。"
        )
    ),
    AuraVersionInfo(
        version = "v13.0",
        title = "手势跃升",
        date = "2026.09",
        isLatest = false,
        accentColor = Color(0xFFA855F7), // 兰花紫
        highlights = listOf(
            "主页背景视觉全面升级，引入高级感设计。",
            "首次新增时态渐变色彩，打破视觉沉闷。",
            "大图预览手势深度优化，滑动更自然跟手。"
        )
    ),
    AuraVersionInfo(
        version = "v12.0",
        title = "大图焕新",
        date = "2026.09",
        isLatest = false,
        accentColor = Color(0xFF97D0CA), // 淡罗兰
        highlights = listOf(
            "支持长按小标题进行自定义修改，你的应用你做主。",
            "大图预览页面美术风格全面升级，质感凸显。"
        )
    ),
    AuraVersionInfo(
        version = "v11.0",
        title = "轮换标语",
        date = "2026.08",
        isLatest = false,
        accentColor = Color(0xFFE879F9), // 霓虹紫
        highlights = listOf(
            "新增应用专属徽章，彰显独特身份。",
            "新增主标语时态轮换功能，每次打开都有新意。",
            "重构图片局部显示逻辑，视觉呈现更精准。"
        )
    ),
    AuraVersionInfo(
        version = "v10.0",
        title = "性能提速",
        date = "2026.08",
        isLatest = false,
        accentColor = Color(0xFFE7CA6D), // 浅卡其金黄 (Khaki Gold)
        highlights = listOf(
            "优化标题栏与底栏跟手滑动隐藏效果，视野更开阔。",
            "深度移动端性能优化，应用运行效率大幅提升。",
            "新增精密滚轮交互，优雅调整图片展示数量。",
            "完善应用通知权限，重要信息不漏接。"
        )
    ),
    AuraVersionInfo(
        version = "v9.0",
        title = "全屏空间",
        date = "2026.07",
        isLatest = false,
        accentColor = Color(0xFFF48093), // 珊瑚红
        highlights = listOf(
            "首次新增标题栏与底栏滑动隐藏，打造全屏沉浸感。",
            "新增 GitHub 主页一键跳转按钮。"
        )
    ),
    AuraVersionInfo(
        version = "v8.0",
        title = "架构奠基",
        date = "2026.06",
        isLatest = false,
        accentColor = Color(0xFF673AB7), // 经典深紫色 (Material Deep Purple)
        highlights = listOf(
            "拖动图片与按钮触发清脆的物理震感。",
            "支持自定义多选与长按空间排序规则。",
            "环境色彩调和，根据时辰自动律动渐变底色。"
        )
    ),
    AuraVersionInfo(
        version = "v7.0",
        title = "高刷适配",
        date = "2026.05",
        isLatest = false,
        accentColor = Color(0xFF00E5FF), // 荧光亮青色 (Electric Cyan)
        highlights = listOf(
            "全面适配 120Hz 帧模式，体验前所未有的丝滑。",
            "新增长按大图自定义排序，照片按你心意排布。",
            "解决特定场景下大图预览显示顺序异常问题。"
        )
    ),
    AuraVersionInfo(
        version = "v6.0",
        title = "存储优化",
        date = "2026.05",
        isLatest = false,
        accentColor = Color(0xFFEAB308), // 明亮黄
        highlights = listOf(
            "重构底层加载算法，大幅降低滑动时的 CPU 负载。",
            "新增缓存清理引擎，一键释放多余空间。",
            "中枢控制面板：全新添加设置入口，功能配置更集中。"
        )
    ),
    AuraVersionInfo(
        version = "v5.0",
        title = "稳态修复",
        date = "2026.04",
        isLatest = false,
        accentColor = Color(0xFFE040FB), // 荧光紫罗兰 (Violet / Magenta)
        highlights = listOf(
            "矩阵式多选引擎：解锁图片多选能力，批量操作更高效。",
            "预览体验进化：深度优化大图预览功能，视觉无死角。",
            "架构稳态防线：攻克特定机型进入大图时的计算闪退 Bug。"
        )
    ),
    AuraVersionInfo(
        version = "v4.0",
        title = "跟手优化",
        date = "2026.03",
        isLatest = false,
        accentColor = Color(0xFF03A9F4), // 晴空浅蓝色 (Material Light Blue)
        highlights = listOf(
            "首页标题新增微动效，呼吸感呼之欲出。",
            "大幅提升界面滑动阻尼算法，如丝般顺滑。",
            "全面优化应用架构，基石级稳定性提升。"
        )
    ),
    AuraVersionInfo(
        version = "v3.0",
        title = "档案记录",
        date = "2026.03",
        isLatest = false,
        accentColor = Color(0xFF14B8A6), // 青碧
        highlights = listOf(
            "添加了照片数量统计展示。",
            "首次加入版本更新日志，记录 Aura 进化的每一步。",
            "新增“关于此应用”，移除繁冗的使用帮助。"
        )
    ),
    AuraVersionInfo(
        version = "v2.0",
        title = "视界重构",
        date = "2026.02",
        isLatest = false,
        accentColor = Color(0xFFB7DBEB), // 蓝绿
        highlights = listOf(
            "上线大图预览功能，细节纤毫毕现。",
            "完美适配横屏比例，打破握持姿势限制。",
            "修复已知 Bug，打磨基础体验。"
        )
    ),
    AuraVersionInfo(
        version = "v1.0",
        title = "交互新生",
        date = "2026.02",
        isLatest = false,
        accentColor = Color(0xFF64748B), // 经典石板灰
        highlights = listOf(
            "重构页面基础布局，比例更协调。",
            "底部新增交互按钮，奠定 Aura 操控逻辑。",
            "首次提供使用帮助与基础设置选项。"
        )
    ),
    AuraVersionInfo(
        version = "Beta",
        title = "一切开始的起点",
        date = "2026.01",
        isLatest = false,
        accentColor = Color(0xFFF41203), // 烈火鲜烈红 (Fire Engine Red)
        highlights = listOf(
            "一款专为极客打造的自定义照片墙应用正式诞生。",
            "左滑精准删除，初始支持最多 4 张高定图片载入。",
            "首发即适配沉浸式状态栏功能，打破系统边界。"
        )
    )
)

@Composable
fun AuraVersionTimelineRow(
    item: AuraVersionInfo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(16.dp),
        // 1. 边框颜色：始终跟随自身的 accentColor
        border = BorderStroke(
            width = 1.dp,
            color = item.accentColor.copy(alpha = if (item.isLatest) 0.45f else 0.25f)
        ),
        colors = CardDefaults.cardColors(
            // 全版本动态通透：最新版 8% 高亮微透，旧版本 4% 浅色微透（粒子完全穿透无遮挡）
            containerColor = item.accentColor.copy(
                alpha = if (item.isLatest) 0.08f else 0.04f
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 版本标徽
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.accentColor.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = item.accentColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item.version,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = item.accentColor,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 单行主标题
            Text(
                text = item.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (LocalIsDarkTheme.current) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// =========================================================================================
// 📊 [主系统状态与版本演进看板：AuraMainDashboardCard]
// 【灵魂三问】：
// 1. 这是什么？
//    - 这是 Aura 18.0 的主系统状态监控与版本全景看板组件。
// 2. 它能干什么？
//    - 汇总展示内存负载、安全锁防护状态、图片总存储容量以及版本演进历史轴；
//    - 配备手势黑洞拦截器（NestedScrollConnection），实现列表顺畅内滚并彻底杜绝手势穿透冲突。
// 3. 怎么调整参数？
//    - 卡片尺寸与内边距：修改 modifier 的 height 与 padding。
//    - 手势滚动隔离：调整 isolateScrollConnection 的消费逻辑。
// =========================================================================================

/**
 * 🌟 [手势黑洞隔离拦截器: rememberIsolateScrollConnection]
 * 子列表优先消费滚动，未消费剩余的位移/惯性在 Post 阶段被完全吸收消费，绝不冒泡传递给外层父容器。
 */
@Composable
fun rememberIsolateScrollConnection(): NestedScrollConnection {
    return remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = available

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity = available
        }
    }
}

@Composable
fun AuraMainDashboardCard(
    onSelectVersion: (AuraVersionInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 🌟 红区专属：手势黑洞隔离拦截器 (复用通用隔离连接)
    val isolateScrollConnection = rememberIsolateScrollConnection()

    val isDarkTheme = LocalIsDarkTheme.current

    androidx.compose.material3.Surface(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = if (isDarkTheme) 0.25f else 0.05f),
                ambientColor = Color.Black.copy(alpha = if (isDarkTheme) 0.15f else 0.03f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.82f),
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.95f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. 顶部固定标题栏 (Pinned Header - 不随列表滚动)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF34C759))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aura 版本时间线",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDarkTheme) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 3.5.dp)
                    ) {
                        Text(
                            text = "共 ${auraMilestones.size} 个",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                // 2. 独立版本时间线可滚动列表流 (红区：挂载手势黑洞拦截器)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nestedScroll(isolateScrollConnection),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = auraMilestones,
                        key = { it.version },
                        contentType = { "milestone_item" }
                    ) { item ->
                        AuraVersionTimelineRow(
                            item = item,
                            onClick = { onSelectVersion(item) }
                        )
                    }
                }
            }

            // 3. 底部浮动轻量“返回顶部”辅助小按钮 (derivedStateOf 性能守卫：杜绝 120Hz 下高频无效重组)
            val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }
            androidx.compose.animation.AnimatedVisibility(
                visible = showScrollToTop,
                enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200)) +
                        androidx.compose.animation.slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = androidx.compose.animation.core.tween(200)
                        ),
                exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(150)) +
                       androidx.compose.animation.slideOutVertically(
                           targetOffsetY = { it / 2 },
                           animationSpec = androidx.compose.animation.core.tween(150)
                       ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.88f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↑ 返回顶部",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AuraReleaseNotesModal(
    versionInfo: AuraVersionInfo,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modalScrollConnection = rememberIsolateScrollConnection()

    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp) // 💡 调整此处更改弹窗固定高度（默认 420.dp，与大卡片 1:1 等大重合，保持绝对规整）
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.25f),
                ambientColor = Color.Black.copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.96f), // 深色极客暗晶质感
        border = androidx.compose.foundation.BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    versionInfo.accentColor,
                    Color(0xFF00E5FF),
                    Color(0xFF007AFF)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            // 🌟 1. 固定头部结构 (Pinned Header)：版本标签 + 主标题 + 霓虹分割线 (绝对锚定，无权重)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 顶部状态标签（版本标徽 + 发布时间，极简纯粹，已移除右上角冗余 X 按钮）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(versionInfo.accentColor.copy(alpha = 0.2f))
                            .border(1.dp, versionInfo.accentColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = versionInfo.version,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = versionInfo.accentColor,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${versionInfo.date} RELEASE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.6f),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp)) // 💡 调整此处更改顶部状态与主标题之间的间距

                // 版本主标题
                Text(
                    text = versionInfo.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp)) // 💡 调整此处更改分割线上方间距（默认 10.dp，确保紧凑贴合主标题）

                // 分割细线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    versionInfo.accentColor.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp)) // 💡 调整此处更改分割线与日志列表间距

            // 🌟 2. 可滚动内容区：更新日志列表强行占用全部剩余空间（weight(1f)），彻底锚定头部位置
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 🌟 核心锚定：占满中间剩余空间，无论日志多长多短，头部位置绝对零偏移
                    .nestedScroll(modalScrollConnection)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                versionInfo.highlights.forEach { note ->
                    Text(
                        text = "•  $note",
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 15.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp)) // 💡 调整此处更改日志列表与底部确认按钮间距

            // 🌟 3. 底部固定确认关闭按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                versionInfo.accentColor,
                                Color(0xFF007AFF)
                            )
                        )
                    )
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "确认",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// =========================================================================================
// 💧 [空间 3 专属] Telegram 液态水滴头像吸入摄像头动效组件
// =========================================================================================
@Composable
fun TelegramAvatarHeader(
    scrollState: androidx.compose.foundation.ScrollState,
    onAvatarPositioned: ((Offset, Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val collapseRangePx = with(density) { 140.dp.toPx() } // 自适应拉长跑道行程
    val cameraYPx = with(density) { 20.dp.toPx() }
    val cameraRadiusPx = with(density) { 11.dp.toPx() }
    val startYPx = with(density) { 140.dp.toPx() }        // 🌟 下调头像初始纵坐标 (140.dp)，提供充足纵向拉伸距离
    val startRadiusPx = with(density) { 46.dp.toPx() }

    // 归一化折叠进度 [0f, 1f]
    val progress = (scrollState.value / collapseRangePx).coerceIn(0f, 1f)

    // 🌟 制造 Y 轴平移的“物理滞后感” (Damped Movement)：
    // 前中段缓慢移离原点、抵抗上拉（模拟被卡片与重力拉扯），终段加速贴合至摄像头
    val yProgress = progress.pow(1.75f)

    // 动态坐标与尺寸插值
    val currentAvatarY = startYPx + (cameraYPx - startYPx) * yProgress
    val currentRadius = startRadiusPx + (cameraRadiusPx - startRadiusPx) * progress

    // 🌟 重新审视渐黑遮罩 (Blackout) 时机：
    // 在阶段一萌生轻微变黑 (0.20f -> 0.92f)，在 0.64f 延迟吸附瞬间达到 ~66% 浓度，最终融合时 100% 同化
    val blackoutRaw = ((progress - 0.20f) / (0.92f - 0.20f)).coerceIn(0f, 1f)
    val blackoutAlpha = blackoutRaw * blackoutRaw * (3f - 2f * blackoutRaw)

    // 整体渐隐透明度 (progress > 0.96f 时渐隐，与物理黑孔无缝融为一体)
    val overallAlpha = when {
        progress <= 0.96f -> 1f
        progress >= 1.0f -> 0f
        else -> 1f - (progress - 0.96f) / (1.0f - 0.96f)
    }

    // 完善物理弹性反馈：Q 弹自然的物理吸入与回弹曲线
    val snapSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // 松手磁吸物理回弹 (Snap Behavior)
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress) {
            val currentValue = scrollState.value
            if (currentValue in 1 until collapseRangePx.roundToInt()) {
                if (progress < 0.64f) {
                    scrollState.animateScrollTo(0, animationSpec = snapSpring)
                } else {
                    scrollState.animateScrollTo(collapseRangePx.roundToInt(), animationSpec = snapSpring)
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 🌟 两阶段真实物理状态机 (Two-Stage Fluid Physics)：
        // 临界吸附阈值 snapThreshold = 0.64f（让头像充分靠近至极近距离）
        // 阶段一 (0.05f <= progress < 0.64f)：双向极限引力形变（悬滴与小耳朵靠近、展开并零间隙相切）
        // 阶段二 (0.64f <= progress <= 0.98f)：连续表面张力拉丝（在 0.64f 处以 100% C1 连续性衔接并平滑展宽）
        val snapThreshold = 0.64f

        if (progress in 0.05f..0.98f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = overallAlpha }
            ) {
                val screenWidthPx = size.width
                val centerX = screenWidthPx / 2f
                val r1 = cameraRadiusPx
                val y1 = cameraYPx
                val r2 = currentRadius
                val y2 = currentAvatarY
                val deltaY = (y2 - y1).coerceAtLeast(1f)

                // 物理接触几何基准：
                val airGap = (deltaY - r1 - r2).coerceAtLeast(0f)
                val contactY = y1 + r1 + airGap * 0.50f // 摄像头与头像正中央的精确物理接触平面
                val w0 = r1 * 0.34f                     // 临界吸附接缝处基准半宽 (~4.76dp)

                if (progress < snapThreshold) {
                    // =================================================================
                    // 💧 阶段一：双向引力形变 (未连接状态，尖端平滑展宽并在 0.64f 达成 0 间隙相切)
                    // =================================================================
                    val stage1Progress = ((progress - 0.05f) / (snapThreshold - 0.05f)).coerceIn(0f, 1f)
                    val stage1Alpha = ((progress - 0.05f) / (0.14f - 0.05f)).coerceIn(0f, 1f)
                    val curvedT = stage1Progress.pow(1.30f) // 物理引力非线性指数拉伸

                    // 尖端展宽因子 (Tip Spread Hermite Smoothstep)：在最后冲刺阶段平滑展宽至 w0
                    val tipSpreadT = ((stage1Progress - 0.60f) / 0.40f).coerceIn(0f, 1f)
                    val smoothTipSpread = tipSpreadT * tipSpreadT * (3f - 2f * tipSpreadT)
                    val tipHalfWidth = w0 * smoothTipSpread

                    // 1px 亚像素微搭接：消除双多边形相切处的光栅化抗锯齿缝隙
                    val contactOverlap = 1f * smoothTipSpread

                    // 1. 摄像头黑孔底部向下延伸的纯黑“悬滴” (Pendant Droplet)
                    // 极限帧 0.64f 精确延伸至 contactY，两端水滴在临界点达成 0 间隙相切
                    val cameraDropLength = airGap * 0.50f * (0.12f + 0.88f * curvedT) + contactOverlap * 0.5f
                    val cameraTipY = y1 + r1 + cameraDropLength
                    val tipSharpness = r1 * 0.25f * (1f - 0.55f * stage1Progress)

                    val cameraPath = Path()
                    // 摄像头上半圆 (顺时针 180° -> 0°)
                    cameraPath.moveTo(centerX - r1, y1)
                    cameraPath.arcTo(
                        rect = Rect(
                            left = centerX - r1,
                            top = y1 - r1,
                            right = centerX + r1,
                            bottom = y1 + r1
                        ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    // 从右侧切向向下延伸、收束为向下的水滴
                    cameraPath.cubicTo(
                        x1 = centerX + r1 * (0.95f - 0.22f * stage1Progress),
                        y1 = y1 + r1 * 0.65f + (cameraTipY - y1 - r1) * 0.25f,
                        x2 = centerX + tipHalfWidth + tipSharpness,
                        y2 = cameraTipY - tipSharpness * 0.25f,
                        x3 = centerX + tipHalfWidth,
                        y3 = cameraTipY
                    )
                    // 尖端底线平滑过渡
                    if (tipHalfWidth > 0.5f) {
                        cameraPath.lineTo(centerX - tipHalfWidth, cameraTipY)
                    } else {
                        cameraPath.lineTo(centerX, cameraTipY)
                    }
                    // 从尖端对称回连至左侧
                    cameraPath.cubicTo(
                        x1 = centerX - tipHalfWidth - tipSharpness,
                        y1 = cameraTipY - tipSharpness * 0.25f,
                        x2 = centerX - r1 * (0.95f - 0.22f * stage1Progress),
                        y2 = y1 + r1 * 0.65f + (cameraTipY - y1 - r1) * 0.25f,
                        x3 = centerX - r1,
                        y3 = y1
                    )
                    cameraPath.close()

                    drawPath(
                        path = cameraPath,
                        color = Color.Black.copy(alpha = stage1Alpha)
                    )

                    // 2. 头像正上方受引力拉扯向上凸起的纯黑“小耳朵/水滴表面” (Upward Bulge)
                    // 极限帧 0.64f 同样精确延伸至 contactY，与上方滴落水滴在 contactY 零缝隙对接
                    val avatarBulgeHeight = airGap * 0.50f * (0.12f + 0.88f * curvedT) + contactOverlap * 0.5f
                    val avatarApexY = y2 - r2 - avatarBulgeHeight
                    val stage1AttachSpread = ((stage1Progress - 0.40f) / 0.60f).coerceIn(0f, 1f)
                    val smoothAttach = stage1AttachSpread * stage1AttachSpread * (3f - 2f * stage1AttachSpread)
                    val bulgeBaseRadius = r1 * (1.05f + 0.40f * smoothAttach)
                    val baseOffsetY = r2 - sqrt((r2 * r2 - bulgeBaseRadius * bulgeBaseRadius).coerceAtLeast(0f))
                    val baseAttachY = y2 - r2 + baseOffsetY
                    val earSharpness = bulgeBaseRadius * 0.28f * (1f - 0.50f * stage1Progress)

                    val avatarBulgePath = Path()
                    avatarBulgePath.moveTo(centerX + bulgeBaseRadius, baseAttachY)
                    // 从右侧基座向上隆起为向上的小耳朵凸起
                    avatarBulgePath.cubicTo(
                        x1 = centerX + bulgeBaseRadius * (0.90f - 0.15f * stage1Progress),
                        y1 = y2 - r2 - (y2 - r2 - avatarApexY) * 0.35f,
                        x2 = centerX + tipHalfWidth + earSharpness,
                        y2 = avatarApexY + earSharpness * 0.25f,
                        x3 = centerX + tipHalfWidth,
                        y3 = avatarApexY
                    )
                    // 耳朵顶线平滑过渡
                    if (tipHalfWidth > 0.5f) {
                        avatarBulgePath.lineTo(centerX - tipHalfWidth, avatarApexY)
                    } else {
                        avatarBulgePath.lineTo(centerX, avatarApexY)
                    }
                    // 从耳朵顶点对称滑向左侧基座
                    avatarBulgePath.cubicTo(
                        x1 = centerX - tipHalfWidth - earSharpness,
                        y1 = avatarApexY + earSharpness * 0.25f,
                        x2 = centerX - bulgeBaseRadius * (0.90f - 0.15f * stage1Progress),
                        y2 = y2 - r2 - (y2 - r2 - avatarApexY) * 0.35f,
                        x3 = centerX - bulgeBaseRadius,
                        y3 = baseAttachY
                    )
                    // 闭合在头像球体内部
                    avatarBulgePath.lineTo(centerX, y2)
                    avatarBulgePath.close()

                    drawPath(
                        path = avatarBulgePath,
                        color = Color.Black.copy(alpha = stage1Alpha)
                    )

                } else {
                    // =================================================================
                    // 🌊 阶段二：表面张力贯通拉丝 (在 0.64f 处以 100% C1 连续性衔接并平滑展宽)
                    // =================================================================
                    val stage2Progress = ((progress - snapThreshold) / (0.98f - snapThreshold)).coerceIn(0f, 1f)
                    val smoothNeck = stage2Progress * stage2Progress * (3f - 2f * stage2Progress)
                    val bridgeAlpha = if (progress > 0.95f) ((0.98f - progress) / (0.98f - 0.95f)).coerceIn(0f, 1f) else 1f

                    // 临界承接：在 0.64f 瞬间脖颈宽度精准为 w0，随后随 smoothNeck 平滑展宽至 r1
                    val baseNeck = w0 + (r1 - w0) * smoothNeck

                    // 基座承接：在 0.64f 瞬间头像附着点精准为 r1 * 1.45f，随后平滑延展至 r2 (赤道)
                    val initialAttachRadius = r1 * 1.45f
                    val currentAttachRadius = initialAttachRadius + (r2 - initialAttachRadius) * smoothNeck
                    val attachOffsetY = r2 - sqrt((r2 * r2 - currentAttachRadius * currentAttachRadius).coerceAtLeast(0f))
                    val attachY = y2 - r2 + attachOffsetY

                    val stage2TipSharpness = r1 * 0.25f * 0.45f * (1f - 0.50f * smoothNeck)
                    val stage2EarSharpness = initialAttachRadius * 0.14f * (1f - 0.50f * smoothNeck)

                    val connectedPath = Path()

                    // 顶部摄像头圆弧（顺时针 180° -> 0°）
                    connectedPath.moveTo(centerX - r1, y1)
                    connectedPath.arcTo(
                        rect = Rect(
                            left = centerX - r1,
                            top = y1 - r1,
                            right = centerX + r1,
                            bottom = y1 + r1
                        ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )

                    // 右侧上半段：从摄像头顺畅滑入中央颈部 (contactY)
                    connectedPath.cubicTo(
                        x1 = centerX + r1 * (0.95f - 0.22f),
                        y1 = y1 + r1 * 0.65f + (contactY - y1 - r1) * 0.25f,
                        x2 = centerX + baseNeck + stage2TipSharpness,
                        y2 = contactY - stage2TipSharpness * 0.25f,
                        x3 = centerX + baseNeck,
                        y3 = contactY
                    )

                    // 右侧下半段：从中央颈部 (contactY) 顺畅延展至头像附着点
                    connectedPath.cubicTo(
                        x1 = centerX + baseNeck + stage2EarSharpness,
                        y1 = contactY + stage2EarSharpness * 0.25f,
                        x2 = centerX + currentAttachRadius * (0.90f - 0.15f),
                        y2 = y2 - r2 - (y2 - r2 - contactY) * 0.35f,
                        x3 = centerX + currentAttachRadius,
                        y3 = attachY
                    )

                    // 头像圆弧部分：从 attachY 顺时针环绕底部到左侧对称附着点
                    val attachAngleRad = kotlin.math.asin(((attachY - y2) / r2).coerceIn(-1f, 1f))
                    val attachAngleDeg = attachAngleRad * (180f / kotlin.math.PI.toFloat())
                    val sweepAngle = 180f - 2f * attachAngleDeg

                    connectedPath.arcTo(
                        rect = Rect(
                            left = centerX - r2,
                            top = y2 - r2,
                            right = centerX + r2,
                            bottom = y2 + r2
                        ),
                        startAngleDegrees = attachAngleDeg,
                        sweepAngleDegrees = sweepAngle,
                        forceMoveTo = false
                    )

                    // 左侧下半段：从左侧附着点回连至中央颈部
                    connectedPath.cubicTo(
                        x1 = centerX - currentAttachRadius * (0.90f - 0.15f),
                        y1 = y2 - r2 - (y2 - r2 - contactY) * 0.35f,
                        x2 = centerX - baseNeck - stage2EarSharpness,
                        y2 = contactY + stage2EarSharpness * 0.25f,
                        x3 = centerX - baseNeck,
                        y3 = contactY
                    )

                    // 左侧上半段：从中央颈部回连至顶部摄像头圆弧左端
                    connectedPath.cubicTo(
                        x1 = centerX - baseNeck - stage2TipSharpness,
                        y1 = contactY - stage2TipSharpness * 0.25f,
                        x2 = centerX - r1 * (0.95f - 0.22f),
                        y2 = y1 + r1 * 0.65f + (contactY - y1 - r1) * 0.25f,
                        x3 = centerX - r1,
                        y3 = y1
                    )

                    connectedPath.close()

                    drawPath(
                        path = connectedPath,
                        color = Color.Black.copy(alpha = bridgeAlpha)
                    )
                }
            }
        }

        // 2. 顶层：圆形头像及渐黑遮罩
        val currentAvatarYDp = with(density) { currentAvatarY.toDp() }
        val currentRadiusDp = with(density) { currentRadius.toDp() }
        val currentDiameterDp = currentRadiusDp * 2
        val avatarTopDp = currentAvatarYDp - currentRadiusDp

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = avatarTopDp)
                .size(currentDiameterDp)
                .graphicsLayer { alpha = overallAlpha }
                .clip(CircleShape)
                .onGloballyPositioned { coordinates ->
                    if (coordinates.isAttached) {
                        val bounds = coordinates.boundsInRoot()
                        // 🌟 严格仅由圆形头像自身上报真实视觉中心与物理半径，杜绝任何二次扣减导致的悬浮偏移
                        onAvatarPositioned?.invoke(bounds.center, bounds.width / 2f)
                    }
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = "Telegram Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (blackoutAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = blackoutAlpha))
                )
            }
        }
    }
}
