package com.tabesto.printer.sample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tabesto.printer.model.PrinterRemainingJob
import com.tabesto.printer.sample.R

class RemainingJobListAdapter(private val listOfRemainingJobs: List<PrinterRemainingJob>) :
    RecyclerView.Adapter<RemainingJobListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var jobPrinterAddress: TextView = view.findViewById(R.id.textView_remaining_job_printer_address)
        var jobScopeTag: TextView = view.findViewById(R.id.textView_remaining_job_scope)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_remaining_job, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listOfRemainingJobs.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.jobPrinterAddress.text = listOfRemainingJobs[position].printerData.printerAddress
        viewHolder.jobScopeTag.text = listOfRemainingJobs[position].scopeTag.name
    }
}
