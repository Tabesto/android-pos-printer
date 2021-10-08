package com.tabesto.printer.multiprinter

import android.content.Context
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterRemainingJob
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult
import com.tabesto.printer.model.ticket.TicketData

interface DeviceManager {
    /**
     * This listener will fire callback to caller app in order
     * to inform if any error happened in data process
     * example : list of printer empty, or unknown printer address
     */
    var deviceManagerListener: DeviceManagerListener?

    /**
     * Initialize given printers :
     * - add given printers to printers managed by device manager.
     * - init sdk for given printers.
     * If a printer is already initialized, it won't do anything.
     *
     * @param printerData targeted printer
     * @param context application context required to init sdk
     */
    fun initializePrinter(printerData: PrinterData, context: Context)

    /**
     * Initialize given printers :
     * - add given printers to printers managed by device manager.
     * - init sdk for given printers.
     * If a printer is already initialized, it won't do anything.
     *
     * @param listOfPrinterData targeted printers
     * @param context application context required to init sdk
     */
    fun initializePrinter(listOfPrinterData: List<PrinterData>, context: Context)

    /**
     * Set Listener to receive callback from [DeviceManagerInitListener]
     */
    fun setInitListener(deviceManagerInitListener: DeviceManagerInitListener)

    /**
     * Set Listener to receive callback from [DeviceManagerConnectListener]
     */
    fun setConnectListener(deviceManagerConnectListener: DeviceManagerConnectListener)

    /**
     * Set Listener to receive callback from [DeviceManagerPrintListener]
     */
    fun setPrintListener(deviceManagerPrintListener: DeviceManagerPrintListener)

    /**
     * Set Listener to receive callback from [DeviceManagerStatusListener]
     *
     * @param deviceManagerStatusListener
     */
    fun setStatusListener(deviceManagerStatusListener: DeviceManagerStatusListener)

    /**
     * Set Listeners to receive callback from all Listeners.
     * Permit to select all listeners you need in only one method
     * @param deviceManagerInitListener
     * @param deviceManagerConnectListener
     * @param deviceManagerPrintListener
     * @param deviceManagerStatusListener
     */
    fun setListeners(
        deviceManagerInitListener: DeviceManagerInitListener? = null,
        deviceManagerConnectListener: DeviceManagerConnectListener? = null,
        deviceManagerPrintListener: DeviceManagerPrintListener? = null,
        deviceManagerStatusListener: DeviceManagerStatusListener? = null,
        deviceManagerListener: DeviceManagerListener
    ) {
        deviceManagerInitListener?.let { setInitListener(it) }
        deviceManagerConnectListener?.let { setConnectListener(it) }
        deviceManagerPrintListener?.let { setPrintListener(it) }
        deviceManagerStatusListener?.let { setStatusListener(it) }
        deviceManagerListener.let { this.deviceManagerListener = it }
    }

    /**
     * Connect given printer
     * if a specific printer address  is already connected it will put connected successful
     * This method must be called after initialization else it will fire a device manager error callback
     *
     * @param printerData targeted printer
     * @param timeout connection timeout in milliseconds
     */
    fun connectPrinter(printerData: PrinterData, timeout: Int? = null)

    /**
     * Connect given printers
     * if a specific printer address  is already connected it will put connected successful
     * This method must be called after initialization else it will fire a device manager error callback
     *
     * @param listOfPrinterData targeted printers
     * @param timeout connection timeout in milliseconds
     */
    fun connectPrinter(listOfPrinterData: List<PrinterData>, timeout: Int? = null)

    /**
     * Connect all managed printer
     *
     * @param timeout connection timeout in milliseconds
     */
    fun connectPrinter(timeout: Int? = null)

    /**
     * Print ticket on given printer
     *
     * @param printerData targeted printer
     * @param ticketData ticket to print
     * @param timeout print timeout in milliseconds
     */
    fun printData(printerData: PrinterData, ticketData: TicketData, timeout: Int? = null)

