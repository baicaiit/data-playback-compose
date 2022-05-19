package ui

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.WindowState
import ui.page.HomeWindowState

class DataPlaybackAppState {
  val tray = TrayState()
  var isVisible by mutableStateOf(true)
    private set
  val windowState = WindowState(width = 900.dp, height = 850.dp)

  private val _windows = mutableStateListOf<HomeWindowState>()
  val windows: List<HomeWindowState> get() = _windows

  fun newWindow() {
    _windows.add(
      HomeWindowState(this)
    )
  }

  fun sendNotification(notification: Notification) {
    tray.sendNotification(notification)
  }

  fun hideWindow() {
    isVisible = false
  }

  fun showWindow() {
    isVisible = true
  }
}

@Composable
fun rememberDataPlaybackAppState() = remember {
  DataPlaybackAppState()
}
