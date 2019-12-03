package org.ndp.port_scan

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Log {
    private const val ANSI_RESET = "\u001B[0m"
    private const val ANSI_RED = "\u001B[31m"
    private const val ANSI_GREEN = "\u001B[32m"
    private const val ANSI_YELLOW = "\u001B[33m"
    private const val ANSI_BLUE = "\u001B[34m"
    private const val DEBUG = "$ANSI_GREEN[DEBUG]$ANSI_RESET"
    private const val INFO = "$ANSI_BLUE[INFO]$ANSI_RESET"
    private const val WARN = "$ANSI_YELLOW[WARN]$ANSI_RESET"
    private const val ERROR = "$ANSI_RED[ERROR]$ANSI_RESET"

    private fun getDatetime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    fun debug(content: String) {
        println("[${getDatetime()}]$DEBUG $content")
    }

    fun info(content: String) {
        println("[${getDatetime()}]$INFO $content")
    }

    fun warn(content: String) {
        println("[${getDatetime()}]$WARN $content")
    }

    fun error(content: String) {
        println("[${getDatetime()}]$ERROR $content")
    }
}