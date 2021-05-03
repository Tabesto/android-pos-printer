package com.tabesto.printer.dagger

import com.tabesto.printer.Printer
import com.tabesto.printer.model.PrinterData
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
class DeviceManagerModule {

    @Provides
    fun providesListOfPrinterData(): HashMap<PrinterData, Printer> = HashMap()

    @Provides
    fun providesDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
