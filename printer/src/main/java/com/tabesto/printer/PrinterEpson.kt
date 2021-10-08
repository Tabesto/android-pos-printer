package com.tabesto.printer

import android.content.Context
import com.epson.epos2.Epos2CallbackCode
import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.PrinterStatusInfo
import com.epson.epos2.printer.ReceiveListener
import com.epson.epos2.printer.StatusChangeListener
import com.tabesto.printer.dagger.DaggerPrinterEpsonComponent
import com.tabesto.printer.dagger.PrinterEpsonModule
import com.tabesto.printer.model.Action
import com.tabesto.printer.model.ConnectionMode
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterLogInfo
import com.tabesto.printer.model.ScopeTag
import com.tabesto.printer.model.error.EposException
import com.tabesto.printer.model.error.PrinterCallbackCode
import com.tabesto.printer.model.error.PrinterError
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.error.PrinterException.PrinterExceptionBuilder
import com.tabesto.printer.model.status.PrinterStatus
import com.tabesto.printer.model.ticket.TicketData
import com.tabesto.printer.utils.AppExceptionHandler
import com.tabesto.printer.utils.EposPrinter
import com.tabesto.printer.utils.EposStatusMessageManager
import com.tabesto.printer.utils.log.Logger
import com.tabesto.printer.utils.log.LoggerExtraArgument
import com.tabesto.printer.utils.toConnectTimeout
import com.tabesto.printer.utils.toPrintTimeout
import com.tabesto.printer.writer.PrinterWriter
import com.tabesto.printer.writer.PrinterWriterListener
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

/**
 * Printer class of printer module is the entry point of any outside application in order to interact
 * with printer device (managed by caller application)
 * This class create a printer
 * @param printerData object gives the possibility to initialize a printer object of SDK
 */
