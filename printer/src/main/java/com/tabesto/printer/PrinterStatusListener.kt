package com.tabesto.printer

import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.status.PrinterStatus

interface PrinterStatusListener {
    fun onStatusUpdate(printerData: PrinterData, status: String)
    fun onStatusReceived(printerData: PrinterData, printerStatus: PrinterStatus)
}
