package com.tabesto.printer.model.ticket

import com.epson.epos2.printer.Printer

/**
 * Defines printer halftone please check Epson doc
 *
 * @property halftoneMode
 */
@Suppress("unused")
enum class PrinterHalftone(val halftoneMode: Int) {
    HALFTONE_DITHER(Printer.HALFTONE_DITHER),
    HALFTONE_ERROR_DIFFUSION(Printer.HALFTONE_ERROR_DIFFUSION),
    HALFTONE_THRESHOLD(Printer.HALFTONE_THRESHOLD),
    DEFAULT(Printer.PARAM_DEFAULT)
}
