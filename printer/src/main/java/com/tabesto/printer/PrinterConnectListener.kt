package com.tabesto.printer

import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.error.PrinterException

interface PrinterConnectListener {

    /**
     * This method is fired when the [Printer] is successfully disconnected
     *
     */
    fun onDisconnectSuccess(printerData: PrinterData)

    /**
     * This method is called by printer library if an error occurs while printer disconnection
     *
     * @param printerException object will contain the error message returned by printer module
     */
    fun onDisconnectFailure(printerData: PrinterData, printerException: PrinterException)

    /**
     * This method is called by printer library if an error occurs while the printer try to connect
     *
     * @param printerException object will contain the error message returned by printer module
     */
    fun onConnectFailure(printerData: PrinterData, printerException: PrinterException)

    /**
     * This method is fired when the printer is connected successfully
     *
     * @param printerData : parameter will precise which printer is connected successfully
     */
    fun onConnectSuccess(printerData: PrinterData)
}
