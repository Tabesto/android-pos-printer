package com.tabesto.printer.system

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.utils.log.Logger

/**
 * BluetoothHelper class restart bluetooth connection of android device
 * while it tried to restart bluetooth, it registers to broadcastReceiver for bluetooth state change ir order to enable it
 * at the end of the process it unregisters from broadcastReceiver
 * resetBluetoothModule -> enableBluetoothAdapter -> onSuccessfulBluetoothEnabled
 *
 * @property listener
 * @property context
 */
internal class BluetoothHelper(
    private val listener: BluetoothHelperListener,
    private val context: Context
) {
    private val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

    private var bluetoothAdapter: BluetoothAdapter? = null

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private var logger: Logger = Logger(BluetoothHelper::class.java.simpleName)

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            try {
                val action: String? = intent.action
                if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state: Int = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> enableBluetoothAdapter()
                        BluetoothAdapter.STATE_ON -> onSuccessfulBluetoothEnabled()
                    }
                }
            } catch (e: Exception) {
                logger.e("an exception occured while receiving bluetooth event ${e.message}")
            }
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        bluetoothAdapter?.let { bluetoothAdapter -> return bluetoothAdapter.isEnabled } ?: return false
    }

    /**
     * resetBluetoothModule method resets bluetooth module of android device
     * if bluetooth is already disabled it enable it directly
     * else it launch a disable
     * Before doing any of those actions , this method register to bluetooth state change
     */
    fun resetBluetoothModule() {
        context.registerReceiver(receiver, filter)
        try {
            if (isBluetoothEnabled()) {
                bluetoothAdapter?.disable()
            } else {
                enableBluetoothAdapter()
            }
        } catch (e: Exception) {
            context.unregisterReceiver(receiver)
            logger.e("an exception happened while trying to disable enable bluetooth " + e.message)
            throw PrinterException(" an exception happened while trying to disable/enable bluetooth ")
        }
    }

    /**
     * enableBluetoothAdapter method can be called by BroadCast receiver intent above, when bluetooth is disable
     * or  directly by method resetBluetoothModule when bluetooth is already disabled
     *
     */
    private fun enableBluetoothAdapter() {
        try {
            bluetoothAdapter?.enable()
        } catch (e: Exception) {
            context.unregisterReceiver(receiver)
            logger.e("an exception happened while trying to enable bluetooth " + e.message)
            throw PrinterException(" an exception happened while trying to enable bluetooth ")
        }
    }

    /**
     * onSuccessfulBluetoothEnabled method is called when bluetooth is enabled successfully
     * it unregisters to broadcast event receiver and notify caller app that process of restart bluetooth
     * is finished
     *
     */
    private fun onSuccessfulBluetoothEnabled() {
        try {
            logger.i(" bluetooth has been enabled successfully")
            context.unregisterReceiver(receiver)
            listener.onBluetoothRestartedManually()
        } catch (e: Exception) {
            context.unregisterReceiver(receiver)
            logger.e("an exception happened while trying return bluetooth restart result " + e.message)
            throw PrinterException(" an exception happened while trying to restart bluetooth ")
        }
    }
}
