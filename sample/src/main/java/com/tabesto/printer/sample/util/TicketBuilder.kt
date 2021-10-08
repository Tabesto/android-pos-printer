package com.tabesto.printer.sample.util

import android.content.Context
import android.graphics.BitmapFactory
import com.tabesto.printer.model.ticket.FeedCutLine
import com.tabesto.printer.model.ticket.FeedLine
import com.tabesto.printer.model.ticket.ImageLine
import com.tabesto.printer.model.ticket.StringLine
import com.tabesto.printer.model.ticket.StyleLine
import com.tabesto.printer.model.ticket.TicketData
import com.tabesto.printer.sample.R

object TicketBuilder {

    /**
     * Build ticket data with TicketBuilder in order to pass data to printer to print data
     *
     */
    fun buildSampleTicketData(context: Context): TicketData {
        val logoData = BitmapFactory.decodeResource(context.resources, R.drawable.tabestologoresized)
        val ticketDataBuilder = TicketData.TicketDataBuilder()
        val imageLineBuilder = ImageLine.ImageLineBuilder()
        imageLineBuilder.withBitmap(logoData)
        return ticketDataBuilder
            .withImageLine(imageLineBuilder.build())
            .withFeedLine(FeedLine())
            .withLine(StringLine("THE STORE 123 (555) 555 – 5555", StyleLine()))
            .withFeedLine(FeedLine())
            .withLine(StringLine("------------------------------", StyleLine()))
            .withLine(StringLine("400 OHEIDA 3PK SPRINGF  9.99 R", StyleLine()))
            .withLine(StringLine("410 3 CUP BLK TEAPOT    9.99 R", StyleLine()))
            .withLine(StringLine("------------------------------", StyleLine()))
            .withFeedLine(FeedLine())
            .withLine(StringLine("TOTAL    174.81", StyleLine(textHeight = 2, textWidth = 2)))
            .withFeedLine(FeedLine())
            .withLine(StringLine(context.resources.getString(R.string.powered_tabesto), StyleLine()))
            .withFeedCutLine(FeedCutLine())
            .build()
    }

    /**
     * Build small ticket data to limit paper usage
     *
     */
    fun buildSmallTicketData(context: Context): TicketData = TicketData.TicketDataBuilder()
        .withLine(StringLine(context.resources.getString(R.string.powered_tabesto), StyleLine()))
        .withFeedCutLine(FeedCutLine())
        .build()
}
