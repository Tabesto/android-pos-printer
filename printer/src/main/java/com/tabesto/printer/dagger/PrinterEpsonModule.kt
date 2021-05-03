package com.tabesto.printer.dagger

import android.content.Context
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterModel
import com.tabesto.printer.model.PrinterRegion
import com.tabesto.printer.utils.EposPrinter
import com.tabesto.printer.writer.PrinterWriter
import com.tabesto.printer.writer.PrinterWriterListener
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module

class PrinterEpsonModule(
    private val eposPrinter: EposPrinter? = null,
    private val printerData: PrinterData? = null,
    private val printerWriterListener: PrinterWriterListener,
    private val context: Context
) {

    @Provides
    @Singleton
    fun providesPrinterEpson(): EposPrinter = eposPrinter
        ?: if (printerData != null) {
            EposPrinter(printerData.printerModel.eposModel, printerData.printerRegion.eposRegion, context)
        } else {
            EposPrinter(PrinterModel.PRINTER_TM_M10.eposModel, PrinterRegion.PRINTER_ANK.eposRegion, context)
        }

    @Provides
    @Singleton
    fun providesPrinterWriter(eposPrinter: EposPrinter): PrinterWriter = PrinterWriter(eposPrinter, printerWriterListener)

    @Provides
    @Singleton
    fun providesCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)
}
