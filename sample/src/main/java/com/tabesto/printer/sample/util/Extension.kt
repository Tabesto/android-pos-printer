package com.tabesto.printer.sample.util

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.mikepenz.fastadapter.binding.BindingViewHolder
import com.mikepenz.fastadapter.items.BaseItem

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline bindingInflater: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) { bindingInflater.invoke(layoutInflater) }

/**
 * FastAdapter items needs absolutely to have unique ids.
 * Unfortunately, ids from entities are simple integers (for e.g. sub item can easily have same id in parent items).
 * So to ensure that, we use the Item type as a salt.
 */
fun <T : RecyclerView.ViewHolder> BaseItem<out T>?.getTypedIdentifier(id: Int?): Long =
    id?.let { this?.type?.toLong()?.let { type -> it * type } } ?: -1L

/**
 * Permit to use [com.mikepenz.fastadapter.listeners.EventHook] with [com.mikepenz.fastadapter.binding.AbstractBindingItem]
 * See https://github.com/mikepenz/FastAdapter/issues/884#issuecomment-619419014
 */
inline fun <reified T : ViewBinding> RecyclerView.ViewHolder.asBinding(block: (T) -> View): View? {
    return if (this is BindingViewHolder<*> && this.binding is T) {
        block(this.binding as T)
    } else {
        null
    }
}
