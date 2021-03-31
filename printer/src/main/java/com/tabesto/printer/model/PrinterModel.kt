package com.tabesto.printer.model

import com.epson.epos2.printer.Printer

@Suppress("unused")
enum class PrinterModel(val eposModel: Int = Printer.TM_M10) {
    PRINTER_TM_T88(Printer.TM_T88),
    PRINTER_TM_M10(Printer.TM_M10),
    PRINTER_TM_M30(Printer.TM_M30),
    PRINTER_TM_T20III(Printer.TM_T20);
}
