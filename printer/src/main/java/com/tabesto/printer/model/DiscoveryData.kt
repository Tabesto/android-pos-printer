package com.tabesto.printer.model

import android.content.Context
import com.tabesto.printer.discovery.DiscoveryListener

/**
 * [DiscoveryData] class is necessary to initialize Discovery class and To create the correct Discovery into DiscoveryFactory
 *
 * @property context : Context of Android's caller app, needed to launch a discovery and register to its discovery event
 * @property listener : caller app need to implement DiscoveryPrinterListener in order to receive result from Discovery
 * @property printerType : Epson or other Type of printer
 */
internal data class DiscoveryData(val context: Context, val listener: DiscoveryListener, val printerType: PrinterType)
