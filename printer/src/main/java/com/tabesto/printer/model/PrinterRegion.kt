package com.tabesto.printer.model

import com.epson.epos2.printer.Printer

@Suppress("unused")
enum class PrinterRegion(val eposRegion: Int = Printer.MODEL_ANK) {
    PRINTER_ANK(Printer.MODEL_ANK),
    PRINTER_CHINA(Printer.MODEL_CHINESE),
    PRINTER_THAI(Printer.MODEL_THAI),
    PRINTER_JAPAN(Printer.MODEL_JAPANESE),
    PRINTER_KOREAN(Printer.MODEL_KOREAN),
    PRINTER_SOUTH_ASIA(Printer.MODEL_SOUTHASIA),
    PRINTER_TAIWAN(Printer.MODEL_TAIWAN);
}
