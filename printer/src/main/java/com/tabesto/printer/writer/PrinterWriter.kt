package com.tabesto.printer.writer

import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.Printer
import com.tabesto.printer.model.PrinterLogInfo
import com.tabesto.printer.model.error.PrinterError
import com.tabesto.printer.model.error.PrinterException
import com.tabesto.printer.model.ticket.FeedCutLine
import com.tabesto.printer.model.ticket.FeedLine
import com.tabesto.printer.model.ticket.ImageLine
import com.tabesto.printer.model.ticket.StringLine
import com.tabesto.printer.model.ticket.StyleLine
import com.tabesto.printer.model.ticket.TicketData
import com.tabesto.printer.utils.log.Logger
import com.tabesto.printer.utils.log.LoggerExtraArgument

/**
 * PrinterWriter class give the possibility to write ticket data sent by caller app
 * it will apply the style defined inside each line and write the line or image (a line can be also a feed line or cut  feed)
 *
 * @property printer
 */
open class PrinterWriter(val printer: Printer?, private val listener: PrinterWriterListener) {

    private var logger: Logger = Logger(PrinterWriter::class.java.simpleName)

    fun writeData(ticketData: TicketData) {
        try {
            printer?.clearCommandBuffer()

            for (ticketLine in ticketData.ticketLineList) {
                when (ticketLine) {
                    is StringLine -> writeStringLine(ticketLine)
                    is ImageLine -> writeImageLine(ticketLine)
                    is FeedLine -> writeFeedLine(ticketLine)
                    is FeedCutLine -> writeFeedCutLine()
                }
            }
        } catch (epos2Exception: Epos2Exception) {
            val printerError = PrinterError.getPrinterException(epos2Exception.errorStatus)
            val printerExceptionBuilder = PrinterException.PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withPrinterError(printerError).build()
            printer?.clearCommandBuffer()
            val printerLogInfo = PrinterLogInfo(printerException = printerException)
            logger.e(printerLogInfo, loggerExtraArg = *arrayOf(LoggerExtraArgument("method-name", "writeData")))
            listener.onFailureWriteData(printerException)
        } catch (exception: Exception) {
            val printerExceptionBuilder = PrinterException.PrinterExceptionBuilder()
            val printerException = printerExceptionBuilder.withPrinterError(errorMessage = exception.message).build()
            printer?.clearCommandBuffer()
            val printerLogInfo = PrinterLogInfo(printerException = printerException)
            logger.e(printerLogInfo, loggerExtraArg = *arrayOf(LoggerExtraArgument("method-name", "writeData")))
            listener.onFailureWriteData(printerException)
        }
    }

    @Throws(Exception::class)
    fun writeImageLine(imageLine: ImageLine) {
        if (imageLine.bitmap != null) {
            printer?.addTextAlign(imageLine.alignStyle.epsonAlign)
            val imageWidth = imageLine.bitmap?.width ?: imageLine.width
            val imageHeight = imageLine.bitmap?.height ?: imageLine.height
            printer?.addImage(
                imageLine.bitmap,
                imageLine.horizontalStartPosition,
                imageLine.verticalStartPosition,
                imageWidth,
                imageHeight,
                imageLine.color.colorInt,
                imageLine.printerColorMode.colorMode,
                imageLine.printerHalftone.halftoneMode,
                imageLine.brightness,
                imageLine.compress.compressMode
            )
        }
    }

    @Throws(Exception::class)
    private fun writeFeedCutLine() {
        printer?.addCut(Printer.CUT_FEED)
    }

    @Throws(Exception::class)
    private fun writeFeedLine(feedLine: FeedLine) {
        printer?.addFeedLine(feedLine.number)
    }

    @Throws(Exception::class)
    private fun writeStringLine(stringLine: StringLine) {
        val styleLine: StyleLine = stringLine.styleLine
        printer?.addTextAlign(styleLine.alignStyle.epsonAlign)
        printer?.addTextFont(styleLine.fontStyle.fontStyle)
        printer?.addTextSize(styleLine.textWidth, styleLine.textHeight)
        printer?.addTextStyle(
            checkTextStyle(styleLine.textStyle.reverse),
            checkTextStyle(styleLine.textStyle.underscore),
            checkTextStyle(styleLine.textStyle.bold),
            styleLine.textStyle.color
        )
        printer?.addText("${stringLine.text} \n")
    }

    /**
     * checkTextStyleBoolean method return correct value of Printer Parameter
     * If it is not null it will send the correct parameter matching to boolean
     * true -->  Printer.TRUE
     * false --> Printer.FALSE
     * if it is null it will send back default value Printer.PARAM_DEFAULT
     *
     * @param isEnable
     * @return Int : corresponding to printer parameter
     */
    private fun checkTextStyle(isEnable: Boolean?): Int {
        return isEnable?.let { if (isEnable) Printer.TRUE else Printer.FALSE } ?: Printer.PARAM_DEFAULT
    }
}
