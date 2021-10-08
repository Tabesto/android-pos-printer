package com.tabesto.printer.model

import com.tabesto.printer.model.ticket.TicketData

sealed class Action {
    class Connect(val timeout: Int? = null) : Action()
    class Print(val ticketData: TicketData?, val timeout: Int? = null) : Action()
    object Disconnect : Action()

    override fun toString(): String {
        return "${this.javaClass.simpleName} Action"
    }
}
