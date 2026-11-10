package com.example.wxxx

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.wxxx.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.launch

/**
 * 实验性功能 DataStore 专用持久化实例
 */
val Context.experimentalDataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_experimental_settings")
val PERFORMANCE_MONITOR_KEY = booleanPreferencesKey("performance_monitor_enabled")

/**
 * 🧪 [二级全屏面板：实验性功能中心 - AuraExperimentalSubScreen]
 * 包含“实时占用”等实验性特性入口卡片及专属开关控制
 */
@Composable
fun AuraExperimentalSubScreen(
    onNavigateToPerformance: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isDarkTheme = LocalIsDarkTheme.current

    val prefsState by context.experimentalDataStore.data.collectAsState(initial = null)
    val isPerformanceEnabled = prefsState?.get(PERFORMANCE_MONITOR_KEY) ?: false

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部返回与标题栏 (Box 布局确保「实验性功能」标题绝对物理居中)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp)
        ) {
            // 1. 返回键：严格锚定在左侧
            AuraCapsuleBackButton(
                onBack = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // 2. 页面标题：严格锚定在物理中心，不受左侧返回键宽度影响
            Text(
                text = "实验性功能",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 分组标题
        Text(
            text = "性能监控",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )

        // 2. 实时占用入口大卡片 (1:1 复用设置页统一标准样式：16.dp 圆角、0.88f 纯净透白背景、1.dp 描边)
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
                .clickable(enabled = isPerformanceEnabled) {
                    onNavigateToPerformance()
                }
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧图标
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "性能",
                        tint = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1C1C1E),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 标题与辅助说明
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "性能",
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1C1C1E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isPerformanceEnabled) "点击进入性能面板" else "开启后可进入性能面板",
                        fontSize = 12.5.sp,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 右侧 Switch 开关
                Switch(
                    checked = isPerformanceEnabled,
                    onCheckedChange = { targetState ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            context.experimentalDataStore.edit { prefs ->
                                prefs[PERFORMANCE_MONITOR_KEY] = targetState
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFFFFF),
                        checkedTrackColor = Color(0xFF05ACF7),
                        uncheckedThumbColor = if (isDarkTheme) Color(0xFF64748B) else Color(0x5DCBC9C9),
                        uncheckedTrackColor = if (isDarkTheme) Color(0xFF334155).copy(alpha = 0.5f) else Color(0x4CFFFFFF)
                    )
                )
            }
        }
    }
}
