package ai.ciel.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CielColorScheme = darkColorScheme(
    primary = CielAccentCyan,
    onPrimary = CielBgDark,
    secondary = CielAccentBlue,
    onSecondary = CielTextPrimary,
    background = CielBgDark,
    onBackground = CielTextPrimary,
    surface = CielSurfaceDark,
    onSurface = CielTextPrimary,
    surfaceVariant = CielCardGlass,
    onSurfaceVariant = CielTextSecondary
)

@Composable
fun CielTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CielColorScheme,
        content = content
    )
}
