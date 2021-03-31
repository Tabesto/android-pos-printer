package com.tabesto.printer.discovery

import com.tabesto.printer.model.DiscoveryData
import com.tabesto.printer.model.PrinterType

internal class DiscoveryFactory {
    fun getDiscovery(discoveryData: DiscoveryData): Discovery {
        if (discoveryData.printerType == PrinterType.PRINTER_EPSON) {
            return DiscoveryEpson(discoveryData)
        }
        // by default if we do not recognize printer type we return Epson type Discovery
        return DiscoveryEpson(discoveryData)
    }
}
