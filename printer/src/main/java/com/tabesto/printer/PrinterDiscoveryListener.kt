package com.tabesto.printer

import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.error.PrinterException

interface PrinterDiscoveryListener {

    /**
     * This method is fired when discovery process is finished by Discovery module
     *
     * @param isAvailable : tells if specific printer, with target address precised at discovery launch, is available to print documents
     */
    fun onDiscoveryFinish(printerData: PrinterData, isAvailable: Boolean)

    /**
     * This method is fired when an error occur while trying research printers
     *
     * @param printerException : contains error message returns by Discovery module
     */
    fun onDiscoveryFailure(printerData: PrinterData, printerException: PrinterException)

    /**
     * This method is fired when the stop discovery command has been executed successfully
     *
     */
    fun onDiscoveryStopSuccess(printerData: PrinterData)

    /**
     * This method is fired when bluetooth is restarted successfully
     *
     */
    fun onBluetoothRestartSuccess(printerData: PrinterData)
}
