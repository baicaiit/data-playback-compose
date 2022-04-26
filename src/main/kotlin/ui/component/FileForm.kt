package ui.area

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import state.HomeWindowState
import java.awt.FileDialog
import java.awt.Frame

@Composable
fun FileForm(homeWindowState: HomeWindowState) {
  var isFileChooserOpen by remember { mutableStateOf(false) }

  if (isFileChooserOpen) {
    FileDialog(
      onCloseRequest = {
        isFileChooserOpen = false
        homeWindowState.onReadExcel(it)
      }
    )
  }

  Text("选择需要发送的文件")
  AnimatedVisibility(homeWindowState.selectedFilePath.isNotEmpty()) {
    Text(homeWindowState.selectedFilePath)
  }
  OutlinedButton(
    onClick = { isFileChooserOpen = true },
    modifier = Modifier.padding(horizontal = 8.dp)
  ) {
    Text("选择")
  }
}

@Composable
fun FileDialog(
  parent: Frame? = null,
  onCloseRequest: (result: String?) -> Unit,
) = AwtWindow(
  create = {
    object : FileDialog(parent, "选择一个文件", LOAD) {
      override fun setVisible(value: Boolean) {
        super.setVisible(value)
        if (value) {
          onCloseRequest(if (file == null) null else directory + file)
        }
      }
    }
  },
  dispose = FileDialog::dispose
)