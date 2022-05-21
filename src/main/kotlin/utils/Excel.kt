package utils

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.lang.Integer.min
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NoValidTimeException(val msg: String) : Exception(msg)
class TimeIndexOutOfIndexException(val msg: String) : Exception(msg)
class TimeIndexNotValidException(val msg: String) : Exception(msg)
class CSVAutoDateNotSupportException(val msg: String) : Exception(msg)

private enum class FileType {
  CSV,
  EXCEL,
  OTHER
}

fun readExcelOrCsv(
  path: String,
  dateColIndex: Int = -1,
  dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
  selectedColIndex: List<Int> = listOf(),
  startRowIndex: Int = 0,
  endRowIndex: Int = Int.MAX_VALUE,
): Map<LocalDateTime, List<String>> {

  require(path.trim().length == path.length) { "路径首尾存在空白字符" }

  val fileType = when {
    path.endsWith(".csv") -> FileType.CSV
    path.endsWith(".xlsx") -> FileType.EXCEL
    else -> FileType.OTHER
  }
  check(fileType == FileType.EXCEL || fileType == FileType.CSV) { "$path 并非 csv 或者 excel 文件" }

  check(File(path).isFile) { "$path 文件不存在" }

  return if (fileType == FileType.EXCEL) {
    readExcel(path, dateColIndex, selectedColIndex, startRowIndex, endRowIndex)
  } else {
    readCsv(path, dateColIndex, dateTimeFormatter, selectedColIndex, startRowIndex, endRowIndex)
  }
}

private fun readExcel(
  path: String,
  dateColIndex: Int,
  selectedColIndex: List<Int>,
  startRowIndex: Int,
  endRowIndex: Int,
): Map<LocalDateTime, List<String>> {
  val map = HashMap<LocalDateTime, List<String>>()
  val xssfWorkbook = XSSFWorkbook(path)
  val sheetNum = xssfWorkbook.numberOfSheets
  for (i in 0 until sheetNum) {
    val sheet = xssfWorkbook.getSheetAt(i)
    val maxRow = sheet.lastRowNum

    for (row in startRowIndex..min(maxRow, endRowIndex)) {
      var dateFlag = false
      val content = ArrayList<String>()
      val maxCol = sheet.getRow(row).lastCellNum.toInt()

      if (dateColIndex >= maxCol) {
        throw TimeIndexOutOfIndexException("所选时间列号不存在")
      }
      if (dateColIndex != -1) {
        val dateTime = sheet.getRow(row).getCell(dateColIndex).localDateTimeCellValue
        if (dateTime.year == 1900) {
          throw TimeIndexNotValidException("所选时间列数据并非时间格式")
        }
        map[dateTime] = content
        dateFlag = true
      }
      for (col in 0 until maxCol) {
        val cell = sheet.getRow(row).getCell(col)
        if (!dateFlag && DateUtil.isCellDateFormatted(cell)) {
          map[cell.localDateTimeCellValue] = content
          dateFlag = true
        } else {
          if (col != dateColIndex) {
            if (selectedColIndex.isEmpty() || (col in selectedColIndex)) {
              content.add(cell.toString().trim())
            }
          }
        }
      }
      if (!dateFlag) {
        throw NoValidTimeException("所选文件无有效的时间列")
      }
    }
  }
  return map.toSortedMap()
}

private fun readCsv(
  path: String,
  dateColIndex: Int,
  dateTimeFormatter: DateTimeFormatter,
  selectedColIndex: List<Int>,
  startRowIndex: Int,
  endRowIndex: Int,
): Map<LocalDateTime, List<String>> {

  require(dateColIndex >= 0) { "csv 文件不支持自动获取时间列" }

  val map = HashMap<LocalDateTime, List<String>>()

  csvReader().open(path) {
    readAllAsSequence().forEachIndexed { cur, line ->
      check(dateColIndex < line.size) { "所选时间列号 $dateColIndex 不存在" }

      if (cur > endRowIndex) {
        return@open
      }

      if (cur in startRowIndex..endRowIndex) {
        try {
          val dateTime = LocalDateTime.parse(line[dateColIndex].trim(), dateTimeFormatter)
          map[dateTime] = line.filterIndexed { index, _ ->
            if (selectedColIndex.isEmpty() || (index in selectedColIndex)) {
              index != dateColIndex
            } else {
              false
            }
          }.map { it.trim() }
        } catch (e: DateTimeException) {
          throw TimeIndexNotValidException("所选时间列数据并非时间格式")
        }
      }
    }
  }

  return map.toSortedMap()
}