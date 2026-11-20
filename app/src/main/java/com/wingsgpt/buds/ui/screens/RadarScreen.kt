
package com.wingsgpt.buds.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wingsgpt.buds.ui.theme.Cyan
import com.wingsgpt.buds.ui.theme.Muted
import com.wingsgpt.buds.ui.theme.Panel
import com.wingsgpt.buds.ui.theme.TextPrimary
import com.wingsgpt.buds.ui.theme.Void

@Composable
fun RadarScreen() {
    val transition = rememberInfiniteTransition(label = "radar")
    val sweep = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Void)
            .padding(horizontal = 22.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FIND MY BUDS",
            color = TextPrimary,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Bluetooth discovery radar",
            color = Muted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.height.coerceAtMost(size.width) * 0.42f

                for (i in 1..4) {
                    drawCircle(
                        color = Cyan.copy(alpha = 0.13f),
                        radius = radius * i / 4f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                drawLine(
                    color = Cyan.copy(alpha = 0.15f),
                    start = Offset(center.x - radius, center.y),
                    end = Offset(center.x + radius, center.y),
                    strokeWidth = 1.dp.toPx()
                )

                drawLine(
                    color = Cyan.copy(alpha = 0.15f),
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y + radius),
                    strokeWidth = 1.dp.toPx()
                )

                drawArc(
                    color = Cyan.copy(alpha = 0.55f),
                    startAngle = sweep.value,
                    sweepAngle = 38f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(
                        radius * 2f,
                        radius * 2f
                    )
                )

                drawCircle(
                    color = Cyan,
                    radius = 5.dp.toPx(),
                    center = center
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Panel,
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "RADAR READY",
                    color = Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "No verified earbud signal yet",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "This radar is currently a visual interface. It does not show real distance, direction, or live earbud location.",
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(88.dp))
    }
}
