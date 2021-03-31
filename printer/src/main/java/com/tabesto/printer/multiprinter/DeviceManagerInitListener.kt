package com.tabesto.printer.multiprinter

import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.error.PrinterException

interface DeviceManagerInitListener {
    /**
     * This method is fired if an error occurs printer at printer object initialization
     *
     * @param printerData
     * @param printerException
     */
    fun onInitPrinterFailure(printerData: PrinterData, printerException: PrinterException)
}
