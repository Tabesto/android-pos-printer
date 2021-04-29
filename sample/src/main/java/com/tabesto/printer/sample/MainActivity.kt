package com.tabesto.printer.sample

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.tabesto.printer.model.ticket.FeedCutLine
import com.tabesto.printer.model.ticket.FeedLine
import com.tabesto.printer.model.ticket.ImageLine
import com.tabesto.printer.model.ticket.StringLine
import com.tabesto.printer.model.ticket.StyleLine
import com.tabesto.printer.model.ticket.TicketData
import com.tabesto.printer.model.ticket.TicketData.TicketDataBuilder
import com.tabesto.printer.multiprinter.DeviceManager
import com.tabesto.printer.multiprinter.DeviceManagerConnectListener
import com.tabesto.printer.multiprinter.DeviceManagerDiscoveryListener
import com.tabesto.printer.multiprinter.DeviceManagerImpl
import com.tabesto.printer.multiprinter.DeviceManagerInitListener
import com.tabesto.printer.multiprinter.DeviceManagerListener
import com.tabesto.printer.multiprinter.DeviceManagerPrintListener
import com.tabesto.printer.multiprinter.DeviceManagerStatusListener
import com.tabesto.printer.sample.dialog.DialogPrinterErrorDetails
import com.tabesto.printer.sample.dialog.JobResultListDialog
import com.tabesto.printer.sample.dialog.JobResultListDialogListener
import com.tabesto.printer.sample.dialog.PrinterManagedListDialog
import com.tabesto.printer.sample.dialog.PrinterManagedListDialogListener
import com.tabesto.printer.sample.dialog.RemainingJobListDialog
import com.tabesto.printer.sample.dialog.RemainingJobListDialogListener
import timber.log.Timber

