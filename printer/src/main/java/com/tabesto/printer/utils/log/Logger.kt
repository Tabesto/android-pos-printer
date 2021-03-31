package com.tabesto.printer.utils.log

import com.tabesto.printer.model.PrinterLogInfo

@Suppress("unused")
class Logger(className: String) {

    private val loggerFormatter: LoggerFormatter = LoggerFormatter(className)

    fun i(message: String? = null) {
        loggerFormatter.log(LoggerLevel.INFO, message = message)
    }

    fun i(vararg loggerExtraArg: LoggerExtraArgument = emptyArray()) {
        loggerFormatter.log(LoggerLevel.INFO, loggerExtraArg = *loggerExtraArg)
    }

    fun i(
        printerLogInfo: PrinterLogInfo? = null,
        vararg loggerExtraArg: LoggerExtraArgument = emptyArray()
    ) {
        loggerFormatter.log(LoggerLevel.INFO, printerLogInfo, loggerExtraArg = *loggerExtraArg)
    }

    fun d(message: String? = null) {
        loggerFormatter.log(LoggerLevel.DEBUG, message = message)
    }

    fun d(vararg loggerExtraArg: LoggerExtraArgument = emptyArray()) {
        loggerFormatter.log(LoggerLevel.DEBUG, loggerExtraArg = *loggerExtraArg)
    }

    fun d(
        printerLogInfo: PrinterLogInfo? = null,
        vararg loggerExtraArg: LoggerExtraArgument = emptyArray()
    ) {
        loggerFormatter.log(LoggerLevel.DEBUG, printerLogInfo, loggerExtraArg = *loggerExtraArg)
    }

    fun w(message: String? = null) {
        loggerFormatter.log(LoggerLevel.WARNING, message = message)
    }

    fun w(vararg loggerExtraArg: LoggerExtraArgument = emptyArray()) {
        loggerFormatter.log(LoggerLevel.WARNING, loggerExtraArg = *loggerExtraArg)
    }

    fun w(
        printerLogInfo: PrinterLogInfo? = null,
        vararg loggerExtraArg: LoggerExtraArgument = emptyArray()
    ) {
        loggerFormatter.log(LoggerLevel.WARNING, printerLogInfo, loggerExtraArg = *loggerExtraArg)
    }

    fun e(message: String? = null) {
        loggerFormatter.log(LoggerLevel.ERROR, message = message)
    }

    fun e(vararg loggerExtraArg: LoggerExtraArgument = emptyArray()) {
        loggerFormatter.log(LoggerLevel.ERROR, loggerExtraArg = *loggerExtraArg)
    }

    fun e(
        printerLogInfo: PrinterLogInfo? = null,
        vararg loggerExtraArg: LoggerExtraArgument = emptyArray()
    ) {
        loggerFormatter.log(
            LoggerLevel.ERROR,
            printerLogInfo,
            loggerExtraArg = *loggerExtraArg
        )
    }

    fun disablePrinterDataFullInLogs() {
        loggerFormatter.printerDataFullLogIsEnabled = false
    }

    fun disablePrinterStatusFullInLogs() {
        loggerFormatter.printerStatusFullLogIsEnabled = false
    }

    fun enableLogger(isEnabled: Boolean = true) {
        loggerFormatter.loggerIsEnabled = isEnabled
    }

    fun enablePrinterDataFullInLogs() {
        loggerFormatter.printerDataFullLogIsEnabled = true
    }

    fun enablePrinterStatusFullInLogs() {
        loggerFormatter.printerStatusFullLogIsEnabled = true
    }
}
