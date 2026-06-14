package edu.nd.pmcburne.hwapp.one.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PickupGameColorScheme = lightColorScheme(
    primary = PickupGreen,
    onPrimary = White,
    primaryContainer = ChipGreen,
    onPrimaryContainer = ChipTextGreen,
    secondary = PickupGreenLight,
    onSecondary = White,
    background = White,
    onBackground = DarkText,
    surface = White,
    onSurface = DarkText,
    surfaceVariant = LightGray,
    onSurfaceVariant = SubtitleGray,
    outline = Color(0xFFBDBDBD)
)

@Composable
fun PickupGameTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PickupGameColorScheme,
        typography = Typography,
        content = content
    )
}
