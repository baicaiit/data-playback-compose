package ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import ui.page.HomePage

@Composable
fun ApplicationScope.DataPlaybackApp(
  state: DataPlaybackAppState = rememberDataPlaybackAppState(),
) {
  val isDark = rememberDesktopDarkTheme()

  for (window in state.windows) {
    key(window) {
      Window(
        title = window.title,
        state = window.windowState,
        onCloseRequest = {
          window.close()
          if (state.windows.isEmpty()) {
            exitApplication()
          }
        }
      ) {
        MaterialTheme(
          colors = if (isDark) darkColors else lightColors
        ) {
          HomePage(window)
        }
      }
    }
  }
  AppTray(state)
}

@Composable
fun ApplicationScope.AppTray(state: DataPlaybackAppState) {
  Tray(
    state = state.tray,
    icon = painterResource("CarbonData1.svg"),
    menu = {
      Item(
        "新窗口",
        onClick = { state.newWindow() }
      )
      Item(
        "退出程序",
        onClick = {
          state.exit()
          exitApplication()
        }
      )
    }
  )
}