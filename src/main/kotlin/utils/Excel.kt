package utils

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.time.LocalDateTime
import java.util.*

/**
 * 依据文件路径录入excel数据，读取失败时返回null
 */
fun String.redExcel(): Map<LocalDateTime, List<String>>? {
  val map: MutableMap<LocalDateTime, List<String>> = HashMap()
  try {
    val xssfWorkbook = XSSFWorkbook(FileInputStream(this))
    val sheetNum = xssfWorkbook.numberOfSheets
    for (i in 0 until sheetNum) {
      val sheet = xssfWorkbook.getSheetAt(i)
      val maxRow = sheet.lastRowNum
      for (row in 1..maxRow) {
        var dateFlag = false
        val content: MutableList<String> = ArrayList()
        val maxCol = sheet.getRow(row).lastCellNum.toInt()
        for (col in 0 until maxCol) {
          val cell = sheet.getRow(row).getCell(col)
          if (!dateFlag && cell.localDateTimeCellValue != null) {
            map[cell.localDateTimeCellValue] = content
            dateFlag = true
          } else {
            content.add(cell.toString())
          }
        }
      }
    }
  } catch (e: Exception) {
    e.printStackTrace()
    return null
  }
  return map.toSortedMap()
}