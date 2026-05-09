package com.example.wxxx

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.wxxx.ui.theme.WXXXTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "photo_settings")
private val SAVED_URIS_KEY = stringPreferencesKey("saved_uris")

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
    val selectedImageUris = remember { mutableStateListOf<Uri?>(null, null, null, null) }

    var currentSelectedIndex by remember { mutableIntStateOf(-1) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var previewUri by remember { mutableStateOf<Uri?>(null) }

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
                if (path.isNotEmpty() && index < 4) {
                    selectedImageUris[index] = Uri.parse(path)
                }
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && currentSelectedIndex != -1) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { e.printStackTrace() }
            selectedImageUris[currentSelectedIndex] = uri
            saveUrisToDisk()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExpandableFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onSettingsClick = { isFabExpanded = false },
                onGestureClick = {
                    isFabExpanded = false
                    showHelpDialog = true
                }
            )
        }
    ) { innerPadding ->
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
                    modifier = Modifier.padding(top = 16.dp, start = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "欢迎",
                        color = Color.Black,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "您已获得这款应用的抢先体验",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
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
                items(4) { index ->
                    AppListItem(
                        uri = selectedImageUris[index],
                        onSelect = {
                            currentSelectedIndex = index
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onPreview = { uri -> previewUri = uri },
                        onDelete = {
                            selectedImageUris[index] = null
                            saveUrisToDisk()
                        }
                    )
                }
            }
        }

        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = { Text(text = "使用帮助") },
                text = { Text(text = "使用帮助：点击+添加您的照片用于展示，左滑图片可删除") },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("我知道了")
                    }
                }
            )
        }

        if (previewUri != null) {
            FullScreenDialog(onDismiss = { previewUri = null }) {
                PreviewScreen(uri = previewUri!!, onDismiss = { previewUri = null })
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

    Box(contentAlignment = Alignment.BottomEnd) {
        FilledTonalButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .offset(y = bubble1Offset)
                .size(48.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }

        FilledTonalButton(
            onClick = onGestureClick,
            modifier = Modifier
                .offset(y = bubble2Offset)
                .size(48.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
        }

        FloatingActionButton(
            onClick = onToggle,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Expand Menu",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    uri: Uri?,
    onSelect: () -> Unit,
    onPreview: (Uri) -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val decaySpec = rememberSplineBasedDecay<Float>()
    val haptic = LocalHapticFeedback.current
    val actionSizePx = with(density) { 80.dp.toPx() }

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Settled,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            decayAnimationSpec = decaySpec
        )
    }

    LaunchedEffect(state) {
        snapshotFlow { state.currentValue }
            .distinctUntilChanged()
            .collect { value ->
                if (value == DragValue.Open) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
    }

    SideEffect {
        state.updateAnchors(
            DraggableAnchors {
                DragValue.Settled at 0f
                DragValue.Open at -actionSizePx
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red)
                .clickable {
                    onDelete()
                    scope.launch { state.animateTo(DragValue.Settled) }
                },
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.padding(end = 24.dp)
            )
        }

        Card(
            modifier = Modifier
                .offset {
                    val rawOffset = state.offset
                    val adjustedOffset = if (rawOffset < -actionSizePx) {
                        -actionSizePx + (rawOffset + actionSizePx) * 0.3f
                    } else {
                        rawOffset
                    }
                    IntOffset(
                        x = if (adjustedOffset.isNaN()) 0 else adjustedOffset.roundToInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(state, Orientation.Horizontal)
                .fillMaxSize()
                .clickable {
                    if (uri == null) {
                        onSelect()
                    } else {
                        onPreview(uri)
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (uri == null) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Image",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}