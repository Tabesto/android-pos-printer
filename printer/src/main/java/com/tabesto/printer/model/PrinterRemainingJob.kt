package com.tabesto.printer.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PrinterRemainingJob(val printerData: PrinterData, val scopeTag: ScopeTag) : Parcelable
