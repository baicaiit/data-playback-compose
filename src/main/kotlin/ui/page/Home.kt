package ui.page

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.component.FileForm
import ui.component.RadioGroup
import utils.onlyReturnNumber

@Composable
fun HomePage(
  homeWindowState: HomeWindowState,
) {

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

          TimeForm(homeWindowState)
          RowIndexForm(homeWindowState)
          ColIndexForm(homeWindowState)
          FileForm(
            includedFileType = listOf("csv", "xlsx"),
            onFileSelected = { path ->
              path?.let {
                homeWindowState.readExcel(it)
              } ?: false
            }
          )

          Text("回放间隔倍率")
          OutlinedTextField(
            value = homeWindowState.playSpeed,
            onValueChange = { value ->
              homeWindowState.playSpeed = value
            },
          )
        }

        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.wrapContentWidth().widthIn(max = 400.dp)
        ) {
          TargetForm(homeWindowState)

          OperatingArea(homeWindowState)

          Divider(Modifier.height(2.dp).width(400.dp))

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
fun OperatingArea(homeWindowState: HomeWindowState) {
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
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 8.dp)
      ) {
        OutlinedButton(
          onClick = {
            if (homeWindowState.isPaused) {
              homeWindowState.resume()
            } else {
              homeWindowState.pause()
            }
          },
        ) {
          Text(if (homeWindowState.isPaused) "恢复" else "暂停")
        }

        OutlinedButton(
          onClick = {
            homeWindowState.onDataSendCancel()
          },
        ) {
          Text("取消")
        }
      }
    }
  }
}

@Composable
fun TargetForm(homeWindowState: HomeWindowState) {
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

  AnimatedVisibility(
    !homeWindowState.isNettyTarget,
    enter = slideInVertically() + expandVertically(),
    exit = slideOutVertically() + shrinkVertically(),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("Topic")
      OutlinedTextField(
        value = homeWindowState.topic,
        onValueChange = { homeWindowState.topic = it },
        label = { Text("例如：Topic1") }
      )
    }
  }
}

@Composable
fun ColIndexForm(homeWindowState: HomeWindowState) {
  Text("请输入需要发送的数据列，默认发送除时间列外数据")

  OutlinedTextField(
    value = homeWindowState.selectedColIndex,
    onValueChange = { value ->
      homeWindowState.selectedColIndex = value
    },
    label = { Text("选择的列号，以逗号分割") }
  )
}

@Composable
fun RowIndexForm(homeWindowState: HomeWindowState) {
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
}

@Composable
fun TimeForm(homeWindowState: HomeWindowState) {
  RadioGroup(
    title = "获取时间方式",
    options = listOf("自动", "手动指定时间列"),
    selectedIndex = if (homeWindowState.isGetTimeAutomatically) 0 else 1,
    onSelectedChanged = { index ->
      homeWindowState.isGetTimeAutomatically = index == 0
    }
  )

  AnimatedVisibility(
    !homeWindowState.isGetTimeAutomatically,
    enter = slideInVertically() + expandVertically(),
    exit = slideOutVertically() + shrinkVertically(),
  ) {
    OutlinedTextField(
      value = homeWindowState.dateColIndex,
      onValueChange = { value ->
        homeWindowState.dateColIndex = value.onlyReturnNumber()
      }
    )
  }

  OutlinedTextField(
    value = homeWindowState.dateTimeFormatterString,
    onValueChange = { value ->
      homeWindowState.dateTimeFormatterString = value
    },
    label = { Text("时间日期格式规则") }
  )
}
