package com.tabesto.printer.dagger

import com.tabesto.printer.PrinterEpson
import dagger.Component

@Component(modules = [PrinterEpsonModule::class])
interface PrinterEpsonComponent {
    fun inject(printerEpson: PrinterEpson)
}
