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
import state.HomeWindowState
import state.rememberHomeWindowState
import ui.component.RadioGroup
import utils.onlyReturnNumber
import java.awt.FileDialog
import java.awt.Frame

val playSpeed = listOf("0.1倍速", "0.5倍速", "1倍速", "10倍速", "100倍速")

@Composable
@Preview
fun App(
  homeWindowState: HomeWindowState = rememberHomeWindowState(),
) {

  var isFileChooserOpen by remember { mutableStateOf(false) }

  if (isFileChooserOpen) {
    FileDialog(
      onCloseRequest = {
        isFileChooserOpen = false
        homeWindowState.onReadExcel(it)
      }
    )
  }

  LaunchedEffect(homeWindowState.error) {
    homeWindowState.showErrorMsg()
  }

  Scaffold(
    scaffoldState = homeWindowState.scaffoldState
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.surface)
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.wrapContentWidth().widthIn(max = 300.dp)
        ) {

          RadioGroup(
            title = "获取时间方式",
            options = listOf("自动", "手动指定时间列"),
            selectedIndex = if (homeWindowState.isGetTimeAutomatically) 0 else 1,
            onSelectedChanged = { index ->
              homeWindowState.isGetTimeAutomatically = index == 0
            }
          )

          AnimatedVisibility(!homeWindowState.isGetTimeAutomatically) {
            OutlinedTextField(
              value = homeWindowState.dateColIndex,
              onValueChange = { value ->
                homeWindowState.dateColIndex = value.onlyReturnNumber()
              }
            )
          }

          Text("请输入起始行号和终止行号，默认发送全部数据")

          OutlinedTextField(
            value = homeWindowState.startRowIndex,
            onValueChange = { value ->
              homeWindowState.startRowIndex = value.onlyReturnNumber()
            },
            label = { Text("起始行") }
          )
          OutlinedTextField(
            value = homeWindowState.endRowIndex,
            onValueChange = { value ->
              homeWindowState.endRowIndex = value.onlyReturnNumber()
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

          RadioGroup(
            title = "回放速度",
            options = playSpeed,
            selectedIndex = homeWindowState.playSpeedIndex,
            onSelectedChanged = { index ->
              homeWindowState.playSpeedIndex = index
            },
            isHorizontal = false
          )
        }

        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.wrapContentWidth()
        ) {
          RadioGroup(
            title = "目标端口类型",
            options = listOf("Netty", "Kafka"),
            selectedIndex = if (homeWindowState.isNettyTarget) 0 else 1,
            onSelectedChanged = { index ->
              homeWindowState.isNettyTarget = index == 0
            }
          )

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
    MaterialTheme(
      colors = if (isSystemInDarkTheme()) ui.darkColors else ui.lightColors
    ) {
      App()
    }
  }
}
