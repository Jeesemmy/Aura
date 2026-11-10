package com.example.wxxx

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wxxx.ui.theme.LocalIsDarkTheme

/**
 * 🌟 [Aura 19.0 新特性：应用主题枚举模式]
 */
enum class AppThemeMode(val value: Int, val label: String, val description: String) {
    MODE_LIGHT(0, "浅色", "清爽明亮的日间视觉"),
    MODE_DARK(1, "深色", "深邃舒适的沉浸暗色体验"),
    MODE_SYSTEM(2, "跟随系统", "根据系统当前深浅色外观自适应切换");

    companion object {
        fun fromValue(value: Int): AppThemeMode = entries.firstOrNull { it.value == value } ?: MODE_LIGHT
    }
}

/**
 * 🎨 [Aura 19.0 二级全屏设置页面：应用主题与深色模式]
 */
@Composable
fun AuraThemeScreen(
    currentThemeMode: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDarkTheme = LocalIsDarkTheme.current

    // 🌟 本地即时响应乐观状态：用户点击选项时单选框圆钮瞬时点亮，后台异步执行存盘，彻底消除点按钝感
    var optimisticThemeMode by remember(currentThemeMode) { mutableStateOf(currentThemeMode) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部返回栏 (彻底移除标题文案，保持极简通透)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp)
        ) {
            // 返回键：锚定在左侧
            AuraCapsuleBackButton(
                onBack = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 分组标题
        Text(
            text = "外观模式",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )

        // 2. 核心大卡片：包含三种主题选项 (16.dp 圆角、微透质感)
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
            Column(modifier = Modifier.fillMaxWidth()) {
                ThemeOptionRow(
                    title = "浅色（默认）",
                    subtitle = AppThemeMode.MODE_LIGHT.description,
                    selected = optimisticThemeMode == AppThemeMode.MODE_LIGHT,
                    isDarkTheme = isDarkTheme,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        optimisticThemeMode = AppThemeMode.MODE_LIGHT
                        onThemeSelected(AppThemeMode.MODE_LIGHT)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    thickness = 0.5.dp,
                    color = if (isDarkTheme) Color(0xFF334155).copy(alpha = 0.6f)
                    else Color(0xFFE2E8F0).copy(alpha = 0.75f)
                )

                ThemeOptionRow(
                    title = "深色",
                    subtitle = AppThemeMode.MODE_DARK.description,
                    selected = optimisticThemeMode == AppThemeMode.MODE_DARK,
                    isDarkTheme = isDarkTheme,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        optimisticThemeMode = AppThemeMode.MODE_DARK
                        onThemeSelected(AppThemeMode.MODE_DARK)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    thickness = 0.5.dp,
                    color = if (isDarkTheme) Color(0xFF334155).copy(alpha = 0.6f)
                    else Color(0xFFE2E8F0).copy(alpha = 0.75f)
                )

                ThemeOptionRow(
                    title = "跟随系统",
                    subtitle = AppThemeMode.MODE_SYSTEM.description,
                    selected = optimisticThemeMode == AppThemeMode.MODE_SYSTEM,
                    isDarkTheme = isDarkTheme,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        optimisticThemeMode = AppThemeMode.MODE_SYSTEM
                        onThemeSelected(AppThemeMode.MODE_SYSTEM)
                    }
                )
            }
        }
    }
}

/**
 * 🌟 单个主题选项行
 */
@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        }

        // 单选指示器 (与性能开关及应用锁保持统一的系统蓝色 Color(0xFF05ACF7))
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (selected) Color(0xFF05ACF7)
                    else if (isDarkTheme) Color(0xFF334155).copy(alpha = 0.5f)
                    else Color(0xFFE2E8F0)
                )
                .border(
                    width = if (selected) 0.dp else 1.5.dp,
                    color = if (selected) Color.Transparent
                    else if (isDarkTheme) Color(0xFF475569)
                    else Color(0xFFCBD5E1),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
