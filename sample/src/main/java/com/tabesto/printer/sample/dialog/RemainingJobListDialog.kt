package com.tabesto.printer.sample.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tabesto.printer.model.PrinterRemainingJob
import com.tabesto.printer.sample.R
import com.tabesto.printer.sample.adapter.RemainingJobListAdapter

class RemainingJobListDialog : DialogFragment() {
    private lateinit var remainingJobListRecyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var buttonClose: Button
    private lateinit var buttonRemainingJobCancel: Button
    private lateinit var remainingJobListAdapter: RemainingJobListAdapter
    private lateinit var listener: RemainingJobListDialogListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_job_remaining_list, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listOfRemainingJob: List<PrinterRemainingJob> =
            arguments?.getParcelableArrayList<PrinterRemainingJob>(listOfRemainingJobTag) as List<PrinterRemainingJob>

        remainingJobListRecyclerView = view.findViewById(R.id.recyclerView_remaining_job_List)
        buttonClose = view.findViewById(R.id.button_close_remaining_job_list)
        buttonRemainingJobCancel = view.findViewById(R.id.button_cancel_remaining_job_list)

        buttonClose.setOnClickListener { dismiss() }

        buttonRemainingJobCancel.setOnClickListener {
            listener.onCancelJobs()
            dismiss()
        }

        linearLayoutManager = LinearLayoutManager(this.context)
        remainingJobListRecyclerView.layoutManager = linearLayoutManager
        remainingJobListAdapter = RemainingJobListAdapter(listOfRemainingJob)
        remainingJobListRecyclerView.adapter = remainingJobListAdapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as RemainingJobListDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() +
                    " must implement RemainingJobListDialogListener")
            )
        }
    }

    companion object {
        const val listOfRemainingJobTag = "listOfRemainingJob"

        fun newInstance(listOfRemainingJob: List<PrinterRemainingJob>): RemainingJobListDialog {
            return RemainingJobListDialog().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(listOfRemainingJobTag, ArrayList(listOfRemainingJob))
                }
            }
        }
    }
}
