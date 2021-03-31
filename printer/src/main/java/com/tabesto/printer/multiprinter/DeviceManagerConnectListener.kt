package com.tabesto.printer.multiprinter

import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult

interface DeviceManagerConnectListener {
    /**
     * onConnectResult returns result of device manager's connect instruction
     * It can contains a list of results (success or failure) if you use it on one or multiple printer
     * at the same time
     * This callback is fired only once after finishing instruction on all printer
     * @param listOfJobsResult
     */
    fun onConnectResult(listOfJobsResult: ArrayList<DeviceManagerJobResult>)

    /**
     * onDisconnectResult returns result of device manager's disconnect instruction
     * It can contains a list of results (success or failure) if you use it on one or multiple printer
     * at the same time
     * This callback is fired only once after finishing instruction on all printer
     * @param listOfJobsResult
     */
    fun onDisconnectResult(listOfJobsResult: ArrayList<DeviceManagerJobResult>)
}
