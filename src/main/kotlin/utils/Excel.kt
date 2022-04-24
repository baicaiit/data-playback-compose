package utils

import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.time.LocalDateTime

class NoValidTimeException : Exception()

/**
 * 依据文件路径录入excel数据，读取失败时返回null
 */
fun String.redExcel(): Map<LocalDateTime, List<String>> {
  val map = HashMap<LocalDateTime, List<String>>()
  val xssfWorkbook = XSSFWorkbook(FileInputStream(this))
  val sheetNum = xssfWorkbook.numberOfSheets
  for (i in 0 until sheetNum) {
    val sheet = xssfWorkbook.getSheetAt(i)
    val maxRow = sheet.lastRowNum
    for (row in 1..maxRow) {
      var dateFlag = false
      val content = ArrayList<String>()
      val maxCol = sheet.getRow(row).lastCellNum.toInt()
      for (col in 0 until maxCol) {
        val cell = sheet.getRow(row).getCell(col)
        if (!dateFlag && DateUtil.isCellDateFormatted(cell)) {
          map[cell.localDateTimeCellValue] = content
          dateFlag = true
        } else {
          content.add(cell.toString())
        }
      }
      if (!dateFlag) {
        throw NoValidTimeException()
      }
    }
  }
  return map.toSortedMap()
}