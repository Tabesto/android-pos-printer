package com.tabesto.printer.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
import com.tabesto.printer.model.ConnectionMode
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterManaged
import com.tabesto.printer.model.PrinterModel
import com.tabesto.printer.model.PrinterRegion
import com.tabesto.printer.model.PrinterType
import com.tabesto.printer.model.ScopeTag
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.status.PrinterStatus
import com.tabesto.printer.model.ticket.TicketData
import com.tabesto.printer.multiprinter.DeviceManager
import com.tabesto.printer.multiprinter.DeviceManagerConnectListener
import com.tabesto.printer.multiprinter.DeviceManagerImpl
import com.tabesto.printer.multiprinter.DeviceManagerInitListener
import com.tabesto.printer.multiprinter.DeviceManagerListener
import com.tabesto.printer.multiprinter.DeviceManagerPrintListener
import com.tabesto.printer.multiprinter.DeviceManagerStatusListener
import com.tabesto.printer.sample.databinding.ActivityMainBinding
import com.tabesto.printer.sample.dialog.DialogPrinterErrorDetails
import com.tabesto.printer.sample.dialog.JobResultListDialog
import com.tabesto.printer.sample.dialog.JobResultListDialogListener
import com.tabesto.printer.sample.dialog.PrinterManagedListDialog
import com.tabesto.printer.sample.dialog.PrinterManagedListDialogListener
import com.tabesto.printer.sample.dialog.RemainingJobListDialog
import com.tabesto.printer.sample.dialog.RemainingJobListDialogListener
import com.tabesto.printer.sample.util.TicketBuilder.buildSmallTicketData
import com.tabesto.printer.sample.util.viewBinding
import timber.log.Timber

