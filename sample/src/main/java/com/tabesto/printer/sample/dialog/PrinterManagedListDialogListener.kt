package com.tabesto.printer.sample.dialog

import com.tabesto.printer.model.PrinterData

interface PrinterManagedListDialogListener {
    fun onPrinterSelected(printerData: PrinterData)
    fun onRemovePrinter(printerData: PrinterData)
}
