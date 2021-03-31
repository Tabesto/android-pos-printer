package com.tabesto.printer.dagger

import com.tabesto.printer.PrinterEpson
import dagger.Component

@Component(modules = [PrinterEpsonModuleTest::class])
interface PrinterEpsonComponentTest {
    fun inject(app: PrinterEpson)
}
