package com.tabesto.printer.utils.log

import timber.log.Timber

class LoggerWriter(private val className: String) {

    companion object {
        private const val lineSeparator = "******************************************************************************************"
    }

    private fun printLoggerSeparator(loggerLevel: LoggerLevel) {
        when (loggerLevel) {
            LoggerLevel.DEBUG -> {
                Timber.tag(className)
                Timber.d(lineSeparator)
            }
            LoggerLevel.INFO -> {
                Timber.tag(className)
                Timber.i(lineSeparator)
            }
            LoggerLevel.ERROR -> {
                Timber.tag(className)
                Timber.e(lineSeparator)
            }
            LoggerLevel.WARNING -> {
                Timber.tag(className)
                Timber.w(lineSeparator)
            }
        }
    }

    private fun printLoggerBody(loggerLevel: LoggerLevel, listLoggerBodyContent: List<LoggerExtraArgument>) {
        when (loggerLevel) {
            LoggerLevel.DEBUG -> for (log in listLoggerBodyContent) {
                Timber.tag(className)
                Timber.d("* ${log.name} : ${log.value} ")
            }
            LoggerLevel.INFO -> for (log in listLoggerBodyContent) {
                Timber.tag(className)
                Timber.i("* ${log.name} : ${log.value} ")
            }
            LoggerLevel.ERROR -> for (log in listLoggerBodyContent) {
                Timber.tag(className)
                Timber.e("* ${log.name} : ${log.value} ")
            }
            LoggerLevel.WARNING -> for (log in listLoggerBodyContent) {
                Timber.tag(className)
                Timber.w("* ${log.name} : ${log.value} ")
            }
        }
    }

    fun printLog(loggerLevel: LoggerLevel, listLoggerBodyContent: List<LoggerExtraArgument>) {
        printLoggerSeparator(loggerLevel)

        printLoggerBody(loggerLevel, listLoggerBodyContent)

        printLoggerSeparator(loggerLevel)
    }
}
