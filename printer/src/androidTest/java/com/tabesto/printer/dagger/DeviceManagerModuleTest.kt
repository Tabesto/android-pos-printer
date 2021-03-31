package com.tabesto.printer.dagger

import com.tabesto.printer.Printer
import com.tabesto.printer.model.PrinterData
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@Suppress("unused")
@Module
class DeviceManagerModuleTest(private val listOfPrinters: HashMap<PrinterData, Printer>) {
    @Provides
    fun providesPrinterEpson(): HashMap<PrinterData, Printer> {
        return listOfPrinters
    }

    @ExperimentalCoroutinesApi
    @Provides
    fun providesDispatcher(): CoroutineDispatcher {
        return TestCoroutineDispatcher()
    }
}
