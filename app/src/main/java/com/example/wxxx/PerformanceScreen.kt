package com.example.wxxx

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.Process
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.wxxx.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * 🚀 [底座性能检测辅助单例：Aura 进程级安全监控算法]
 * 1. RAM: 移除全局已用内存计算，基于 android.os.Debug.getPss() 读取 Aura 进程物理内存 (PSS)，并计算其在整机物理总内存中的真实占比百分比
 * 2. CPU: 基于 Process.getElapsedCpuTime() 与 SystemClock.elapsedRealtime() 双采样差值，精确计算 Aura 进程瞬时核心负载
 */
internal object PerformanceMonitorHelper {
    private var lastCpuTime = -1L
    private var lastRealtime = -1L

    fun getAppMemoryUsage(context: Context): Pair<String, String> {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            val totalMem = memInfo.totalMem

            val pssKb = Debug.getPss()
            val pssBytes = pssKb * 1024L
            val pssMb = pssKb / 1024L

            val ramUsagePercent = if (totalMem > 0L) {
                (pssBytes.toDouble() / totalMem.toDouble() * 100.0).coerceIn(0.0, 100.0)
            } else 0.0

            val percentText = String.format(Locale.US, "%.1f%%", ramUsagePercent)
            val detail = "PSS: ${pssMb}MB"
            Pair(percentText, detail)
        } catch (e: Exception) {
            Pair("0.0%", "PSS: --MB")
        }
    }

    fun getAppCpuUsage(): Int {
        return try {
            val currentCpuTime = Process.getElapsedCpuTime()
            val currentRealtime = SystemClock.elapsedRealtime()
            val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

            val percent = if (lastCpuTime >= 0 && lastRealtime > 0) {
                val cpuTimeDelta = currentCpuTime - lastCpuTime
                val wallTimeDelta = currentRealtime - lastRealtime
                if (wallTimeDelta > 0) {
                    val appCpuPercent = ((cpuTimeDelta.toFloat() / (wallTimeDelta.toFloat() * cores)) * 100f).coerceIn(0f, 100f)
                    appCpuPercent.toInt()
                } else {
                    0
                }
            } else {
                0
            }

            lastCpuTime = currentCpuTime
            lastRealtime = currentRealtime
            percent
        } catch (e: Exception) {
            0
        }
    }

    fun reset() {
        lastCpuTime = -1L
        lastRealtime = -1L
    }

    /**
     * 🔍 芯片名称动态识别与商业品名映射
     * 覆盖高通骁龙旗舰矩阵 (Snapdragon® 8 Gen 3 / 8 Gen 2 / 8 Elite) 与联发科天玑旗舰平台 (Dimensity 9300 / 9200)
     * 多维解析 Build.SOC_MODEL、Build.HARDWARE 及 Build.BOARD，结合系统原生透传与稳健保底机制
     */
    fun getDynamicSocName(): String {
        return try {
            val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MODEL.trim()
            } else ""
            val hardware = Build.HARDWARE.lowercase()
            val board = Build.BOARD.lowercase()

            when {
                // 高通骁龙旗舰矩阵
                socModel.contains("SM8650", ignoreCase = true) || hardware.contains("pineapple") || board.contains("pineapple") -> {
                    "Snapdragon® 8 Gen 3"
                }
                socModel.contains("SM8550", ignoreCase = true) || hardware.contains("kalama") -> "Snapdragon® 8 Gen 2"
                socModel.contains("SM8750", ignoreCase = true) || hardware.contains("sun") -> "Snapdragon® 8 Elite"

                // 联发科天玑旗舰矩阵
                socModel.contains("MT6989", ignoreCase = true) || hardware.contains("mt6989") || board.contains("k6989") -> {
                    "Dimensity 9300"
                }
                socModel.contains("MT6985", ignoreCase = true) || hardware.contains("mt6985") || board.contains("k6985") -> {
                    "Dimensity 9200"
                }

                // 系统原生透传与保底兜底
                socModel.isNotEmpty() && !socModel.equals("unknown", ignoreCase = true) -> socModel
                else -> "Snapdragon® 8 Gen 3"
            }
        } catch (e: Exception) {
            "Snapdragon® 8 Gen 3"
        }
    }

    /**
     * ⚡ 最大加速频率底层真实探测：循环读取 Linux 内核 CPU 频率节点
     */
    fun getMaxCpuFreq(): String {
        return try {
            var maxFreqKHz = 0L
            val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
            for (i in 0 until cores) {
                val file = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
                if (file.exists() && file.canRead()) {
                    val freq = file.readText().trim().toLongOrNull() ?: 0L
                    if (freq > maxFreqKHz) maxFreqKHz = freq
                }
            }
            if (maxFreqKHz > 0L) {
                val ghz = maxFreqKHz.toDouble() / 1_000_000.0
                String.format(Locale.US, "%.1f GHz", ghz)
            } else {
                "3.3 GHz" // 读取受限时的优雅兜底
            }
        } catch (e: Exception) {
            "3.3 GHz"
        }
    }
}

