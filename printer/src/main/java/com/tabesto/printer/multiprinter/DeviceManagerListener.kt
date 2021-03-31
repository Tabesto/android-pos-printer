package com.tabesto.printer.multiprinter

import com.tabesto.printer.model.PrinterManaged
import com.tabesto.printer.model.ScopeTag
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult

interface DeviceManagerListener {
    /**
     * This method is fired when at one job of one of the printer list failed because the printer address
     * given by parameter didn't exist into existing list of printer
     *
     * @param deviceManagerJobResult : contains a job result
     * @param isLastReturnOfJob : this boolean will be fixed true if device manager detect it is the last callback
     */
    fun onDeviceManagerErrorForAJobResult(deviceManagerJobResult: DeviceManagerJobResult, isLastReturnOfJob: Boolean = false)

    /**
     * this callback is fired when a job is sent on an existing list of printer, but that list is empty
     *
     * @param scope : will precise from which command this callback was fired
     */
    fun onListOfPrintersEmpty(scope: ScopeTag)

    /**
     * onListOfPrinterManagedReceived will be fired back when device manager's getManagedPrinterDataAndStatusList
     * is called , it sends back a list of printer with its related printer status
     *
     * @return
     */
    fun onListOfPrinterManagedReceived(listOfPrinterDataAndPrinterStatus: MutableList<PrinterManaged>)
}
