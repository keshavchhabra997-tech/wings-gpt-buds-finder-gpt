
package com.wingsgpt.buds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Void = Color(0xFF070910)
private val Panel = Color(0xFF111724)
private val PanelLight = Color(0xFF192334)
private val Cyan = Color(0xFF74E8FF)
private val Violet = Color(0xFFB27BFF)
private val Muted = Color(0xFF929DB0)
private val White = Color(0xFFF5F7FC)
private val Green = Color(0xFF70E6B1)
private val Orange = Color(0xFFFFC978)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WingsApp()
        }
    }
}

@Composable
fun WingsApp() {
    var showIntro by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Cyan,
            secondary = Violet,
            background = Void,
            surface = Panel
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Void)
        ) {
            Crossfade(
                targetState = showIntro,
                animationSpec = tween(700),
                label = "introTransition"
            ) { intro ->
                if (intro) {
                    CinematicIntro {
                        showIntro = false
                    }
                } else {
                    MainDashboard(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun CinematicIntro(done: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "intro")

    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200),
        label = "logoScale"
    )

    LaunchedEffect(Unit) {
        delay(2600)
        done()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF10172B),
                        Void,
                        Color(0xFF100D1D)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(270.dp)
        ) {
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Cyan.copy(alpha = 0.05f),
                        Violet.copy(alpha = 0.8f),
                        Cyan.copy(alpha = 0.05f)
                    )
                ),
                radius = size.minDimension / 2,
                style = Stroke(width = 2.dp.toPx())
            )

            drawArc(
                color = Cyan,
                startAngle = rotation,
                sweepAngle = 75f,
                useCenter = false,
                topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                size = Size(
                    size.width - 20.dp.toPx(),
                    size.height - 20.dp.toPx()
                ),
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Cyan, Violet)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "W",
                    color = Void,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "WINGS GPT",
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 5.sp
            )

            Spacer(Modifier.height(9.dp))

            Text(
                "YOUR AUDIO. YOUR CONTROL.",
                color = Muted,
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(45.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Cyan,
                strokeWidth = 2.dp
            )
        }

        TextButton(
            onClick = done,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Text("SKIP", color = Muted)
        }
    }
}

@Composable
private fun MainDashboard(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    label = "pageTransition"
                ) { page ->
                    when (page) {
                        0 -> HomeScreen()
                        1 -> RadarScreen()
                        2 -> DeviceScreen()
                        3 -> LabScreen()
                        else -> SettingsScreen()
                    }
                }
            }

            GlassNavigation(
                selected = selectedTab,
                onSelect = onTabSelected
            )
        }
    }
}

@Composable
private fun GlassNavigation(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val items = listOf(
        "⌂" to "Home",
        "◎" to "Radar",
        "♫" to "Device",
        "⌘" to "Lab",
        "⚙" to "Settings"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFF171E2B))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(25.dp)
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { index, item ->
            val active = selected == index
            val scale by animateFloatAsState(
                targetValue = if (active) 1.08f else 1f,
                label = "navScale"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(index) }
                    .padding(vertical = 7.dp)
                    .scale(scale),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    item.first,
                    color = if (active) Cyan else Muted,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    item.second,
                    color = if (active) White else Muted,
                    fontSize = 9.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ScreenColumn(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Void)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 21.dp, vertical = 25.dp),
        content = content
    )
}

@Composable
private fun ScreenHeading(
    title: String,
    subtitle: String
) {
    Text(
        title,
        color = White,
        fontSize = 27.sp,
        fontWeight = FontWeight.ExtraBold
    )
    Spacer(Modifier.height(6.dp))
    Text(
        subtitle,
        color = Muted,
        fontSize = 13.sp
    )
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = Panel,
        shape = RoundedCornerShape(23.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun HomeScreen() {
    ScreenColumn {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "WELCOME BACK",
                    color = Cyan,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "Your audio space.",
                    color = White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PanelLight),
                contentAlignment = Alignment.Center
            ) {
                Text("W", color = Cyan, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(25.dp))

        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                "YOUR DEVICE",
                color = Cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1C3042), Color(0xFF19172B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "W",
                    color = Cyan.copy(alpha = 0.9f),
                    fontSize = 90.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(17.dp))

            Text(
                "keshav ke airpods",
                color = White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Orange)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Connection not verified",
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(15.dp))

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            Spacer(Modifier.height(13.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Battery", color = Muted, fontSize = 12.sp)
                Text("Unavailable", color = Orange, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(22.dp))

        Text(
            "QUICK ACCESS",
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(12.dp))

        GlassCard(Modifier.fillMaxWidth()) {
            QuickActionRow("◎", "Find My Buds", "Open discovery radar")
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            QuickActionRow("♫", "Device manager", "Connection and device details")
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            QuickActionRow("⌘", "Diagnostics", "Inspect available information")
        }

        Spacer(Modifier.height(22.dp))

        Text(
            "WINGS GPT",
            color = Muted,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(95.dp))
    }
}

@Composable
private fun QuickActionRow(
    icon: String,
    title: String,
    description: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(PanelLight),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = Cyan, fontSize = 21.sp)
        }

        Spacer(Modifier.width(13.dp))

        Column {
            Text(title, color = White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(description, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RadarScreen() {
    val infinite = rememberInfiniteTransition(label = "radar")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRotation"
    )

    ScreenColumn {
        ScreenHeading("Find My Buds", "Bluetooth discovery radar")

        Spacer(Modifier.height(25.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(340.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension * 0.43f

                for (i in 1..4) {
                    drawCircle(
                        color = Cyan.copy(alpha = 0.15f),
                        radius = radius * i / 4f,
                        center = center,
                        style = Stroke(1.dp.toPx())
                    )
                }

                drawLine(
                    Cyan.copy(alpha = 0.14f),
                    Offset(center.x - radius, center.y),
                    Offset(center.x + radius, center.y),
                    1.dp.toPx()
                )

                drawLine(
                    Cyan.copy(alpha = 0.14f),
                    Offset(center.x, center.y - radius),
                    Offset(center.x, center.y + radius),
                    1.dp.toPx()
                )

                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            Cyan.copy(alpha = 0f),
                            Cyan.copy(alpha = 0.65f),
                            Violet.copy(alpha = 0.1f)
                        )
                    ),
                    startAngle = rotation,
                    sweepAngle = 65f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                drawCircle(Cyan, 5.dp.toPx(), center)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("W", color = Cyan, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text("RADAR", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp)
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                "RADAR READY",
                color = Cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(9.dp))
            Text(
                "No verified earbud signal",
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(7.dp))
            Text(
                "This is a visual radar animation. Live scanning, direction and distance are not connected yet.",
                color = Muted,
                fontSize = 12.sp,
                lineHeight = 19.sp
            )
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun DeviceScreen() {
    ScreenColumn {
        ScreenHeading("My Device", "Your earbud information")

        Spacer(Modifier.height(25.dp))

        GlassCard(Modifier.fillMaxWidth()) {
            Text("PAIRED DEVICE", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(13.dp))
            Text(
                "keshav ke airpods",
                color = White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text("Previously paired Bluetooth name", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.heigh
