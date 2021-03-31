package com.tabesto.printer.model

import android.os.Parcelable
import com.tabesto.printer.model.status.PrinterStatus
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PrinterManaged(val printerData: PrinterData, val printerStatus: PrinterStatus) : Parcelable
