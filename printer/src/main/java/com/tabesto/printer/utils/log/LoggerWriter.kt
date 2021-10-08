package com.tabesto.printer.utils.log

import timber.log.Timber

class LoggerWriter(private val className: String) {

    companion object {
        private const val lineSeparator = "******************************************************************************************"
    }

    private fun printLoggerSeparator(loggerLevel: LoggerLevel) {
        Timber.tag(className)
        when (loggerLevel) {
            LoggerLevel.DEBUG -> {
                Timber.d(lineSeparator)
            }
            LoggerLevel.INFO -> {
                Timber.i(lineSeparator)
            }
            LoggerLevel.ERROR -> {
                Timber.e(lineSeparator)
            }
            LoggerLevel.WARNING -> {
                Timber.w(lineSeparator)
            }
        }
    }

    private fun printLoggerBody(loggerLevel: LoggerLevel, listLoggerBodyContent: List<LoggerExtraArgument?>) {
        for (log in ArrayList(listLoggerBodyContent)) {
            Timber.tag(className)
            val message = "* ${log?.name} : ${log?.value}"
            when (loggerLevel) {
                LoggerLevel.DEBUG -> Timber.d(message)
                LoggerLevel.INFO -> Timber.i(message)
                LoggerLevel.ERROR -> Timber.e(message)
                LoggerLevel.WARNING -> Timber.w(message)
            }
        }
    }

    fun printLog(loggerLevel: LoggerLevel, listLoggerBodyContent: List<LoggerExtraArgument?>) {
        printLoggerSeparator(loggerLevel)
        printLoggerBody(loggerLevel, listLoggerBodyContent)
        printLoggerSeparator(loggerLevel)
    }
}
