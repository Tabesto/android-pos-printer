package com.tabesto.printer.system

/**
 * BluetoothHelperListener listener can notify discovery module or any other module
 * when the bluetooth module is restarted
 *
 */
internal interface BluetoothHelperListener {
    fun onBluetoothRestartedManually()
}
