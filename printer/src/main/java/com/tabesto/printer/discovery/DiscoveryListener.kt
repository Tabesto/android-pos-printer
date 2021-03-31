package com.tabesto.printer.discovery

import com.tabesto.printer.model.error.PrinterException

/**
 * DiscoveryPrinterListener class implemented by caller app
 * will send back result of printer device research
 *
 */
internal interface DiscoveryListener {
    /**
     * onFinishDiscovery is fired when [Discovery] process is finished by Discovery module
     *
     * @param isAvailable : tells if specific printer, with target address precised at discovery launch, is available to print documents
     */
    fun onFinishDiscovery(isAvailable: Boolean)

    /**
     * onFailureDiscovery is fired when an error occur while trying research printers
     *
     * @param printerException contains error message
     */
    fun onFailureDiscovery(printerException: PrinterException)

    /**
     * onDiscoveryStoppedSuccessfully method is fired when Discovery module stop successfully on going discovery
     *
     */
    fun onDiscoveryStoppedSuccessfully()
}
