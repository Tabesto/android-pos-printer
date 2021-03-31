package com.tabesto.printer

import android.content.Context
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterType

class PrinterFactory {
    fun getPrinter(printerData: PrinterData, context: Context): Printer {
        if (printerData.printerType == PrinterType.PRINTER_EPSON) {
            return PrinterEpson(printerData, context)
        }
        // by default if we do not recognize printer type we return Epson type
        return PrinterEpson(printerData, context)
    }
}
