package com.tabesto.printer

import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.error.PrinterException

interface PrinterInitListener {

    /**
     * This method is called by [Printer] module if an error occurs printer object initialization
     *
     * @param printerException object will contain the error message returned by [Printer] module
     */
    fun onInitPrinterFailure(printerData: PrinterData, printerException: PrinterException)
}
