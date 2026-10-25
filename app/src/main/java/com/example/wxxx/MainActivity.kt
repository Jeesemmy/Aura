package com.example.wxxx

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.example.wxxx.ui.theme.WXXXTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// =========================================================================================
// 💡 [底层数据存储配置区]
// 这里负责定义本地存储（DataStore）的名称和各种数据的“钥匙”（Key）。
// 相当于给 App 买了一个保险箱，并贴上标签。
// ========================================================
private val Context.dataStore by preferencesDataStore(name = "photo_settings")
private val SAVED_URIS_KEY = stringPreferencesKey("saved_uris")
private val SHOW_ANNOUNCEMENT_KEY = booleanPreferencesKey("show_announcement")
private val PHOTO_COUNT_KEY = stringPreferencesKey("photo_count") // 存储图框数量（1-10）
private val SUBTITLE_KEY = stringPreferencesKey("custom_subtitle")

// 左滑删除的状态枚举：Settled(静止/复位) | Open(划开显示垃圾桶)
enum class DragValue { Settled, Open }

// =========================================================================================
// 💡 [App 入口：MainActivity]
// 这是整个应用的起点，就像房子的地基。
// ========================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()// 启用原生冷启动闪屏页 (12.0+ 标准)
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

        // 挂载 Compose UI 主界面
        setContent {
            WXXXTheme {
                MainScreen()
            }
        }
    }
}

