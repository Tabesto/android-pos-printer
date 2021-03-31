package com.tabesto.printer.model

/**
 *
 * @property deviceName
 * @property deviceTarget
 * @property deviceMacAddress
 * @property deviceType
 * @property ipAddress
 * @property bdAddress
 */
data class PrinterDevice(
    var deviceName: String,
    var deviceTarget: String,
    var deviceMacAddress: String,
    var deviceType: Int,
    var ipAddress: String,
    var bdAddress: String
)
