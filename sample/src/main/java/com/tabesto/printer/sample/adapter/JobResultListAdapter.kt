package com.tabesto.printer.sample.adapter

import android.graphics.Color.GREEN
import android.graphics.Color.RED
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult
import com.tabesto.printer.sample.R

class JobResultListAdapter(listOfHistoryJobsResult: ArrayList<DeviceManagerJobResult>, val listener: JobResultListAdapterListener) :
    RecyclerView.Adapter<JobResultListAdapter.ViewHolder>() {
    private val listOfJobsResult: ArrayList<DeviceManagerJobResult> = listOfHistoryJobsResult

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var jobDateTime: TextView = view.findViewById(R.id.textView_job_timestamp)
        var jobPrinterAddress: TextView = view.findViewById(R.id.textView_job_printer_address)
        var jobScopeTag: TextView = view.findViewById(R.id.textView_job_scope)
        var jobResult: TextView = view.findViewById(R.id.textView_job_result)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_job_result, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = listOfJobsResult.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.jobDateTime.text = listOfJobsResult[position].datetime
        viewHolder.jobPrinterAddress.text = listOfJobsResult[position].printerData.printerAddress
        viewHolder.jobScopeTag.text = listOfJobsResult[position].scopeTag.name

        if (listOfJobsResult[position].isSuccessful) {
            viewHolder.jobResult.text = viewHolder.itemView.context.getString(R.string.job_succeed)
            viewHolder.jobResult.setTextColor(GREEN)
        } else {
            viewHolder.jobResult.text = viewHolder.itemView.context.getString(R.string.job_failed)
            viewHolder.jobResult.setTextColor(RED)
            viewHolder.jobResult.setOnClickListener { listener.onClickOnJobResult(listOfJobsResult[position]) }
        }
    }
}
