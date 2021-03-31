package com.tabesto.printer.model.ticket

import android.graphics.Bitmap

open class TicketLine

/**
 * FeedCutLine is a feed cut line type class
 * it will cut printer ticket paper
 *
 */
class FeedCutLine : TicketLine()

/**
 * FeedLine represents an empty line
 *
 * @param number of line to add
 */
data class FeedLine(var number: Int = 1) : TicketLine()

/**
 * StringLine class is passed to printer writer module in order
 *
 * @param text : contains text to write
 * @param styleLine : contains text Style to define to printer before writing it
 */
data class StringLine(var text: String?, var styleLine: StyleLine) : TicketLine()

/**
 * ImageLine class represent a line with an image on your ticket
 */
class ImageLine : TicketLine() {
    var bitmap: Bitmap? = null
    var alignStyle: AlignStyle = AlignStyle.CENTER
    var horizontalStartPosition: Int = 0
        set(value) {
            when {
                value in 0..65534 -> field = value
                value < 0 -> field = 0
                value > 65534 -> field = 65534
            }
        }
    var verticalStartPosition: Int = 0
        set(value) {
            when {
                value in 0..65534 -> field = value
                value < 0 -> field = 0
                value > 65534 -> field = 65534
            }
        }
    var width: Int = 160
        set(value) {
            when {
                value in 0..65535 -> field = value
                value < 0 -> field = 0
                value > 65535 -> field = 65535
            }
        }
    var height: Int = 80
        set(value) {
            when {
                value in 0..65535 -> field = value
                value < 0 -> field = 0
                value > 65535 -> field = 65535
            }
        }

    var color: PrinterColor = PrinterColor.COLOR1
    var printerColorMode: PrinterColorMode = PrinterColorMode.MODE_MONO
    var printerHalftone: PrinterHalftone = PrinterHalftone.HALFTONE_DITHER
    var brightness: Double = 1.0
        set(value) {
            when {
                value < 0.1 && value > 10.0 -> field = value
                value < 0.1 -> field = 0.1
                value > 10.0 -> field = 10.0
            }
        }
    var compress: PrinterImageCompress = PrinterImageCompress.COMPRESS_AUTO

    @Suppress("unused")
    class ImageLineBuilder {
        private var bitmap: Bitmap? = null
        private var alignStyle: AlignStyle = AlignStyle.CENTER
        private var horizontalStartPosition: Int = 0
        private var verticalStartPosition: Int = 0
        private var width: Int = 160
        private var height: Int = 80
        private var color: PrinterColor = PrinterColor.COLOR1
        private var printerColorMode: PrinterColorMode = PrinterColorMode.MODE_MONO
        private var printerHalftone: PrinterHalftone = PrinterHalftone.HALFTONE_DITHER
        private var brightness: Double = 1.0
        private var compress: PrinterImageCompress = PrinterImageCompress.COMPRESS_AUTO

        fun withBitmap(bitmap: Bitmap) {
            this.bitmap = bitmap
        }

        fun withAlignStyle(alignStyle: AlignStyle) {
            this.alignStyle = alignStyle
        }

        fun withHorizontalStartPosition(horizontalStartPosition: Int) {
            this.horizontalStartPosition = horizontalStartPosition
        }

        fun withVerticalStartPosition(verticalStartPosition: Int) {
            this.verticalStartPosition = verticalStartPosition
        }

        fun withWidth(width: Int) {
            this.width = width
        }

        fun withHeight(height: Int) {
            this.height = height
        }

        fun withPrinterColorMode(printerColorMode: PrinterColorMode) {
            this.printerColorMode = printerColorMode
        }

        fun withPrinterHalftone(printerHalftone: PrinterHalftone) {
            this.printerHalftone = printerHalftone
        }

        fun withBrightness(brightness: Double) {
            this.brightness = brightness
        }

        fun withCompress(compress: PrinterImageCompress) {
            this.compress = compress
        }

        fun build(): ImageLine {
            val imageLine = ImageLine()
            imageLine.bitmap = bitmap
            imageLine.alignStyle = alignStyle
            imageLine.horizontalStartPosition = horizontalStartPosition
            imageLine.verticalStartPosition = verticalStartPosition
            imageLine.width = width
            imageLine.height = height
            imageLine.color = color
            imageLine.printerColorMode = printerColorMode
            imageLine.printerHalftone = printerHalftone
            imageLine.brightness = brightness
            imageLine.compress = compress
            return imageLine
        }
    }
}
