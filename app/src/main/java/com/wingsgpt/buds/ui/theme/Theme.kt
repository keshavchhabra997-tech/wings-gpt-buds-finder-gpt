
package com.wingsgpt.buds.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Void = Color(0xFF050609)
val Panel = Color(0xFF11151D)
val Glass = Color(0xAA252B38)
val Cyan = Color(0xFF74E8FF)
val Violet = Color(0xFFB27BFF)
val Muted = Color(0xFF8B93A5)
val TextPrimary = Color(0xFFF5F7FC)
val Warning = Color(0xFFFFC978)

@Composable
fun WingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Cyan,
            secondary = Violet,
            background = Void,
            surface = Panel
        ),
        content = content
    )
}
