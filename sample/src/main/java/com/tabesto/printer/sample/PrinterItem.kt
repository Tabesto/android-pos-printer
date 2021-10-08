package com.tabesto.printer.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.select.SelectExtension
import com.tabesto.printer.model.PrinterData
import com.tabesto.printer.sample.databinding.ItemviewPrinterItemBinding
import com.tabesto.printer.sample.util.asBinding

class PrinterItem(var printerData: PrinterData?, private val listener: Listener? = null) :
    AbstractBindingItem<ItemviewPrinterItemBinding>() {

    override val type: Int
        get() = R.id.fastadapter_printer_item_id

    override fun bindView(binding: ItemviewPrinterItemBinding, payloads: List<Any>) {
        binding.edittextPrinterItemAddress.apply {
            setText(printerData?.printerAddress)
            doAfterTextChanged {
                val enteredAddress = it.toString()
                val newPrinterData = printerData?.copy(printerAddress = enteredAddress)
                printerData = newPrinterData
            }
        }
        isSelected = binding.checkboxPrinterItem.isChecked
        binding.checkboxPrinterItem.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listener?.onPrinterSelected(this)
            } else {
                listener?.onPrinterUnselected(this)
            }
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemviewPrinterItemBinding =
        ItemviewPrinterItemBinding.inflate(inflater, parent, false)

    class CheckBoxClickEvent : ClickEventHook<PrinterItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.asBinding<ItemviewPrinterItemBinding> {
                it.checkboxPrinterItem
            }
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<PrinterItem>,
            item: PrinterItem
        ) {
            val selectExtension: SelectExtension<PrinterItem> = fastAdapter.requireExtension()
            selectExtension.toggleSelection(position)
        }
    }

    class StatusClickEvent(private val listener: Listener? = null) : ClickEventHook<PrinterItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.asBinding<ItemviewPrinterItemBinding> {
                it.imageviewPrinterItemStatusButton
            }
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<PrinterItem>,
            item: PrinterItem
        ) {
            listener?.onPrinterStatusButtonClick(position, item)
        }
    }

    interface Listener {
        fun onPrinterStatusButtonClick(position: Int, item: PrinterItem)
        fun onPrinterSelected(item: PrinterItem)
        fun onPrinterUnselected(item: PrinterItem)
    }
}