class PrinterEpson constructor(override var printerData: PrinterData, override var context: Context) : ReceiveListener,
    PrinterWriterListener, Printer, StatusChangeListener {

    private var printerInitListener: PrinterInitListener? = null
    private var printerConnectListener: PrinterConnectListener? = null
    private var printerPrintListener: PrinterPrintListener? = null
    private var printerStatusListener: PrinterStatusListener? = null
    private var logger: Logger = Logger(PrinterEpson::class.java.simpleName)
    private val actionQueue = ConcurrentLinkedQueue<Action>()
    private var pendingAction: Action? = null

    @Inject
    lateinit var printer: EposPrinter

    @Inject
    lateinit var printerWriter: PrinterWriter

    @Inject
    lateinit var coroutineScope: CoroutineScope

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        lateinit var printerException: PrinterException
        val printerExceptionBuilder = PrinterExceptionBuilder()
        printerException = if (exception is Epos2Exception) {
            val printerError = PrinterError.getPrinterException(exception.errorStatus)
            printerExceptionBuilder.withPrinterError(printerError).build()
        } else {
            printerExceptionBuilder.withPrinterError(errorMessage = exception.message).build()
        }
        val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
        logger.e(printerLogInfo, LoggerExtraArgument("method-name", "coroutineExceptionHandler"))
        printerPrintListener?.onPrintFailure(printerData, printerException)
    }

    init {
        // Default injection of EposPrinter in order to avoid crash null pointer because it is a late init for injection
        DaggerPrinterEpsonComponent.builder().printerEpsonModule(
            PrinterEpsonModule(
                context = this.context,
                printerWriterListener = this
            )
        )
            .build()
            .inject(this)

        logger.enablePrinterStatusFullInLogs()
        logger.enablePrinterDataFullInLogs()

        Thread.setDefaultUncaughtExceptionHandler(AppExceptionHandler())
    }

    @Synchronized
    private fun enqueueAction(action: Action) {
        logger.i("Add $action into the queue")
        actionQueue.add(action)
        logger.i("Remaining actions in queue for $printer $printerData (after enqueuing) : $actionQueue")
        if (pendingAction == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun doNextOperation() {
        if (pendingAction != null) {
            logger.e("Operation called when an operation is pending! Aborting.")
            return
        }

        val action = actionQueue.poll() ?: run {
            logger.d("Operation queue is empty! Aborting.")
            return
        }
        pendingAction = action

        logger.i("Operation on $action called.")

        when (action) {
            is Action.Connect -> connectPrinter(action.timeout)
            is Action.Print -> action.ticketData?.let { printData(it, action.timeout) }
            Action.Disconnect -> disconnectPrinter()
        }
    }

    @Synchronized
    private fun signalEndOfAction(withError: Boolean? = false) {
        val pendingActionLogMessage = if (withError == true) {
            "End of $pendingAction with error, do not continue queue processing.  Clear Queue."
        } else {
            "End of $pendingAction without error, continue queue processing"
        }
        pendingAction = null
        if (withError == true) {
            logger.i(pendingActionLogMessage)
            actionQueue.clear()
        } else if (withError == false && actionQueue.isNotEmpty()) {
            logger.i(pendingActionLogMessage)
            logger.i("Remaining actions in queue for $printer $printerData (after end of action) : $actionQueue")
            doNextOperation()
        }
    }

    private fun signalEndOfActionIfNeeded(withError: Boolean? = false) {
        if (printerData.connectionMode == ConnectionMode.AUTO) {
            signalEndOfAction(withError)
        }
    }

    override fun initializePrinter() {
        val printerStatus = getStatusRaw()
        // This connection status check will let us know if we can inject a
        //  new EposPrinter potentially with a new address only if connection status is false
        // this will avoid the program to have a zombie flux opened to another printer address
        if (printerStatus.connectionStatus.isConnected == false) {
            try {
                DaggerPrinterEpsonComponent.builder().printerEpsonModule(
                    PrinterEpsonModule(
                        printerData = this.printerData,
                        printerWriterListener = this,
                        context = context
                    )
                ).build()
                    .inject(this)
            } catch (epos2Exception: Epos2Exception) {
                val printerError = PrinterError.getPrinterException(epos2Exception.errorStatus)
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(printerError).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                printerInitListener?.onInitPrinterFailure(printerData, printerException)
            } catch (exception: Exception) {
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                printerInitListener?.onInitPrinterFailure(printerData, printerException)
            }
        } else {
            // Do nothing because printer is already managed
            logger.i("printer is already initialized")
        }
    }

    override fun setInitListener(printerInitListener: PrinterInitListener) {
        this.printerInitListener = printerInitListener
    }

    override fun setConnectListener(printerConnectListener: PrinterConnectListener) {
        this.printerConnectListener = printerConnectListener
    }

    override fun setPrintListener(printerPrintListener: PrinterPrintListener) {
        this.printerPrintListener = printerPrintListener
    }

    override fun setStatusListener(printerStatusListener: PrinterStatusListener) {
        this.printerStatusListener = printerStatusListener
    }

    override fun connectPrinter(timeout: Int?) {
        getCurrentCoroutineScope().launch {
            try {
                logger.i(" just before connectPrinter ")
                printer.connect(printerData.printerAddress, timeout.toConnectTimeout())
                ensureActive()
                logger.i(" just after connectPrinter ")
                printer.setReceiveEventListener(this@PrinterEpson)
                printer.setStatusChangeEventListener(this@PrinterEpson)
                printer.startMonitor()
                signalEndOfActionIfNeeded(false)
                printerConnectListener?.onConnectSuccess(printerData)
            } catch (epos2Exception: Epos2Exception) {
                ensureActive()
                val printerError: PrinterError = if (epos2Exception.errorStatus == EposException.EPOS_EXCEPTION_ERR_ILLEGAL.codeInt) {
                    signalEndOfActionIfNeeded(false)
                    PrinterError.getPrinterException(epos2Exception.errorStatus, ScopeTag.CONNECT)
                } else {
                    signalEndOfActionIfNeeded(true)
                    PrinterError.getPrinterException(epos2Exception.errorStatus)
                }
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(printerError).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                printerConnectListener?.onConnectFailure(printerData, printerException)
            } catch (exception: Exception) {
                ensureActive()
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                signalEndOfActionIfNeeded(true)
                printerConnectListener?.onConnectFailure(printerData, printerException)
            }

        }
    }

    override fun printData(ticketData: TicketData, timeout: Int?) {
        try {
            printerWriter.writeData(ticketData)
            printer.sendData(timeout.toPrintTimeout())
        } catch (epos2Exception: Epos2Exception) {
            val printerError: PrinterError = if (epos2Exception.errorStatus == EposException.EPOS_EXCEPTION_ERR_ILLEGAL.codeInt) {
                PrinterError.getPrinterException(epos2Exception.errorStatus, ScopeTag.PRINT_DATA)
            } else {
                PrinterError.getPrinterException(epos2Exception.errorStatus)
            }
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withPrinterError(printerError).build()
            val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
            logger.e(printerLogInfo)
            signalEndOfActionIfNeeded(true)
            printerPrintListener?.onPrintDataFailure(printerData, printerException)
        } catch (exception: Exception) {
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message).build()
            val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
            logger.e(printerLogInfo)
            signalEndOfActionIfNeeded(true)
            printerPrintListener?.onPrintDataFailure(printerData, printerException)
        } finally {
            this.printer.clearCommandBuffer()
        }
    }

    override fun printDataOnDemand(ticketData: TicketData, connectTimeout: Int?, printTimeout: Int?) {
        enqueueAction(Action.Connect(connectTimeout))
        enqueueAction(Action.Print(ticketData, printTimeout))
        enqueueAction(Action.Disconnect)
    }

    override fun disconnectPrinter() {
        getCurrentCoroutineScope().launch(coroutineExceptionHandler) {
            try {
                printer.setReceiveEventListener(null)
                printer.setStatusChangeEventListener(null)
                printer.stopMonitor()
                printer.disconnect()
                ensureActive()
                signalEndOfActionIfNeeded(false)
                printerConnectListener?.onDisconnectSuccess(printerData)
            } catch (epos2Exception: Epos2Exception) {
                ensureActive()
                val printerError: PrinterError = if (epos2Exception.errorStatus == EposException.EPOS_EXCEPTION_ERR_ILLEGAL.codeInt) {
                    signalEndOfActionIfNeeded(false)
                    PrinterError.getPrinterException(epos2Exception.errorStatus, ScopeTag.DISCONNECT)
                } else {
                    signalEndOfActionIfNeeded(true)
                    PrinterError.getPrinterException(epos2Exception.errorStatus)
                }
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(printerError).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                printerConnectListener?.onDisconnectFailure(printerData, printerException)
            } catch (exception: Exception) {
                ensureActive()
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                signalEndOfActionIfNeeded(true)
                printerConnectListener?.onDisconnectFailure(printerData, printerException)
            }
        }
    }

    override fun getStatusRaw(): PrinterStatus {
        return PrinterStatus(printer.status)
    }

    override fun getStatus() {
        getCurrentCoroutineScope().launch(coroutineExceptionHandler) {
            val statusInfo = printer.status
            ensureActive()
            printerStatusListener?.onStatusReceived(printerData, PrinterStatus(statusInfo))
        }
    }

    override fun cancelAllJob() {
        getCurrentCoroutineScope().cancel()
        pendingAction = null
        actionQueue.clear()
    }

    private fun getCurrentCoroutineScope(): CoroutineScope {
        if (!coroutineScope.isActive) {
            DaggerPrinterEpsonComponent.builder().printerEpsonModule(
                PrinterEpsonModule(
                    eposPrinter = printer,
                    printerData = this.printerData,
                    printerWriterListener = this,
                    context = context
                )
            ).build()
                .inject(this)
        }
        return coroutineScope
    }

    /**
     * onPtrReceive method receive callback from Epson printer
     * error or sucess code is fired to this method
     * @param printer
     * @param code
     * @param status
     * @param errorMessage
     */
    override fun onPtrReceive(
        printer: EposPrinter,
        code: Int,
        status: PrinterStatusInfo?,
        errorMessage: String?
    ) {
        val printerCallbackCode = PrinterCallbackCode.getPrinterCallbackCode(code)
        logger.i("  in onPtrReceive printer result  ${printerCallbackCode.eposCallbackCode.codeString}")

        if (code == Epos2CallbackCode.CODE_SUCCESS) {
            printerPrintListener?.onPrintSuccess(printerData, PrinterStatus(status))
            signalEndOfActionIfNeeded(false)
        } else {
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withMessage(printerCallbackCode.message)
                .withAction(printerCallbackCode.action)
                .withCode(printerCallbackCode.eposCallbackCode.codeString).build()
            val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
            logger.e(printerLogInfo)
            printerPrintListener?.onPrintFailure(printerData, printerException)
            signalEndOfActionIfNeeded(true)
        }

        this.printer.clearCommandBuffer()
    }

    /**
     * Printer Writer Class will fire an error message by this method if it catch an error message in the process
     *
     * @param printerException
     */
    override fun onFailureWriteData(printerException: PrinterException) {
        printerPrintListener?.onPrintFailure(printerData, printerException)
    }

    override fun onPtrStatusChange(printer: EposPrinter?, code: Int) {
        logger.i(" PRINTER STATUS ${EposStatusMessageManager.makeStatusMessage(code)}")
        printerStatusListener?.onStatusUpdate(printerData, EposStatusMessageManager.makeStatusMessage(code))
    }
}
