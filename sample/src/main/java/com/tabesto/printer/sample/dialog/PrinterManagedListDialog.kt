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
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.model.PrinterManaged
import com.tabesto.printer.sample.R
import com.tabesto.printer.sample.adapter.PrinterDataAndStatusListAdapter
import com.tabesto.printer.sample.adapter.PrinterDataAndStatusListAdapterListener

@Suppress("unchecked_cast")
class PrinterManagedListDialog : DialogFragment(), PrinterDataAndStatusListAdapterListener {
    private lateinit var listener: PrinterManagedListDialogListener
    private lateinit var printerAndStatusListRecyclerView: RecyclerView
    private lateinit var printerDataAndStatusListAdapter: PrinterDataAndStatusListAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var buttonClose: Button

    fun newInstance(listOfPrinterDataAndPrinterStatus: List<PrinterManaged>): PrinterManagedListDialog {
        return PrinterManagedListDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(listOfPrinterDataAndPrinterStatusTag, ArrayList(listOfPrinterDataAndPrinterStatus))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_printer_and_status_list, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listOfPrinterDataAndPrinterStatus: List<PrinterManaged> =
            arguments?.getParcelableArrayList<PrinterManaged>(listOfPrinterDataAndPrinterStatusTag) as List<PrinterManaged>

        printerAndStatusListRecyclerView = view.findViewById(R.id.recyclerViewPrinterDataAndStatusList)
        buttonClose = view.findViewById(R.id.button_printer_list_close)

        buttonClose.setOnClickListener { dismiss() }

        linearLayoutManager = LinearLayoutManager(this.context)
        printerAndStatusListRecyclerView.layoutManager = linearLayoutManager
        printerDataAndStatusListAdapter = PrinterDataAndStatusListAdapter(listOfPrinterDataAndPrinterStatus, this)
        printerAndStatusListRecyclerView.adapter = printerDataAndStatusListAdapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as PrinterManagedListDialogListener
        } catch (e: ClassCastException) {
            Log.e(JobResultListDialogTag, "an error happened while trying to attach listener " + e.message)
        }
    }

    override fun onClickPrinterItem(printerData: PrinterData) {
        listener.onPrinterSelected(printerData)
        dismiss()
    }

    override fun remove(printerData: PrinterData) {
        listener.onRemovePrinter(printerData)
        dismiss()
    }

    companion object {
        const val JobResultListDialogTag = "JobResultListDialog"
        const val listOfPrinterDataAndPrinterStatusTag = "listOfPrinterDataAndPrinterStatus"
    }
}
