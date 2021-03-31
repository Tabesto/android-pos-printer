package com.tabesto.printer.dagger

import android.content.Context
import com.tabesto.printer.model.PrinterModel
import com.tabesto.printer.model.PrinterRegion
import com.tabesto.printer.utils.EposPrinter
import com.tabesto.printer.writer.PrinterWriter
import com.tabesto.printer.writer.PrinterWriterListener
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
class PrinterEpsonDefaultModule(var context: Context?, var printerWriterListener: PrinterWriterListener) {
    private lateinit var eposPrinter: EposPrinter

    @Provides
    fun providesPrinterEpson(): EposPrinter {
        eposPrinter = EposPrinter(PrinterModel.PRINTER_TM_M10.eposModel, PrinterRegion.PRINTER_ANK.eposRegion, context)
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
