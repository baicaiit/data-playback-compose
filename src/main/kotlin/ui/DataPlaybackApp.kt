package ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import ui.page.HomePage

@Composable
fun ApplicationScope.DataPlaybackApp(
  state: DataPlaybackAppState =
    rememberDataPlaybackAppState().apply { newWindow() },
) {
  val isDark = rememberDesktopDarkTheme()

  Window(
    title = "数据回放系统",
    state = state.windowState,
    visible = state.isVisible,
    onCloseRequest = { state.hideWindow() }
  ) {
    MaterialTheme(
      colors = if (isDark) darkColors else lightColors
    ) {
      HomePage(state.windows[0])
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
        "主页面",
        onClick = { state.showWindow() }
      )
      Item(
        "退出程序",
        onClick = ::exitApplication
      )
    }
  )
}