@SuppressLint("LogNotTimber")
class MainActivity : AppCompatActivity(), DeviceManagerListener,
    DeviceManagerConnectListener, DeviceManagerPrintListener,
    DeviceManagerInitListener, DeviceManagerDiscoveryListener, JobResultListDialogListener, PrinterManagedListDialogListener,
    DeviceManagerStatusListener, RemainingJobListDialogListener {
    private lateinit var deviceManager: DeviceManager
    private var printerData: PrinterData? = null
    private var progressBar: ProgressBar? = null
    private var addressEditText: EditText? = null
    private var ticketData: TicketData? = null
    private var buttonShowRemainingJobList: Button? = null
    private val listOfButtons = listOf(
        R.id.buttonDisconnectManual,
        R.id.buttonLaunchDiscovery,
        R.id.buttonPrintManual,
        R.id.buttonConnectPrintManual,
        R.id.buttonStopDiscovery,
        R.id.buttonGetStatus,
        R.id.buttonShowJobResultArchive,
        R.id.buttonPrintAll,
        R.id.buttonDisconnectAll,
        R.id.buttonShowManagedPrinterList,
        R.id.buttonStartExampleActivity
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeWidget()

        setupView()

        buildTicketData()

        initializeLog()

        deviceManager = DeviceManagerImpl.getInstance()
    }

    private fun initializeLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initializeWidget() {
        progressBar = findViewById(R.id.progressBar)
        addressEditText = findViewById(R.id.editTextAddress)

        buttonShowRemainingJobList = findViewById(R.id.buttonShowRemainingJobList)
    }

    private fun setupView() {
        buttonShowRemainingJobList?.setOnClickListener { showRemainingJobListDialog() }
    }

    override fun onResume() {
        super.onResume()
        setDeviceManagerListeners()

        addressEditText?.text?.toString()?.let { address ->
            printerData =
                PrinterData(
                    PrinterModel.PRINTER_TM_M10,
                    PrinterRegion.PRINTER_ANK,
                    address,
                    ConnectionMode.MANUAL,
                    PrinterType.PRINTER_EPSON
                )

            printerData?.let { data ->
                Log.d(TAG, " just before initialize object ")
                deviceManager.initializePrinter(data, this)
            }
        }
    }

    private fun setDeviceManagerListeners() {
        deviceManager.setListeners(
            deviceManagerInitListener = this,
            deviceManagerConnectListener = this,
            deviceManagerPrintListener = this,
            deviceManagerDiscoveryListener = this,
            deviceManagerStatusListener = this,
            deviceManagerListener = this
        )
    }

    /**
     * Build ticket data with TicketBuilder in order to pass data to printer to print data
     *
     */
    private fun buildTicketData() {
        val logoData = BitmapFactory.decodeResource(this.resources, R.drawable.tabestologoresized)
        val ticketDataBuilder = TicketDataBuilder()
        val imageLineBuilder = ImageLine.ImageLineBuilder()
        imageLineBuilder.withBitmap(logoData)
        ticketData = ticketDataBuilder
            .withImageLine(imageLineBuilder.build())
            .withFeedLine(FeedLine())
            .withLine(StringLine("THE STORE 123 (555) 555 – 5555", StyleLine()))
            .withFeedLine(FeedLine())
            .withLine(StringLine("------------------------------", StyleLine()))
            .withLine(StringLine("400 OHEIDA 3PK SPRINGF  9.99 R", StyleLine()))
            .withLine(StringLine("410 3 CUP BLK TEAPOT    9.99 R", StyleLine()))
            .withLine(StringLine("------------------------------", StyleLine()))
            .withFeedLine(FeedLine())
            .withLine(StringLine("TOTAL    174.81", StyleLine(textHeight = 2, textWidth = 2)))
            .withFeedLine(FeedLine())
            .withLine(StringLine(this.resources.getString(R.string.powered_tabesto), StyleLine()))
            .withFeedCutLine(FeedCutLine())
            .build()
    }

    @Suppress("UNUSED_PARAMETER")
    fun connectInManualMode(view: View) {
        addressEditText?.text?.toString()?.let { address ->
            Log.d(TAG, " onClick print printReceiptInManualMode ")
            showProgressBar()
            enableButtons(false)
            printerData =
                PrinterData(
                    PrinterModel.PRINTER_TM_M10,
                    PrinterRegion.PRINTER_ANK,
                    address,
                    ConnectionMode.MANUAL,
                    PrinterType.PRINTER_EPSON
                )

            printerData?.let { data ->
                Log.d(TAG, " just before initialize object ")
                deviceManager.initializePrinter(data, this)
                deviceManager.connectPrinter(data)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun launchPrintManual(view: View) {
        Log.d(TAG, " onClick print printReceiptInManualMode ")

        addressEditText?.text?.toString()?.let { address ->
            showProgressBar()
            enableButtons(false)

            val printerData =
                PrinterData(
                    PrinterModel.PRINTER_TM_M10,
                    PrinterRegion.PRINTER_ANK,
                    address,
                    ConnectionMode.MANUAL,
                    PrinterType.PRINTER_EPSON
                )

            ticketData?.let { ticketData ->
                Log.d(TAG, " onClick print launchPrintManual")
                deviceManager.printData(printerData, ticketData)
            }

        } ?: showToast(" no address given in input")
    }

    @Suppress("UNUSED_PARAMETER")
    fun launchPrintOnAllPrinter(view: View) {
        showProgressBar()
        enableButtons(false)
        Log.d(TAG, " onClick print launchPrintOnAllPrinter")
        ticketData?.let { ticketData ->
            deviceManager.printData(ticketData = ticketData)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun disconnectPrinterInManualMode(view: View) {
        Log.d(TAG, " onClick print disconnectPrinterInManualMode ")
        addressEditText?.text?.toString()?.let { address ->
            showProgressBar()
            enableButtons(false)

            val printerData =
                PrinterData(
                    PrinterModel.PRINTER_TM_M10,
                    PrinterRegion.PRINTER_ANK,
                    address,
                    ConnectionMode.MANUAL,
                    PrinterType.PRINTER_EPSON
                )
            deviceManager.disconnectPrinter(printerData)

        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun disconnectAllPrinter(view: View) {
        showProgressBar()
        enableButtons(false)
        Log.d(TAG, " onClick  disconnectAllPrinter")
        deviceManager.disconnectPrinter()
    }

    @Suppress("UNUSED_PARAMETER")
    fun launchDiscovery(view: View) {
        Log.d(TAG, " onClick launchDiscovery ")
        showProgressBar()
        enableButtons(false)
        try {
            printerData?.let { printerData ->
                deviceManager.restartBluetoothAndLaunchDiscovery(printerData)
            }
        } catch (exception: Exception) {
            Log.e(TAG, "an error happened while trying to discover printer in activity ${exception.message}")
            hideProgressBar()
            showToast("an error happened while trying to discover printer in main activity ")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun stopDiscovery(view: View) {
        Log.d(TAG, " onClick stopDiscovery ")
        try {
            showProgressBar()
            printerData?.let { printerData ->
                deviceManager.stopDiscovery(printerData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "an error happened while trying to stop discovering  printer in main activity " + e.message)
            hideProgressBar()
            enableButtons(true)
            showToast("an error happened while trying to stop discovering printer in main activity ")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun getStatus(view: View) {
        addressEditText?.text?.toString()?.let { address ->
            showProgressBar()

            val printerData =
                PrinterData(
                    PrinterModel.PRINTER_TM_M10,
                    PrinterRegion.PRINTER_ANK,
                    address,
                    ConnectionMode.MANUAL,
                    PrinterType.PRINTER_EPSON
                )

            deviceManager.getStatus(printerData)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun showJobResultHistoryList(view: View) {
        openDialogWithJobResultList(canEnableClearListButton = true)
    }

    @Suppress("UNUSED_PARAMETER")
    fun showManagedPrinterList(view: View) {
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
                showToast(" The list of printer is empty ")
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun startExampleActivity(view: View) {
        val intent = Intent(this, ExampleActivity::class.java)
        startActivity(intent)
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
            showToast(" The list of history job is empty ")
        }
    }

    private fun showProgressBar() {
        Log.d(TAG, " show progress bar")
        progressBar?.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        Log.d(TAG, " hide progress bar")
        progressBar?.visibility = View.INVISIBLE
    }

    private fun enableButtons(enable: Boolean) {
        listOfButtons.map { buttonId ->
            val button: Button = findViewById(buttonId)
            button.isEnabled = enable
        }
    }

    override fun onDeviceManagerErrorForAJobResult(deviceManagerJobResult: DeviceManagerJobResult, isLastReturnOfJob: Boolean) {
        Log.e(
            TAG,
            "onDeviceManagerErrorForAJobResult message " +
                "${deviceManagerJobResult.printerData.printerAddress} ${deviceManagerJobResult.deviceManagerException?.error}"
        )

        if (isLastReturnOfJob) {
            runOnUiThread {
                hideProgressBar()
                showToast("all jobs ended with an error ")
                enableButtons(true)
            }
            openDialogWithJobResultList()
        }
    }

    override fun onListOfPrintersEmpty(scope: ScopeTag) {
        Log.d(TAG, " in onListOfPrintersEmpty in ${scope.name}")
        runOnUiThread {
            hideProgressBar()
            showToast(" there is no printer managed ")
            enableButtons(true)
        }
    }

    override fun onConnectResult(listOfJobsResult: List<DeviceManagerJobResult>) {
        for (jobResult in listOfJobsResult) {
            if (jobResult.isSuccessful) {
                Log.d(TAG, "successfully connected ${jobResult.printerData.printerAddress}")
                runOnUiThread {
                    showToast("successfully connected")
                }
            } else {
                Log.e(
                    TAG,
                    "onConnectFailure error message ${jobResult.printerData.printerAddress} : ${jobResult.printerException?.message}"
                )
                runOnUiThread {
                    showToast("an error happened while trying to connect printer")
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
                Log.d(TAG, "successfully disconnected ${jobResult.printerData.printerAddress}")
                runOnUiThread {
                    showToast("successfully disconnected")
                }
            } else {
                Log.e(
                    TAG,
                    "onFailureDisconnectPrinter error message ${jobResult.printerData.printerAddress} : ${jobResult.printerException?.message}"
                )
                runOnUiThread {
                    showToast("An error happen while trying to disconnect from the printer(s) ")
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
                Log.d(TAG, "successfully print  ${jobResult.printerData.printerAddress}")
                runOnUiThread {
                    showToast("jobs printed successfully")
                }
            } else {
                Log.e(TAG, "onPrintFailure error message ${jobResult.printerData.printerAddress} : ${jobResult.printerException?.message}")
                runOnUiThread {
                    showToast("An error happen while trying to print ticket ")
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
        Log.e(TAG, "onInitPrinterFailure error message ${printerData.printerAddress} : ${printerException.message}")

        runOnUiThread {
            hideProgressBar()
            showToast("an error happened while trying to init print")
            enableButtons(true)
        }
    }

    override fun onDiscoveryFinish(printerData: PrinterData, isAvailable: Boolean) {
        Log.d(TAG, "onFinishDiscovery")
        runOnUiThread {
            Log.d(TAG, " onFinishDiscovery ")
            hideProgressBar()
            if (isAvailable) {
                Log.d(TAG, " true ")
                showToast(" Yeah printer is available ! ${printerData.printerAddress}")
            } else {
                Log.d(TAG, " false ")
                showToast(" Oh No printer is not available ! ${printerData.printerAddress}")
            }
            enableButtons(true)
        }
    }

    override fun onDiscoveryFailure(printerData: PrinterData, printerException: PrinterException) {
        Log.d(TAG, "onFailureDiscovery" + printerException.message)
        runOnUiThread {
            hideProgressBar()
            showToast(" an error occurred while discovering printer  ${printerData.printerAddress}")
            enableButtons(true)
        }
    }

    override fun onDiscoveryStopSuccess(printerData: PrinterData) {
        Log.d(TAG, "onDiscoveryStoppedSuccessfully  ${printerData.printerAddress}")
        hideProgressBar()
    }

    override fun onBluetoothRestartSuccess(printerData: PrinterData) {
        Log.d(TAG, "onBluetoothRestartedSuccessfully")
        showToast(" Bluetooth is restarted successfully ${printerData.printerAddress}")
        enableButtons(true)
    }

    private fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, length).show()
    }

    override fun clearListOfJobsResult() {
        deviceManager.clearJobsResultHistoryList()
        showToast(" list cleared !")
    }

    override fun onJobResultErrorSelected(jobResult: DeviceManagerJobResult) {
        val dialogPrinterErrorDetails = DialogPrinterErrorDetails().newInstance(jobResult)
        dialogPrinterErrorDetails.show(supportFragmentManager, "DialogPrinterErrorDetails")
    }

    override fun onPrinterSelected(printerData: PrinterData) {
        addressEditText?.setText(printerData.printerAddress)
        showToast("printer address is selected")
    }

    override fun onRemovePrinter(printerData: PrinterData) {
        val result = deviceManager.removePrinter(printerData)

        if (result) {
            showToast("printer is removed successfully !")
        } else {
            showToast("printer can't be removed please check its address or if it stills connected")
        }
    }

    override fun onStatusUpdate(printerData: PrinterData, status: String) {
        Log.d(TAG, "on status update received fir printer  ${printerData.printerAddress} $status")
    }

    override fun onStatusReceived(printerData: PrinterData, printerStatus: PrinterStatus) {

        runOnUiThread {
            hideProgressBar()
            enableButtons(true)
            showToast(
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

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
