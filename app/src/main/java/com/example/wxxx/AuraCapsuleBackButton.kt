package com.example.wxxx

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wxxx.ui.theme.LocalIsDarkTheme

/**
 * 🌟 [Aura 19.0 全局统一高亮返回胶囊组件：AuraCapsuleBackButton]
 * 严格以「应用锁定页」图 1 为唯一视觉基准规范：
 * 1. 纯胶囊形态：CircleShape / 50% 纯圆角；
 * 2. 尺寸规范：视觉高度 36.dp，左右内容内边距 14.dp，外层保留 48.dp 触控判定区；
 * 3. 深色模式（Dark）：高对比度高亮浅色底 (Color(0xFFEDEBF0)) + 极客深色文字与箭头 (Color(0xFF1A1B21))；
 * 4. 浅色模式（Light）：纯白底色无缝相融 (Color.White) + 极客深色文字与箭头 (Color(0xFF1A1B21))；
 * 5. 排版：左侧箭头 "←" (16.sp, Bold) + 4.dp 间距 + "返回" 文本 (13.5.sp, Medium)。
 */
@Composable
fun AuraCapsuleBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "返回"
) {
    val haptic = LocalHapticFeedback.current
    val isDarkTheme = LocalIsDarkTheme.current

    val containerColor = if (isDarkTheme) Color(0xFFEDEBF0) else Color.White
    val contentColor = Color(0xFF1A1B21)
    val borderStroke = if (isDarkTheme) BorderStroke(1.dp, Color.White.copy(alpha = 0.95f)) else null

    Box(
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onBack()
            },
            shape = CircleShape,
            color = containerColor,
            border = borderStroke,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "←",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = text,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}
