package core

import java.time.LocalDateTime

abstract class Task constructor(val time: LocalDateTime, val content: List<String>) {
  open suspend fun run(onfinish: (content: String) -> Unit) {}
}