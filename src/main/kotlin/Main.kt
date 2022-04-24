import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
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
import core.NettyTask
import kotlinx.coroutines.launch
import utils.redExcel
import java.awt.FileDialog
import java.awt.Frame
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

val playSpeed = listOf("0.1倍速", "0.5倍速", "1倍速", "5倍速", "10倍速")
val magnification = listOf(10.0, 2.0, 1.0, 0.2, 0.1)

@Composable
@Preview
fun App() {

  var selectedFilePath by remember { mutableStateOf("") }
  var isNettyTarget by remember { mutableStateOf(true) }
  var playSpeedIndex by remember { mutableStateOf(2) }
  var host by remember { mutableStateOf("127.0.0.1") }
  var port by remember { mutableStateOf("9999") }
  var logs by remember { mutableStateOf(listOf<String>()) }
  var data by remember { mutableStateOf<Map<LocalDateTime, List<String>>?>(HashMap()) }

  var isFileChooserOpen by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  if (isFileChooserOpen) {
    FileDialog(onCloseRequest = { filePath ->
      isFileChooserOpen = false
      selectedFilePath = filePath ?: ""
      data = filePath?.redExcel()
      println(data)
    })
  }

  MaterialTheme {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background)
        .padding(16.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text("选择需要发送的文件: $selectedFilePath")
        OutlinedButton(onClick = {
          isFileChooserOpen = true
        }, modifier = Modifier.padding(horizontal = 8.dp)) {
          Text("选择")
        }

        Text("回放速度")
        Row(Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
          playSpeed.forEachIndexed { index, s ->
            RadioButton(selected = playSpeedIndex == index, onClick = { playSpeedIndex = index })
            Text(s)
          }
        }

        Text("目标端口类型")
        Row(Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
          RadioButton(selected = isNettyTarget, onClick = { isNettyTarget = true })
          Text("Netty")
          RadioButton(selected = !isNettyTarget, onClick = { isNettyTarget = false })
          Text("Kafka")
        }

        Text("地址")
        OutlinedTextField(value = host, onValueChange = {
          host = it
        }, label = { Text("例如：127.0.0.1") })

        Text("端口")
        OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("例如：9999") })

        Button(onClick = {
          data?.let {
            val firstTaskTime = it.keys.first()
            val baseTime = LocalDateTime.now()

            val tasks = data!!.map { entry ->
              val durationLong = (entry.key.toEpochSecond(ZoneOffset.UTC) - firstTaskTime.toEpochSecond(ZoneOffset.UTC))
              val durationWithSpeed = Duration.ofSeconds((durationLong * magnification[playSpeedIndex]).toLong())
              NettyTask(baseTime.plus(durationWithSpeed), entry.value, host, port.toInt())
            }

            scope.launch {
              tasks.forEach { task ->
                task.run {
                  logs = logs + "${task.time}:$it"
                }
              }
            }
          }
        }, enabled = !data.isNullOrEmpty() && host.isNotEmpty() && port.isNotEmpty()) {
          Text("开始发送")
        }

        Divider(Modifier.height(2.dp))

        Text("发送日志")
        LazyColumn(modifier = Modifier.height(200.dp)) {
          items(logs) { log ->
            Text(log)
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
  val windowState = rememberWindowState(width = 600.dp, height = 800.dp)
  Window(
    title = "数据回放系统",
    state = windowState,
    resizable = false,
    onCloseRequest = ::exitApplication
  ) {
    App()
  }
}
