package com.tabesto.printer.model.error

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceManagerException(var error: String = "UNKNOWN") : Parcelable
