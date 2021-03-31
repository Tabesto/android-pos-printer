package com.tabesto.printer.model.error

import android.os.Parcelable
import com.tabesto.printer.utils.Constants.PRINTER_UNKNOWN_ACTION
import com.tabesto.printer.utils.Constants.PRINTER_UNKNOWN_CODE
import kotlinx.android.parcel.Parcelize

@Parcelize
open class PrinterException(
    override val message: String,
    val printerCode: String = PRINTER_UNKNOWN_CODE,
    val action: String = PRINTER_UNKNOWN_ACTION
) : Parcelable,
    Exception() {
    constructor (printerExceptionBuilder: PrinterExceptionBuilder) : this(
        printerExceptionBuilder.message,
        printerExceptionBuilder.printerCode,
        printerExceptionBuilder.action
    )

    class PrinterExceptionBuilder {

        var message: String = ""
            private set
        var printerCode: String = PRINTER_UNKNOWN_CODE
            private set
        var action: String = PRINTER_UNKNOWN_ACTION
            private set

        @Suppress("unused")
        fun reset() {
            this.message = ""
            this.printerCode = PRINTER_UNKNOWN_CODE
            this.action = PRINTER_UNKNOWN_ACTION
        }

        fun withPrinterError(
            printerError: PrinterError = PrinterError.ERR_UNKNOWN,
            errorMessage: String? = null
        ): PrinterExceptionBuilder {
            if (errorMessage != null) {
                this.message = errorMessage
            } else {
                this.message = printerError.message
            }
            this.printerCode = printerError.eposException.codeString
            this.action = printerError.action
            return this
        }

        fun withMessage(message: String): PrinterExceptionBuilder {
            this.message = message
            return this
        }

        fun withCode(printerCode: String): PrinterExceptionBuilder {
            this.printerCode = printerCode
            return this
        }

        fun withAction(action: String): PrinterExceptionBuilder {
            this.action = action
            return this
        }

        fun build(): PrinterException {
            return PrinterException(this)
        }
    }
}
