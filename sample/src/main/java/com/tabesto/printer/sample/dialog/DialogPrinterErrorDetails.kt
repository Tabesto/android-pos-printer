package com.tabesto.printer.sample.dialog

import android.graphics.Color.RED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.tabesto.printer.model.devicemanager.DeviceManagerJobResult
import com.tabesto.printer.sample.R

class DialogPrinterErrorDetails : DialogFragment() {
    private lateinit var textViewPrinterAddress: TextView
    private lateinit var textViewScope: TextView
    private lateinit var textViewDate: TextView
    private lateinit var textViewErrorMessage: TextView
    private lateinit var textViewErrorCode: TextView
    private lateinit var textViewErrorAction: TextView
    private lateinit var buttonClose: Button

    fun newInstance(jobResult: DeviceManagerJobResult): DialogPrinterErrorDetails {
        return DialogPrinterErrorDetails().apply {
            arguments = Bundle().apply {
                putParcelable(jobResultTag, jobResult)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_printer_error_details, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewPrinterAddress = view.findViewById(R.id.textView_error_printer_address)
        textViewScope = view.findViewById(R.id.textView_error_scope)
        textViewDate = view.findViewById(R.id.textView_error_timestamp)
        textViewErrorMessage = view.findViewById(R.id.textView_error_message)
        textViewErrorCode = view.findViewById(R.id.textView_error_code)
        textViewErrorAction = view.findViewById(R.id.textView_error_action)
        buttonClose = view.findViewById(R.id.button_printer_error_close)
        textViewScope.setTextColor(RED)

        val jobResult: DeviceManagerJobResult? = arguments?.getParcelable(jobResultTag)

        jobResult?.let { result ->
            context?.let { context ->
                textViewPrinterAddress.text =
                    String.format(context.getString(R.string.job_result_error_printer_address, result.printerData.printerAddress))
                textViewScope.text = String.format(context.getString(R.string.job_result_error_scope, result.scopeTag.name))
                textViewDate.text = String.format(context.getString(R.string.job_result_error_date, result.datetime))

                if (result.printerException != null) {
                    textViewErrorMessage.text =
                        String.format(context.getString(R.string.job_result_error_message, result.printerException?.message))
                } else {
                    textViewErrorMessage.text =
                        String.format(context.getString(R.string.job_result_error_message, result.deviceManagerException?.error))
                }

                textViewErrorCode.text =
                    String.format(context.getString(R.string.job_result_error_code, result.printerException?.printerCode))
                textViewErrorAction.text =
                    String.format(context.getString(R.string.job_result_error_action, result.printerException?.action))
            }
        }

        buttonClose.setOnClickListener { dismiss() }
    }

    companion object {
        const val jobResultTag = "jobResult"
    }
}
