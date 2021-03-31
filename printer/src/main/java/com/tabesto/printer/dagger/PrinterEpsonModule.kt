package com.tabesto.printer.dagger

import android.content.Context
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.utils.EposPrinter
import com.tabesto.printer.writer.PrinterWriter
import com.tabesto.printer.writer.PrinterWriterListener
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
class PrinterEpsonModule(var printerData: PrinterData, var printerWriterListener: PrinterWriterListener, var context: Context) {
    private lateinit var eposPrinter: EposPrinter

    @Provides
    fun providesPrinterEpson(): EposPrinter {
        eposPrinter = EposPrinter(printerData.printerModel.eposModel, printerData.printerRegion.eposRegion, context)
        return eposPrinter
    }

    @Provides
    fun providesPrinterWriter(): PrinterWriter {
        return PrinterWriter(eposPrinter, printerWriterListener)
    }

    @Provides
    fun providesDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}
