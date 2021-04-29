package com.tabesto.printer.sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tabesto.printer.model.PrinterManaged
import com.tabesto.printer.model.ScopeTag
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult
import com.tabesto.printer.multiprinter.DeviceManager
import com.tabesto.printer.multiprinter.DeviceManagerImpl
import com.tabesto.printer.multiprinter.DeviceManagerListener
import com.tabesto.printer.sample.dialog.PrinterManagedListDialog

class ExampleActivity : AppCompatActivity(), DeviceManagerListener {

    private val deviceManager: DeviceManager = DeviceManagerImpl.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
    }

    override fun onResume() {
        super.onResume()
        setDeviceManagerListeners()
    }

    private fun setDeviceManagerListeners() {
        deviceManager.deviceManagerListener = this
    }

    @Suppress("unused_parameter")
    fun showPrinterManagedList(view: View) {
        deviceManager.getManagedPrinterDataAndStatusList()
    }

    private fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, length).show()
    }

    override fun onDeviceManagerErrorForAJobResult(deviceManagerJobResult: DeviceManagerJobResult, isLastReturnOfJob: Boolean) {
        // Do Nothing
    }

    override fun onListOfPrintersEmpty(scope: ScopeTag) {
        // Do nothing
    }

    override fun onListOfPrinterManagedReceived(listOfPrinterDataAndPrinterStatus: List<PrinterManaged>) {
        runOnUiThread {
            if (listOfPrinterDataAndPrinterStatus.isNotEmpty()) {
                val dialogPrinterDataAndStatusList = PrinterManagedListDialog().newInstance(listOfPrinterDataAndPrinterStatus)
                dialogPrinterDataAndStatusList.show(supportFragmentManager, "PrinterManagedListDialog")
            } else {
                showToast(" The list of printer is empty ")
            }
        }
    }
}
