import androidx.compose.animation.AnimatedVisibility
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
import core.KafkaTask
import core.NettyTask
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException
import utils.NoValidTimeException
import utils.TimeIndexNotValidException
import utils.TimeIndexOutOfIndexException
import utils.redExcel
import java.awt.FileDialog
import java.awt.Frame
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

val playSpeed = listOf("0.1倍速", "0.5倍速", "1倍速", "5倍速", "10倍速", "1000倍速")
val magnification = listOf(10.0, 2.0, 1.0, 0.2, 0.1, 0.001)

enum class JobStatus {
  NEW,
  ACTIVE,
  CANCELLING,
  CANCELLED,
  COMPLETED
}

fun Job.status(): JobStatus {
  println("job status: isActive $isActive isCompleted $isCompleted isCancelled $isCancelled")
  return when {
    !isActive && isCompleted && !isCancelled -> JobStatus.COMPLETED
    !isActive && isCompleted && isCancelled -> JobStatus.CANCELLED
    !isActive && !isCompleted && isCancelled -> JobStatus.CANCELLING
    isActive && !isCompleted && isCancelled -> JobStatus.ACTIVE
    else -> JobStatus.NEW
  }
}

@OptIn(InternalCoroutinesApi::class)
@Composable
@Preview
fun App() {

  var error by remember { mutableStateOf("") }
  var selectedFilePath by remember { mutableStateOf("") }
  var isGetTimeAutomatically by remember { mutableStateOf(true) }
  var dateIndex by remember { mutableStateOf("") }
  var isNettyTarget by remember { mutableStateOf(true) }
  var playSpeedIndex by remember { mutableStateOf(2) }
  var host by remember { mutableStateOf("127.0.0.1") }
  var port by remember { mutableStateOf("9999") }
  var topic by remember { mutableStateOf("Topic1") }
  var logs by remember { mutableStateOf(listOf<String>()) }
  var data by remember { mutableStateOf<Map<LocalDateTime, List<String>>?>(HashMap()) }
  var job by remember { mutableStateOf<Job?>(null) }
  var isStartButtonEnabled by remember { mutableStateOf(true) }
  val scaffoldState = rememberScaffoldState()
  var isFileChooserOpen by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  if (isFileChooserOpen) {
    FileDialog(onCloseRequest = { filePath ->
      isFileChooserOpen = false
      try {
        data = filePath?.redExcel(dateIndex = if (isGetTimeAutomatically) -1 else dateIndex.toInt())
        println(data)
        selectedFilePath = filePath ?: ""
        error = ""
      } catch (e: Exception) {
        data = null
        selectedFilePath = ""
        error = when (e) {
          is NoValidTimeException -> e.msg
          is NotOfficeXmlFileException -> {
            "所选文件并非有效 excel 文件"
          }
          is TimeIndexOutOfIndexException -> e.msg
          is TimeIndexNotValidException -> e.msg
          is NullPointerException -> "请检查上传的文件，确保不含空值"
          else -> {
            e.printStackTrace()
            "未知错误"
          }
        }
      }
    })
  }

  if (error.isNotEmpty()) {
    LaunchedEffect(scaffoldState.snackbarHostState) {
      scaffoldState.snackbarHostState.showSnackbar(error)
    }
  }

  MaterialTheme {
    Scaffold(scaffoldState = scaffoldState) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colors.background)
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.wrapContentWidth()
          ) {

            Text("获取时间方式")
            Row(Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
              RadioButton(selected = isGetTimeAutomatically, onClick = { isGetTimeAutomatically = true })
              Text("自动")
              RadioButton(selected = !isGetTimeAutomatically, onClick = { isGetTimeAutomatically = false })
              Text("手动指定时间列")
            }
            AnimatedVisibility(!isGetTimeAutomatically) {
              OutlinedTextField(
                value = dateIndex,
                onValueChange = { value ->
                  if (value.length <= 2) {
                    dateIndex = value.filter { it.isDigit() }
                  }
                }
              )
            }

            Text("选择需要发送的文件")
            AnimatedVisibility(selectedFilePath.isNotEmpty()) {
              Text(selectedFilePath, modifier = Modifier.widthIn(max = 300.dp))
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
                  RadioButton(selected = playSpeedIndex == index, onClick = { playSpeedIndex = index })
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

            if (!isNettyTarget) {
              Text("Topic")
              OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("例如：Topic1") })
            }

            Row {
              Button(
                onClick = {
                  isStartButtonEnabled = false
                  data?.let {
                    val firstTaskTime = it.keys.first()
                    val baseTime = LocalDateTime.now()

                    val tasks = data!!.map { entry ->
                      val durationLong =
                        (entry.key.toEpochSecond(ZoneOffset.UTC) - firstTaskTime.toEpochSecond(ZoneOffset.UTC))
                      val durationWithSpeed =
                        Duration.ofSeconds((durationLong * magnification[playSpeedIndex]).toLong())
                      if (isNettyTarget) {
                        NettyTask(baseTime.plus(durationWithSpeed), entry.value, host, port.toInt())
                      } else {
                        KafkaTask(baseTime.plus(durationWithSpeed), entry.value, host, port.toInt(), topic)
                      }
                    }

                    job = scope.launch {
                      tasks.forEach { task ->
                        task.run { content ->
                          logs = logs + "${task.time}:$content"
                        }
                      }
                    }
                    job?.let { job ->
                      job.invokeOnCompletion(true) {
                        if (
                          job.status() == JobStatus.COMPLETED ||
                          job.status() == JobStatus.CANCELLED ||
                          job.status() == JobStatus.CANCELLING
                        ) {
                          isStartButtonEnabled = true
                        }
                      }
                    }
                  }
                },
                enabled = isStartButtonEnabled
              ) {
                Text("开始发送")
              }
              AnimatedVisibility(!isStartButtonEnabled) {
                OutlinedButton(
                  onClick = {
                    job?.cancel()
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
              items(logs) { log ->
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
  val windowState = rememberWindowState(height = 650.dp)
  Window(
    title = "数据回放系统",
    state = windowState,
    onCloseRequest = ::exitApplication
  ) {
    App()
  }
}
