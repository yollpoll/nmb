package com.yollpoll.base

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yollpoll.framework.extensions.toListJson
import com.yollpoll.framework.paging.BaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

open class BaseAdapter<T : Any>(
    private val layoutId: Int,
    private val variableId: Int?,
    private val onBindViewHolder: ((T?, Int, BaseViewHolder<T>, MutableList<Any>) -> Unit)? = null,
    private val itemSame: (T, T) -> Boolean = { old, new -> old == new },
    private val contentSame: (T, T) -> Boolean = { old, new -> old == new },
    private val getChangePayload: ((T, T) -> Any?)? = null
) : RecyclerView.Adapter<BaseViewHolder<T>>() {
    var oldData = listOf<T>()
    lateinit var context: Context

    //注意，newData不能和old是同一个集合，必须用深拷贝，否则会导致刷新失败
    fun submitData(newData: List<T>) {
        val diffResult =
            DiffUtil.calculateDiff(
                MyDiff(
                    oldData,
                    newData,
                    itemSame,
                    contentSame,
                    getChangePayload
                ), true
            )
        diffResult.dispatchUpdatesTo(this)
        oldData = newData
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

    override fun onBindViewHolder(
        holder: BaseViewHolder<T>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        "bind".logI()
        val item = oldData[position]
        holder.bind(item)
        onBindViewHolder?.run {
            this.invoke(item, position, holder, payloads)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {

    }

    override fun getItemCount() = oldData.size
}

open class MyDiff<T>(
    private val oldData: List<T>,
    private val newData: List<T>,
    private val itemSame: (T, T) -> Boolean,
    private val contentSame: (T, T) -> Boolean,
    private val getChangePayload: ((T, T) -> Any?)? = null
) :
    DiffUtil.Callback() {
    override fun getOldListSize() = oldData.size

    override fun getNewListSize() = newData.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        itemSame.invoke(oldData[oldItemPosition], newData[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        contentSame.invoke(oldData[oldItemPosition], newData[newItemPosition])

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return getChangePayload?.invoke(oldData[oldItemPosition], newData[newItemPosition])
    }
}
