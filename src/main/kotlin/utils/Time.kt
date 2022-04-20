package utils

import java.time.Duration
import java.time.LocalDateTime

fun calculateDelayTime(start: LocalDateTime, end: LocalDateTime): Long {
  return Duration.between(start, end).toMillis()
}
