package state

import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import com.github.doyaaaaaken.kotlincsv.util.CSVParseFormatException
import core.KafkaTask
import core.NettyTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

val magnification = listOf(10.0, 2.0, 1.0, 0.1, 0.01)

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

class HomeWindowState(
  val scaffoldState: ScaffoldState,
  private val scope: CoroutineScope,
) {
  var isStartButtonEnabled by mutableStateOf(true)
  var error by mutableStateOf("")
  var isGetTimeAutomatically by mutableStateOf(true)
  var dateColIndex by mutableStateOf("")
  var startRowIndex by mutableStateOf("")
  var endRowIndex by mutableStateOf("")
  var data by mutableStateOf<Map<LocalDateTime, List<String>>?>(null)
  var playSpeedIndex by mutableStateOf(2)
  var isNettyTarget by mutableStateOf(true)
  var host by mutableStateOf("127.0.0.1")
  var port by mutableStateOf("9999")
  var topic by mutableStateOf("Topic1")
  var logs by mutableStateOf(listOf<String>())
  var job by mutableStateOf<Job?>(null)

  fun getSubmitState(): Boolean {
    if (data == null) return false
    if (host.isEmpty()) return false
    if (port.isEmpty()) return false
    if (!isNettyTarget && topic.isEmpty()) return false
    return true
  }

  fun onReadExcel(filePath: String?): Boolean {
    try {
      data = filePath?.readExcel(
        dateColIndex = if (isGetTimeAutomatically) -1 else dateColIndex.toInt() - 1,
        startRowIndex = if (startRowIndex.isNotEmpty()) startRowIndex.toInt() - 1 else 0,
        endRowIndex = if (endRowIndex.isNotEmpty()) endRowIndex.toInt() - 1 else Int.MAX_VALUE
      )
      println(data)
      error = ""
      return true
    } catch (e: Exception) {
      data = null
      error = when (e) {
        is NoValidTimeException -> e.msg
        is CSVParseFormatException -> {
          "所选文件并非有效 excel 或 csv 文件"
        }
        is TimeIndexOutOfIndexException -> e.msg
        is TimeIndexNotValidException -> e.msg
        is NullPointerException -> "请检查上传的文件，确保不含空值"
        is CSVAutoDateNotSupportException -> e.msg
        else -> {
          e.printStackTrace()
          "未知错误 ${e.message}"
        }
      }
      return false
    }
  }

  suspend fun showErrorMsg() {
    if (error.isNotEmpty()) {
      scaffoldState.snackbarHostState.showSnackbar(error)
    }
  }

  fun onDataSendCancel() {
    job?.cancel()
  }

  @OptIn(InternalCoroutinesApi::class)
  fun onDataSend() {
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
  }
}

@Composable
fun rememberHomeWindowState(
  scaffoldState: ScaffoldState = rememberScaffoldState(),
  scope: CoroutineScope = rememberCoroutineScope(),
) = remember {
  HomeWindowState(
    scaffoldState = scaffoldState,
    scope = scope
  )
}
