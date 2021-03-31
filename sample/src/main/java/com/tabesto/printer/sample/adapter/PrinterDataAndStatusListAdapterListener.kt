package com.tabesto.printer.sample.adapter

import com.tabesto.printer.model.PrinterData

interface PrinterDataAndStatusListAdapterListener {
    fun onClickPrinterItem(printerData: PrinterData)
    fun remove(printerData: PrinterData)
}
