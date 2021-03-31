package com.tabesto.printer.model.ticket

import com.epson.epos2.printer.Printer

/**
 * PrinterImageCompress compress the image
 * please see Epson doc
 *
 * @property compressMode
 */
@Suppress("unused")
enum class PrinterImageCompress(var compressMode: Int) {
    COMPRESS_DEFLATE(Printer.COMPRESS_DEFLATE),
    COMPRESS_NONE(Printer.COMPRESS_NONE),
    COMPRESS_AUTO(Printer.COMPRESS_AUTO),
    DEFAULT(Printer.PARAM_DEFAULT)
}
