package com.tabesto.printer

import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.status.PrinterStatus

interface PrinterPrintListener {

    /**
     * This method is called by [Printer] module when ticket is printed successfully
     *
     * @param printerStatus object will contain the all [PrinterStatus] info
     */
    fun onPrintSuccess(printerData: PrinterData, printerStatus: PrinterStatus)

    /**
     * This method is called by printer library if an error occurs at the end of the printing workflow
     *
     * @param printerException object will contain the error message returned by [Printer] module
     */
    fun onPrintFailure(printerData: PrinterData, printerException: PrinterException)

    /**
     * This method is called by printer library if an error occurs while printing
     *
     * @param printerException object will contain the error message returned by printer module
     */
    fun onPrintDataFailure(printerData: PrinterData, printerException: PrinterException)
}
