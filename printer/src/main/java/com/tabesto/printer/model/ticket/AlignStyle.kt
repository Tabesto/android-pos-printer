package com.tabesto.printer.model.ticket

import com.epson.epos2.printer.Printer

/**
 * AlignStyle enums different alignment that will be defined when printer writes text
 *
 * @property epsonAlign
 */
@Suppress("unused")
enum class AlignStyle(val epsonAlign: Int) {
    LEFT(Printer.ALIGN_LEFT),
    RIGHT(Printer.ALIGN_RIGHT),
    CENTER(Printer.ALIGN_CENTER);
}
