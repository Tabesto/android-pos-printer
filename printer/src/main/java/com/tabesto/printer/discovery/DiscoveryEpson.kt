package com.tabesto.printer.discovery

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.epson.epos2.Epos2Exception
import com.epson.epos2.discovery.DeviceInfo
import com.epson.epos2.discovery.FilterOption
import com.tabesto.printer.model.DiscoveryData
import com.tabesto.printer.model.PrinterDevice
import com.tabesto.printer.model.PrinterLogInfo
import com.tabesto.printer.model.error.PrinterError
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.error.PrinterException.PrinterExceptionBuilder
import com.tabesto.printer.utils.EposDiscovery
import com.tabesto.printer.utils.EposDiscoveryListener
import com.tabesto.printer.utils.log.Logger

/**
 * DiscoveryEpson class is an internal module that gives the possibility to printer
 * to discover printers available for printing
 *
 * @param discoveryData : is an object that initialize parameter necessary for good running of discovery
 */
internal class DiscoveryEpson(discoveryData: DiscoveryData) : EposDiscoveryListener, Discovery {
    private var filterOption: FilterOption = FilterOption()
    private var context: Context
    private var listener: DiscoveryListener
    private var printerAddress = ""
    private var isDiscovering = false
    private var isDelayedForResult = false
    private var delayMillis: Long = 0
    private var handler: Handler? = null

    private var logger: Logger = Logger(DiscoveryEpson::class.java.simpleName)

    private var deviceList: ArrayList<PrinterDevice> = ArrayList()

    init {
        handler = Handler(Looper.getMainLooper())
        filterOption.deviceType = EposDiscovery.TYPE_PRINTER
        filterOption.epsonFilter = EposDiscovery.FILTER_NAME
        context = discoveryData.context
        listener = discoveryData.listener
    }

    override fun launchDiscovery(printerAddress: String, delayMillis: Long) {
        try {
            if (isDelayedForResult) {
                listener.onFailureDiscovery(PrinterException("A delayed discovery is already in process, please wait !"))
            } else {
                // we clear list before launching a discovery
                deviceList.clear()

                this.printerAddress = printerAddress

                startDiscovery()

                if (delayMillis > 0) {
                    isDelayedForResult = true
                    this.delayMillis = delayMillis
                    logger.i(" result is delayed isDelayedForResult :  $isDelayedForResult ,  launch delay")
                    handler?.postDelayed({
                        runnableDelayedDiscovery.run()
                    }, delayMillis)
                }
            }
        } catch (epos2Exception: Epos2Exception) {
            val printerError = PrinterError.getPrinterException(epos2Exception.errorStatus)
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException =
                printerExceptionBuilder.withPrinterError(printerError)
                    .build()
            val printerLogInfo = PrinterLogInfo(printerException = printerException)
            logger.e(printerLogInfo)
            throw printerException
        } catch (exception: Exception) {
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message)
                .build()
            val printerLogInfo = PrinterLogInfo(printerException = printerException)
            logger.e(printerLogInfo)
            throw printerException
        }
    }

    private fun startDiscovery() {
        try {
            logger.i(" just before launch DiscoveryEpson start ")
            if (!isDiscovering) {
                logger.i(" isDiscovering is $isDiscovering SO WE JUST DO START ")
                isDiscovering = true
                EposDiscovery.start(context, filterOption, this)
            } else {
                logger.i(" isDiscovering is $isDiscovering SO WE DO STOP THEN START  ")
                EposDiscovery.stop()
                isDiscovering = false
                startDiscovery()
            }
        } catch (epos2Exception: Epos2Exception) {
            val printerError = PrinterError.getPrinterException(epos2Exception.errorStatus)
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException =
                printerExceptionBuilder.withPrinterError(printerError)
                    .build()
            val printerLogInfo = PrinterLogInfo(printerException = printerException)
            logger.e(printerLogInfo)
            throw printerException
        } catch (exception: Exception) {
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message)
                .build()
            val printerLogInfo = PrinterLogInfo(printerException = printerException)
            logger.e(printerLogInfo)
            throw printerException
        }
    }

    override fun stopDiscovery() {
        try {
            logger.i(" just before launch DiscoveryEpson stop ")
            when {
                isDelayedForResult -> {
                    logger.i(" isDelayedForResult is true , a delayed discovery is already running ! ")
                    listener.onFailureDiscovery(PrinterException("Delayed Discovery is already running , wait until it stops"))
                }
                isDiscovering -> {
                    logger.i(" isDiscovering is true so we stop discovering ")
                    EposDiscovery.stop()
                    isDiscovering = false
                    listener.onDiscoveryStoppedSuccessfully()
                }
                else -> {
                    listener.onFailureDiscovery(PrinterException("Discovery is already stopped"))
                }
            }
        } catch (epos2Exception: Epos2Exception) {
            val printerError = PrinterError.getPrinterException(epos2Exception.errorStatus)
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException =
                printerExceptionBuilder.withPrinterError(printerError)
                    .build()
            val printerLogInfo = PrinterLogInfo(printerException = printerException)
            logger.e(printerLogInfo)
            throw printerException
        } catch (exception: Exception) {
            val printerExceptionBuilder = PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message)
                .build()
            val printerLogInfo = PrinterLogInfo(printerException = printerException)
            logger.e(printerLogInfo)
            throw printerException
        }
    }

    override fun onDiscovery(deviceInfo: DeviceInfo?) {
        logger.i(
            message = " just received deviceInfo : " + deviceInfo.toString()
        )
        if (deviceInfo != null) {
            val printerDevice = PrinterDevice(
                deviceInfo.deviceName,
                deviceInfo.target,
                deviceInfo.macAddress,
                deviceInfo.deviceType,
                deviceInfo.ipAddress,
                deviceInfo.bdAddress
            )
            deviceList.add(printerDevice)
        }

        sendBackResultToCallerApp()
    }

    private fun sendBackResultToCallerApp() {
        logger.i(" in sendBackResultToCallerApp ")
        try {
            if (!isDelayedForResult) {
                val isAvailable = deviceList.isNotEmpty() && deviceList.any { it.deviceTarget == printerAddress }
                logger.i(" in sendBackResultToCallerApp isDiscovering is put ot $isDiscovering ")

                listener.onFinishDiscovery(isAvailable)
            }
        } catch (exception: Exception) {
            logger.e(
                " an exception caught in sendBackResultToCallerApp ${exception.message}"
            )
            throw PrinterException(" an error happened while trying to search the printer ")
        }
    }

    private val runnableDelayedDiscovery = Runnable {
        isDelayedForResult = false
        logger.i(
            " after delay finish isDelayedForResult :  $isDelayedForResult ,  " +
                "isDiscovering : $isDiscovering , before send result to caller app"
        )
        sendBackResultToCallerApp()
        // after a delay we stop by default the discovery
        stopDiscovery()
    }
}