    /**
     * Print ticket on given printer with on demand connection mode
     *
     * @param printerData targeted printer
     * @param ticketData ticket to print
     * @param connectTimeout connection timeout in milliseconds
     * @param printTimeout print timeout in milliseconds
     */
    fun printDataOnDemand(printerData: PrinterData, ticketData: TicketData, connectTimeout: Int? = null, printTimeout: Int? = null)

    /**
     * Print ticket on given printers
     *
     * @param listOfPrinterData targeted printers
     * @param ticketData ticket to print
     * @param timeout print timeout in milliseconds
     */
    fun printData(listOfPrinterData: List<PrinterData>, ticketData: TicketData, timeout: Int? = null)

    /**
     * Print ticket on given printers with on demand connection mode
     *
     * @param listOfPrinterData targeted printers
     * @param ticketData ticket to print
     * @param connectTimeout connection timeout in milliseconds
     * @param printTimeout print timeout in milliseconds
     */
    fun printDataOnDemand(
        listOfPrinterData: List<PrinterData>,
        ticketData: TicketData,
        connectTimeout: Int? = null,
        printTimeout: Int? = null
    )

    /**
     * Print ticket on all managed printer
     *
     * @param ticketData ticket to print
     * @param timeout print timeout in milliseconds
     */
    fun printData(ticketData: TicketData, timeout: Int? = null)

    /**
     * Print ticket on all managed printer with on demand connection mode
     *
     * @param ticketData ticket to print
     * @param connectTimeout connection timeout in milliseconds
     * @param printTimeout print timeout in milliseconds
     */
    fun printDataOnDemand(ticketData: TicketData, connectTimeout: Int? = null, printTimeout: Int? = null)

    /**
     * Disconnect given printer
     *
     * @param printerData targeted printer
     */
    fun disconnectPrinter(printerData: PrinterData)

    /**
     * Disconnect given printers
     *
     * @param listOfPrinterData
     */
    fun disconnectPrinter(listOfPrinterData: List<PrinterData>)

    /**
     * Disconnect all managed printer
     */
    fun disconnectPrinter()

    /**
     * Retrieve status of given printer
     * by callback onStatusReceived in [DeviceManagerStatusListener]
     *
     * @param printerData targeted printer
     */
    fun getStatus(printerData: PrinterData)

    /**
     * Retrieve list of printer already managed by device manager
     *
     * @return list of printer already managed by device manager
     */
    fun getManagedPrinterDataList(): List<PrinterData>

    /**
     *  Retrieve list of printer already managed by device manager
     *  with their status by callback onListOfPrinterManagedReceived in [DeviceManagerListener]
     *
     */
    fun getManagedPrinterDataAndStatusList()

    /**
     * Retrieve list of all jobs done by the device manager
     *
     * @return list of all jobs done by the device manager
     */
    fun getJobsResultHistoryList(): List<DeviceManagerJobResult>

    /**
     * Clear the history of job result list
     *
     */
    fun clearJobsResultHistoryList()

    /**
     * Enable or disable logs
     * By default logs are enabled
     *
     * @param enable logs activation
     */
    fun enableLogs(enable: Boolean = true)

    /**
     * Remove given printer from all managed printers
     *
     * @param printerData
     * @return if it is connected it will not remove it and send back false
     * if it is disconnected it will remove the printer and send back true
     */
    fun removePrinter(printerData: PrinterData): Boolean

    /**
     * Remove all managed printers
     *
     * @return if it is connected it will not remove it and send back false
     * if it is disconnected it will remove the printer and send back true
     */
    fun removePrinter(): Boolean

    /**
     * Cancel all background jobs running on each printer and unlock main job
     *
     */
    fun cancelAllJobsAndUnlock()

    /**
     * Retrieve list of background jobs still in progress
     *
     */
    fun getListOfRemainingJobs(): List<PrinterRemainingJob>
}
