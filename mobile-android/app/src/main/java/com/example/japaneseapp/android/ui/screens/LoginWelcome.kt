package com.example.japaneseapp.android.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun LoginWelcome(passwordFocused: Boolean, modifier: Modifier = Modifier) {
    val floatTransition = rememberInfiniteTransition(label = "mascot-float")
    val floatOffset by floatTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot-offset"
    )

    Column(
        modifier = modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .background(Color(0x99FFFFFF), CircleShape)
            )
            Text(
                text = "✦",
                color = Color(0xFFFFC857),
                fontSize = 34.sp,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-20).dp, y = 18.dp)
            )
            Text(
                text = "★",
                color = LoginPalette.pink,
                fontSize = 25.sp,
                modifier = Modifier.align(Alignment.CenterStart).offset(x = 8.dp, y = (-42).dp)
            )
            Column(
                modifier = Modifier
                    .offset(y = floatOffset.dp)
                    .background(Color(0xFFFFF9FC), RoundedCornerShape(34.dp))
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = if (passwordFocused) "🙈" else "🐱", fontSize = 80.sp, lineHeight = 88.sp)
                Text(text = "📚  ✨", fontSize = 28.sp)
            }
            Text(
                text = "♡",
                color = LoginPalette.pink,
                fontSize = 32.sp,
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-16).dp, y = (-18).dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Welcome Back, Scholar! ✨",
            color = LoginPalette.ink,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Cùng mèo nhỏ khám phá thêm\nmột bài học mới nhé!",
            color = LoginPalette.muted,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
