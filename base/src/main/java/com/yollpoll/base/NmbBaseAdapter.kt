package com.yollpoll.base

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.yollpoll.framework.paging.BasePagingDataAdapter
import com.yollpoll.framework.paging.BaseViewHolder
import com.yollpoll.skin.SkinInflaterFactory

/**
 * 相比基础库里，回调新增T
 */
open class NmbPagingDataAdapter<T : Any>(
    private val layoutId: Int,
    private val variableId: Int?,
    private val onBindViewHolder: ((T?, Int, BaseViewHolder<T>) -> Unit)? = null,
    private val onBindDataBinding: ((T?, Int, ViewDataBinding) -> Unit)? = null,
    private val onBindPlaceHolder: ((T?, Int, ViewDataBinding) -> Unit)? = null,
    private val itemSame: (T, T) -> Boolean = { old, new -> old == new },
) : PagingDataAdapter<T, BaseViewHolder<T>>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return itemSame.invoke(oldItem, newItem)
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}) {

    lateinit var context: Context

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val item = getItem(position)
        if (null != item) {
            holder.bind(item)
        } else {
            onBindPlaceHolder?.run {
                Log.d("spq", "onBindViewHolder: ")
                this.invoke(item,position, holder.binding)
            }
        }
        onBindViewHolder?.run {
            this.invoke(item,position, holder)
        }
        onBindDataBinding?.run {
            this.invoke(item,position, holder.binding)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        context = parent.context
        val inflater=LayoutInflater.from(context).cloneInContext(context)
        inflater.factory2 = SkinInflaterFactory

        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            inflater,
            layoutId,
            parent,
            false
        )
        return BaseViewHolder(binding, variableId)
    }

}