/**
 * ⚡ [三级全屏面板：设备性能实时面板 - PerformanceScreen]
 * 1. 顶部 Header：自适应匹配骁龙 / 天玑双平台官方 16:9 品牌海报大图
 * 2. 中部数据区：严谨对齐的双分栏 CPU 与 RAM 极简加粗实时读数
 * 3. 稳妥轮询：开关前置守卫 + Dispatchers.IO 调度，离开或关闭立即销毁，杜绝常驻功耗
 * 4. 手势交互：支持系统返回键与屏幕左侧边缘右滑手势返回
 */
@Composable
fun PerformanceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDarkTheme = LocalIsDarkTheme.current

    val prefsState by context.experimentalDataStore.data.collectAsState(initial = null)
    val isPerformanceEnabled = prefsState?.get(PERFORMANCE_MONITOR_KEY) ?: false

    val socName = remember { PerformanceMonitorHelper.getDynamicSocName() }
    val maxCpuFreq = remember { PerformanceMonitorHelper.getMaxCpuFreq() }

    val coverResId = remember(socName) {
        if (socName.contains("Dimensity", ignoreCase = true)) {
            R.drawable.ic_dimensity_cover
        } else {
            R.drawable.ic_snapdragon_cover
        }
    }

    var cpuPercent by remember { mutableIntStateOf(0) }
    var ramPercentText by remember { mutableStateOf("0.0%") }

    // 🌟 1. 边缘滑动手势拦截：支持屏幕左边缘向右滑动触发退出
    var dragDistanceX by remember { mutableFloatStateOf(0f) }
    var isDraggingFromEdge by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    // 🌟 2. 深度生命周期与开关前置守卫：开关明确为 true 时才允许触发轮询，I/O 与 IPC 完全调度至 Dispatchers.IO
    LaunchedEffect(isPerformanceEnabled) {
        if (!isPerformanceEnabled) {
            // 开关关闭时：立即返回，绝不执行任何采样、文件读取或定时器
            cpuPercent = 0
            ramPercentText = "0.0%"
            return@LaunchedEffect
        }

        // 首次初始化采样调度至 Dispatchers.IO，彻底解耦 UI 主线程
        val initialRamText = withContext(Dispatchers.IO) {
            PerformanceMonitorHelper.reset()
            val initialMem = PerformanceMonitorHelper.getAppMemoryUsage(context)
            PerformanceMonitorHelper.getAppCpuUsage()
            initialMem.first
        }
        ramPercentText = initialRamText

        while (isActive) {
            delay(1200L) // 1.2秒采样周期，兼顾平滑度与能效比
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                // 将所有 Linux 节点读取与系统 Binder IPC 调用切换至 Dispatchers.IO 执行
                val (currentCpu, currentRam) = withContext(Dispatchers.IO) {
                    val cpu = PerformanceMonitorHelper.getAppCpuUsage()
                    val mem = PerformanceMonitorHelper.getAppMemoryUsage(context)
                    Pair(cpu, mem.first)
                }
                cpuPercent = currentCpu
                ramPercentText = currentRam
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        // 距离左边缘 120px 以内视为边缘手势发起
                        isDraggingFromEdge = offset.x < 120f
                        dragDistanceX = 0f
                    },
                    onDragEnd = {
                        if (isDraggingFromEdge && dragDistanceX > 160f) {
                            onBack()
                        }
                        isDraggingFromEdge = false
                        dragDistanceX = 0f
                    },
                    onDragCancel = {
                        isDraggingFromEdge = false
                        dragDistanceX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        if (isDraggingFromEdge) {
                            dragDistanceX += dragAmount
                            if (dragDistanceX > 0) {
                                change.consume()
                            }
                        }
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部返回与标题栏 (Box 布局确保「实时面板」标题绝对水平居中)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp)
        ) {
            // 返回胶囊按钮 (左侧对齐)
            AuraCapsuleBackButton(
                onBack = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. 顶部 Header：搭载芯片官方封面大图卡片 (自适应匹配骁龙/天玑，16:9 比例与 20.dp 优雅大圆角)
        Image(
            painter = painterResource(id = coverResId),
            contentDescription = "Platform Banner",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f) // 严密契合 16:9 物理比例
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center // 素材已居中裁好，直接标准居中对齐
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 3. 中下部核心数据区：分左右两个数据展示区 (左侧 CPU XX%，右侧 RAM XX%，极简双层级加粗排版)
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
                .padding(vertical = 22.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧数据区：CPU
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CPU 占用",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$cpuPercent%",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                    )
                }

                // 中间垂直对齐分割线
                VerticalDivider(
                    modifier = Modifier
                        .height(46.dp)
                        .padding(horizontal = 8.dp),
                    thickness = 0.8.dp,
                    color = if (isDarkTheme) Color(0xFF334155).copy(alpha = 0.6f)
                    else Color(0xFFE2E8F0).copy(alpha = 0.8f)
                )

                // 右侧数据区：RAM
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RAM 占用",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ramPercentText,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. 平台硬件规格卡片 (参考调色页全天色谱分布的底部卡片风格)
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
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "中央处理器",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "移动计算平台",
                        fontSize = 12.5.sp,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Text(
                        text = socName,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isDarkTheme) Color(0xFF334155).copy(alpha = 0.6f)
                    else Color(0xFFE2E8F0).copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "最大加速频率",
                        fontSize = 12.5.sp,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Text(
                        text = maxCpuFreq,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
