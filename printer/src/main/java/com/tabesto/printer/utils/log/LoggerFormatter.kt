package com.tabesto.printer.utils.log

import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterLogInfo
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.status.PrinterStatus
import timber.log.Timber

class LoggerFormatter(className: String) {
    var printerDataFullLogIsEnabled: Boolean = false
    var printerStatusFullLogIsEnabled: Boolean = false
    private var listLoggerBodyContent: MutableList<LoggerExtraArgument?> = mutableListOf()
    var loggerIsEnabled: Boolean = true
    private val loggerWriter = LoggerWriter(className)

    companion object {
        private const val argPrefix = "x-arg-"
    }

    fun log(
        loggerLevel: LoggerLevel = LoggerLevel.DEBUG,
        printerLogInfo: PrinterLogInfo? = null,
        vararg loggerExtraArg: LoggerExtraArgument?,
        message: String? = null
    ) {
        try {
            addExtraArgListInBody(*loggerExtraArg)

            buildLoggerDefaultBodyContent(
                message,
                printerLogInfo?.printerData,
                printerLogInfo?.printerStatus,
                printerLogInfo?.printerException
            )

            if (loggerIsEnabled) {
                loggerWriter.printLog(loggerLevel, listLoggerBodyContent)
            }

            clearListLoggerBodyContent()
        } catch (exception: Exception) {
            //FIXME crash sometimes when logs comes from Action queue (not thread-safe)
            // this is not critical for now because these logs are not used in production in apps, only for debug
            Timber.d(exception)
        }
    }

    private fun clearListLoggerBodyContent() {
        listLoggerBodyContent.clear()
    }

    private fun addSimpleMessageLogInBody(message: String? = null) {
        message?.let {
            listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}message", message))
        }
    }

    private fun addPrinterDataLogInBody(printerData: PrinterData? = null) {
        printerData?.let {
            printerData.printerAddress?.let { printerAddress ->
                listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-address", printerAddress))
            }
            listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-model", printerData.printerModel.name))
            if (printerDataFullLogIsEnabled) {
                listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-region", printerData.printerRegion.name))
                listLoggerBodyContent.add(
                    LoggerExtraArgument(
                        "${argPrefix}printer-connection-mode",
                        printerData.connectionMode.name
                    )
                )
                listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-printer-type", printerData.printerType.name))
            }
        }
    }

    private fun addPrinterStatusLogInBody(printerStatus: PrinterStatus? = null) {
        printerStatus?.let {
            listLoggerBodyContent.add(
                LoggerExtraArgument(
                    "${argPrefix}printer-connection-status",
                    printerStatus.connectionStatus.name
                )
            )
            if (printerStatusFullLogIsEnabled) {
                listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-online-status", printerStatus.onlineStatus.name))
                listLoggerBodyContent.add(
                    LoggerExtraArgument(
                        "${argPrefix}printer-cover-open-status",
                        printerStatus.coverOpenStatus.name
                    )
                )
                listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-paper-status", printerStatus.paperStatus.name))
                listLoggerBodyContent.add(
                    LoggerExtraArgument(
                        "${argPrefix}printer-paper-feed-status",
                        printerStatus.paperFeedStatus.name
                    )
                )
                listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-drawer-status", printerStatus.drawerStatus.name))
                listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-error-status", printerStatus.errorStatus.name))
            }
        }
    }

    private fun addPrinterErrorLogInBody(printerException: PrinterException? = null) {
        printerException?.let {
            listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-error-message", printerException.message))
            listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-error-code", printerException.printerCode))
            listLoggerBodyContent.add(LoggerExtraArgument("${argPrefix}printer-error-action", printerException.action))
        }
    }

    private fun addExtraArgListInBody(vararg loggerExtraArg: LoggerExtraArgument?) {
        listLoggerBodyContent = loggerExtraArg.filterNotNull().map { arg ->
            LoggerExtraArgument("${argPrefix}${replaceSpaceByMiddleDash(arg.name)}", arg.value)
        }.toMutableList()
    }

    private fun replaceSpaceByMiddleDash(textToModify: String): String {
        return textToModify.replace(" ", "-")
    }

    private fun buildLoggerDefaultBodyContent(
        message: String? = null,
        printerData: PrinterData? = null,
        printerStatus: PrinterStatus? = null,
        printerException: PrinterException? = null
    ) {
        addSimpleMessageLogInBody(message)
        addPrinterDataLogInBody(printerData)
        addPrinterStatusLogInBody(printerStatus)
        addPrinterErrorLogInBody(printerException)
    }
}
