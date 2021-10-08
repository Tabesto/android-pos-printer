package com.tabesto.printer.multiprinter

import android.annotation.SuppressLint
import android.content.Context
import com.tabesto.printer.Printer
import com.tabesto.printer.PrinterConnectListener
import com.tabesto.printer.PrinterFactory
import com.tabesto.printer.PrinterInitListener
import com.tabesto.printer.PrinterPrintListener
import com.tabesto.printer.PrinterStatusListener
import com.tabesto.printer.dagger.DaggerDeviceManagerComponent
import com.tabesto.printer.dagger.DeviceManagerModule
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterManaged
import com.tabesto.printer.model.PrinterRemainingJob
import com.tabesto.printer.model.ScopeTag
import com.tabesto.printer.model.ScopeTag.CONNECT
import com.tabesto.printer.model.ScopeTag.DISCONNECT
import com.tabesto.printer.model.ScopeTag.GET_STATUS
import com.tabesto.printer.model.ScopeTag.INITIALIZE
import com.tabesto.printer.model.ScopeTag.PRINT_DATA
import com.tabesto.printer.model.ScopeTag.PRINT_DATA_ON_DEMAND
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult
import com.tabesto.printer.model.error.DeviceManagerException
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.status.PrinterStatus
import com.tabesto.printer.model.ticket.TicketData
import com.tabesto.printer.utils.Constants.MAIN_JOB_IS_RUNNING
import com.tabesto.printer.utils.Constants.PRINTER_DATA_NOT_MANAGED
import com.tabesto.printer.utils.SingletonHolder
import com.tabesto.printer.utils.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class DeviceManagerImpl internal constructor() : DeviceManager, PrinterInitListener, PrinterConnectListener, PrinterPrintListener,
    PrinterStatusListener {
    private val printerFactory: PrinterFactory = PrinterFactory()
    private var deviceManagerInitListener: DeviceManagerInitListener? = null
    private var deviceManagerConnectListener: DeviceManagerConnectListener? = null
    private var deviceManagerPrintListener: DeviceManagerPrintListener? = null
    private var deviceManagerStatusListener: DeviceManagerStatusListener? = null
    private val listOfJobs: HashMap<PrinterData, ScopeTag> = HashMap()
    private val listOfJobsResult: MutableList<DeviceManagerJobResult> = mutableListOf()
    private val listOfHistoryJobsResult: MutableList<DeviceManagerJobResult> = mutableListOf()
    private val logger: Logger = Logger(DeviceManagerImpl::class.java.simpleName)
    private var numberOfPrinterWithOperation = 0
    private var counterPrinterNotManaged = 0
    override var deviceManagerListener: DeviceManagerListener? = null

    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

    @Inject
    lateinit var listOfPrinters: HashMap<PrinterData, Printer>

    @Inject
    lateinit var coroutineScope: CoroutineScope

    /**
     * mainJobIsRunning variable will allow us to lock some public instruction when other job are running
     * multiple initialization, one connection, multiple connection, one printing, multiple printing, one
     * disconnection, multiple disconnection. if one of those method above is running you cannot run another
     * method of above list at the same time
     */
    internal var mainJobIsRunning = false

    init {
        enableLogs()
        DaggerDeviceManagerComponent.builder().deviceManagerModule(DeviceManagerModule()).build().inject(this)
    }

    override fun initializePrinter(printerData: PrinterData, context: Context) {
        if (!mainJobIsRunning) {
            if (listOfPrinters[printerData] == null) {
                addPrinterToList(printerData, context)
            }
        } else {
            logger.d("initializePrinter one printer data blocked mainJobIsRunning is running")
            returnMainJobIsRunning(printerData, INITIALIZE)
        }
    }

    override fun initializePrinter(listOfPrinterData: List<PrinterData>, context: Context) {
        if (!mainJobIsRunning) {
            mainJobIsRunning = true
            addPrinterList(listOfPrinterData, context)
            mainJobIsRunning = false
        } else {
            logger.d("initializePrinter list of printer data blocked mainJobIsRunning is running")
            returnMainJobIsRunning(listOfPrinterData, INITIALIZE)
        }
    }

    private fun addPrinterList(listOfPrinterData: List<PrinterData>, context: Context) {
        listOfPrinterData.forEach { printerData ->
            if (!listOfPrinters.containsKey(printerData)) {
                addPrinterToList(printerData, context)
            }
        }
    }

    private fun addPrinterToList(printerData: PrinterData, context: Context) {
        val printer: Printer = printerFactory.getPrinter(printerData, context)
        printer.initializePrinter()
        printer.setListeners(this, this, this, this)
        listOfPrinters[printerData] = printer
    }

    override fun setInitListener(deviceManagerInitListener: DeviceManagerInitListener) {
        this.deviceManagerInitListener = deviceManagerInitListener
    }

    override fun setConnectListener(deviceManagerConnectListener: DeviceManagerConnectListener) {
        this.deviceManagerConnectListener = deviceManagerConnectListener
    }

    override fun setPrintListener(deviceManagerPrintListener: DeviceManagerPrintListener) {
        this.deviceManagerPrintListener = deviceManagerPrintListener
    }

    override fun setStatusListener(deviceManagerStatusListener: DeviceManagerStatusListener) {
        this.deviceManagerStatusListener = deviceManagerStatusListener
    }

    override fun connectPrinter(printerData: PrinterData, timeout: Int?) {
        if (!mainJobIsRunning) {
            mainJobIsRunning = true

            val listOfPrinterDataManaged = initializeJobList(printerData, CONNECT)

            listOfPrinterDataManaged.forEach {
                listOfPrinters[it]?.connectPrinter(timeout)
            }
        } else {
            logger.d("connectPrinter blocked mainJobIsRunning is running")
            logRemainingJobs()
            returnMainJobIsRunning(printerData, CONNECT)
        }
    }

    override fun connectPrinter(listOfPrinterData: List<PrinterData>, timeout: Int?) {
        if (!mainJobIsRunning) {
            mainJobIsRunning = true

            val listOfPrinterDataManaged = initializeJobList(listOfPrinterData, CONNECT)

            listOfPrinterDataManaged.forEach {
                listOfPrinters[it]?.connectPrinter(timeout)
            }
        } else {
            logger.d("connectPrinter blocked mainJobIsRunning is running")
            logRemainingJobs()
            returnMainJobIsRunning(listOfPrinterData, CONNECT)
        }
    }

    override fun connectPrinter(timeout: Int?) {
        launchBatchOfJobIfPrinterListNotEmpty(scope = CONNECT, connectTimeout = timeout)
    }

    override fun printData(printerData: PrinterData, ticketData: TicketData, timeout: Int?) {
        val listOfPrinterDataManaged = initializeJobList(printerData, PRINT_DATA)

        listOfPrinterDataManaged.forEach {
            listOfPrinters[it]?.printData(ticketData, timeout)
        }
    }

    override fun printDataOnDemand(printerData: PrinterData, ticketData: TicketData, connectTimeout: Int?, printTimeout: Int?) {
        val listOfPrinterDataManaged = initializeJobList(printerData, PRINT_DATA)

        listOfPrinterDataManaged.forEach {
            listOfPrinters[it]?.printDataOnDemand(ticketData, connectTimeout, printTimeout)
        }
    }

    override fun printData(listOfPrinterData: List<PrinterData>, ticketData: TicketData, timeout: Int?) {
        val listOfPrinterDataManaged = initializeJobList(listOfPrinterData, PRINT_DATA)

        listOfPrinterDataManaged.forEach {
            listOfPrinters[it]?.printData(ticketData, timeout)
        }
    }

    override fun printDataOnDemand(
        listOfPrinterData: List<PrinterData>,
        ticketData: TicketData,
        connectTimeout: Int?,
        printTimeout: Int?
    ) {
        val listOfPrinterDataManaged = initializeJobList(listOfPrinterData, PRINT_DATA)

        listOfPrinterDataManaged.forEach {
            listOfPrinters[it]?.printDataOnDemand(ticketData, connectTimeout, printTimeout)
        }
    }

    override fun printData(ticketData: TicketData, timeout: Int?) {
        launchBatchOfJobIfPrinterListNotEmpty(PRINT_DATA, ticketData, timeout)
    }

    override fun printDataOnDemand(ticketData: TicketData, connectTimeout: Int?, printTimeout: Int?) {
        launchBatchOfJobIfPrinterListNotEmpty(PRINT_DATA_ON_DEMAND, ticketData, connectTimeout, printTimeout)
    }

    override fun disconnectPrinter(printerData: PrinterData) {
        if (!mainJobIsRunning) {
            mainJobIsRunning = true

            val listOfPrinterDataManaged = initializeJobList(printerData, DISCONNECT)

            listOfPrinterDataManaged.forEach {
                listOfPrinters[it]?.disconnectPrinter()
            }
        } else {
            logger.d("disconnectPrinter blocked mainJobIsRunning is running")
            logRemainingJobs()
            returnMainJobIsRunning(printerData, DISCONNECT)
        }
    }

    override fun disconnectPrinter(listOfPrinterData: List<PrinterData>) {
        if (!mainJobIsRunning) {
            mainJobIsRunning = true

            val listOfPrinterDataManaged = initializeJobList(listOfPrinterData, DISCONNECT)

            listOfPrinterDataManaged.forEach {
                listOfPrinters[it]?.disconnectPrinter()
            }
        } else {
            logger.d("disconnectPrinter blocked mainJobIsRunning is running")
            returnMainJobIsRunning(listOfPrinterData, DISCONNECT)
        }
    }

    override fun disconnectPrinter() {
        launchBatchOfJobIfPrinterListNotEmpty(DISCONNECT)
    }

    private fun launchBatchOfJobIfPrinterListNotEmpty(
        scope: ScopeTag,
        ticketData: TicketData? = null,
        connectTimeout: Int? = null,
        printTimeout: Int? = null
    ) {
        when (scope) {
            PRINT_DATA -> {
                checkIfListOfPrinterIsEmpty(scope)
                listOfPrinters.forEach { ticketData?.let { data -> it.value.printData(data, connectTimeout) } }
            }
            PRINT_DATA_ON_DEMAND -> {
                checkIfListOfPrinterIsEmpty(scope)
                listOfPrinters.forEach { ticketData?.let { data -> it.value.printDataOnDemand(data, connectTimeout, printTimeout) } }
            }
            else -> {
                if (mainJobIsRunning) {
                    logger.d("launchBatchOfJobIfPrinterListNotEmpty blocked mainJobIsRunning is running $scope")
                    returnMainJobIsRunning(listOfPrinters.keys.toList(), scope)
                    logRemainingJobs()
                    return
                }

                checkIfListOfPrinterIsEmpty(scope)

                mainJobIsRunning = true
                initializeJobList(listOfPrinters.keys.toList(), scope)

                listOfPrinters.forEach {
                    when (scope) {
                        CONNECT -> it.value.connectPrinter(connectTimeout)
                        DISCONNECT -> it.value.disconnectPrinter()
                        else -> logger.i(" we are in launchBatchOfJobIfPrinterListNotEmpty , an unknown scope has been called ${scope.name} ")
                    }
                }
            }
        }
    }

    private fun checkIfListOfPrinterIsEmpty(scope: ScopeTag) {
        if (listOfPrinters.isEmpty()) {
            mainJobIsRunning = false
            deviceManagerListener?.onListOfPrintersEmpty(scope)
            return
        }
    }

    private fun initializeJobList(printerData: PrinterData, scope: ScopeTag): List<PrinterData> {
        numberOfPrinterWithOperation = 1

        val listOfPrinterDataManaged: MutableList<PrinterData> = mutableListOf()

        listOfPrinters[printerData]?.let {
            listOfJobs[it.printerData] = scope
            listOfPrinterDataManaged.add(it.printerData)
        } ?: returnDeviceManagerExceptionForAJob(printerData, scope)

        return listOfPrinterDataManaged
    }

    private fun initializeJobList(listOfPrinterData: List<PrinterData>, scope: ScopeTag): List<PrinterData> {
        numberOfPrinterWithOperation = listOfPrinterData.size

        val listOfPrinterDataManaged: MutableList<PrinterData> = mutableListOf()

        listOfPrinterData.forEach { printerData ->
            listOfPrinters[printerData]?.let {
                listOfJobs[it.printerData] = scope
                listOfPrinterDataManaged.add(it.printerData)
            } ?: returnDeviceManagerExceptionForAJob(printerData, scope)
        }

        return listOfPrinterDataManaged
    }

    override fun getStatus(printerData: PrinterData) {
        val listOfPrinterDataManaged = initializeJobList(printerData, GET_STATUS)
        listOfPrinterDataManaged.forEach { listOfPrinters[it]?.getStatus() }
    }

    private fun returnDeviceManagerExceptionForAJob(
        printerData: PrinterData,
        scopeTag: ScopeTag,
        errorMessage: String = PRINTER_DATA_NOT_MANAGED
    ) {
        counterPrinterNotManaged++

        val deviceManagerException = DeviceManagerException(errorMessage)
        val jobResult = DeviceManagerJobResult(
            printerData = printerData,
            scopeTag = scopeTag,
            isSuccessful = false,
            datetime = getCurrentDateTime(),
            deviceManagerException = deviceManagerException
        )
        listOfHistoryJobsResult.add(jobResult)

        if (counterPrinterNotManaged == numberOfPrinterWithOperation) {
            mainJobIsRunning = false
            resetCounterPrinterNotManagedAndNumberOfPrinterWithOperation()
            deviceManagerListener?.onDeviceManagerErrorForAJobResult(jobResult, true)
        } else {
            deviceManagerListener?.onDeviceManagerErrorForAJobResult(jobResult, false)
        }
    }

    private fun resetCounterPrinterNotManagedAndNumberOfPrinterWithOperation() {
        counterPrinterNotManaged = 0
        numberOfPrinterWithOperation = 0
    }

    override fun onInitPrinterFailure(printerData: PrinterData, printerException: PrinterException) {
        listOfPrinters.remove(printerData)
        deviceManagerInitListener?.onInitPrinterFailure(printerData, printerException)
    }

    override fun onDisconnectSuccess(printerData: PrinterData) {
        removeJobAndAddJobResultInList(printerData, DISCONNECT, true)

        if (listOfJobs.isEmpty()) {
            val listOfJobToReturn = ArrayList(listOfJobsResult)
            addResultsToHistoryAndUnlockMainJobAndResetCounters()
            deviceManagerConnectListener?.onDisconnectResult(listOfJobToReturn)
        }
    }

    override fun onDisconnectFailure(printerData: PrinterData, printerException: PrinterException) {
        removeJobAndAddJobResultInList(printerData, DISCONNECT, false, printerException = printerException)
        if (listOfJobs.isEmpty()) {
            val listOfJobToReturn = ArrayList(listOfJobsResult)
            addResultsToHistoryAndUnlockMainJobAndResetCounters()
            deviceManagerConnectListener?.onDisconnectResult(listOfJobToReturn)
        }
    }

    override fun onConnectFailure(printerData: PrinterData, printerException: PrinterException) {
        removeJobAndAddJobResultInList(printerData, CONNECT, false, printerException = printerException)
        if (listOfJobs.isEmpty()) {
            val listOfJobToReturn = ArrayList(listOfJobsResult)
            addResultsToHistoryAndUnlockMainJobAndResetCounters()
            deviceManagerConnectListener?.onConnectResult(listOfJobToReturn)
        }
    }

    override fun onConnectSuccess(printerData: PrinterData) {
        removeJobAndAddJobResultInList(printerData, CONNECT, true)
        if (listOfJobs.isEmpty()) {
            val listOfJobToReturn = ArrayList(listOfJobsResult)
            addResultsToHistoryAndUnlockMainJobAndResetCounters()
            deviceManagerConnectListener?.onConnectResult(listOfJobToReturn)
        }
    }

    override fun onPrintSuccess(printerData: PrinterData, printerStatus: PrinterStatus) {
        removeJobAndAddJobResultInList(printerData, PRINT_DATA, true)
        val listOfJobToReturn = ArrayList(listOfJobsResult)
        addResultsToHistoryAndUnlockMainJobAndResetCounters()
        deviceManagerPrintListener?.onPrintResult(listOfJobToReturn)
    }

    override fun onPrintFailure(printerData: PrinterData, printerException: PrinterException) {
        onPrintFailureManage(printerData, printerException)
    }

    override fun onPrintDataFailure(printerData: PrinterData, printerException: PrinterException) {
        onPrintFailureManage(printerData, printerException)
    }

    private fun onPrintFailureManage(printerData: PrinterData, printerException: PrinterException) {
        removeJobAndAddJobResultInList(printerData, PRINT_DATA, false, printerException = printerException)
        val listOfJobToReturn = ArrayList(listOfJobsResult)
        addResultsToHistoryAndUnlockMainJobAndResetCounters()
        deviceManagerPrintListener?.onPrintResult(listOfJobToReturn)
    }

    private fun returnMainJobIsRunning(printerData: PrinterData, scope: ScopeTag) {
        val deviceManagerException = DeviceManagerException(MAIN_JOB_IS_RUNNING)

        val jobResult = DeviceManagerJobResult(
            printerData = printerData,
            scopeTag = scope,
            isSuccessful = false,
            datetime = getCurrentDateTime(),
            deviceManagerException = deviceManagerException
        )
        listOfHistoryJobsResult.add(jobResult)

        deviceManagerListener?.onDeviceManagerErrorForAJobResult(jobResult, true)
    }

    private fun returnMainJobIsRunning(listOfPrinterData: List<PrinterData>, scope: ScopeTag) {
        listOfPrinterData.forEachIndexed { index, printerData ->
            val deviceManagerException = DeviceManagerException(MAIN_JOB_IS_RUNNING)

            val jobResult = DeviceManagerJobResult(
                printerData = printerData,
                scopeTag = scope,
                isSuccessful = false,
                datetime = getCurrentDateTime(),
                deviceManagerException = deviceManagerException
            )
            listOfHistoryJobsResult.add(jobResult)

            val isLastReturnOfJob: Boolean = index == listOfPrinterData.lastIndex
            deviceManagerListener?.onDeviceManagerErrorForAJobResult(jobResult, isLastReturnOfJob)
        }
    }

    private fun removeJobAndAddJobResultInList(
        printerData: PrinterData,
        scopeTag: ScopeTag,
        isSuccessful: Boolean = true,
        deviceManagerException: DeviceManagerException? = null,
        printerException: PrinterException? = null
    ) {
        listOfJobs.remove(printerData)
        listOfJobsResult.add(
            DeviceManagerJobResult(
                printerData = printerData,
                scopeTag = scopeTag,
                isSuccessful = isSuccessful,
                datetime = getCurrentDateTime(),
                deviceManagerException = deviceManagerException,
                printerException = printerException
            )
        )
    }

    private fun addResultsToHistoryAndUnlockMainJobAndResetCounters() {
        listOfHistoryJobsResult.addAll(listOfJobsResult)
        listOfJobsResult.clear()
        resetCounterPrinterNotManagedAndNumberOfPrinterWithOperation()
        mainJobIsRunning = false
    }

    override fun getManagedPrinterDataList() = listOfPrinters.keys.toList()

    override fun getManagedPrinterDataAndStatusList() {
        getCurrentCoroutineScope().launch {
            val listOfPrinterDataAndPrinterStatus: List<PrinterManaged> = listOfPrinters.map { (printerData, printer) ->
                initializeJobList(printerData, GET_STATUS)
                val currentPrinterStatus = printer.getStatusRaw()
                removeJobAndAddJobResultInList(printerData, GET_STATUS, true)
                addResultsToHistoryAndUnlockMainJobAndResetCounters()
                PrinterManaged(printerData, currentPrinterStatus)
            }
            ensureActive()
            deviceManagerListener?.onListOfPrinterManagedReceived(listOfPrinterDataAndPrinterStatus)
        }
    }

    private fun getCurrentCoroutineScope(): CoroutineScope {
        if (!coroutineScope.isActive) {
            DaggerDeviceManagerComponent.builder().deviceManagerModule(DeviceManagerModule(listOfPrinters = listOfPrinters)).build()
                .inject(this)
        }
        return coroutineScope
    }

    override fun getJobsResultHistoryList() = listOfHistoryJobsResult

    override fun clearJobsResultHistoryList() = listOfHistoryJobsResult.clear()

    override fun enableLogs(enable: Boolean) {
        logger.enableLogger(enable)
    }

    override fun removePrinter(printerData: PrinterData): Boolean {
        if (!mainJobIsRunning) {
            logger.d("removePrinter mainJobIsRunning is NOT running")
            listOfPrinters[printerData]?.let { printer ->
                val printerStatus = printer.getStatusRaw()
                if (printerStatus.connectionStatus.isConnected == false) {
                    listOfPrinters.remove(printerData)
                    return true
                }
            }
        }
        logRemainingJobs()
        return false
    }

    override fun removePrinter(): Boolean {
        if (!mainJobIsRunning) {
            logger.d("removePrinter mainJobIsRunning is NOT running")
            listOfPrinters.forEach { (printerData, printer) ->
                val printerStatus = printer.getStatusRaw()
                if (printerStatus.connectionStatus.isConnected == false) {
                    listOfPrinters.remove(printerData)
                }
            }
            return true
        }
        logRemainingJobs()
        return false
    }

    override fun cancelAllJobsAndUnlock() {
        getCurrentCoroutineScope().cancel()
        listOfPrinters.forEach { (_, printer) -> printer.cancelAllJob() }
        listOfJobs.clear()
        addResultsToHistoryAndUnlockMainJobAndResetCounters()
    }

    override fun getListOfRemainingJobs(): List<PrinterRemainingJob> =
        listOfJobs.map { (printerData, scope) -> PrinterRemainingJob(printerData, scope) }

    private fun logRemainingJobs() {
        logger.i(" in logRemainingJobs ")
        for ((printerData, scope) in listOfJobs) {
            logger.i(" remaining job for this address ${printerData.printerAddress} and scope ${scope.name}")
        }
    }

    private fun getCurrentDateTime() = dateFormat.format(Date())

    companion object : SingletonHolder<DeviceManagerImpl>(DeviceManagerImpl())

    override fun onStatusUpdate(printerData: PrinterData, status: String) {
        deviceManagerStatusListener?.onStatusUpdate(printerData, status)
    }

    override fun onStatusReceived(printerData: PrinterData, printerStatus: PrinterStatus) {
        removeJobAndAddJobResultInList(printerData, GET_STATUS, true)
        addResultsToHistoryAndUnlockMainJobAndResetCounters()
        deviceManagerStatusListener?.onStatusReceived(printerData, printerStatus)
    }
}
