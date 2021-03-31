package com.tabesto.printer.discovery

/**
 * Discovery class gives the possibility to scan the network (bluetooth or wifi or ethernet (TCP))
 * and inform the caller which printer is available to connect and do printing
 *
 */
internal interface Discovery {
    /**
     * launchDiscovery method gives the possibility to launch a discovery (with delay in ms or not)
     * it also manage multiple launch of discovery and if it needs to restart the discovery or not
     *
     * WARNING : We advice you to not use the delay timer , let it to 0 second
     *
     * @param printerAddress is printer target address , example : BT:01:02:04:44:3D:F2
     * @param delayMillis is delay in milli seconds that precise after how much time the result will be sent back to caller
     */
    fun launchDiscovery(printerAddress: String, delayMillis: Long = 0)

    /**
     * stopDiscovery method gives the possibility to printer module to stop discovery if a discovery is on process
     *
     */
    fun stopDiscovery()
}
