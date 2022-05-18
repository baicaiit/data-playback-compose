import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.page.HomePage
import ui.rememberDesktopDarkTheme

fun main() = application {
  val windowState = rememberWindowState(width = 900.dp, height = 850.dp)
  val darkTheme = rememberDesktopDarkTheme()
  Window(
    title = "数据回放系统",
    state = windowState,
    onCloseRequest = ::exitApplication
  ) {
    MaterialTheme(
      colors = if (darkTheme) ui.darkColors else ui.lightColors
    ) {
      HomePage()
    }
  }
}
