package com.tabesto.printer.writer

import com.tabesto.printer.model.error.PrinterException

interface PrinterWriterListener {
    fun onFailureWriteData(printerException: PrinterException)
}
