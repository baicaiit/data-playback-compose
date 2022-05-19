package ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Window

@Composable
fun FileForm(
  includedFileType: List<String> = listOf(),
  onFileSelected: (result: String?) -> Boolean,
) {
  var isFileChooserOpen by remember { mutableStateOf(false) }
  var selectedFilePath by remember { mutableStateOf("") }

  if (isFileChooserOpen) {
    FileDialog(
      includedFileType = includedFileType,
      onFileSelected = { path ->
        isFileChooserOpen = false
        path?.let {
          if (onFileSelected(it)) {
            selectedFilePath = it
          }
        }
      }
    )
  }

  Text("选择需要发送的文件")
  AnimatedVisibility(selectedFilePath.isNotEmpty()) {
    Text(selectedFilePath)
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
  includedFileType: List<String> = listOf(),
  onFileSelected: (result: String?) -> Unit,
) = AwtWindow(
  create = {
    AwtFileDialog(onFileSelected).apply {
      this.setFilenameFilter { _, name ->
        if (includedFileType.isNotEmpty()) {
          includedFileType.any {
            name.endsWith(".${it}")
          }
        } else {
          true
        }
      }
    }
  },
  dispose = Window::dispose
)

class AwtFileDialog(
  private val onCloseRequest: (result: String?) -> Unit,
  parent: Frame? = null,
  title: String = "",
  mode: Int = LOAD,
) : FileDialog(parent, title, mode) {

  override fun setVisible(value: Boolean) {
    super.setVisible(value)
    if (!this.isVisible) {
      onCloseRequest(if (file == null) null else directory + file)
    }
  }
}
