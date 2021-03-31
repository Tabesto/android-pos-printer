package com.tabesto.printer.model.ticket

/**
 * StyleLine defines the whole style of [StringLine] before its string value is written by printer writer
 *
 * @param alignStyle : define text alignment
 * @param fontStyle : define text font
 * @param textWidth : define text width
 * @param textHeight : define text height
 * @param textStyle : define text style , bold etc.
 */
data class StyleLine(
    var alignStyle: AlignStyle = AlignStyle.CENTER,
    var fontStyle: FontStyle = FontStyle.DEFAULT,
    var textWidth: Int = 1,
    var textHeight: Int = 1,
    var textStyle: TextStyle = TextStyle()
)
