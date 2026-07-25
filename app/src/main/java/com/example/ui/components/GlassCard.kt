package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    hasAccentGlow: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (hasAccentGlow) {
        Brush.horizontalGradient(listOf(CyberCyan.copy(alpha = 0.5f), ElectricBlue.copy(alpha = 0.5f)))
    } else {
        Brush.horizontalGradient(listOf(DarkCardBorder.copy(alpha = 0.6f), DarkCardBorder.copy(alpha = 0.6f)))
    }

    val cardModifier = if (onClick != null) {
        modifier.bounceClick(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        shape = shape,
        color = DarkSurface.copy(alpha = 0.9f),
        border = BorderStroke(0.5.dp, borderBrush),
        tonalElevation = 2.dp,
        modifier = cardModifier
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}