class MainActivity : AppCompatActivity(), DeviceManagerListener, DeviceManagerConnectListener, DeviceManagerPrintListener,
    DeviceManagerInitListener, JobResultListDialogListener, PrinterManagedListDialogListener, DeviceManagerStatusListener,
    RemainingJobListDialogListener, PrinterItem.Listener {

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val printerAdapter = ItemAdapter<PrinterItem>()
    private lateinit var printerFastAdapter: FastAdapter<PrinterItem>
    private lateinit var deviceManager: DeviceManager
    private var printerDataList: MutableList<PrinterData> = mutableListOf()
    private var ticketData: TicketData? = null
    private var connectionMode: ConnectionMode = ConnectionMode.MANUAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViews()
        setupLogs()
        deviceManager = DeviceManagerImpl.getInstance()
        initData()
    }

    private fun setupViews() {
        with(binding) {
            printerFastAdapter = FastAdapter.with(printerAdapter).apply {
                setHasStableIds(true)
                addEventHooks(
                    listOf(
                        PrinterItem.CheckBoxClickEvent(),
                        PrinterItem.StatusClickEvent(this@MainActivity),
                    )
                )
                getSelectExtension().apply {
                    isSelectable = true
                    multiSelect = true
                }
            }

            recyclerviewMainPrinterList.apply {
                layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
                setHasFixedSize(true)
                adapter = printerFastAdapter
                itemAnimator = null
            }

            radiogroupMainConnectionMode.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radiobutton_main_persistent_connection_mode -> {
                        connectionMode = ConnectionMode.MANUAL
                        buttonMainInitPrinter.isEnabled = true
                        buttonMainConnectPrinter.isEnabled = true
                        buttonMainDisconnectPrinter.isEnabled = true
                    }
                    R.id.radiobutton_main_on_demand_connection_mode -> {
                        connectionMode = ConnectionMode.AUTO
                        buttonMainInitPrinter.isEnabled = false
                        buttonMainConnectPrinter.isEnabled = false
                        buttonMainDisconnectPrinter.isEnabled = false
                        showToast("Managed printer list cleared ! \nInitialization will be done on printing on demand", Toast.LENGTH_LONG)
                    }
                }

                refreshPrinterListOnConnectionModeChanged(connectionMode)
            }

            buttonMainInitPrinter.setOnClickListener { initPrinters() }
            buttonMainConnectPrinter.setOnClickListener { connectPrinter() }
            buttonMainPrint.setOnClickListener { printTicket() }
            buttonMainDisconnectPrinter.setOnClickListener { disconnectPrinter() }
            buttonMainJobHistory.setOnClickListener { showJobResultHistoryList() }
            buttonMainPrinterList.setOnClickListener { showManagedPrinterList() }
            buttonMainRemainingJobs.setOnClickListener { showRemainingJobListDialog() }
            buttonMainExampleActivity.setOnClickListener { startExampleActivity() }
        }
    }

    private fun refreshPrinterListOnConnectionModeChanged(connectionMode: ConnectionMode) {
        // 1. clear printer list
        printerAdapter.adapterItems.forEach {
            onPrinterUnselected(it)
        }

        // 2. replace connection mode on each selected printer on connection mode change
        val newPrinterList = printerAdapter.adapterItems.map { printerItem ->
            val newPrinterData = printerItem.printerData?.copy(connectionMode = connectionMode)
            printerItem.printerData = newPrinterData
            printerItem
        }
        printerAdapter.setNewList(newPrinterList)

        // 3. re set printer list with new connection mode for selected printers
        val selectExtension: SelectExtension<PrinterItem> = printerFastAdapter.requireExtension()
        printerDataList = selectExtension.selectedItems.mapNotNull { it.printerData }.toMutableList()
    }

    private fun setupLogs() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initData() {
        // ticketData = buildSampleTicketData(this)
        ticketData = buildSmallTicketData(this)
        val printerData1 = PrinterData(
            PrinterModel.PRINTER_TM_M10,
            PrinterRegion.PRINTER_ANK,
            getString(R.string.main_bluetooth_address_2),
            connectionMode,
            PrinterType.PRINTER_EPSON
        )
        val printerData2 = PrinterData(
            PrinterModel.PRINTER_TM_M10,
            PrinterRegion.PRINTER_ANK,
            getString(R.string.main_tcp_address_1),
            connectionMode,
            PrinterType.PRINTER_EPSON
        )
        val printerData3 = PrinterData(
            PrinterModel.PRINTER_TM_M10,
            PrinterRegion.PRINTER_ANK,
            getString(R.string.main_bluetooth_address_1),
            connectionMode,
            PrinterType.PRINTER_EPSON
        )
        val printerItemList =
            listOf(
                PrinterItem(printerData1, this@MainActivity),
                PrinterItem(printerData2, this@MainActivity),
                PrinterItem(printerData3, this@MainActivity),
            )
        printerAdapter.setNewList(printerItemList)
    }

    override fun onResume() {
        super.onResume()
        setDeviceManagerListeners()
    }

    private fun setDeviceManagerListeners() {
        deviceManager.setListeners(
            deviceManagerInitListener = this,
            deviceManagerConnectListener = this,
            deviceManagerPrintListener = this,
            deviceManagerStatusListener = this,
            deviceManagerListener = this
        )
    }

    private fun initPrinters() {
        Timber.d(TAG, "Init printer in persistent mode for \n${getPrinterListString()}")
        val message: String = if (printerDataList.isEmpty()) {
            "Printer addresses are empty or not selected"
        } else {
            printerDataList.forEach {
                Timber.d(TAG, "Just before initialize printer object for ${it.printerAddress}")
                deviceManager.initializePrinter(it, applicationContext)
            }
            "Printer list initialized : \n${getPrinterListString()}"
        }
        showToast(message)
    }

    private fun connectPrinter() {
        showProgressBar()
        enableButtons(false)
        when (connectionMode) {
            ConnectionMode.MANUAL -> {
                Timber.d(TAG, "Connect printer in persistent mode for \n${getPrinterListString()}")
                deviceManager.connectPrinter()
            }
            ConnectionMode.AUTO -> {
                Timber.d(TAG, "Connect printer on demand mode is not possible")
            }
        }
    }

    private fun printTicket() {
        showProgressBar()
        enableButtons(false)
        when (connectionMode) {
            ConnectionMode.MANUAL -> {
                Timber.d(TAG, "Print ticket in persistent mode for \n${getPrinterListString()}")
                ticketData?.let { ticketData ->
                    deviceManager.printData(ticketData)
                }
            }
            ConnectionMode.AUTO -> {
                Timber.d(TAG, "Print ticket on demand mode for \n${getPrinterListString()}")
                initPrinters()
                ticketData?.let { ticketData ->
                    deviceManager.printDataOnDemand(ticketData, getConnectTimeout(), getPrintTimeout())
                }
            }
        }
    }

    private fun disconnectPrinter() {
        showProgressBar()
        enableButtons(false)
        when (connectionMode) {
            ConnectionMode.MANUAL -> {
                Timber.d(TAG, "Disconnect printer in persistent mode for \n${getPrinterListString()}")
                deviceManager.disconnectPrinter()
            }
            ConnectionMode.AUTO -> {
                Timber.d(TAG, "Disconnect printer on demand mode is not possible")
            }
        }
    }

    private fun showJobResultHistoryList() {
        openDialogWithJobResultList(canEnableClearListButton = true)
    }

    private fun showManagedPrinterList() {
        showProgressBar()
        enableButtons(false)
        deviceManager.getManagedPrinterDataAndStatusList()
    }

    override fun onListOfPrinterManagedReceived(listOfPrinterDataAndPrinterStatus: List<PrinterManaged>) {
        runOnUiThread {
            hideProgressBar()
            enableButtons(true)

            if (listOfPrinterDataAndPrinterStatus.isNotEmpty()) {
                val dialogPrinterDataAndStatusList = PrinterManagedListDialog().newInstance(listOfPrinterDataAndPrinterStatus)
                dialogPrinterDataAndStatusList.show(supportFragmentManager, "PrinterManagedListDialog")
            } else {
                showToast("List of printer is empty ")
            }
        }
    }

    private fun startExampleActivity() {
        startActivity(Intent(this, ExampleActivity::class.java))
    }

    private fun openDialogWithJobResultList(
        listOfJobsResult: List<DeviceManagerJobResult>? = null,
        canEnableClearListButton: Boolean = false
    ) {
        val list: List<DeviceManagerJobResult> = listOfJobsResult ?: deviceManager.getJobsResultHistoryList()

        if (list.isNotEmpty()) {
            val dialogJobsResultList = JobResultListDialog().newInstance(list, canEnableClearListButton)
            dialogJobsResultList.show(supportFragmentManager, "JobResultListDialog")
        } else {
            showToast("List of history job is empty ")
        }
    }

    private fun showProgressBar() {
        Timber.d(TAG, " show progress bar")
        binding.progressbarMainProgress.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        Timber.d(TAG, " hide progress bar")
        binding.progressbarMainProgress.visibility = View.INVISIBLE
    }

    private fun enableButtons(enable: Boolean) {
        with(binding) {
            buttonMainInitPrinter.isEnabled = enable && !radiobuttonMainOnDemandConnectionMode.isChecked
            buttonMainConnectPrinter.isEnabled = enable && !radiobuttonMainOnDemandConnectionMode.isChecked
            buttonMainDisconnectPrinter.isEnabled = enable && !radiobuttonMainOnDemandConnectionMode.isChecked
            buttonMainPrint.isEnabled = enable || radiobuttonMainOnDemandConnectionMode.isChecked
        }
    }

    override fun onDeviceManagerErrorForAJobResult(deviceManagerJobResult: DeviceManagerJobResult, isLastReturnOfJob: Boolean) {
        Timber.e(
            TAG,
            "onDeviceManagerErrorForAJobResult message " +
                "${deviceManagerJobResult.printerData.printerAddress} ${deviceManagerJobResult.deviceManagerException?.error}"
        )

        if (isLastReturnOfJob) {
            runOnUiThread {
                hideProgressBar()
                showToast("All jobs ended with an error")
                enableButtons(true)
            }
            openDialogWithJobResultList()
        }
    }

    override fun onListOfPrintersEmpty(scope: ScopeTag) {
        Timber.d(TAG, " in onListOfPrintersEmpty in ${scope.name}")
        runOnUiThread {
            hideProgressBar()
            showToast("There is no printer managed")
            enableButtons(true)
        }
    }

    override fun onConnectResult(listOfJobsResult: List<DeviceManagerJobResult>) {
        for (jobResult in listOfJobsResult) {
            if (jobResult.isSuccessful) {
                Timber.d(TAG, "successfully connected ${jobResult.printerData.printerAddress}")
                runOnUiThread {
                    showToast("Successfully connected")
                }
            } else {
                Timber.e(
                    TAG,
                    "onConnectFailure error message ${jobResult.printerData.printerAddress} : ${jobResult.printerException?.message}"
                )
                runOnUiThread {
                    showToast("An error happened while trying to connect printer")
                }
            }
        }
        runOnUiThread {
            openDialogWithJobResultList(listOfJobsResult)
            hideProgressBar()
            enableButtons(true)
        }
    }

    override fun onDisconnectResult(listOfJobsResult: List<DeviceManagerJobResult>) {
        for (jobResult in listOfJobsResult) {
            if (jobResult.isSuccessful) {
                Timber.d(TAG, "successfully disconnected ${jobResult.printerData.printerAddress}")
                runOnUiThread {
                    showToast("Successfully disconnected")
                }
            } else {
                Timber.e(
                    TAG,
                    "onFailureDisconnectPrinter error message ${jobResult.printerData.printerAddress} : ${jobResult.printerException?.message}"
                )
                runOnUiThread {
                    showToast("An error happened while trying to disconnect the printer")
                }
            }
        }

        runOnUiThread {
            openDialogWithJobResultList(listOfJobsResult)
            hideProgressBar()
            enableButtons(true)
        }
    }

    override fun onPrintResult(listOfJobsResult: List<DeviceManagerJobResult>) {
        for (jobResult in listOfJobsResult) {
            if (jobResult.isSuccessful) {
                Timber.d(TAG, "successfully print  ${jobResult.printerData.printerAddress}")
                runOnUiThread {
                    showToast("Jobs printed successfully")
                }
            } else {
                Timber.e(
                    TAG,
                    "onPrintFailure error message ${jobResult.printerData.printerAddress} : ${jobResult.printerException?.message}"
                )
                runOnUiThread {
                    showToast("An error happened while trying to print ticket ")
                }
            }
        }

        runOnUiThread {
            hideProgressBar()
            openDialogWithJobResultList(listOfJobsResult)
            enableButtons(true)
        }
    }

    override fun onInitPrinterFailure(printerData: PrinterData, printerException: PrinterException) {
        Timber.e(TAG, "onInitPrinterFailure error message ${printerData.printerAddress} : ${printerException.message}")

        runOnUiThread {
            hideProgressBar()
            showToast("An error happened while trying to init print")
            enableButtons(true)
        }
    }

    private fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, length).show()
    }

    override fun clearListOfJobsResult() {
        deviceManager.clearJobsResultHistoryList()
        showToast("Job list cleared !")
    }

    override fun onJobResultErrorSelected(jobResult: DeviceManagerJobResult) {
        val dialogPrinterErrorDetails = DialogPrinterErrorDetails().newInstance(jobResult)
        dialogPrinterErrorDetails.show(supportFragmentManager, "DialogPrinterErrorDetails")
    }

    override fun onPrinterSelected(printerData: PrinterData) {
        // Do nothing
    }

    override fun onRemovePrinter(printerData: PrinterData) {
        val result = deviceManager.removePrinter(printerData)

        if (result) {
            showToast("Printer is removed successfully !")
        } else {
            showToast("Printer can't be removed please check its address or if it still connected")
        }
    }

    override fun onStatusUpdate(printerData: PrinterData, status: String) {
        Timber.d(TAG, "on status update received fir printer  ${printerData.printerAddress} $status")
    }

    override fun onStatusReceived(printerData: PrinterData, printerStatus: PrinterStatus) {
        runOnUiThread {
            hideProgressBar()
            enableButtons(true)
            showToast(
                "${printerData.printerAddress} \n" +
                    "Connection Status: ${printerStatus.connectionStatus} \n" +
                    "Online Status: ${printerStatus.onlineStatus} \n" +
                    "Cover Open Status: ${printerStatus.coverOpenStatus} \n" +
                    "Paper Status: ${printerStatus.paperStatus} \n" +
                    "Paper Feed Status: ${printerStatus.paperFeedStatus} \n" +
                    "Panel Switch Status: ${printerStatus.panelSwitchStatus} \n" +
                    "Drawer Status: ${printerStatus.drawerStatus} \n" +
                    "Error Status: ${printerStatus.errorStatus} \n" +
                    "Auto-Recovery Error Status: ${printerStatus.autoRecoveryErrorStatus} \n" +
                    "Buzzer Sound Status: ${printerStatus.buzzerSoundStatus} \n" +
                    "Power Adapter Status: ${printerStatus.powerAdapterStatus} \n" +
                    "Battery Level Status: ${printerStatus.batteryLevelStatus} \n",
                Toast.LENGTH_LONG
            )
        }
    }

    private fun showRemainingJobListDialog() {
        val listOfRemainingJob = deviceManager.getListOfRemainingJobs()
        val remainingJobListDialog = RemainingJobListDialog.newInstance(listOfRemainingJob)
        remainingJobListDialog.show(supportFragmentManager, "RemainingJobListDialog")
    }

    override fun onCancelJobs() {
        deviceManager.cancelAllJobsAndUnlock()
        showToast("All jobs have been canceled !")
        enableButtons(true)
        hideProgressBar()
    }

    private fun getConnectTimeout(): Int? =
        binding.edittextMainConnectionTimeout.text?.toString()?.let { timeout ->
            if (timeout.isNotBlank()) {
                timeout.toInt()
            } else {
                null
            }
        }

    private fun getPrintTimeout(): Int? =
        binding.edittextMainPrintTimeout.text?.toString()?.let { timeout ->
            if (timeout.isNotBlank()) {
                timeout.toInt()
            } else {
                null
            }
        }

    override fun onPrinterStatusButtonClick(position: Int, item: PrinterItem) {
        item.printerData?.let { printerData ->
            if (printerData.printerAddress?.isNotBlank() == true) {
                showProgressBar()
                deviceManager.getStatus(printerData)
            } else {
                showToast("This printer address is empty")
            }
        }
    }

    override fun onPrinterSelected(item: PrinterItem) {
        item.printerData?.let { selectedPrinterData ->
            printerDataList.add(selectedPrinterData)
            Timber.d(TAG, "Printer list: \n${getPrinterListString()}")
        }
    }

    override fun onPrinterUnselected(item: PrinterItem) {
        item.printerData?.let { unselectedPrinterData ->
            deviceManager.removePrinter(unselectedPrinterData)
            printerDataList.remove(unselectedPrinterData)
            Timber.d(TAG, "Printer list: \n${getPrinterListString()}")
        }
    }

    private fun getPrinterListString() = printerDataList.joinToString(separator = "\n") { it.printerAddress.toString() }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
