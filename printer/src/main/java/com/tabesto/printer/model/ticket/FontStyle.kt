package com.tabesto.printer.model.ticket

import com.epson.epos2.printer.Printer

/**
 * enum class FontStyle contains different Text font style before printer write text
 *
 * @property fontStyle
 */
@Suppress("unused")
enum class FontStyle(var fontStyle: Int) {
    FONT_A(Printer.FONT_A),
    FONT_B(Printer.FONT_B),
    FONT_C(Printer.FONT_C),
    FONT_D(Printer.FONT_D),
    FONT_E(Printer.FONT_E),
    DEFAULT(Printer.FONT_A)
}
