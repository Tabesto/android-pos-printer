package com.tabesto.printer.dagger

import com.tabesto.printer.utils.EposPrinter
import com.tabesto.printer.writer.PrinterWriter
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@Module
class PrinterEpsonModuleTest(
    private val eposPrinter: EposPrinter,
    private val printerWriter: PrinterWriter
) {
    @Provides
    fun providesPrinterEpson(): EposPrinter {
        return eposPrinter
    }

    @Provides
    fun providesPrinterWriter(): PrinterWriter {
        return printerWriter
    }

    @ExperimentalCoroutinesApi
    @Provides
    fun providesDispatcher(): CoroutineDispatcher {
        return TestCoroutineDispatcher()
    }
}
