package com.tabesto.printer.sample.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tabesto.printer.model.PrinterManaged
import com.tabesto.printer.sample.R

class PrinterDataAndStatusListAdapter(
    private val listOfPrinterDataAndPrinterStatus: List<PrinterManaged>,
    private val listener: PrinterDataAndStatusListAdapterListener
) : RecyclerView.Adapter<PrinterDataAndStatusListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var printerAddress: TextView = view.findViewById(R.id.textView_printer_address)
        var printerStatus: TextView = view.findViewById(R.id.textView_printer_status)
        var printerBinImage: ImageView = view.findViewById(R.id.imageView_bin_remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_printer_data_and_printer_status, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = listOfPrinterDataAndPrinterStatus.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.printerAddress.text = listOfPrinterDataAndPrinterStatus[position].printerData.printerAddress

        val isConnected = listOfPrinterDataAndPrinterStatus[position].printerStatus.connectionStatus.isConnected
        if (isConnected == true) {
            viewHolder.printerStatus.text = viewHolder.itemView.context.getString(R.string.printer_connected)
            viewHolder.printerStatus.setTextColor(Color.GREEN)
            viewHolder.printerBinImage.visibility = View.GONE
        } else {
            viewHolder.printerStatus.text = viewHolder.itemView.context.getString(R.string.printer_disconnected)
            viewHolder.printerStatus.setTextColor(Color.RED)
            viewHolder.printerBinImage.visibility = View.VISIBLE
            viewHolder.printerBinImage.setOnClickListener {
                listener.remove(listOfPrinterDataAndPrinterStatus[position].printerData)
            }
        }
        viewHolder.printerAddress.setOnClickListener {
            listener
                .onClickPrinterItem(listOfPrinterDataAndPrinterStatus[position].printerData)
        }
    }
}
