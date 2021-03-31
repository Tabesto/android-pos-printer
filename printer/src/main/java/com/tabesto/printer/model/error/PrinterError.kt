package com.tabesto.printer.model.error

import com.tabesto.printer.model.ScopeTag

@Suppress("unused")
enum class PrinterError(val message: String, val eposException: EposException, val action: String, val scopeTag: ScopeTag) {
    ERR_PARAM(
        "An invalid parameter was passed.",
        EposException.EPOS_EXCEPTION_ERR_PARAM,
        "Wrong parameter passed, please check your parameter",
        ScopeTag.ANY
    ),
    ERR_ILLEGAL_CONNECT(
        "Tried to start communication with a printer with which communication had been already established.",
        EposException.EPOS_EXCEPTION_ERR_ILLEGAL,
        "Your printer is already connected to specified printer address please try to print a document",
        ScopeTag.CONNECT
    ),
    ERR_ILLEGAL_DISCONNECT(
        "Tried to end communication where it had not been established.",
        EposException.EPOS_EXCEPTION_ERR_ILLEGAL,
        "Please check if you are connected to your printer in order to disconnect it",
        ScopeTag.DISCONNECT
    ),
    ERR_ILLEGAL_PRINT_DATA(
        "The control commands have not been buffered.This API was called while no communication had been started.",
        EposException.EPOS_EXCEPTION_ERR_ILLEGAL,
        "Please check if you are connected to your printer in order to print ticket.",
        ScopeTag.PRINT_DATA
    ),
    ERR_ILLEGAL_ANY(
        "This API was called while no communication had been started.",
        EposException.EPOS_EXCEPTION_ERR_ILLEGAL,
        "Please check if you are connected to your printer.",
        ScopeTag.ANY
    ),
    ERR_MEMORY(
        "Memory necessary for processing could not be allocated.",
        EposException.EPOS_EXCEPTION_ERR_MEMORY,
        "Close unnecessary applications.",
        ScopeTag.ANY
    ),
    ERR_UNSUPPORTED(
        "A model name or language not supported was specified.",
        EposException.EPOS_EXCEPTION_ERR_UNSUPPORTED,
        "A function cannot be used if it is not supported by the specified model. Specify a print method supported by the printer.",
        ScopeTag.ANY
    ),

    ERR_TIMEOUT(
        "Failed to communicate with the devices within the specified time.",
        EposException.EPOS_EXCEPTION_ERR_TIMEOUT,
        "Check the timeout period. Set the timeout period to longer than the time required for printing.",
        ScopeTag.ANY
    ),

    ERR_CONNECT(
        "Failed to open the device.",
        EposException.EPOS_EXCEPTION_ERR_CONNECT,
        "Run disconnect for the appropriate class and then connect to restore communication. Failed to connect to the printer.\n" +
            "When a connection is established via Bluetooth, the Android OS automatically attempts a reconnection. If this error\n" +
            "status occurs even after 20 seconds or more have elapsed, recover the communication according to the method described above.",
        ScopeTag.ANY
    ),

    ERR_FAILURE(
        "An unknown error occurred.",
        EposException.EPOS_EXCEPTION_ERR_FAILURE,
        "Check for a problem with the execution environment.",
        ScopeTag.ANY
    ),
    ERR_PROCESSING(
        "Could not run the process.",
        EposException.EPOS_EXCEPTION_ERR_PROCESSING,
        "The timing of the processes has overlapped. Re-execute the API in which the error occurred.",
        ScopeTag.ANY
    ),

    ERR_NOT_FOUND(
        "The device could not be found.",
        EposException.EPOS_EXCEPTION_ERR_NOT_FOUND,
        "Check if the connection type and/or IP address are correct.",
        ScopeTag.ANY
    ),
    ERR_IN_USE(
        "The device was in use.",
        EposException.EPOS_EXCEPTION_ERR_IN_USE,
        "Stop using the device from another application.",
        ScopeTag.ANY
    ),
    ERR_TYPE_INVALID(
        "The device type is different.",
        EposException.EPOS_EXCEPTION_ERR_TYPE_INVALID,
        "Check the system configuration, and specify the appropriate connection method.",
        ScopeTag.ANY
    ),
    ERR_DISCONNECT(
        "Failed to disconnect the device.",
        EposException.EPOS_EXCEPTION_ERR_DISCONNECT,
        "Check connection with the device.",
        ScopeTag.ANY
    ),
    ERR_UNKNOWN(
        "Unknown error",
        EposException.EPOS_EXCEPTION_ERR_UNKNOWN,
        "Unknown action",
        ScopeTag.ANY
    );

    companion object {
        const val genericErrorMessage = "Use the API in an appropriate manner."

        /**
         * getPrinterException return the correct enum matching with codeInt passed by parameter
         * Explanation : If the code exception is ERR_ILLEGAL we have to specify the scope of method also inside
         * PrinterError.getPrinterException in order to get specific message
         * ERR_ILLEGAL same code contains 3 different message for different methods
         *
         * @param codeInt
         * @param scopeTag
         * @return
         */
        fun getPrinterException(codeInt: Int, scopeTag: ScopeTag? = ScopeTag.ANY): PrinterError =
            enumValues<PrinterError>().first { printErrorObject ->
                printErrorObject.eposException.codeInt == codeInt && printErrorObject.scopeTag == scopeTag
            }
    }
}
