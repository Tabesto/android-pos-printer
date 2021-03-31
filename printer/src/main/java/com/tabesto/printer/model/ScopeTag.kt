package com.tabesto.printer.model

/**
 * ScopeTag contains object that represent the scope of printer method
 * it is mainly used for PrinterError in order to get the correct action message for caller app
 *
 */
@Suppress("unused")
enum class ScopeTag {
    PRINT_DATA, CONNECT, DISCONNECT, ANY, BLUETOOTH, DISCOVERY, INITIALIZE
}
