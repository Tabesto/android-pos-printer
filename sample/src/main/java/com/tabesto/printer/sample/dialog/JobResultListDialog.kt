package com.tabesto.printer.sample.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult
import com.tabesto.printer.sample.R
import com.tabesto.printer.sample.adapter.JobResultListAdapter
import com.tabesto.printer.sample.adapter.JobResultListAdapterListener

@Suppress("UNCHECKED_CAST")
class JobResultListDialog : DialogFragment(), JobResultListAdapterListener {
    private lateinit var jobResultListRecyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var buttonClose: Button
    private lateinit var buttonCLearList: Button
    private lateinit var jobResultListAdapter: JobResultListAdapter
    private var listener: JobResultListDialogListener? = null

    fun newInstance(
        listOfHistoryJobsResult: List<DeviceManagerJobResult>,
        isClearListButtonEnabled: Boolean = false
    ): JobResultListDialog {
        return JobResultListDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(listOfHistoryJobsResultTag, ArrayList(listOfHistoryJobsResult))
                putBoolean(isClearListButtonEnabledTag, isClearListButtonEnabled)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_job_result_list, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listOfHistoryJobsResult: List<DeviceManagerJobResult> =
            arguments?.getParcelableArrayList<DeviceManagerJobResult>(listOfHistoryJobsResultTag) as List<DeviceManagerJobResult>
        val isClearListButtonEnabled = arguments?.getBoolean(isClearListButtonEnabledTag)

        jobResultListRecyclerView = view.findViewById(R.id.recyclerViewJobResultList)
        buttonClose = view.findViewById(R.id.button_close)
        buttonCLearList = view.findViewById(R.id.button_clear_list)

        isClearListButtonEnabled?.let { isButtonEnabled ->
            if (isButtonEnabled) {
                buttonCLearList.visibility = View.VISIBLE
            } else {
                buttonCLearList.visibility = View.GONE
            }
        }

        buttonClose.setOnClickListener { dismiss() }
        buttonCLearList.setOnClickListener {
            listener?.clearListOfJobsResult() ?: Log.d(JobResultListDialogTag, "listener is null")
            dismiss()
        }

        linearLayoutManager = LinearLayoutManager(this.context)
        linearLayoutManager.reverseLayout = true
        jobResultListRecyclerView.layoutManager = linearLayoutManager
        jobResultListAdapter = JobResultListAdapter(listOfHistoryJobsResult, this)
        jobResultListRecyclerView.adapter = jobResultListAdapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as JobResultListDialogListener
        } catch (e: ClassCastException) {
            Log.e(JobResultListDialogTag, "an error happened while trying to attach listener " + e.message)
        }
    }

    override fun onClickOnJobResult(jobResult: DeviceManagerJobResult) {
        listener?.onJobResultErrorSelected(jobResult)
        dismiss()
    }

    companion object {
        const val JobResultListDialogTag = "JobResultListDialog"
        const val listOfHistoryJobsResultTag = "listOfHistoryJobsResult"
        const val isClearListButtonEnabledTag = "isClearListButtonEnabled"
    }
}
