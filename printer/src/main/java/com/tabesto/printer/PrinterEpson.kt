package com.tabesto.printer

import android.content.Context
import com.epson.epos2.Epos2CallbackCode
import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.PrinterStatusInfo
import com.epson.epos2.printer.ReceiveListener
import com.epson.epos2.printer.StatusChangeListener
import com.tabesto.printer.discovery.Discovery
import com.tabesto.printer.discovery.DiscoveryFactory
import com.tabesto.printer.discovery.DiscoveryListener
import com.tabesto.printer.model.ConnectionMode
import com.tabesto.printer.model.DiscoveryData
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.ScopeTag
import com.tabesto.printer.model.error.EposException
import com.tabesto.printer.model.error.PrinterCallbackCode
import com.tabesto.printer.model.error.PrinterError
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.error.PrinterException.PrinterExceptionBuilder
import com.tabesto.printer.model.status.PrinterStatus
import com.tabesto.printer.model.ticket.TicketData
import com.tabesto.printer.system.BluetoothHelper
import com.tabesto.printer.system.BluetoothHelperListener
import com.tabesto.printer.utils.AppExceptionHandler
import com.tabesto.printer.utils.EposPrinter
import com.tabesto.printer.utils.EposStatusMessageManager
import com.tabesto.printer.dagger.DaggerPrinterEpsonComponent
import com.tabesto.printer.dagger.DaggerPrinterEpsonDefaultComponent
import com.tabesto.printer.dagger.PrinterEpsonDefaultModule
import com.tabesto.printer.dagger.PrinterEpsonModule
import com.tabesto.printer.model.PrinterLogInfo
import com.tabesto.printer.utils.log.Logger
import com.tabesto.printer.utils.log.LoggerExtraArgument
import com.tabesto.printer.writer.PrinterWriter
import com.tabesto.printer.writer.PrinterWriterListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Printer class of printer module is the entry point of any outside application in order to interact
 * with printer device (managed by caller application)
 * This class create a printer
 * @param printerData object gives the possibility to initialize a printer object of SDK
 */