// =========================================================================================
// 💡 [核心主屏幕：MainScreen]
// 这是 Aura App 的心脏，所有的主页 UI、状态管理、动画都聚集在这里。
// ==============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current// 震动马达控制器8.0
    val scope = rememberCoroutineScope()// 协程作用域，用于执行异步任务（如存数据、播动画）

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
    val selectedImageUris = remember { mutableStateListOf<Uri?>().apply { repeat(10) { add(null) } } }// 存放最多10张图的真实地址
    val itemOrder = remember { mutableStateListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) }// 图片视觉排序的索引清单

    // 各种弹窗和面板的开关控制
    var isFabExpanded by remember { mutableStateOf(false) }
    var customSubtitle by remember { mutableStateOf("今天也要开心呀🥰") }
    var showEditSubtitleDialog by remember { mutableStateOf(false) }
    var tempSubtitleInput by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) } // 🌟 新增：控制独立排序弹窗的显示与隐藏
    var showAnnouncementDialog by rememberSaveable { mutableStateOf(false) }
    var previewUri by rememberSaveable { mutableStateOf<Uri?>(null) }// 用于大图预览，如果非空就弹起全屏大图
    var titleVisible by remember { mutableStateOf(false) }

    // ----------------------------------------------------------------------
    // 📜 [列表滑动联动动画]
    // 监听滚轮状态，控制顶部标题和底部胶囊的自动隐藏/浮现
    // ----------------------------------------------------------------------
    val lazyListState = rememberLazyListState()
    val isScrollingUp = lazyListState.isScrollingUp()// 判断当前是否在向上滑动

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
        displayCount = prefs[PHOTO_COUNT_KEY]?.toIntOrNull() ?: 7

        // 🌟 副标语在这里
        customSubtitle = prefs[SUBTITLE_KEY] ?: "长按我可修改🗽"

        val savedString = prefs[SAVED_URIS_KEY] ?: ""
        if (savedString.isNotEmpty()) {
            savedString.split("|").forEachIndexed { index, path ->

                if (path.isNotEmpty() && index < 10) {
                    selectedImageUris[index] = Uri.parse(path)
                }
            }
        }
    }

    // 判断是否需要弹出版本公告
    var hasCheckedAnnouncement by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasCheckedAnnouncement) {
            val prefs = context.dataStore.data.first()
            if (prefs[SHOW_ANNOUNCEMENT_KEY] ?: true) {
                showAnnouncementDialog = true
            }
            hasCheckedAnnouncement = true
        }
    }

    // 5.2 升级：支持多选图片并优化填充逻辑
    // 相册多选图选择器
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val emptySlots = itemOrder.filter { selectedImageUris[it] == null }

            uris.take(emptySlots.size).forEachIndexed { index, uri ->
                try {
                    // 申请图片的永久读取权限
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                selectedImageUris[emptySlots[index]] = uri
            }
            saveUrisToDisk()
        }
    }

    // ----------------------------------------------------------------------
    // 🎨 [时态视觉引擎 (Gemini Style)]
    // 根据当前 24 小时制时间，动态计算问候语和底部光晕颜色
    // ----------------------------------------------------------------------
    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }

    val greetingText = when (hour) {
        in 7..11 -> "早上好"
        in 12..14 -> "中午好"
        in 15..19 -> "下午好"
        else -> "晚上好"
    }

    // 2. 统一计算底部光晕颜色（全天候 Gemini 呼吸光晕风格）
    val bottomGradientColor = when (hour) {
        in 7..11 -> Color(0xFFFFCC80).copy(alpha = 0.4f)  // 🌅 早上：Gemini风 晨曦粉橘 (Orange 200)
        in 12..14 -> Color(0xFF80DEEA).copy(alpha = 0.4f) // ☀️ 中午：Gemini风 清透浅青 (Cyan 200)
        in 15..19 -> Color(0xFF8AB4F8).copy(alpha = 0.45f)// ⛅ 下午：Gemini本尊 天空蓝 (Blue 300)
        else -> Color(0xFFB39DDB).copy(alpha = 0.45f)      // 🌃 晚上：Gemini风 梦幻星空紫 (Deep Purple 200)
    }


    // 极光呼吸动效引擎：控制底部光晕在 0.4 到 0.8 的屏幕高度间缓慢往复起伏
    val breathingTransition = rememberInfiniteTransition(label = "breathing")
    val glowFraction by breathingTransition.animateFloat(
        initialValue = 0.80f,  // 状态 A：光晕收缩（压在屏幕底部 75% 处）
        targetValue = 0.40f,   // 状态 B：光晕扩散（向上蔓延到屏幕 40% 处）
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing), // 4秒一次深呼吸的缓慢流体节奏
            repeatMode = RepeatMode.Reverse // 来回往复，生生不息
        ),
        label = "glowFraction"
    )

    // =========================================================================================
    // 🖼️ [页面主骨架：Scaffold & UI 树]
    // ==============================================
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // 底色必须透明，才能露出后方光晕
        floatingActionButtonPosition = FabPosition.Center,
        // 👇 这里挂载底部灵动绿叶胶囊
        floatingActionButton = {
            if (previewUri == null)// 大图预览时隐藏胶囊
                Box(modifier = Modifier.offset { androidx.compose.ui.unit.IntOffset(0, bottomBarOffset.roundToPx()) }) {
                    ExpandableFab(
                        // 智能识别：告诉胶囊现在该滑到哪个图标下面
                        selectedIndex = when {
                            isSettingsOpen -> 2      // 对应右侧的“设置”按钮
                            showHelpDialog -> 1      // 对应中间的“版本日志”按钮
                            else -> 0                // 默认停在左侧占位符 //
                            },
                        onHomeClick = { isSettingsOpen = false
                            // 点主页，关掉所有弹窗
                            showHelpDialog = false
                        },
                        onLogsClick = {
                            // 点日志，打开日志弹窗
                            isSettingsOpen = false
                            showHelpDialog = true
                        },
                        onSettingsClick = {
                            // 点设置，打开设置弹窗
                            isSettingsOpen = true
                            showHelpDialog = false
                        }
                    )
                }
        }

    ) { innerPadding ->
        // 背景光晕渲染层
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 三段式柔和渐变，营造无边界大雾过渡感
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        0.0f to MaterialTheme.colorScheme.background, // 顶部：保持纯净底色
                        glowFraction to bottomGradientColor.copy(alpha = 0.15f), // 🌊 中间：引入一层极淡的同色系过渡雾气，完美过渡！
                        1.0f to bottomGradientColor                   // 底部：最浓郁的色彩核心
                    )
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
                            // 👇 3. 核心修复：加一层 50% 不透明度的背景底板，完美隔离底部滚动的图片！
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))// 半透明遮罩隔离底部滑动的图片
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
                            color = Color.Black,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.graphicsLayer { rotationZ = angle }// 摇摆特效
                        )

                        Spacer(modifier = Modifier.width(12.dp))
                        // 🌟 改造右侧副标题：支持长按弹出自定义输入框
                        Text(
                            text = "       $customSubtitle\n",
                            color = Color.Gray,
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

                // ----------------------------------------------------------------------
                // 📱 画框主列表 (LazyColumn)
                // --------------------------------
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
                    // 依然读取你的动态数量 displayCount，保证设置滚轮有效
                    items(items = itemOrder.take(displayCount), key = { it }) { originalIndex ->

                        @OptIn(ExperimentalFoundationApi::class)
                        Box(
                            modifier = Modifier
                                .animateItemPlacement(
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                                // 🌟 核心修复：用 pointerInput 统一接管单击与长按，彻底解决冲突没有反应的问题
                                .pointerInput(Unit) { // 如果上面报错，也可以直接用 .pointerInput(Unit)
                                    detectTapGestures(
                                        onTap = {
                                            // 如果图片为空就去选图，有图片就进入大图预览
                                            if (selectedImageUris[originalIndex] == null) {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            } else {
                                                previewUri = selectedImageUris[originalIndex]
                                            }
                                        },
                                        onLongPress = {
                                            // 长按逻辑：震动并弹出独立排序窗口
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showSortDialog = true
                                        }
                                    )
                                }
                        ) {
                            // ⚠️ 注意：这里直接渲染 AppListItem，不再把它的内部点击事件写得太复杂
                            // 单张图片卡片组件
                            AppListItem(
                                uri = selectedImageUris[originalIndex],
                                onSelect = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onPreview = { uri ->
                                    if (uri != null) previewUri = uri
                                },
                                onDelete = {
                                    selectedImageUris[originalIndex] = null
                                    saveUrisToDisk()
                                },
                                // 🌟 绑定长按事件弹出天气风格的排序窗口
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
        // =======================================
        // 1. 版本公告弹窗
        if (showAnnouncementDialog) {
            val maxDialogHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.7f).dp
            AlertDialog(
                modifier = Modifier
                    .width(340.dp)
                    .heightIn(max = maxDialogHeight), // 🌟 自适应高度（Wrap Content）：字少紧凑包裹，字多最高占屏幕70%并支持滚动
                onDismissRequest = { showAnnouncementDialog = false },
                title = { Text(text = "公告🍛") },
                text = {
                    // 用 Column 把多行文字垂直排列起来，支持上下滑动
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "本次更新：")
                        Text(text = "1.优化大图预览动画效果.")
                        Text(text = "2.优化大图预览视觉效果.")
                        Text(text = "3.优化开发者卡片视觉效果.")


                        // 第 5 行：水平排列文字与双芯徽章（无背景透明版）
                        // 合作伙伴徽章排列
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "4.技术参考与支持")
                            Spacer(modifier = Modifier.width(6.dp))

                            // 1. 🌟 新增：Google 图标（无背景）
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.height(14.dp), // 保持 14.dp 与骁龙、天玑完美对齐
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp)) // 两个图标之间的间距

                            // 2. 骁龙图标（无背景）
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_gemini_color),
                                    contentDescription = "Snapdragon",
                                    modifier = Modifier.height(14.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp)) // 两个图标之间的间距

                            // 3. 天玑图标（无背景）
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_antigravity),
                                    contentDescription = "Dimensity",
                                    modifier = Modifier.height(14.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAnnouncementDialog = false }) {
                        Text("我知道了")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                context.dataStore.edit { it[SHOW_ANNOUNCEMENT_KEY] = false }
                            }
                            showAnnouncementDialog = false
                        }
                    ) {
                        Text("不再显示")
                    }
                }
            )
        }

        // 2. 自定义副标语修改弹窗
        if (showEditSubtitleDialog) {
            AlertDialog(
                onDismissRequest = { showEditSubtitleDialog = false },
                shape = RoundedCornerShape(28.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {

                        Text(text = "副标语", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "输入你想对自己说的话（最多 10 个字）",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        // 🌟 加上完整的包名前缀
                        androidx.compose.material3.OutlinedTextField(
                            value = tempSubtitleInput,
                            onValueChange = {
                                if (it.length <= 10) {
                                    tempSubtitleInput = it
                                }
                            },
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(16.dp),
                            placeholder = { Text("例如：你终于发现这里了", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "${tempSubtitleInput.length}/10",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (tempSubtitleInput.length == 10) Color.Red else Color.Gray
                            )
                        }
                    }
                },
                confirmButton = {
                    // 🌟 加上完整的包名前缀
                    androidx.compose.material3.Button(
                        onClick = {
                            if (tempSubtitleInput.isNotBlank()) {
                                customSubtitle = tempSubtitleInput
                                scope.launch {
                                    context.dataStore.edit { it[SUBTITLE_KEY] = customSubtitle }
                                }
                            }
                            showEditSubtitleDialog = false
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("保存", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showEditSubtitleDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("取消", color = Color.Gray)
                    }
                }
            )
        }

        // 3. 版本日志弹窗 (书籍图标)
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                // 🌟 直接在这里指定宽度为 340.dp，让它和设置面板的视觉宽度完全一致
                modifier = Modifier
                    .width(340.dp)
                    .fillMaxHeight(0.7f),
                title = {
                    Text(
                        text = "关于应用",
                        fontWeight = FontWeight.Bold, // 👈 标题加粗，对齐设置页标题样式
                        fontSize = 22.sp
                    )
                },
                text = {
                    Text(
                        text = "历史版本日志-\n\n" +

                                "(26.10.26)-16.0版本🍛\n1.优化大图预览动画效果.\n2.优化大图预览视觉效果.\n3.优化开发者卡片视觉效果.\n\n" +
                                "(26.10.22)-15.0版本💌\n1.优化大图预览回弹动画.\n2.替换了全新的扩展与功能图标.\n3.修复了多项历史版本的UI细节与动效.\n" +
                                "4.清理缓存功能升级,释放无用临时文件.\n5.新增大图预览的下划跟手返回动画效果.\n6.新增下载更新专属进度条弹窗,实时显示.\n" +
                                "7.全新升级联网检测更新功能,便捷化更新.\n\n" +

                                "(26.10.20)-14.0版本🔖\n优化主页背景时态渐变色彩视觉体验.\n优化长按图片自定义排序功能.\n优化胶囊底栏视觉体验.\n\n" +
                                "(26.10.19)-13.0版本🥂\n主页背景美术升级.\n新增时态渐变色彩.\n优化了大图预览手势交互.\n\n" +
                                "(26.10.17)-12.0版本🍹\n长按小标题可自定义修改.\n大图预览页面美术升级.\n\n" +
                                "(26.10.1)-11.0版本🍃\n新增应用专属徽章.\n优化图片高刷显示链路.\n新增主标语时态轮换功能.\n\n" +
                                "(26.9.12)-10.0版本✨\n优化标题栏与底栏滑动隐藏效果.\n移动端性能优化,提高运行效率.\n新增应用通知权限.\n新增滚轮调整图片数量.\n\n" +
                                "(26.9.10)-9.0版本🎉\n新增标题栏与底栏的滑动隐藏效果.\n新增GitHub主页跳转.\n\n" +
                                "(26.5.24)-8.0版本🏆\n优化视觉体验.提升响应速度.\n拖动图片引入震动反馈.\n新增检测更新功能.\n\n" +
                                "(26.5.19)-7.0版本🧋\n适配了120Hz帧模式.\n长按大图可自定义排序.\n修复了大图预览显示顺序异常的问题.\n\n" +
                                "(26.4.23)-6.0版本🐳\n优化了加载策略.降低滑动时CPU负载.\n" + "新增缓存清理功能.\n" + "添加了设置入口.\n\n" +
                                "(26.4.15)-5.0版本🤖\n优化大图预览功能.\n新增图片多选功能.\n修复了特定机型下进入大图预览的\n计算闪退 bug.\n\n" +
                                "(26.4.14)-4.0版本🤔\n提升应用稳定性.架构全面优化.\n首页标题新增动画.\n提升界面滑动跟手性.\n\n" +
                                "(26.4.13)-3.0版本😳\n添加了照片数量.移除使用帮助.\n新增关于此应用.\n添加了版本日志.\n\n" +
                                "(26.4.11)-2.0版本🥽\n新增大图预览功能.\n适配横屏比例.修复已知bug.\n\n" +
                                "(26.2.14)-1.0版本✨\n优化页面布局.\n底部新增交互按钮.\n提供了使用帮助与设置选项.\n\n" +
                                "(26.1.04)-Beta版本🎗️\nPs：一款可以添加自定义照片的应用.\n左滑图片可删除.最多添加四张图片.\n适配沉浸式状态栏功能.",
                        lineHeight = 20.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("关闭")
                    }
                }
            )
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
            val safeInitialPage = remember(previewUri) {
                val rawIndex = selectedImageUris.indexOf(previewUri)
                itemOrder.indexOf(rawIndex).coerceAtLeast(0)
            }
            val pagerState = rememberPagerState(initialPage = safeInitialPage, pageCount = { displayCount })

            FullScreenDialog(onDismiss = { previewUri = null }) { pagerModifier, bgAlpha, onTriggerExit ->
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
                            AsyncImage(
                                model = currentBgUri,
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
                        beyondViewportPageCount = 1
                    ) { page ->
                        val realIndex = itemOrder.getOrNull(page) ?: page
                        val currentUri = selectedImageUris.getOrNull(realIndex)

                        // 翻页微缩放动效
                        val pageOffset = kotlin.math.abs((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
                        val pageScale = 1f - (pageOffset * 0.12f)
                        val pageAlpha = 1f - (pageOffset * 0.4f)

                        if (currentUri != null) {
                            var scale by rememberSaveable(page) { mutableFloatStateOf(1f) }
                            var offsetX by rememberSaveable(page) { mutableFloatStateOf(0f) }
                            var offsetY by rememberSaveable(page) { mutableFloatStateOf(0f) }

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
                                        scaleX = pageScale
                                        scaleY = pageScale
                                        alpha = pageAlpha
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
                                val painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(context)
                                        .data(currentUri)
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

                    // 🌟 核心升级 4：底部灵动圆点指示器
                    Row(
                        modifier = Modifier
                            .graphicsLayer { alpha = bgAlpha }
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 42.dp)
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

        // 6. 系统设置大面板 (控制图片数量、缓存清理、Github检查)
        if (isSettingsOpen) {
            AuraSettingsScreen(
                currentCount = displayCount,               // 👈 把当前的数量传进去
                onCountChange = { displayCount = it },     // 👈 把修改后的数量传回给主
                onBack = { isSettingsOpen = false })
        }
    }
}

// =========================================================================================
// 💊 [底部灵动悬浮胶囊：ExpandableFab]
// 14.0 核心黑科技：绿叶滑动动效、毛玻璃质感底座
// ==================================================
@Composable
fun ExpandableFab(
    selectedIndex: Int,// 传入状态：0(主页占位), 1(日志), 2(设置)
    onHomeClick: () -> Unit,
    onLogsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {

    // 依据当前的选中状态，利用弹簧动画计算绿叶 Logo X 轴的物理偏移量 (每次滑 60.dp)
    val indicatorOffset by animateDpAsState(
        targetValue = (selectedIndex * 60).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,// Q弹阻尼
            stiffness = Spring.StiffnessLow// 缓慢丝滑的弹性
        ),
        label = "indicatorOffset"
    )

    // 极致毛玻璃底座 (无黑边阴影，纯净反光白边)
    Surface(
        modifier = Modifier.height(64.dp),   //👈控制着胶囊的总高度！
        shape = RoundedCornerShape(32.dp),  // 👈 这里的 32.dp 是两头的圆角弧度
        color = Color.White.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = Color.White.copy(alpha = 0.4f)
        ),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 18.dp)// 👈 把原本的 12.dp 改大（比如 20.dp 或 24.dp），胶囊两头就会自动向外延伸变长
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            // 底层：会滑动的绿叶 Logo！纯净无底色叠加。
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset.value.dp)
                    .size(48.dp), // 只保留 48.dp 的移动占位，不加任何颜色背景
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo_leaf), // 你的 Logo 资源
                    contentDescription = "Active Indicator",
                    modifier = Modifier.size(45.dp), // 调整 Logo 大小
                    contentScale = ContentScale.Fit
                )
            }

            // 顶层：横向排列的三个图标区域
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 用于图标被绿叶光顾时产生微量放大的呼吸感
                val homeScale by androidx.compose.animation.core.animateFloatAsState(if (selectedIndex == 0) 1.15f else 1f)
                val logsScale by androidx.compose.animation.core.animateFloatAsState(if (selectedIndex == 1) 1.15f else 1f)
                val settingsScale by androidx.compose.animation.core.animateFloatAsState(if (selectedIndex == 2) 1.15f else 1f)

                // 占位主页按钮：物理留存宽度，视觉彻底隐藏，给绿叶留出滑行轨道
                Spacer(modifier = Modifier.size(48.dp))

                // 日志图标区域：剥除系统点击水波纹，实现纯净点击
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onLogsClick() } // 🌟 仅保留纯粹的点击跳转
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit_calendar),
                        contentDescription = "Edit Calendar",
                        // 如果绿叶滑过来，变成通透白色，否则为高级深灰
                        tint = if (selectedIndex == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                // 设置图标区域
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onSettingsClick() } // 🌟 仅保留纯粹的点击跳转
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = if (selectedIndex == 2) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
    content: @Composable (pagerModifier: Modifier, bgAlpha: Float, onTriggerExit: () -> Unit) -> Unit
) {
    val view = androidx.compose.ui.platform.LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 进入预览时将顶部时间/电量图标变为纯白色，退出预览时自动恢复深色
    androidx.compose.runtime.DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val controller =
            window?.let { androidx.core.view.WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = true
        }
    }

    // 核心物理动效控制器：缩放、位移X、位移Y、圆角半径、底层遮罩透明度、卡片内容透明度
    // 🌟 初始第0帧状态：alpha=0f（彻底杜绝首帧黑屏闪现），主体卡片 scale=0.9f，radius=20dp
    val animScale = remember { androidx.compose.animation.core.Animatable(0.9f) }
    val animOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val animOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val animRadius = remember { androidx.compose.animation.core.Animatable(20f) }
    val animScrimAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val animCardAlpha = remember { androidx.compose.animation.core.Animatable(0f) }

    var isDismissing by remember { mutableStateOf(false) }

    // 统一的多轨惯性退场动画闭环：下拉释放与右上角关闭共用
    val triggerExit: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            scope.launch {
                val exitOffsetY = animOffsetY.value + with(density) { 360.dp.toPx() }
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

    // 屏幕内直接下滑拖拽手势 (Drag-to-Dismiss)
    var dragAccumulatedY by remember { mutableFloatStateOf(0f) }
    val dismissThresholdPx = with(density) { 120.dp.toPx() }

    val safeRadius = animRadius.value.coerceAtLeast(0f)
    val safeAlpha = animCardAlpha.value.coerceIn(0f, 1f)
    val safeBgAlpha = animScrimAlpha.value.coerceIn(0f, 1f)

    // 🌟 单独赋给内部 HorizontalPager 的专属修饰符：承担呼吸缩放、下滑位移与圆角，绝不影响外层铺满的黑底
    val pagerModifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
            scaleX = animScale.value.coerceAtLeast(0.5f)
            scaleY = animScale.value.coerceAtLeast(0.5f)
            translationX = animOffsetX.value
            translationY = animOffsetY.value
            alpha = safeAlpha
            clip = safeRadius > 0.1f
            shape = RoundedCornerShape(safeRadius.dp)
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f)
            .pointerInput(Unit) { detectTapGestures { } } // 拦截穿透点击
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        if (!isDismissing) {
                            dragAccumulatedY = 0f
                        }
                    },
                    onDragEnd = {
                        if (!isDismissing) {
                            if (dragAccumulatedY > dismissThresholdPx) {
                                // 🌟 超过阈值：多轨惯性协同退场，卡片与背景如薄雾般柔和消散，彻底告别硬切
                                triggerExit()
                            } else {
                                // 未达阈值：物理弹簧柔和归位
                                scope.launch {
                                    launch { animOffsetY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) }
                                    launch { animOffsetX.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) }
                                    launch { animScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) }
                                    launch { animCardAlpha.animateTo(1f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) }
                                    launch { animRadius.animateTo(0f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) }
                                    launch { animScrimAlpha.animateTo(1f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) }
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        if (!isDismissing) {
                            scope.launch {
                                launch { animOffsetY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) }
                                launch { animOffsetX.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) }
                                launch { animScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) }
                                launch { animCardAlpha.animateTo(1f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) }
                                launch { animRadius.animateTo(0f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) }
                                launch { animScrimAlpha.animateTo(1f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) }
                            }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (!isDismissing) {
                            dragAccumulatedY += dragAmount
                            if (dragAccumulatedY > 0) {
                                change.consume()
                                val dragRatio = (dragAccumulatedY / 500f).coerceIn(0f, 1f)
                                val currentScale = (1f - dragRatio * 0.22f).coerceIn(0.78f, 1f)
                                val currentRadius = (dragRatio * 28f).coerceIn(0f, 28f)
                                val currentScrim = (1f - dragRatio * 0.75f).coerceIn(0f, 1f)
                                val currentCardAlpha = (1f - dragRatio * 0.15f).coerceIn(0.6f, 1f)

                                scope.launch {
                                    animOffsetY.snapTo(dragAccumulatedY)
                                    animScale.snapTo(currentScale)
                                    animRadius.snapTo(currentRadius)
                                    animScrimAlpha.snapTo(currentScrim)
                                    animCardAlpha.snapTo(currentCardAlpha)
                                }
                            }
                        }
                    }
                )
            }
    ) {
        content(pagerModifier, safeBgAlpha, triggerExit)
    }
}

