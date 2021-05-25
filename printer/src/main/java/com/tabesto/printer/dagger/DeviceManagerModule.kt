package com.tabesto.printer.dagger

import com.tabesto.printer.Printer
import com.tabesto.printer.model.PrinterData
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
class DeviceManagerModule(private val listOfPrinters: HashMap<PrinterData, Printer>? = null) {

    @Provides
    @Singleton
    fun providesListOfPrinterData(): HashMap<PrinterData, Printer> = listOfPrinters ?: HashMap()

    @Provides
    @Singleton
    fun providesCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)
}
