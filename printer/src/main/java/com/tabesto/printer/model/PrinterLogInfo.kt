package com.tabesto.printer.model

import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.status.PrinterStatus

data class PrinterLogInfo(
    val printerData: PrinterData? = null,
    val printerStatus: PrinterStatus? = null,
    val printerException: PrinterException? = null
)
