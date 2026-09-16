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
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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


private val Context.dataStore by preferencesDataStore(name = "photo_settings")
private val SAVED_URIS_KEY = stringPreferencesKey("saved_uris")
private val SHOW_ANNOUNCEMENT_KEY = booleanPreferencesKey("show_announcement")

private val PHOTO_COUNT_KEY = stringPreferencesKey("photo_count") // 存储图框数量（1-10）


enum class DragValue { Settled, Open }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent {
            WXXXTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    // 👇👇👇 8.0 新增：把震动控制器加在这里 👇👇👇
    val haptic = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()

    // 👇 把申请通知权限的代码加在这里
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 用户同意了
        }
    }

    LaunchedEffect(key1 = Unit) {
        // 1. 请求通知权限 (你原有的功能)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 2. 👇 新增：设定一个“每 24 小时执行一次”的后台任务
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


    // 🌟 将原本固定的 7 改为由状态控制（默认 7 个，最大 10 个）
    var displayCount by remember { mutableIntStateOf(7) }

    val selectedImageUris = remember { mutableStateListOf<Uri?>().apply { repeat(10) { add(null) } } }
    val itemOrder = remember { mutableStateListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) }


    var isFabExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }

    var showAnnouncementDialog by rememberSaveable { mutableStateOf(false) }
    var previewUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    var titleVisible by remember { mutableStateOf(false) }
    // 👇👇👇 复制这段代码到 titleVisible 的下面 👇👇👇
    val lazyListState = rememberLazyListState()
    val isScrollingUp = lazyListState.isScrollingUp()

    val topBarOffset by animateDpAsState(
        targetValue = if (isScrollingUp) 0.dp else (-170).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "TopBar"
    )

    val bottomBarOffset by animateDpAsState(
        targetValue = if (isScrollingUp) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 300),
        label = "BottomBar"
    )
    // 👆👆👆 ========================================= 👆👆👆

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

    val saveUrisToDisk = {
        scope.launch {
            // 🛡️ 核心微调：按照拖动后的【视觉顺序 itemOrder】重新组合数据并存储
            val data = itemOrder.map { selectedImageUris[it]?.toString() ?: "" }.joinToString("|")
            context.dataStore.edit { it[SAVED_URIS_KEY] = data }
        }
    }


    LaunchedEffect(Unit) {
        titleVisible = true
        val prefs = context.dataStore.data.first()
        displayCount = prefs[PHOTO_COUNT_KEY]?.toIntOrNull() ?: 7

        val savedString = prefs[SAVED_URIS_KEY] ?: ""
        if (savedString.isNotEmpty()) {
            savedString.split("|").forEachIndexed { index, path ->

                if (path.isNotEmpty() && index < 10) {
                    selectedImageUris[index] = Uri.parse(path)
                }
            }
        }
    }

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
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val emptySlots = itemOrder.filter { selectedImageUris[it] == null }

            uris.take(emptySlots.size).forEachIndexed { index, uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) { e.printStackTrace() }
                selectedImageUris[emptySlots[index]] = uri
            }
            saveUrisToDisk()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            // 👇 加上这个 Box 和偏移量，让胶囊图标听从滑动指挥！
            Box(modifier = Modifier.offset { androidx.compose.ui.unit.IntOffset(0, bottomBarOffset.roundToPx()) }) {
                ExpandableFab(
                    isExpanded = isFabExpanded,
                    onToggle = {
                        isSettingsOpen = false
                        showHelpDialog = false
                    },
                    onSettingsClick = {
                        isSettingsOpen = true
                        isFabExpanded = false
                    },
                    onGestureClick = {
                        isFabExpanded = false
                        showHelpDialog = true
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // ✅ 改成只保留底部的导航栏安全距离，顶部完全不加限制（直通屏幕顶部）：
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = titleVisible,
                    modifier = Modifier.offset { androidx.compose.ui.unit.IntOffset(0, topBarOffset.roundToPx()) }.zIndex(1f),
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
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                            // 👇 2. 新增这行！在背景之后加状态栏 padding，把文字安全地挤下来
                            .statusBarsPadding() // 👈 这里保留，保证“你好”文字在状态栏下面，不被摄像头遮挡

                            // 👇 加上这一行：单纯把整行元素在视觉上往下平移
                            .offset(y = 20.dp)
                            // 👇 4. 底部增加一点 padding，把背景板撑开，看起来更像原生的 TopBar
                            .padding(top = 14.dp, bottom = 16.dp, start = 24.dp)
                    ) {

                        // 👇 1. 新增：获取当前手机时间（24小时制），并根据你的时间表匹配问候语
                        val greetingText = remember {
                            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                            when (hour) {
                                in 7..11 -> "早上好"   // 7:00 - 11:59
                                in 12..13 -> "中午好"  // 12:00 - 13:59
                                in 14..17 -> "下午好"  // 14:00 - 17:59
                                else -> "晚上好"       // 18:00 - 6:59
                            }
                        }

                        // 👇 2. 修改 Text，让它使用我们刚刚计算出来的 greetingText
                        Text(
                            text = greetingText, // 👈 替换掉写死的 "你好"
                            color = Color.Black,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.graphicsLayer { rotationZ = angle }
                        )

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text ="       今天也要开心呀🥰\n" ,
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.offset(y = 12.dp)
                        )
                    }
                }

                // 🎯 升级为更稳定的【Key（稳定ID）追踪流派】
                var draggedKey by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableFloatStateOf(0f) }
                val itemHeightPx = with(LocalDensity.current) { (180 + 16).dp.toPx() } // 卡片高度180dp + 间距16dp



                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = isFabExpanded) { isFabExpanded = false },
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 140.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    //核心修改：只展示前 displayCount 个图框！
                    items(items = itemOrder.take(displayCount), key = { it }) { originalIndex ->


                        // 🛡️ 用绝对稳定的原始 Key 来判定当前谁在被拖拽
                        val isDragging = draggedKey == originalIndex
                        val currentIndex = itemOrder.indexOf(originalIndex)

                        @OptIn(ExperimentalFoundationApi::class)
                        Box(
                            modifier = Modifier
                                // 🌟 绝杀招式：只有当【不是当前拖拽项】时才应用让位动画！正在拖拽的卡片绝不参与动画，防止乱跳！
                                .then(if (!isDragging) Modifier.animateItemPlacement() else Modifier)
                                .zIndex(if (isDragging) 99f else 0f)
                                .graphicsLayer {
                                    translationY = if (isDragging) dragOffset else 0f
                                    scaleX = if (isDragging) 1.04f else 1f // 拽起时轻微放大
                                    scaleY = if (isDragging) 1.04f else 1f
                                }
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedKey = originalIndex // 锁定被拖拽的稳定ID
                                            dragOffset = 0f
                                            // 👇👇👇 8.0 新增：在手指长按生效的瞬间，触发清脆的震动反馈！
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },

                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount.y

                                            // 🔄 动态实时查找当前卡片在队列中的最新视觉位置，杜绝闭包滞后
                                            val freshIndex = itemOrder.indexOf(originalIndex)
                                            if (freshIndex != -1) {

                                                // 🛑 核心修复 2：【天花板/地板边界锁】防止卡片在顶部或底部继续空飞
                                                if (freshIndex == 0 && dragOffset < 0f) dragOffset = 0f
                                                if (freshIndex == 6 && dragOffset > 0f) dragOffset = 0f

                                                // 实时边界碰撞判定
                                                if (dragOffset > itemHeightPx / 2 && freshIndex < 6) {
                                                    // 向下精准对调
                                                    val temp = itemOrder[freshIndex]
                                                    itemOrder[freshIndex] = itemOrder[freshIndex + 1]
                                                    itemOrder[freshIndex + 1] = temp

                                                    dragOffset -= itemHeightPx // 瞬间对齐位移补偿


                                                    // 🌟 核心修复 1 落地：一旦对调涉及第 0 项，强行把列表拉回顶部，粉碎 Key 追踪机制的偷跑
                                                    if (freshIndex == 0 || freshIndex + 1 == 0) {
                                                        scope.launch { lazyListState.scrollToItem(0, 0) }
                                                    }
                                                } else if (dragOffset < -itemHeightPx / 2 && freshIndex > 0) {
                                                    // 向上精准对调
                                                    val temp = itemOrder[freshIndex]
                                                    itemOrder[freshIndex] = itemOrder[freshIndex - 1]
                                                    itemOrder[freshIndex - 1] = temp

                                                    dragOffset += itemHeightPx // 瞬间对齐位移补偿
                                                    saveUrisToDisk()

                                                    // 🌟 核心修复 1 落地：一旦对调涉及第 0 项，强行把列表拉回顶部
                                                    if (freshIndex == 0 || freshIndex - 1 == 0) {
                                                        scope.launch { lazyListState.scrollToItem(0, 0) }
                                                    }
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggedKey = null
                                            dragOffset = 0f
                                            saveUrisToDisk()
                                        },
                                        onDragCancel = {
                                            draggedKey = null
                                            dragOffset = 0f
                                            saveUrisToDisk()
                                        }
                                    )
                                }
                        ) {
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
                                }
                            )
                        }
                    }
                }

            }


        }

        if (showAnnouncementDialog) {
            AlertDialog(
                onDismissRequest = { showAnnouncementDialog = false },
                title = { Text(text = "公告🍃") },
                text = {
                    // 用 Column 把多行文字垂直排列起来
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "更新内容：\n")
                        Text(

                            text = "1. 你好 Jeesemmy",
                            color = Color(0xFF9ADC4D) // 换成你喜欢的主题蓝，或者用 MaterialTheme.colorScheme.primary
                        )

                        Text(text = "2. 新增应用认证专属徽章.")
                        Text(text = "3. 优化图片高刷显示链路.")
                        Text(text = "4. 新增主页标题时态轮换功能.")

                        // 第 5 行：水平排列文字与双芯徽章（无背景透明版）
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "5. 技术合作伙伴")
                            Spacer(modifier = Modifier.width(6.dp))

                            // 1. 骁龙图标（无背景）
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.snapdragon_logo),
                                    contentDescription = "Snapdragon",
                                    modifier = Modifier.height(14.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp)) // 两个图标之间的间距

                            // 2. 天玑图标（无背景）
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.dimensity_logo),
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

        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                modifier = Modifier.fillMaxHeight(0.7f),
                title = { Text(text = "关于应用") },
                text = {
                    Text(
                        text = "历史版本日志-\n\n" +
                                "(2026.10.1) 11.0版本🍃\n1. 新增应用认证专属徽章.\n2. 优化图片高刷显示链路.\n3. 新增主页标题时态轮换功能.\n\n"+
                                "(2026.9.12) 10.0版本✨\n1: 9月大版本现已发布.\n2: 首页美术升级，提升视觉效果.\n3: 移动端性能优化,提高运行效率.\n4: 新增应用通知权限.\n5: 新增图片展示数量功能.\n\n"+
                                "(2026.9.10) 9.0版本🎉\n优化页面布局.\n优化CPU.GPU性能调度.\n新增主页选项卡.可直达作者主页.\n\n"+

                                "(2026.5.24) 8.0版本🏆\n优化视觉体验,提升响应速度.\n拖动图片引入震动反馈.\n新增检测更新功能.\n\n"+
                                "(2026.5.19) 7.0版本🧋\n适配了120Hz帧模式.\n新增图片长按自定义排序.\n修复了大图预览显示顺序异常的问题.\n\n"+

                                "(2026.4.23) 6.0版本🐳\n针对照片列表,大幅降低滑动过程的卡顿.\n优化了加载策略,降低滑动时CPU负载.\n" + "新增缓存清理功能.\n" +
                                "深度优化架构,确保响应更跟手.\n添加了设置入口.\n\n"+
                                "(2026.4.20) 5.3版本🤒\n新增图片多选功能,优化图片添加体验.\n修复了特定机型下进入大图预览的计算闪退 bug.\n\n"+

                                "(2026.4.18) 5.2版本：\n优化应用运行速度.优化预览界面.\n修复特定场景下的闪退问题.\n\n"+
                                "(2026.4.16) 5.1版本：\n视觉与稳定性加固,优化对话框滚动体验.\n\n "+

                                "(2026.4.15) 5.0版本：\n优化大图预览功能,对应用代码进行了精简.\n\n" +
                                "(2026.4.14) 4.0版本：\n提升应用稳定性,架构全面优化,运行更流畅.\n首页标题新增动画,提升界面滑动跟手性.\n\n" +
                                "(2026.4.13) 3.0版本：\n添加了照片数量,移除使用帮助,新增关于此应用,添加了版本日志.\n\n" +

                                "(2026.4.11) 2.0版本：\n新增大图预览功能,适配横屏比例,修复已知bug.\n\n" +
                                "(2026.2.14) 1.0版本:\n优化页面布局,底部新增交互按钮,提供了使用帮助与设置选项.\n\n" +

                                "(2026.1.04）Beta版本🎗️\nPs：一款能让您添加自定义照片的应用,左滑图片可删除,目前最多可添加四张图片,适配沉浸式状态栏功能.",
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

        if (previewUri != null) {
            // 🌟 绝杀微调 1：点击进入大图预览时，寻找它在视觉序列 itemOrder 中的真实排位，确保点哪张就开哪张！
            val safeInitialPage = remember(previewUri) {
                val rawIndex = selectedImageUris.indexOf(previewUri)
                itemOrder.indexOf(rawIndex).coerceAtLeast(0)
            }

            val pagerState = rememberPagerState(initialPage = safeInitialPage, pageCount = { displayCount })

            FullScreenDialog(onDismiss = { previewUri = null }) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "预览 (${pagerState.currentPage + 1})",
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Black.copy(alpha = 0.8f),
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White
                            )
                        )
                    },
                    containerColor = Color.Black
                ) { paddingValues ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        beyondViewportPageCount = 1
                    ) { page ->
                        // 🌟 绝杀微调 2：先通过当前页数 page 从映射表 itemOrder 里查出它是第几张图，再拿去取 URI！
                        val realIndex = itemOrder.getOrNull(page) ?: page
                        val currentUri = selectedImageUris.getOrNull(realIndex)

                        if (currentUri != null) {
                            var scale by rememberSaveable { mutableFloatStateOf(1f) }
                            var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
                            var offsetY by rememberSaveable { mutableFloatStateOf(0f) }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        awaitEachGesture {
                                            awaitFirstDown(requireUnconsumed = false)
                                            do {
                                                val event = awaitPointerEvent()
                                                val zoom = event.calculateZoom()
                                                val pan = event.calculatePan()
                                                val isMultiTouch = event.changes.size > 1

                                                if (scale > 1f || isMultiTouch) {
                                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                                                    if (newScale > 1f) {
                                                        offsetX += pan.x * scale
                                                        offsetY += pan.y * scale
                                                    } else {
                                                        offsetX = 0f; offsetY = 0f
                                                    }
                                                    scale = newScale
                                                }
                                            } while (event.changes.any { it.pressed })
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
                                Image(
                                    painter = painter,
                                    contentDescription = "预览图片",
                                    modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1C1E)), contentAlignment = Alignment.Center) {
                                Text("这里还没有添加照片哦~", color = Color.Gray, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // 5.3 挂载逻辑：设置页面覆盖显示
        if (isSettingsOpen) {
            AuraSettingsScreen(
                currentCount = displayCount,               // 👈 把当前的数量传进去
                onCountChange = { displayCount = it },     // 👈 把修改后的数量传回给主
                onBack = { isSettingsOpen = false })
        }
    }
}



@Composable
fun ExpandableFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onGestureClick: () -> Unit
) {
    // 仿 Gemini 风格的底部药丸控制栏
    Surface(
        modifier = Modifier.height(64.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 蓝色主页按钮 (最左侧)
            FilledIconButton(
                onClick = onToggle,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF0388F0)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = Color(0xFFE9EDE9)
                )
            }

            // 2. 版本日志按钮 (移动到中间)
            IconButton(
                onClick = onGestureClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = "Version Logs",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 3. 设置按钮 (移动到最右侧)
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



@Composable
fun FullScreenDialog(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraSettingsScreen(
    currentCount: Int,                // 👈 加上这行
    onCountChange: (Int) -> Unit,     // 👈 加上这行
    onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showUpdateDialog by remember { mutableStateOf(false) }

    // 🚀 核心修改：将 Scaffold 替换为 AlertDialog
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
                // 1. 全新 Google 风格主理人账号卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/Jeesemmy/Aura"))
                        context.startActivity(intent)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
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
                                // 1. 原来的 Aura 渐变徽章
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White,
                                                    Color(0xFF8AB4F8)
                                                )
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .border(0.5.dp, Color(0xFFD2E3FC), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Aura",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp)) // 两个徽章之间的呼吸间距

                                // 2. 新增的 GitHub 徽章 (仿 Shields.io 风格)
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF181717), RoundedCornerShape(10.dp)) // GitHub 经典暗黑色
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // 删除了 Text，换成了读取 drawable 里的图标
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.github_logo),
                                        contentDescription = "GitHub",
                                        tint = Color.White, // 强制把图标渲染成纯白色，适配黑底
                                        modifier = Modifier.size(13.dp) // 尺寸和旁边的 11.sp 字体高度对齐
                                    )
                                }
                            }
                        }

                        // 右侧：下拉箭头
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }



                // 🌟 展示图框数量自定义卡片（完美仿闹钟震动滚轮版）
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "展示图片数量", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "自定义主页卡片 (1-10)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }

                        val haptic = LocalHapticFeedback.current
                        val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentCount - 1).coerceIn(0, 9))

                        // 👇 绝杀 1：像素级实时计算，精准锁定画面正中心的那个数字！
                        val centerIndex by remember {
                            derivedStateOf {
                                val layoutInfo = listState.layoutInfo
                                val visibleItems = layoutInfo.visibleItemsInfo
                                if (visibleItems.isEmpty()) return@derivedStateOf -1

                                // 计算整个滚动框的物理中心线
                                val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
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

                        // 1. 实时监听中心数字变化：滑动过程中每过一格就立刻震动并刷新UI
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

                        // 2. 监听滑动停止：松手停稳后，才把最终结果写入 DataStore 保存
                        LaunchedEffect(listState.isScrollInProgress) {
                            if (!listState.isScrollInProgress && centerIndex != -1) {
                                val finalCount = centerIndex + 1
                                if (finalCount in 1..10) {
                                    coroutineScope.launch {
                                        context.dataStore.edit { it[PHOTO_COUNT_KEY] = finalCount.toString() }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.width(70.dp).height(90.dp), contentAlignment = Alignment.Center) {
                            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = true,
                                contentPadding = PaddingValues(vertical = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                // 👇 绝杀 3：开启系统底层原生的磁吸引擎，松手瞬间绝对居中
                                flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = listState)
                            ) {
                                items(10) { index ->
                                    val number = index + 1
                                    // 核心改变：不再用 currentCount 判断，而是直接绑定实时视觉中心，滑动时丝滑缩放
                                    val isSelected = index == centerIndex

                                    Box(modifier = Modifier.height(30.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$number",
                                            fontSize = if (isSelected) 26.sp else 16.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                // 2. 清理缓存按钮卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    onClick = {
                        context.imageLoader.diskCache?.clear()
                        context.imageLoader.memoryCache?.clear()
                        Toast.makeText(context, "缓存清理成功  Aura 已回巅峰！", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "清理图片缓存", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "释放存储空间", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }

                // 3. 检查更新卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    onClick = {
                        showUpdateDialog = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(2000)
                            showUpdateDialog = false
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/Jeesemmy/Aura/releases"))
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "检查更新", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "前往 GitHub 获取 Aura 最新版本", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }

                Text(
                    text = "当前版本：11.0 \n不会使用Aura？没关系的.\n你可以点击底部的书籍按钮.\n就可以看到以往的更新日志啦.💕",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        },
        confirmButton = {}
    )

    //弹窗专属区域
    // 这个加载弹窗保持不变，它会自动盖在设置弹窗的上面
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(text = "正在检查更新", fontWeight = FontWeight.Bold) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("正在连接GitHub...", fontSize = 15.sp)
                }
            },
            confirmButton = {}
        )
    }
}





@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(uri: Uri?, onSelect: () -> Unit, onPreview: (Uri) -> Unit, onDelete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val actionSizePx = with(density) { 80.dp.toPx() }
    val state = remember { AnchoredDraggableState(DragValue.Settled, { it * 0.5f }, { with(density) { 100.dp.toPx() } }, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), decayAnimationSpec) }

    LaunchedEffect(state) { snapshotFlow { state.currentValue }.distinctUntilChanged().collect { if (it == DragValue.Open) haptic.performHapticFeedback(HapticFeedbackType.LongPress) } }
    SideEffect {
        state.updateAnchors(newAnchors = DraggableAnchors {
            // 默认起点站（死死对齐原点 0f）
            DragValue.Settled at 0f
            // 🛡️ 核心安全锁：只有当 uri 不为 null（有图片）的时候，才开放左滑删除的“终点站”！
            if (uri != null) {
                DragValue.Open at -actionSizePx
            }
        })
    }

    Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp))) {
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (state.offset != 0f) 1f else 0f }.background(Color.Red).clickable { if (state.offset != 0f) { onDelete(); scope.launch { state.animateTo(DragValue.Settled) } } }, contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.padding(end = 24.dp)) }
        Card(
            modifier = Modifier.graphicsLayer { alpha = if (uri == null) 0.6f else 1.0f }.offset { IntOffset(state.offset.let { if (it.isNaN()) 0
            else if (it < -actionSizePx) (-actionSizePx + (it + actionSizePx) * 0.3f).roundToInt() else it.roundToInt() }, 0) }
                // 🛡️ 终极安全锁：只有当 uri 不为 null（有图片）时，才把滑动修饰符挂载上去！没图片时直接忽略！
                .then(if (uri != null) Modifier.anchoredDraggable(state, Orientation.Horizontal) else Modifier)
                .fillMaxSize()
                .clickable { if (uri == null) onSelect() else onPreview(uri) },
            shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (uri == null) { Icon(Icons.Default.Add, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) }
                else {
                    // ✅ 提取并缓存图片请求对象，避免滑动时疯狂创建垃圾
                    val context = LocalContext.current
                    val imageRequest = remember(uri) {
                        ImageRequest.Builder(context)
                            .data(uri)

                            // 1. 删除了 crossfade(true)，斩断 GPU 过度绘制，图片直出
                            .size(coil.size.Size(1080, 600))
                            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                            // 2. 👇 新增：强制开启硬件加速位图！像素数据直达 GPU 显存，彻底解放 CPU
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

// 👇 将这段代码粘贴在文件的最底部
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




@SuppressLint("ServiceCast")
fun sendTestNotification(context: Context) {
    val channelId = "aura_daily_channel"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
        // 这里的图标先用 Android 自带的，后续可以换成你自己的 app 图标
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Aura 每日提醒")
        .setContentText("Hello，快想想还有什么重要的事情没做呢？💫")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true) // 点击后自动消失
        .build()

    // 3. 发送通知
    notificationManager.notify(1001, notification)
}


// 这是给系统的“任务清单”（防报错全名版）
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
