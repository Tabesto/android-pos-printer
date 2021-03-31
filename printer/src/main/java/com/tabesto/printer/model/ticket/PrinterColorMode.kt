package com.tabesto.printer.model.ticket

import com.epson.epos2.printer.Printer

/**
 * Defines Printer color mode
 * please check Epson documentation
 *
 * @property colorMode
 */
@Suppress("unused")
enum class PrinterColorMode(val colorMode: Int) {
    MODE_MONO(Printer.MODE_MONO),
    MONO_GRAY16(Printer.MODE_GRAY16),
    MODE_MONO_HIGH_DENSITY(Printer.MODE_MONO_HIGH_DENSITY),
    DEFAULT(Printer.PARAM_DEFAULT)
}