class PrinterEpson constructor(override var printerData: PrinterData, override var context: Context) : ReceiveListener,
    PrinterWriterListener, Printer,
    StatusChangeListener, DiscoveryListener, BluetoothHelperListener {

    private var printerInitListener: PrinterInitListener? = null
    private var printerConnectListener: PrinterConnectListener? = null
    private var printerPrintListener: PrinterPrintListener? = null
    private var printerDiscoveryListener: PrinterDiscoveryListener? = null
    private var printerStatusListener: PrinterStatusListener? = null
    private var discoveryData: DiscoveryData? = null
    private var discovery: Discovery? = null
    private var bluetoothHelper: BluetoothHelper? = null
    private var discoveryDelayInMs: Long = 0
    private var logger: Logger = Logger(PrinterEpson::class.java.simpleName)

    @Inject
    lateinit var printer: EposPrinter

    @Inject
    lateinit var printerWriter: PrinterWriter

    @Inject
    lateinit var dispatcher: CoroutineDispatcher

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
        bluetoothHelper = BluetoothHelper(this, context)

        // get simple discovery module linked to epson but not linked to printer address
        discoveryData = DiscoveryData(context, this, printerData.printerType)
        discoveryData?.let { discovery = DiscoveryFactory().getDiscovery(it) }

        // Default injection of EposPrinter in order to avoid crash null pointer because it is a late init for injection
        DaggerPrinterEpsonDefaultComponent.builder().printerEpsonDefaultModule(
            PrinterEpsonDefaultModule(
                context = context,
                printerWriterListener = this
            )
        )
            .build()
            .inject(this)

        logger.enablePrinterStatusFullInLogs()
        logger.enablePrinterDataFullInLogs()

        Thread.setDefaultUncaughtExceptionHandler(AppExceptionHandler())
    }

    override fun initializePrinter() {
        val printerStatus = getStatusRaw()
        // This connection status check will let us know if we can inject a
        //  new EposPrinter potentially with a new address only if connection status is false
        // this will avoid the program to have a zombie flux opened to another printer address
        if (printerStatus.connectionStatus.isConnected == false) {
            try {
                DaggerPrinterEpsonComponent.builder().printerEpsonModule(PrinterEpsonModule(printerData, this, context)).build()
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

    override fun setDiscoveryListener(printerDiscoveryListener: PrinterDiscoveryListener) {
        this.printerDiscoveryListener = printerDiscoveryListener
    }

    override fun setStatusListener(printerStatusListener: PrinterStatusListener) {
        this.printerStatusListener = printerStatusListener
    }

    override fun connectPrinter() {
        CoroutineScope(dispatcher).launch {
            try {
                logger.i(" just before connectPrinter ")
                printer.connect(printerData.printerAddress, EposPrinter.PARAM_DEFAULT)
                logger.i(" just after connectPrinter ")
                printer.setReceiveEventListener(this@PrinterEpson)
                printer.setStatusChangeEventListener(this@PrinterEpson)
                printer.startMonitor()
                printerConnectListener?.onConnectSuccess(printerData)
            } catch (epos2Exception: Epos2Exception) {
                val printerError: PrinterError = if (epos2Exception.errorStatus == EposException.EPOS_EXCEPTION_ERR_ILLEGAL.codeInt) {
                    PrinterError.getPrinterException(epos2Exception.errorStatus, ScopeTag.CONNECT)
                } else {
                    PrinterError.getPrinterException(epos2Exception.errorStatus)
                }
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(printerError).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                printerConnectListener?.onConnectFailure(printerData, printerException)
            } catch (exception: Exception) {
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                printerConnectListener?.onConnectFailure(printerData, printerException)
            }
        }
    }

    override fun printData(ticketData: TicketData) {
        try {
            printerWriter.writeData(ticketData)
            printer.sendData(EposPrinter.PARAM_DEFAULT)
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
            printerPrintListener?.onPrintDataFailure(printerData, printerException)
        } catch (exception: Exception) {
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message).build()
            val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
            logger.e(printerLogInfo)
            printerPrintListener?.onPrintDataFailure(printerData, printerException)
        }
    }

    override fun disconnectPrinter() {
        CoroutineScope(dispatcher).launch(coroutineExceptionHandler) {
            try {
                printer.setReceiveEventListener(null)
                printer.setStatusChangeEventListener(null)
                printer.stopMonitor()
                printer.disconnect()
                printerConnectListener?.onDisconnectSuccess(printerData)
            } catch (epos2Exception: Epos2Exception) {
                val printerError: PrinterError = if (epos2Exception.errorStatus == EposException.EPOS_EXCEPTION_ERR_ILLEGAL.codeInt) {
                    PrinterError.getPrinterException(epos2Exception.errorStatus, ScopeTag.DISCONNECT)
                } else {
                    PrinterError.getPrinterException(epos2Exception.errorStatus)
                }
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(printerError).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                printerConnectListener?.onDisconnectFailure(printerData, printerException)
            } catch (exception: Exception) {
                val printerExceptionBuilder = PrinterExceptionBuilder()
                val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message).build()
                val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
                logger.e(printerLogInfo)
                printerConnectListener?.onDisconnectFailure(printerData, printerException)
            }
        }
    }

    override fun restartBluetooth() {
        try {
            bluetoothHelper?.resetBluetoothModule()
        } catch (printerException: PrinterException) {
            val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
            logger.e(printerLogInfo)
            printerDiscoveryListener?.onDiscoveryFailure(printerData, printerException)
        }
    }

    override fun restartBluetoothAndLaunchDiscovery(delayMillis: Long) {
        try {
            discoveryDelayInMs = delayMillis
            bluetoothHelper?.resetBluetoothModule()
        } catch (printerException: PrinterException) {
            val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
            logger.e(printerLogInfo)
            printerDiscoveryListener?.onDiscoveryFailure(printerData, printerException)
        }
    }

    override fun stopDiscovery() {
        try {
            discovery?.stopDiscovery()
        } catch (printerException: PrinterException) {
            val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
            logger.e(printerLogInfo)
            printerDiscoveryListener?.onDiscoveryFailure(printerData, printerException)
        }
    }

    override fun getStatusRaw(): PrinterStatus {
        return PrinterStatus(printer.status)
    }

    override fun getStatus() {
        CoroutineScope(dispatcher).launch(coroutineExceptionHandler) {
            val statusInfo = printer.status
            printerStatusListener?.onStatusReceived(printerData, PrinterStatus(statusInfo))
        }
    }

    override fun cancelAllJob() {
        CoroutineScope(dispatcher).cancel()
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
        } else {
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withMessage(printerCallbackCode.message)
                .withAction(printerCallbackCode.action)
                .withCode(printerCallbackCode.eposCallbackCode.codeString).build()
            val printerLogInfo = PrinterLogInfo(printerData = printerData, printerException = printerException)
            logger.e(printerLogInfo)
            printerPrintListener?.onPrintFailure(printerData, printerException)
        }

        this.printer.clearCommandBuffer()
        if (printerData.connectionMode == ConnectionMode.AUTO) {
            logger.i("  in onPtrReceive before disconnect printer")
            disconnectPrinter()
        }
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

    override fun onFinishDiscovery(isAvailable: Boolean) {
        printerDiscoveryListener?.onDiscoveryFinish(printerData, isAvailable)
    }

    override fun onFailureDiscovery(printerException: PrinterException) {
        printerDiscoveryListener?.onDiscoveryFailure(printerData, printerException)
    }

    override fun onDiscoveryStoppedSuccessfully() {
        printerDiscoveryListener?.onDiscoveryStopSuccess(printerData)
    }

    override fun onBluetoothRestartedManually() {
        printerData.printerAddress?.let { address ->
            discovery?.launchDiscovery(address, discoveryDelayInMs)
            printerDiscoveryListener?.onBluetoothRestartSuccess(printerData)
        }
    }
}
