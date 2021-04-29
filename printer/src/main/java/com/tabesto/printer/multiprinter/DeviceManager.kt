package com.tabesto.printer.multiprinter

import android.content.Context
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterRemainingJob
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult
import com.tabesto.printer.model.ticket.TicketData

interface DeviceManager {
    /**
     * this listener will fire callback to caller app in order
     * to inform if any error happened in data process
     * example : list of printer empty, or unknown printer address
     */
    var deviceManagerListener: DeviceManagerListener?

    /**
     * initialize a printer object for a specific address
     * if it is already initialized it won't do anything
     * @param printerData
     * @param context
     */
    fun initializePrinter(printerData: PrinterData, context: Context)

    /**
     * initialize a list of printer object for list of printer address
     * if it is already initialized it won't do anything
     * @param listOfPrinterData
     * @param context
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
     * Set Listener to receive callback from [DeviceManagerDiscoveryListener]
     */
    fun setDiscoveryListener(deviceManagerDiscoveryListener: DeviceManagerDiscoveryListener)

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
     * @param deviceManagerDiscoveryListener
     * @param deviceManagerStatusListener
     */
    fun setListeners(
        deviceManagerInitListener: DeviceManagerInitListener? = null,
        deviceManagerConnectListener: DeviceManagerConnectListener? = null,
        deviceManagerPrintListener: DeviceManagerPrintListener? = null,
        deviceManagerDiscoveryListener: DeviceManagerDiscoveryListener? = null,
        deviceManagerStatusListener: DeviceManagerStatusListener? = null,
        deviceManagerListener: DeviceManagerListener
    ) {
        deviceManagerInitListener?.let { setInitListener(it) }
        deviceManagerConnectListener?.let { setConnectListener(it) }
        deviceManagerPrintListener?.let { setPrintListener(it) }
        deviceManagerDiscoveryListener?.let { setDiscoveryListener(it) }
        deviceManagerStatusListener?.let { setStatusListener(it) }
        deviceManagerListener.let { this.deviceManagerListener = it }
    }

    /**
     * connect one specific printer by its printer address
     * if it is already connected you will receive a success connection feedback
     * This method must be called after initialization else it will fire a device manager error callback
     * @param printerData
     */
    fun connectPrinter(printerData: PrinterData)

    /**
     * connect all printer address given in parameter
     * if a specific printer address  is already connected it will put connected successful
     * This method must be called after initialization else it will fire a device manager error callback
     * @param listOfPrinterData
     */
    fun connectPrinter(listOfPrinterData: List<PrinterData>)

    /**
     * connect all managed printer
     *
     */
    fun connectPrinter()

    /**
     * This method write ticket data and print it on a specific printer address
     *
     * @param printerData
     * @param ticketData
     */
    fun printData(printerData: PrinterData, ticketData: TicketData)

    /**
     * This method write ticket data and print it on all printer address given in parameter
     * @param listOfPrinterData
     * @param ticketData
     */
    fun printData(listOfPrinterData: List<PrinterData>, ticketData: TicketData)

    /**
     * This method will print the ticket on all managed printer
     *
     * @param ticketData
     */
    fun printData(ticketData: TicketData)

    /**
     * This method disconnect the printer linked to printer address in printerData
     * @param printerData
     */
    fun disconnectPrinter(printerData: PrinterData)

    /**
     * Disconnect all printer given by parameter in printer data list
     *
     * @param listOfPrinterData
     */
    fun disconnectPrinter(listOfPrinterData: List<PrinterData>)

    /**
     * This method will disconnect all managed printer
     *
     */
    fun disconnectPrinter()

    /**
     * This method will send back the status of corresponding printer data
     *
     * @param printerData
     * @return
     */
    fun getStatus(printerData: PrinterData)

    /**
     * this method returns the list of printer data that is already managed by device manager
     *
     * @return
     */
    fun getManagedPrinterDataList(): List<PrinterData>

    /**
     * this method returns the list of printer data that is already managed by device manager
     * with their status by callback onListOfPrinterManagedReceived in [DeviceManagerListener]
     *
     * @return
     */
    fun getManagedPrinterDataAndStatusList()

    /**
     * this method will send the list of all jobs done by the device manager
     *
     * @return
     */
    fun getJobsResultHistoryList(): List<DeviceManagerJobResult>

    /**
     * this method will give the possibility to clear the history of job result list
     *
     */
    fun clearJobsResultHistoryList()

    /**
     * This gives the possibility to enable disable logs
     * By default logs are enable
     * @param enable
     */
    fun enableLogs(enable: Boolean = true)

    /**
     * this method will remove the printer linked to the printer data passed in param
     * if it is connected it will not remove it and send back false
     * if it is disconnected it will remove the printer and send back true
     *
     * @param printerData
     */
    fun removePrinter(printerData: PrinterData): Boolean

    /**
     * This method will cancel all background jobs running on each printer
     * and unlock main job
     *
     */
    fun cancelAllJobsAndUnlock()

    /**
     * This method will return the list of background jobs still in progress
     *
     */
    fun getListOfRemainingJobs(): List<PrinterRemainingJob>

    // region MOVE_DISCOVERY
    fun restartBluetoothAndLaunchDiscovery(printerData: PrinterData, delayMillis: Long = 0)

    fun restartBluetooth(printerData: PrinterData)

    fun stopDiscovery(printerData: PrinterData)
    // endregion MOVE_DISCOVERY
}
