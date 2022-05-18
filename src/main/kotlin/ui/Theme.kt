package ui

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.jthemedetecor.OsThemeDetector

val lightColors = lightColors(

)

val darkColors = darkColors(
  surface = Color(0xFF35363A)
)

// for making window support dark mode, add -Dapple.awt.application.appearance=system
// https://stackoverflow.com/q/55217540
@Composable
fun rememberDesktopDarkTheme(): Boolean {
  var darkTheme by remember {
    mutableStateOf(OsThemeDetector.getDetector().isDark)
  }

  DisposableEffect(Unit) {
    val darkThemeListener: (Boolean) -> Unit = {
      darkTheme = it
    }

    val detector = OsThemeDetector.getDetector().apply {
      registerListener(darkThemeListener)
    }

    onDispose {
      detector.removeListener(darkThemeListener)
    }
  }

  return darkTheme
}