// =========================================================================================
// ⚙️ [设置面板控制台：AuraSettingsScreen]
// GitHub 账号模块、闹钟级滑动选数器、缓存清理
// ===============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraSettingsScreen(
    currentCount: Int,                // 👈 加上这行
    onCountChange: (Int) -> Unit,     // 👈 加上这行
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateReleaseInfo by remember { mutableStateOf<UpdateManager.ReleaseInfo?>(null) }
    val downloadProgressState by UpdateManager.downloadProgress.collectAsState()

    AlertDialog(
        onDismissRequest = onBack, // 点击弹窗外部也会自动关闭
        modifier = Modifier.fillMaxHeight(0.7f), // 限制一下最大高度，防止卡片太多超出屏幕
        title = {
            Text("设置", fontWeight = FontWeight.Bold)
        },
        text = {
            // 在这里加上 verticalScroll，让里面的卡片可以上下滑动
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // --- 作者 GitHub 社交卡片区 ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://github.com/Jeesemmy/Aura")
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE6F5EC), // 极淡青绿色（呼应 Aura 徽章）
                                        Color(0xFFF6F7FA), // 柔和净白过渡
                                        Color(0xFFF0ECF8)  // 极淡浅灰微紫色（高级磨砂 VIP 铭牌质感）
                                    )
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧：带蓝圈的真实头像
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFF4285F4), CircleShape) // Google 经典蓝
                                .padding(4.dp) // 头像和边框的呼吸间距
                                .clip(CircleShape), // 再次裁剪，确保图片是完美的圆形
                            contentAlignment = Alignment.Center
                        ) {
                            // 👇 读取 drawable 文件夹里的 avatar 图片
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.avatar),
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop // 保证图片完美铺满且不变形
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // 中间：用户名与徽章组合
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Jeesemmy",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // 👇 核心改动：用 Row 把两个徽章横向排列在一起
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 1.Aura 徽章（底色与 VIP 铭牌起点融为一体）
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFFE6F5EC),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .border(
                                            0.5.dp,
                                            Color(0xFFD2E3FC),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Aura",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp)) // 两个徽章之间的间距

                                // 2. GitHub 徽章
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF181717),
                                            RoundedCornerShape(10.dp)
                                        ) // GitHub 经典暗黑色
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.github_logo),
                                        contentDescription = "GitHub",
                                        tint = Color.White, // 强制把图标渲染成纯白色，适配黑底
                                        modifier = Modifier.size(13.dp) // 尺寸和旁边的 11.sp 字体高度对齐
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 高级滚动数字选择器 (图框数量) ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_swipe_up),
                                contentDescription = "Swipe Up",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "划动图片数量",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "选择数量 (1-10)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }

                        val haptic = LocalHapticFeedback.current
                        val listState = rememberLazyListState(
                            initialFirstVisibleItemIndex = (currentCount - 1).coerceIn(
                                0, 9
                            )
                        )

                        // 精准判定滚动时物理坐标的中心点，选中中间的数字
                        val centerIndex by remember {
                            derivedStateOf {
                                val layoutInfo = listState.layoutInfo
                                val visibleItems = layoutInfo.visibleItemsInfo
                                if (visibleItems.isEmpty()) return@derivedStateOf -1

                                // 计算整个滚动框的物理中心线
                                val viewportCenter =
                                    layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                                var closestIndex = -1
                                var minDistance = Int.MAX_VALUE

                                // 遍历当前看到的数字，谁最靠近中心线，谁就是主角
                                for (item in visibleItems) {
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

                        // 滚动过程中给出马达机械震动反馈
                        var lastVibratedIndex by remember { mutableIntStateOf(-1) }
                        LaunchedEffect(centerIndex) {
                            if (centerIndex != -1 && centerIndex != lastVibratedIndex) {
                                lastVibratedIndex = centerIndex
                                val newCount = centerIndex + 1
                                if (newCount in 1..10 && newCount != currentCount) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onCountChange(newCount)
                                }
                            }
                        }

                        // 滚动结束后写入本地 DataStore
                        LaunchedEffect(listState.isScrollInProgress) {
                            if (!listState.isScrollInProgress && centerIndex != -1) {
                                val finalCount = centerIndex + 1
                                if (finalCount in 1..10) {
                                    coroutineScope.launch {
                                        context.dataStore.edit {
                                            it[PHOTO_COUNT_KEY] = finalCount.toString()
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = true,
                                contentPadding = PaddingValues(vertical = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                // 👇 绝杀 3：开启系统底层原生的磁吸引擎，松手瞬间绝对居中
                                flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                                    lazyListState = listState
                                )
                            ) {
                                items(10) { index ->
                                    val number = index + 1
                                    // 核心改变：不再用 currentCount 判断，而是直接绑定实时视觉中心，滑动时丝滑缩放
                                    val isSelected = index == centerIndex
                                    Box(
                                        modifier = Modifier
                                            .height(30.dp)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$number",
                                            fontSize = if (isSelected) 26.sp else 16.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.25f
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 缓存清理卡片 ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        )
                    ),
                    onClick = {
                        try {
                            // 1. 清理 Coil 图片内存与磁盘缓存
                            context.imageLoader.diskCache?.clear()
                            context.imageLoader.memoryCache?.clear()

                            // 2. 递归深度清理应用内部缓存目录与外部缓存目录的所有临时文件
                            val clearDirectory: (java.io.File?) -> Unit = { dir ->
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Toast.makeText(context, "缓存清理成功  Aura 已回巅峰！", Toast.LENGTH_SHORT)
                            .show()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete_sweep),
                            contentDescription = "Delete Sweep",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "清理图片缓存",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "释放存储空间",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // --- GitHub 更新卡片 ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        )
                    ),
                    onClick = {
                        if (isCheckingUpdate) return@Card
                        isCheckingUpdate = true
                        coroutineScope.launch {
                            val currentVersion = try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    context.packageManager.getPackageInfo(
                                        context.packageName,
                                        android.content.pm.PackageManager.PackageInfoFlags.of(0)
                                    ).versionName ?: "15.0"
                                } else {
                                    @Suppress("DEPRECATION")
                                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "15.0"
                                }
                            } catch (e: Exception) {
                                "15.0"
                            }
                            val result = UpdateManager.checkUpdate(currentVersion = currentVersion)
                            isCheckingUpdate = false
                            result.onSuccess { release ->
                                if (release.hasNewVersion) {
                                    updateReleaseInfo = release
                                } else {
                                    Toast.makeText(
                                        context,
                                        "当前已是最新版本 (v$currentVersion)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.onFailure { e ->
                                Toast.makeText(
                                    context,
                                    "检查更新失败: ${e.localizedMessage ?: "网络异常"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "检查更新",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "获取最新版本",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                val currentVersionName = remember {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.packageManager.getPackageInfo(
                                context.packageName,
                                android.content.pm.PackageManager.PackageInfoFlags.of(0)
                            ).versionName ?: "15.0"
                        } else {
                            @Suppress("DEPRECATION")
                            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "15.0"
                        }
                    } catch (e: Exception) {
                        "15.0"
                    }
                }

                Text(
                    text = "当前版本：$currentVersionName \n不会使用Aura？没关系的.\n你可以点击底部的日志按钮.\n就可以看到以往的更新日志啦.💕",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        },
        confirmButton = {}
    )

    // 1. 正在检查更新的 Loading 弹窗
    if (isCheckingUpdate) {
        AlertDialog(
            onDismissRequest = { isCheckingUpdate = false },
            title = { Text(text = "检查更新中...", fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("正在连接 GitHub...", fontSize = 15.sp)
                }
            },
            confirmButton = {}
        )
    }

    // 2. 发现新版本时的更新日志与确认弹窗
    updateReleaseInfo?.let { release ->
        AlertDialog(
            onDismissRequest = { updateReleaseInfo = null },
            title = {
                Text(
                    text = "发现新版本 ${release.versionName}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = release.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "本次更新：",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = release.changelog,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val downloadUrl = release.downloadUrl
                        val verName = release.versionName
                        updateReleaseInfo = null
                        Toast.makeText(context, "已在后台启动下载，完成后将自动拉起安装", Toast.LENGTH_SHORT).show()
                        UpdateManager.downloadAndInstall(
                            context = context,
                            downloadUrl = downloadUrl,
                            versionName = verName
                        )
                    }
                ) {
                    Text("立即更新")
                }
            },
            dismissButton = {
                TextButton(onClick = { updateReleaseInfo = null }) {
                    Text("稍后再说")
                }
            }
        )
    }

    // 3. 实时下载进度弹窗 (居中展示进度条、百分比与安装按钮)
    downloadProgressState?.let { progressState ->
        AlertDialog(
            onDismissRequest = {
                // 下载完成或失败后允许点击外部关闭，下载中不自动关闭以防误触
                if (!progressState.isDownloading) {
                    UpdateManager.resetDownloadProgress()
                }
            },
            title = {
                Text(
                    text = "下载",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // 进度条与百分比数字横向排列 (如 60%)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progressState.progress / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "${progressState.progress}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 辅助状态提示
                    if (progressState.isCompleted) {
                        Text(
                            text = "下载已完成，可点击右下角安装",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (progressState.isFailed) {
                        Text(
                            text = "下载失败，请检查网络后重试",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "正在下载更新包...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = progressState.downloadedFile
                        if (file != null && file.exists()) {
                            UpdateManager.installApk(context, file)
                        } else {
                            Toast.makeText(context, "未找到下载的安装包，请重新下载", Toast.LENGTH_SHORT).show()
                        }
                    },
                    // 下载完成前为灰色不可点击，下载完成后变为可点击
                    enabled = progressState.isCompleted,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("安装")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { UpdateManager.resetDownloadProgress() }
                ) {
                    Text("关闭")
                }
            }
        )
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
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // 滑开红底垃圾桶的宽度
    val actionSizePx = with(density) { 80.dp.toPx() }

    // 追踪左滑删除组件的物理锚点和当前位置
    val state = remember {
        AnchoredDraggableState(
            DragValue.Settled,
            { it * 0.5f },
            { with(density) { 100.dp.toPx() } },
            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),// 滑动阻尼效果
            decayAnimationSpec
        )
    }

    // 成功划开删除按钮时给予震动反馈
    LaunchedEffect(state) {
        snapshotFlow { state.currentValue }.distinctUntilChanged()
            .collect { if (it == DragValue.Open) haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    }
    SideEffect {
        state.updateAnchors(newAnchors = DraggableAnchors {
            DragValue.Settled at 0f
            if (uri != null) {
                DragValue.Open at -actionSizePx}// 只有有图的时候才可以划开
        })
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // 底层红色删除按钮与背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 当图标被成功删除时(uri == null)，红底瞬间变透明以去除多余残影，提高顺滑度
                .graphicsLayer { alpha = if (state.offset != 0f && uri != null) 1f else 0f }
                .background(Color.Red)
                .clickable {
                    if (state.offset != 0f) {
                        onDelete(); scope.launch { state.animateTo(DragValue.Settled) }
                    }
                }, contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                Icons.Default.Delete,
                null,
                tint = Color.White,
                modifier = Modifier.padding(end = 24.dp)
            )
        }
        // 顶层主图框展示区域
        Card(
            modifier = Modifier
                .graphicsLayer { alpha = if (uri == null) 0.6f else 1.0f }
                .offset {
                    IntOffset(
                        state.offset.let {
                            if (it.isNaN()) 0
                            else if (it < -actionSizePx) (-actionSizePx + (it + actionSizePx) * 0.3f).roundToInt()
                            else it.roundToInt()
                        },
                        0
                    )
                }
                .then(
                    if (uri != null) Modifier.anchoredDraggable(
                        state,
                        Orientation.Horizontal
                    ) else Modifier
                )
                .fillMaxSize()
                // 🌟 核心修复：绝对严密的双重判断逻辑，杜绝空画框误触预览！
                .pointerInput(uri) { // 绑定 uri 作为 key，确保状态变更时实时刷新
                    detectTapGestures(
                        onTap = {
                            if (uri != null) {
                                onPreview(uri) // 有图：安全进入大图预览
                            } else {           // 严密判空，防误触
                                onSelect()     // 没图（空画框/刚删除的格子）：绝对只唤起相册选图！
                            }
                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),

            // 空画框使用全透明玻璃感底色，有图时保持默认
            colors = CardDefaults.cardColors(
                containerColor = if (uri == null) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
            ),
            // 👇 2. 只改动阴影：如果画框是空的，去掉原本厚重的阴影(0.dp)；如果有图片，保持原来的 8.dp
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (uri == null) 0.dp else 8.dp
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (uri == null) {
                    Icon(
                        Icons.Default.Add,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    val context = LocalContext.current
                    val imageRequest = remember(uri) {
                        ImageRequest.Builder(context)
                            .data(uri)
                            .size(coil.size.Size(1080, 600))
                            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                            .allowHardware(true)
                            .build()
                    }

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

// =========================================================================================
// 🧰 [拓展工具与 Worker 任务区]
// =========================================================================================
// 通过读取首个可见项的偏移量，精准判断列表目前的滑动方向 (上滑/下滑)
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }

    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
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

    // 2. 构建通知内容
    val notification = NotificationCompat.Builder(context, channelId)
        // 这里的图标先用 Android 自带的，后续可以换成你自己的 app 图标 my
        .setSmallIcon(R.drawable.my_icon)
        .setContentTitle("Aura 提醒")
        .setContentText("Hello，快想想还有什么重要的事情没做呢？💫")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true) // 点击后自动消失
        .build()

    // 3. 发送通知
    notificationManager.notify(1001, notification)
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
// ↕️ [长按图片排序专属面板：ImageSortDialog]
// 通过底层的协程计算和坐标映射，实现防断触、有速度补偿的极速拖拽
// ==============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSortDialog(
    currentOrder: MutableList<Int>,
    uris: List<Uri?>,
    displayCount: Int,
    onDismiss: () -> Unit,
    onSave: (List<Int>) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val tempOrder = remember { mutableStateListOf<Int>().apply { addAll(currentOrder) } }
    val density = LocalDensity.current
    val view = androidx.compose.ui.platform.LocalView.current
    val scrollState = rememberScrollState()
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    // --- 新增：在这里提前算好几个关键的物理高度尺寸 ---
    val cardHeightPx = with(density) { (110 + 12).dp.toPx() }// 卡片自身高度 + 间距补偿
    val viewportHeightPx = with(density) { 400.dp.toPx() }// 弹窗可见高度
    val edgeZonePx = with(density) { 80.dp.toPx() }// 上下滚动的敏感边缘距离
    val autoScrollSpeed = remember { mutableFloatStateOf(0f) }

    // 核心物理引擎：计算当前拖拽的位置是否跨越了临界点，从而执行列表内的卡片交换
    val updatePhysics = {
        val curr = draggedIndex
        if (curr != null) {
            val lastIdx = (displayCount - 1).coerceAtLeast(0)

            // 下划交换判定
            if (dragOffsetY > cardHeightPx * 0.5f && curr < lastIdx) {
                val item = tempOrder.removeAt(curr)
                tempOrder.add(curr + 1, item)
                draggedIndex = curr + 1
                dragOffsetY -= cardHeightPx
                onSave(tempOrder)
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)

            // 上划交换判定
            } else if (dragOffsetY < -cardHeightPx * 0.5f && curr > 0) {
                val item = tempOrder.removeAt(curr)
                tempOrder.add(curr - 1, item)
                draggedIndex = curr - 1
                dragOffsetY += cardHeightPx
                onSave(tempOrder)
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
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
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "调整图片顺序 ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                Text(
                    text = "长按右侧划块，上下拖动即可调整顺序",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                tempOrder.take(displayCount).forEachIndexed { index, originalIndex ->
                    val uri = uris.getOrNull(originalIndex)
                    val isDragging = draggedIndex == index

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .zIndex(if (isDragging) 99f else 1f)// 正在拖拽的卡片提高 Z 轴，浮在最表面
                            .graphicsLayer {
                                translationY = if (isDragging) dragOffsetY else 0f
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                                                Color.Black.copy(alpha = 0.5f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.5f)
                                            )
                                        )
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("这个画框还是空的哦 ~", color = Color.Gray, fontSize = 13.sp)
                            }
                        }

                        // 🌟 全新布局：用 Box 自由定位，代替原来的 Row
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 左上角数字标号
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart) // 死死吸附在左上角
                                    .width(44.dp)
                                    .height(36.dp) // 高度变小，不再铺满一整条
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        // 只需要右下角做圆角，左上角会天然贴合外层卡片的 16.dp 裁剪
                                        shape = RoundedCornerShape(bottomEnd = 16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (index + 1 < 10) "0${index + 1}" else "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // 右侧拖动手柄 (接管长按滑动事件)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd) // 吸附在右侧
                                    .fillMaxHeight()
                                    .width(48.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(
                                            topEnd = 16.dp,
                                            bottomEnd = 16.dp
                                        )
                                    )
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
                                                updatePhysics() // 👈 只有这三行！
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
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "拖拽排序",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
