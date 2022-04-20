package utils

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.util.*

/**
 * 依据文件路径录入excel数据，读取失败时返回null
 */
fun String.redExcel(): Map<Date, List<String>>? {
  val map: MutableMap<Date, List<String>> = HashMap()
  try {
    val xssfWorkbook = XSSFWorkbook(FileInputStream(this))
    val sheetNum = xssfWorkbook.numberOfSheets
    for (i in 0 until sheetNum) {
      val sheet = xssfWorkbook.getSheetAt(i)
      val maxRow = sheet.lastRowNum
      for (row in 1..maxRow) {
        val date = sheet.getRow(row).getCell(0).dateCellValue
        val content: MutableList<String> = ArrayList()
        val maxCol = sheet.getRow(row).lastCellNum.toInt()
        for (col in 0 until maxCol) {
          val cell = sheet.getRow(row).getCell(col)
          if (col != 0) {
            content.add(cell.toString())
          }
        }
        map[date] = content
      }
    }
  } catch (e: Exception) {
    e.printStackTrace()
    return null
  }
  return map.toSortedMap()
}