package com.tabesto.printer.multiprinter

import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.status.PrinterStatus

interface DeviceManagerStatusListener {
    fun onStatusUpdate(printerData: PrinterData, status: String)
    fun onStatusReceived(printerData: PrinterData, printerStatus: PrinterStatus)
}
