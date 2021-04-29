package com.tabesto.printer

import android.content.Context
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.status.PrinterStatus
import com.tabesto.printer.model.ticket.TicketData

interface Printer {
    var printerData: PrinterData
    var context: Context

    /**
     * initializePrinterObject method initialize Epos printer object
     * and set its listener in order to receive status code send data for printing
     */
    fun initializePrinter()

    /**
     * Set Listener to receive callback from [PrinterInitListener]
     * @param printerInitListener  gives the possibility to interact back to the caller app to notify if init succeed or failed
     */
    fun setInitListener(printerInitListener: PrinterInitListener)

    /**
     * Set Listener to receive callback from [PrinterConnectListener]
     * @param printerConnectListener  gives the possibility to interact back to the caller app to notify if connection succeed or failed
     */
    fun setConnectListener(printerConnectListener: PrinterConnectListener)

    /**
     * Set Listener to receive callback from [PrinterPrintListener]
     * @param printerPrintListener  gives the possibility to interact back to the caller app to notify if printing succeed or failed
     */
    fun setPrintListener(printerPrintListener: PrinterPrintListener)

    /**
     * Set Listener to receive callback from [PrinterDiscoveryListener]
     * @param printerDiscoveryListener  gives the possibility to interact back to the caller app to notify if discovery succeed or failed
     */
    fun setDiscoveryListener(printerDiscoveryListener: PrinterDiscoveryListener)

    fun setStatusListener(printerStatusListener: PrinterStatusListener)

    /**
     * Set Listeners to receive callback from all Listeners.
     * Permit to select all listeners you need in only one method
     * @param printerInitListener  gives the possibility to interact back to the caller app to notify if init succeed or failed
     * @param printerConnectListener  gives the possibility to interact back to the caller app to notify if connection succeed or failed
     * @param printerPrintListener  gives the possibility to interact back to the caller app to notify if printing succeed or failed
     * @param printerDiscoveryListener  gives the possibility to interact back to the caller app to notify if discovery succeed or failed
     * @param printerStatusListener will receive callback for status get status and status update
     */
    fun setListeners(
        printerInitListener: PrinterInitListener? = null,
        printerConnectListener: PrinterConnectListener? = null,
        printerPrintListener: PrinterPrintListener? = null,
        printerDiscoveryListener: PrinterDiscoveryListener? = null,
        printerStatusListener: PrinterStatusListener? = null
    ) {
        printerInitListener?.let { setInitListener(it) }
        printerConnectListener?.let { setConnectListener(it) }
        printerPrintListener?.let { setPrintListener(it) }
        printerDiscoveryListener?.let { setDiscoveryListener(it) }
        printerStatusListener?.let { setStatusListener(it) }
    }

    /**
     * connectPrinter method that connect the android device to the printer
     * by bluetooth or by wifi
     */
    fun connectPrinter()

    /**
     * This method write ticket data and print it
     *
     */
    fun printData(ticketData: TicketData)

    /**
     * This method disconnect the printer
     *
     */
    fun disconnectPrinter()

    /**
     * launchDiscovery method gives the possibility to launch a discovery (with delay in ms or not)
     * it also manage multiple launch of discovery and if it needs to restart the discovery or not
     * by default bluetooth of android device is restarted just before launching discovery process
     * @param delayMillis is delay in milli seconds that precise after how much time the result will be sent back to caller
     *
     */
    fun restartBluetoothAndLaunchDiscovery(delayMillis: Long = 0)

    fun restartBluetooth()

    /**
     * stopDiscovery method gives the possibility to caller app to stop discovery if a discovery is on going
     *
     */
    fun stopDiscovery()

    /**
     *  This method returns the [PrinterStatus] of current printer but will block the main thread while retrieving the status
     */
    fun getStatusRaw(): PrinterStatus

    /**
     *  This method returns the [PrinterStatus] of current printer by the callback onStatusReceived
     */
    fun getStatus()

    /**
     * This method will cancel all background job in progress
     *
     */
    fun cancelAllJob()
}
