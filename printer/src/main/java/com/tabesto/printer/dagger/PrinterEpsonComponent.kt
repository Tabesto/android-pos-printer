package com.tabesto.printer.dagger

import com.tabesto.printer.PrinterEpson
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [PrinterEpsonModule::class])
interface PrinterEpsonComponent {
    fun inject(printerEpson: PrinterEpson)
}
