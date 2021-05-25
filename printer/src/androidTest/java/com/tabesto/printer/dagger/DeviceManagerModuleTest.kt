package com.tabesto.printer.dagger

import com.tabesto.printer.Printer
import com.tabesto.printer.model.PrinterData
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import javax.inject.Singleton

@Suppress("unused")
@Module
class DeviceManagerModuleTest(private val listOfPrinters: HashMap<PrinterData, Printer>) {
    @Provides
    @Singleton
    fun providesPrinterEpson(): HashMap<PrinterData, Printer> {
        return listOfPrinters
    }

    @ExperimentalCoroutinesApi
    @Provides
    @Singleton
    fun providesCoroutineScope(): CoroutineScope = CoroutineScope(TestCoroutineDispatcher())
}
