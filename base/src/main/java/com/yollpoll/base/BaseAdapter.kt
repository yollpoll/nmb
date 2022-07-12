package com.yollpoll.base

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yollpoll.framework.paging.BaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

open class BaseAdapter<T : Any>(
    private val layoutId: Int,
    private val variableId: Int?,
    private val onBindViewHolder: ((T?, Int, BaseViewHolder<T>) -> Unit)? = null,
    private val itemSame: (T, T) -> Boolean = { old, new -> old == new },
    private val contentSame: (T, T) -> Boolean
) : RecyclerView.Adapter<BaseViewHolder<T>>() {
    var oldData = listOf<T>()
    lateinit var context: Context

    fun submitData(newData: List<T>) {
        val diffResult =
            DiffUtil.calculateDiff(MyDiff(oldData, newData, itemSame, contentSame), true)
        oldData = newData
        diffResult.dispatchUpdatesTo(this)
    }

    //防止计算量过大，应放在子线程计算
    fun submitData(scope: CoroutineScope, newData: List<T>) {
        scope.launch {
            val res = async(Dispatchers.IO) {
                val diffResult =
                    DiffUtil.calculateDiff(MyDiff(oldData, newData, itemSame, contentSame), true)
                oldData = newData
                return@async diffResult
            }.await()
            launch(Dispatchers.IO) {
                res.dispatchUpdatesTo(this@BaseAdapter)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        context = parent.context
        val binding =
            DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context),
                layoutId,
                parent,
                false
            )
        return BaseViewHolder(binding, variableId)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val item = oldData[position]
        holder.bind(item)
        onBindViewHolder?.run {
            this.invoke(item, position, holder)
        }
    }

    override fun getItemCount() = oldData.size
}

open class MyDiff<T>(
    val oldData: List<T>,
    val newData: List<T>,
    private val itemSame: (T, T) -> Boolean,
    private val contentSame: (T, T) -> Boolean
) :
    DiffUtil.Callback() {
    override fun getOldListSize() = oldData.size

    override fun getNewListSize() = newData.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        itemSame.invoke(oldData[oldItemPosition], newData[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        contentSame.invoke(oldData[oldItemPosition], newData[newItemPosition])

}
