import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.page.HomePage

fun main() = application {
  val windowState = rememberWindowState(width = 850.dp, height = 850.dp)
  Window(
    title = "数据回放系统",
    state = windowState,
    onCloseRequest = ::exitApplication
  ) {
    MaterialTheme(
      colors = if (isSystemInDarkTheme()) ui.darkColors else ui.lightColors
    ) {
      HomePage()
    }
  }
}
