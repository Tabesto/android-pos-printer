package com.tabesto.printer.model.ticket

import com.epson.epos2.printer.Printer

/**
 * PrinterColor defines printer color
 * DEFAULT default color
 * COLOR_NONE : no color
 * COLOR1 : color 1 please check Epson doc
 * COLOR2 : color 2 please check Epson doc
 * COLOR3 : color 3 please check Epson doc
 * COLOR4 : color 4 please check Epson doc
 *
 * @property colorInt
 */
@Suppress("unused")
enum class PrinterColor(val colorInt: Int) {
    DEFAULT(Printer.PARAM_DEFAULT),
    COLOR_NONE(Printer.COLOR_NONE),
    COLOR1(Printer.COLOR_1),
    COLOR2(Printer.COLOR_2),
    COLOR3(Printer.COLOR_3),
    COLOR4(Printer.COLOR_4)
}
