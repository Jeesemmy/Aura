package com.example.wxxx

import androidx.activity.compose.BackHandler
import kotlinx.coroutines.flow.map
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import coil.compose.AsyncImage
import coil.imageLoader
import com.example.wxxx.ui.theme.WXXXTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest


private val Context.dataStore by preferencesDataStore(name = "photo_settings")
private val SAVED_URIS_KEY = stringPreferencesKey("saved_uris")
private val SHOW_ANNOUNCEMENT_KEY = booleanPreferencesKey("show_announcement")
private val AUTO_COLLAPSE_KEY = booleanPreferencesKey("auto_collapse")


enum class DragValue { Settled, Open }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
    val scope = rememberCoroutineScope()
    val selectedImageUris = remember { mutableStateListOf<Uri?>(null, null, null, null, null, null, null) }

    var currentSelectedIndex by remember { mutableIntStateOf(-1) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    
    var showAnnouncementDialog by rememberSaveable { mutableStateOf(false) }
    var previewUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    
    var titleVisible by remember { mutableStateOf(false) }

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
            val data = selectedImageUris.joinToString("|") { it?.toString() ?: "" }
            context.dataStore.edit { it[SAVED_URIS_KEY] = data }
        }
    }

    LaunchedEffect(Unit) {
        titleVisible = true
        val prefs = context.dataStore.data.first()
        val savedString = prefs[SAVED_URIS_KEY] ?: ""
        if (savedString.isNotEmpty()) {
            savedString.split("|").forEachIndexed { index, path ->
                 if (path.isNotEmpty() && index < 7) {
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
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 7)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val emptySlots = (0 until 7).filter { selectedImageUris[it] == null }
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
        floatingActionButton = {
            ExpandableFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
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
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                AnimatedVisibility(
                    visible = titleVisible,
                    enter = fadeIn(animationSpec = tween(800)) +
                            slideInVertically(
                                initialOffsetY = { 50 },
                                animationSpec = tween(600)
                            )
                ) {
                    Row(
                        modifier = Modifier.padding(top = 16.dp, start = 24.dp)
                    ) {
                        Text(
                            text = "你好",
                            color = Color.Black,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.graphicsLayer { rotationZ = angle }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "   6.0特别版本!🎉🎉🎉\n" ,
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.offset(y = 12.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = isFabExpanded) { isFabExpanded = false },
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(count = 7, key = { it }) { index ->
                        AppListItem(
                            uri = selectedImageUris[index],
                            onSelect = {
                                currentSelectedIndex = index
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onPreview = { uri -> 
                                if (uri != null) previewUri = uri 
                            },
                            onDelete = {
                                selectedImageUris[index] = null
                                saveUrisToDisk()
                            }
                        )
                    }
                }
            }

            if (showAnnouncementDialog) {
                AlertDialog(
                    onDismissRequest = { showAnnouncementDialog = false },
                    title = { Text(text = "公告") },
                    text = { Text(text = "欢迎使用Aura 6.0 !\n\n1.性能大飞跃：优化重绘机制，配合预加载，告别卡顿,滑动丝滑度大幅提升。\n\n2.稳定性加固：深度优化底层架构，提升代码运行效率。\n\n" +
                            "3.现已上线GitHUb！\n\n"+"更多技术细节请参阅Github提交日志log"
                             ) },
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
                            text = "历史版本日志(2026年)\n\n" +
                                    "(2026.5.9)  6.0版本：\n现已上传GitHub仓库。\n\n"+
                                    "(2026.4.23) 6.0版本🥰：\n列表渲染加速：针对照片列表，极大降低了滑动过程中的重绘频率。优化了加载策略，通过预处理技术，在保证视觉质量的同时，显著降低了滑动时的 CPU 负载。\n" + "资源管理优化：重塑了缓存清理逻辑。架构微调：清理了冗余的代码、易于维护。\n" +
                                    "环境适配：针对 Jetpack Compose 最新标准进行了深度适配，确保 UI 响应更跟手，添加了设置入口，现已提供清除缓存功能。\n\n"+
                                    "(2026.4.20) 5.3版本😘：\n支持多选功能，优化批量图片添加体验，并紧急修复了特定机型下进入大图预览的计算闪退 Bug。\n\n"+
                                    "(2026.4.18) 5.2版本🐱：\n优化应用运行速度。解锁图片多选功能，自动填充图片。深度重构预览模块，彻底修复特定场景下的闪退问题，页面操作更丝滑。\n\n"+
                                    "(2026.4.16) 5.1版本😴：\n视觉与稳定性加固，优化对话框滚动体验。\n\n "+
                                    "(2026.4.15) 5.0版本🥺：\n极致沉浸式进化。移除大图预览多余UI按钮，实现标题栏与状态栏的视觉融合；引入Android原生视觉交互色，并对系统架构进行了深度精简与稳定性加固。\n\n" +
                            " (2026.4.14) 4.0版本🤪：\n提升应用稳定性，架构全面优化，运行更流畅，首页标题新增动画，提升界面滑动跟手性，未添加的图框新增淡化处理，对底部图片列表引入线性渐变遮罩，进一步提升沉浸式体验。\n\n" +
                            "（2026.4.13）3.0版本🫣：\n增加了照片数量 ，移除使用帮助，新增关于此应用，添加了版本日志。\n\n" +
                            "（2026.4.11）2.0版本🧐：\n新增大图预览功能，适配横屏比例，修复已知bug。\n\n" +
                            "（2026.2.14）1.0版本🎨:\n优化页面布局，底部添加交互小按钮，提供了使用帮助与设置入口。\n\n" +
                            "（2026.1.04）Beta版本🎉:\n一款能让您添加自定义照片的应用，增加图片墙纸开关,左滑可删除，目前可最多可添加四张图片，适配沉浸式状态栏功能。",
                            lineHeight = 20.sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) 
                    },
                    confirmButton = {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("我知道了")
                        }
                    }
                )
            }

            if (previewUri != null) {
                val safeInitialPage = remember(previewUri) { 
                    selectedImageUris.indexOf(previewUri).coerceAtLeast(0) 
                }
                val pagerState = rememberPagerState(initialPage = safeInitialPage, pageCount = { 7 })
                
                FullScreenDialog(onDismiss = { previewUri = null }) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { 
                                    Text(
                                        "预览 (${pagerState.currentPage + 1}/7)",
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
                            val currentUri = selectedImageUris.getOrNull(page)
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
                                    Text("此处尚未添加照片", color = Color.Gray, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            // 5.3 挂载逻辑：设置页面覆盖显示
            if (isSettingsOpen) {
                AuraSettingsScreen(onBack = { isSettingsOpen = false })
            }
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
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f, label = "rotation")
    val bubble1Offset by animateDpAsState(targetValue = if (isExpanded) (-140).dp else 0.dp, label = "bubble1")
    val bubble2Offset by animateDpAsState(targetValue = if (isExpanded) (-70).dp else 0.dp, label = "bubble2")
    val scale by animateFloatAsState(targetValue = if (isExpanded) 1f else 0f, label = "scale")
    val alpha by animateFloatAsState(targetValue = if (isExpanded) 1f else 0f, label = "alpha")
    val density = LocalDensity.current

    Box(contentAlignment = Alignment.BottomEnd) {
        FilledTonalButton(onClick = onSettingsClick, modifier = Modifier.offset { IntOffset(0, with(density) { bubble1Offset.roundToPx() }) }.size(48.dp).graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha), shape = CircleShape, contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.Settings, null) }
        FilledTonalButton(onClick = onGestureClick, modifier = Modifier.offset { IntOffset(0, with(density) { bubble2Offset.roundToPx() }) }.size(48.dp).graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha), shape = CircleShape, contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.Face, null) }
        FloatingActionButton(onClick = onToggle, shape = CircleShape, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF3DDC84), modifier = Modifier.rotate(rotation))
        }
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
    SideEffect { state.updateAnchors(DraggableAnchors { DragValue.Settled at 0f; DragValue.Open at -actionSizePx }) }

    Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp))) {
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (state.offset != 0f) 1f else 0f }.background(Color.Red).clickable { if (state.offset != 0f) { onDelete(); scope.launch { state.animateTo(DragValue.Settled) } } }, contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.padding(end = 24.dp)) }
        Card(
            modifier = Modifier.graphicsLayer { alpha = if (uri == null) 0.6f else 1.0f }.offset { IntOffset(state.offset.let { if (it.isNaN()) 0 else if (it < -actionSizePx) (-actionSizePx + (it + actionSizePx) * 0.3f).roundToInt() else it.roundToInt() }, 0) }.anchoredDraggable(state, Orientation.Horizontal)
                .fillMaxSize()
                .clickable { if (uri == null) onSelect() else onPreview(uri) },
            shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (uri == null) { Icon(Icons.Default.Add, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) }
                else { AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
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
fun AuraSettingsScreen(onBack: () -> Unit) {
    // 1. 修复返回键逻辑：确保系统返回手势执行 onBack 闭包
    BackHandler { onBack() }
    val context = LocalContext.current



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 主理人信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "开发者：Jeesemmy",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "版本：6.0 (Aura)   GitHub",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }


            // 清理缓存按钮卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                onClick = {
                    context.imageLoader.diskCache?.clear()
                    context.imageLoader.memoryCache?.clear()
                    Toast.makeText(context, "缓存清理成功，Aura 已回巅峰！", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "清理图片缓存",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "释放存储空间，清理无用缓存 ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Text(
                text = "开发者寄语：\n你好呀🎀，Aura是我开发的最喜欢的一款App应用。\n感谢你你使用全新的6.0版本。\n\n" +
                       "你不用担心不会使用Aura！请返回到主页，然后点击主页底部的小按钮，点击卡通头像，然后就可以看到以往的更新日志了。\n\n\n"
                ,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
