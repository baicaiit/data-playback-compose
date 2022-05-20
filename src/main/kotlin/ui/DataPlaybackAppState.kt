package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import ui.page.HomeWindowState

class DataPlaybackAppState {
  val tray = TrayState()

  private val _windows = mutableStateListOf<HomeWindowState>()
  val windows: List<HomeWindowState> get() = _windows

  init {
    newWindow()
  }

  fun newWindow() {
    _windows.add(createHomeWindowState())
  }

  fun exit() {
    _windows.clear()
  }

  fun sendNotification(notification: Notification) {
    tray.sendNotification(notification)
  }

  private fun createHomeWindowState(
    title: String = "数据回放系统 ${_windows.size + 1}",
  ) = HomeWindowState(
    this,
    title,
    _windows::remove
  )
}

@Composable
fun rememberDataPlaybackAppState() = remember {
  DataPlaybackAppState()
}
