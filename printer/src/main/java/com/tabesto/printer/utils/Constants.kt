package com.tabesto.printer.utils

typealias EposPrinter = com.epson.epos2.printer.Printer

object Constants {

    const val PRINTER_UNKNOWN_CODE = "UNKNOWN_CODE"
    const val PRINTER_UNKNOWN_ACTION = "Unknown action"

    const val UNKNOWN_VALUE = "UNKNOWN"

    // 46 its our own custom code that is not present in EposException, in EposException last int is 45 and 255
    const val PRINTER_ERR_UNKNOWN = 46

    // For Device Manager
    const val PRINTER_DATA_NOT_MANAGED = "Printer is not managed by the device manager, please connect it"
    const val MAIN_JOB_IS_RUNNING = "A main job is already running"

    const val CONNECT_TIMEOUT_MIN = 1000
    const val PRINT_TIMEOUT_MIN = 5000
}
