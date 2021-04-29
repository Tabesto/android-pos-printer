package com.tabesto.printer.multiprinter

import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult

interface DeviceManagerPrintListener {

    /**
     * onPrintResult returns result of device manager's print data instruction
     * It can contains a list of results (success or failure) if you use it on one or multiple printer
     * at the same time
     * This callback will be fired as many times as there is a print data instruction in entry
     * @param listOfJobsResult
     */
    fun onPrintResult(listOfJobsResult: List<DeviceManagerJobResult>)
}
