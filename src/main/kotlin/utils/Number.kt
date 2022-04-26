package utils

fun String.onlyReturnNumber(): String {
  return this.filter { it.isDigit() }
}