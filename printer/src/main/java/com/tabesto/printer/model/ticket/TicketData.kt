package com.tabesto.printer.model.ticket

/**
 * TicketData is an object that represent your whole ticket that  is printed by the printer
 * it has a constructor with a TicketDataBuilder in order to build each line of the ticket
 *
 * @param ticketLineList
 */
class TicketData(var ticketLineList: MutableList<TicketLine> = mutableListOf()) {
    constructor (ticketDataBuilder: TicketDataBuilder) : this(ticketDataBuilder.ticketLineList)

    class TicketDataBuilder {
        var ticketLineList: MutableList<TicketLine> = mutableListOf()
            private set

        /**
         * withLine add a string line with its style
         *
         * @param stringLine
         * @return
         */
        fun withLine(stringLine: StringLine): TicketDataBuilder {
            ticketLineList.add(stringLine)
            return this
        }

        /**
         * withFeedLine add a feed line
         * it will add an empty line
         *
         * @param feedLine
         * @return
         */
        fun withFeedLine(feedLine: FeedLine): TicketDataBuilder {
            ticketLineList.add(feedLine)
            return this
        }

        /**
         * withFeedCutLine add a cut line
         * for example it can cut the ticket at the end of the data
         *
         * @param feedCutLine
         * @return
         */
        fun withFeedCutLine(feedCutLine: FeedCutLine): TicketDataBuilder {
            ticketLineList.add(feedCutLine)
            return this
        }

        /**
         * withImageLine add an image to the ticket with its own style
         *
         * @param imageLine
         * @return
         */
        fun withImageLine(imageLine: ImageLine): TicketDataBuilder {
            ticketLineList.add(imageLine)
            return this
        }

        /**
         * Build TicketData object with its line list and return built object TicketData
         *
         * @return
         */
        fun build(): TicketData {
            return TicketData(this)
        }
    }
}
