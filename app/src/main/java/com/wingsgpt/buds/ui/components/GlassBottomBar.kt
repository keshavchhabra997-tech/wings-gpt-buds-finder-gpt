
package com.wingsgpt.buds.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import com.wingsgpt.buds.ui.theme.*

@Composable
fun GlassBottomBar(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val icons = listOf(
        Icons.Rounded.Home,
        Icons.Rounded.Radar,
        Icons.Rounded.Headphones,
        Icons.Rounded.Biotech,
        Icons.Rounded.Settings
    )

    val labels = listOf(
        "Home", "Radar", "Buds", "Lab", "Settings"
    )

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 22.dp)
            .height(76.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xD91B202B))
            .border(
                1.dp,
                Color.White.copy(alpha = .19f),
                RoundedCornerShape(28.dp)
            )
    ) {
        val itemWidth = maxWidth / 5

        val animatedPosition by animateDpAsState(
            targetValue = itemWidth * selected,
            animationSpec = spring(
                dampingRatio = .72f,
                stiffness = 320f
            ),
            label = "glass-position"
        )

        Box(
            Modifier
                .offset(x = animatedPosition + 5.dp, y = 5.dp)
                .width(itemWidth - 10.dp)
                .height(66.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(.20f),
                            Cyan.copy(.13f),
                            Color.White.copy(.04f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(.24f),
                    RoundedCornerShape(23.dp)
                )
        )

        Row(Modifier.fillMaxSize()) {
            icons.forEachIndexed { index, icon ->
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = labels[index],
                        tint = if (selected == index) Cyan else Muted,
                        modifier = Modifier.size(23.dp)
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        labels[index],
                        color = if (selected == index) TextPrimary else Muted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
