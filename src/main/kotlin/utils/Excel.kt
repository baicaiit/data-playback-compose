package utils

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.lang.Integer.min
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NoValidTimeException(val msg: String) : Exception(msg)
class TimeIndexOutOfIndexException(val msg: String) : Exception(msg)
class TimeIndexNotValidException(val msg: String) : Exception(msg)
class CSVAutoDateNotSupportException(val msg: String) : Exception(msg)

/**
 * 依据文件路径录入excel数据，读取失败时返回null
 */
fun String.readExcel(
  dateColIndex: Int = -1,
  startRowIndex: Int = 0,
  endRowIndex: Int = Int.MAX_VALUE,
): Map<LocalDateTime, List<String>> {
  val map = HashMap<LocalDateTime, List<String>>()
  try {
    val xssfWorkbook = XSSFWorkbook(FileInputStream(this))
    val sheetNum = xssfWorkbook.numberOfSheets
    for (i in 0 until sheetNum) {
      val sheet = xssfWorkbook.getSheetAt(i)
      val maxRow = sheet.lastRowNum
      println("表格共有$maxRow 行")
      for (row in startRowIndex..min(maxRow, endRowIndex)) {
        var dateFlag = false
        val content = ArrayList<String>()
        val maxCol = sheet.getRow(row).lastCellNum.toInt()
        println("表格共有$maxCol 列")
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
            if (col != dateColIndex)
              content.add(cell.toString())
          }
        }
        if (!dateFlag) {
          throw NoValidTimeException("所选文件无有效的时间列")
        }
      }
    }
  } catch (e: NotOfficeXmlFileException) {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val rows = csvReader().readAll(FileInputStream(this))
    if (dateColIndex == -1) {
      throw CSVAutoDateNotSupportException("csv 文件不支持自动获取时间列")
    }

    println("${rows.size} $startRowIndex $endRowIndex")
    for (row in startRowIndex..min(rows.size, endRowIndex)) {
      val content = ArrayList<String>()
      val maxCol = rows[row].size
      println("表格共有$maxCol 列")
      if (dateColIndex >= maxCol) {
        throw TimeIndexOutOfIndexException("所选时间列号不存在")
      }
      try {
        val dateTime = LocalDateTime.parse(rows[row][dateColIndex].trim(), dateTimeFormatter)
        map[dateTime] = content
      } catch (e: DateTimeException) {
        throw TimeIndexNotValidException("所选时间列数据并非时间格式")
      }
      for (col in 0 until maxCol) {
        if (col != dateColIndex)
          content.add(rows[row][col])
      }
    }
  }
  return map.toSortedMap()
}