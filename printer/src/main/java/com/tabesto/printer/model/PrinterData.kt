package com.tabesto.printer.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * [PrinterData] class is necessary for the creation of Printer Object
 * it fixes all must needed property list below in parameter
 *
 * @property printerModel : printer model defines which model is used (TM_M10 etc.)
 * @property printerRegion : printer region defines in which region is used the printer (EUR, ASIA , etc.)
 * @property printerAddress : printer address defines on which mac address we need to connect (BT or TCP)
 * @property connectionMode : connection mode defines if it needs to disconnect automatically or not at the end of process
 * @property printerType : printer type
 * @property customBodyTextSize : precise needed to be used when write ticket body by default it will be 1 if not precised
 */
@Parcelize
data class PrinterData(
    val printerModel: PrinterModel = PrinterModel.PRINTER_TM_M10,
    val printerRegion: PrinterRegion = PrinterRegion.PRINTER_ANK,
    val printerAddress: String? = null,
    val connectionMode: ConnectionMode = ConnectionMode.MANUAL,
    val printerType: PrinterType = PrinterType.PRINTER_EPSON,
    val customBodyTextSize: Int = 1
) : Parcelable
