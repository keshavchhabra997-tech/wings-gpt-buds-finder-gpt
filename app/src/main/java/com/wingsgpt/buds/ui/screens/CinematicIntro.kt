
package com.wingsgpt.buds.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.wingsgpt.buds.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CinematicIntro(done: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }

    val rotation by rememberInfiniteTransition(
        label = "orbit"
    ).animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val scale by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else .35f,
        animationSpec = spring(dampingRatio = .55f),
        label = "logo-scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(1000),
        label = "text-alpha"
    )

    LaunchedEffect(Unit) {
        delay(300)
        phase = 1
        delay(1000)
        phase = 2
        delay(1600)
        done()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Void),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .rotate(rotation)
                .border(
                    2.dp,
                    Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            Cyan,
                            Violet,
                            Color.Transparent
                        )
                    ),
                    RoundedCornerShape(100.dp)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(42.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF536276),
                                Color(0xFF111824)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = .35f),
                        RoundedCornerShape(42.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "W",
                    color = Cyan,
                    fontSize = 78.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(30.dp))

            Column(
                modifier = Modifier.alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WINGS GPT",
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 5.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "PRECISION • CONNECTION • CONTROL",
                    color = Cyan,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        TextButton(
            onClick = done,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Text("SKIP INTRO", color = Muted)
        }
    }
}
