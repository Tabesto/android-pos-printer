package com.tabesto.printer.model.ticket

/**
 * TextStyle define text style to precise to printer before writing text string value
 *
 * @param reverse : see Epson SDK doc
 * @param underscore : it will underline the text
 * @param bold : it will make bold the text
 * @param color : it will apply the specified color on the next ( if it is a color printer)
 */
data class TextStyle(
    var reverse: Boolean? = null,
    var underscore: Boolean? = null,
    var bold: Boolean? = null,
    var color: Int = PrinterColor.DEFAULT.colorInt
)
