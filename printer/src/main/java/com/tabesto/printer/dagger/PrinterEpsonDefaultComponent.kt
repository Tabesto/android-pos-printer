package com.tabesto.printer.dagger

import com.tabesto.printer.PrinterEpson
import dagger.Component

@Component(modules = [PrinterEpsonDefaultModule::class])
interface PrinterEpsonDefaultComponent {
    fun inject(printerEpson: PrinterEpson)
}
