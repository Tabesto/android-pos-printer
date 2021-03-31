package com.tabesto.printer.model.devicemanager

import android.os.Parcelable
import com.tabesto.printer.model.ConnectionMode
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.ScopeTag
import com.tabesto.printer.model.error.DeviceManagerException
import com.tabesto.printer.model.error.PrinterException
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceManagerJobResult(
    val printerData: PrinterData,
    val scopeTag: ScopeTag,
    val isSuccessful: Boolean,
    val datetime: String,
    val deviceManagerException: DeviceManagerException? = null,
    val printerException: PrinterException? = null,
    val connectionMode: ConnectionMode? = null
): Parcelable
