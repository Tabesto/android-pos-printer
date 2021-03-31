package com.tabesto.printer.model.error

@Suppress("unused")
enum class PrinterCallbackCode(val message: String, val eposCallbackCode: EposCallbackCode, val action: String) {
    CODE_SUCCESS(
        "Processed successfully.",
        EposCallbackCode.EPOS_CALLBACK_CODE_SUCCESS,
        ""
    ),

    CODE_PRINTING(
        "Request Print Job was run by specifying a print job ID which was being printed.",
        EposCallbackCode.EPOS_CALLBACK_CODE_PRINTING,
        ""
    ),
    CODE_ERR_AUTORECOVER(
        "Head overheat error occurred or Battery overheat error occurred or Motor driver IC overheat error occurred.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_AUTORECOVER,
        "Start the process after the device is cooled down."
    ),
    CODE_ERR_COVER_OPEN(
        "Cover is open.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_COVER_OPEN,
        "Close the printer cover."
    ),
    CODE_ERR_CUTTER(
        "Auto cutter error occurred.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_CUTTER,
        "Remove the error cause and power off and then on the printer."
    ),
    CODE_ERR_MECHANICAL(
        "Mechanical error occurred.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_MECHANICAL,
        "Remove the error cause and power off and then on the printer."
    ),
    CODE_ERR_EMPTY(
        "No paper is left in the roll paper end detector.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_EMPTY,
        "Please change paper roll inside printer"
    ),
    CODE_ERR_UNRECOVERABLE(
        "Unrecoverable error occurred.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_UNRECOVERABLE,
        "Power off and then on the printer. Contact the distributor or service center if the problem persists."
    ),
    CODE_ERR_FAILURE(
        "An unknown error occurred.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_FAILURE,
        "Check for a problem with the execution environment."
    ),

    CODE_ERR_SYSTEM(
        "An error occurred with the TM-i firmware or TM-DT software.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_SYSTEM,
        "Turn off and then turn on the power supply to the TM-i series/TM-DT series, and restart the OS."
    ),

    CODE_ERR_NOT_FOUND(
        "The connection type and/or IP address are not correct. The specified device is not connected.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_NOT_FOUND,
        "Check if the connection type and/or IP address are correct. Check connection with the device."
    ),
    CODE_ERR_PORT(
        "Error was detected with the communication port.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_PORT,
        "Please check if your printer is POWER ON, then disconnect it and reconnect it in order to print your ticket"
    ),
    CODE_ERR_JOB_NOT_FOUND(
        "Specified print job ID does not exist.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_JOB_NOT_FOUND,
        "Check the specified job ID."
    ),
    CODE_ERR_SPOOLER(
        "Print queue is full.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_SPOOLER,
        "Check if communication with the printer is disconnected."
    ),
    CODE_ERR_BATTERY_LOW(
        "Battery has run out.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_BATTERY_LOW,
        "Replace the battery or connect the AC adapter."
    ),
    CODE_ERR_TOO_MANY_REQUESTS(
        "The number of print jobs sent to the printer has exceeded the allowable limit.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_TOO_MANY_REQUESTS,
        "Wait for the printer to process some of the tasks, then send the jobs or data again."
    ),
    CODE_ERR_REQUEST_ENTITY_TOO_LARGE(
        "The size of the print job data exceeds the capacity of the printer.",
        EposCallbackCode.EPOS_CALLBACK_CODE_ERR_REQUEST_ENTITY_TOO_LARGE,
        "Check the content of the print job, reduce the size of the data, and then resend the print job."
    );

    companion object {
        /**
         * getPrinterCallbackCode return the correct enum matching with codeInt passed by parameter
         *
         * @param codeInt
         * @return
         */
        fun getPrinterCallbackCode(codeInt: Int): PrinterCallbackCode =
            enumValues<PrinterCallbackCode>().first { printerCallbackCodeObject ->
                printerCallbackCodeObject.eposCallbackCode.codeInt == codeInt
            }
    }
}
