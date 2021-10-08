package com.tabesto.printer.utils

fun Int?.toConnectTimeout(): Int = this.toPrinterTimeout(Constants.CONNECT_TIMEOUT_MIN)

fun Int?.toPrintTimeout(): Int = this.toPrinterTimeout(Constants.PRINT_TIMEOUT_MIN)

private fun Int?.toPrinterTimeout(minValue: Int): Int =
    this?.let { timeout ->
        val convertedTimeout = timeout * 1000
        if (convertedTimeout < minValue) {
            minValue
        } else {
            convertedTimeout
        }
    } ?: EposPrinter.PARAM_DEFAULT
