
package com.wingsgpt.buds.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.wingsgpt.buds.ui.theme.*

@Composable
fun HomeScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(Void)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 22.dp,
                end = 22.dp,
                top = 55.dp,
                bottom = 120.dp
            )
    ) {
        Text(
            "W I N G S",
            color = Cyan,
            letterSpacing = 5.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Welcome back.",
            color = TextPrimary,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Your audio universe.",
            color = Muted,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(28.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF26384B),
                            Color(0xFF171B2C),
                            Color(0xFF302044)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(.18f),
                    RoundedCornerShape(30.dp)
                )
                .padding(22.dp)
        ) {
            Column {
                Text(
                    "DEVICE OVERVIEW",
                    color = Muted,
                    fontSize = 10.sp
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "Your Buds",
                    color = TextPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Hardware not identified",
                    color = Warning,
                    fontSize = 11.sp
                )
            }

            Text(
                "W",
                Modifier.align(Alignment.CenterEnd),
                color = Cyan,
                fontSize = 95.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "CONNECTIVITY NOT VERIFIED",
                Modifier.align(Alignment.BottomStart),
                color = Muted,
                fontSize = 9.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "QUICK ACCESS",
            color = Muted,
            fontSize = 11.sp,
            letterSpacing = 3.sp
        )

        Spacer(Modifier.height(14.dp))

        DashboardTile(
            "Bluetooth Radar",
            "Discover nearby BLE advertisers"
        )

        DashboardTile(
            "Find My Buds",
            "Tracking capability not yet verified"
        )

        DashboardTile(
            "Device Lab",
            "Inspect available hardware signals"
        )
    }
}

@Composable
private fun DashboardTile(
    title: String,
    subtitle: String
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Glass
        )
    ) {
        Column(Modifier.padding(19.dp)) {
            Text(
                title,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                subtitle,
                color = Muted,
                fontSize = 12.sp
            )
        }
    }
}
