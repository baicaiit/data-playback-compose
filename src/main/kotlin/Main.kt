import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import state.rememberHomeWindowState
import java.awt.FileDialog
import java.awt.Frame

val playSpeed = listOf("0.1倍速", "0.5倍速", "1倍速", "10倍速", "100倍速")

@Composable
@Preview
fun App() {

  val homeWindowState = rememberHomeWindowState()

  var isFileChooserOpen by remember { mutableStateOf(false) }

  if (isFileChooserOpen) {
    FileDialog(onCloseRequest = { filePath ->
      isFileChooserOpen = false
      homeWindowState.onReadExcel(filePath)
    })
  }

  LaunchedEffect(homeWindowState.scaffoldState.snackbarHostState) {
    homeWindowState.showErrorMsg()
  }

  MaterialTheme(
    colors = if (isSystemInDarkTheme()) ui.darkColors else ui.lightColors
  ) {
    Scaffold(scaffoldState = homeWindowState.scaffoldState) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colors.surface)
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.wrapContentWidth().widthIn(max = 300.dp)
          ) {

            Text("获取时间方式")
            Row(Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = homeWindowState.isGetTimeAutomatically,
                onClick = { homeWindowState.isGetTimeAutomatically = true }
              )
              Text("自动")
              RadioButton(
                selected = !homeWindowState.isGetTimeAutomatically,
                onClick = { homeWindowState.isGetTimeAutomatically = false }
              )
              Text("手动指定时间列")
            }
            AnimatedVisibility(!homeWindowState.isGetTimeAutomatically) {
              OutlinedTextField(
                value = homeWindowState.dateColIndex,
                onValueChange = { value ->
                  if (value.length <= 2) {
                    homeWindowState.dateColIndex = value.filter { it.isDigit() }
                  }
                }
              )
            }

            Text("请输入起始行号和终止行号，默认发送全部数据")

            OutlinedTextField(
              value = homeWindowState.startRowIndex,
              onValueChange = { value ->
                if (value.length <= 2) {
                  homeWindowState.startRowIndex = value.filter { it.isDigit() }
                }
              },
              label = { Text("起始行") }
            )
            OutlinedTextField(
              value = homeWindowState.endRowIndex,
              onValueChange = { value ->
                if (value.length <= 2) {
                  homeWindowState.endRowIndex = value.filter { it.isDigit() }
                }
              },
              label = { Text("终止行") }
            )

            Text("选择需要发送的文件")
            AnimatedVisibility(homeWindowState.selectedFilePath.isNotEmpty()) {
              Text(homeWindowState.selectedFilePath)
            }
            OutlinedButton(onClick = {
              isFileChooserOpen = true
            }, modifier = Modifier.padding(horizontal = 8.dp)) {
              Text("选择")
            }

            Text("回放速度")
            Column(Modifier.selectableGroup()) {
              playSpeed.forEachIndexed { index, s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                  RadioButton(
                    selected = homeWindowState.playSpeedIndex == index,
                    onClick = { homeWindowState.playSpeedIndex = index }
                  )
                  Text(s)
                }
              }
            }
          }

          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.wrapContentWidth()
          ) {
            Text("目标端口类型")
            Row(Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = homeWindowState.isNettyTarget,
                onClick = { homeWindowState.isNettyTarget = true }
              )
              Text("Netty")
              RadioButton(
                selected = !homeWindowState.isNettyTarget,
                onClick = { homeWindowState.isNettyTarget = false }
              )
              Text("Kafka")
            }

            Text("地址")
            OutlinedTextField(
              value = homeWindowState.host,
              onValueChange = {
                homeWindowState.host = it
              },
              label = { Text("例如：127.0.0.1") }
            )

            Text("端口")
            OutlinedTextField(
              value = homeWindowState.port,
              onValueChange = { homeWindowState.port = it },
              label = { Text("例如：9999") }
            )

            if (!homeWindowState.isNettyTarget) {
              Text("Topic")
              OutlinedTextField(value = homeWindowState.topic,
                onValueChange = { homeWindowState.topic = it },
                label = { Text("例如：Topic1") })
            }

            Row {
              Button(
                onClick = {
                  homeWindowState.onDataSend()
                },
                enabled = homeWindowState.isStartButtonEnabled && homeWindowState.getSubmitState()
              ) {
                Text("开始发送")
              }
              AnimatedVisibility(!homeWindowState.isStartButtonEnabled) {
                OutlinedButton(
                  onClick = {
                    homeWindowState.onDataSendCancel()
                  },
                  modifier = Modifier.padding(start = 4.dp)
                ) {
                  Text("取消")
                }
              }
            }

            Divider(Modifier.height(2.dp).width(300.dp))

            Text("发送日志")
            LazyColumn(modifier = Modifier.height(200.dp)) {
              items(homeWindowState.logs) { log ->
                Text(log)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun FileDialog(
  parent: Frame? = null,
  onCloseRequest: (result: String?) -> Unit,
) = AwtWindow(create = {
  object : FileDialog(parent, "选择一个文件", LOAD) {
    override fun setVisible(value: Boolean) {
      super.setVisible(value)
      if (value) {
        onCloseRequest(if (file == null) null else directory + file)
      }
    }
  }
}, dispose = FileDialog::dispose)

fun main() = application {
  val windowState = rememberWindowState(width = 850.dp, height = 850.dp)
  Window(
    title = "数据回放系统",
    state = windowState,
    onCloseRequest = ::exitApplication
  ) {
    App()
  }
}
