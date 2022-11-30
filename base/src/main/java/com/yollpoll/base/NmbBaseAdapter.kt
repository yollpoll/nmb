package com.yollpoll.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.DiffingChangePayload
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
    private val onBindViewHolder: ((T?, Int, BaseViewHolder<T>, MutableList<Any>) -> Unit)? = null,
    private val onBindDataBinding: ((T?, Int, ViewDataBinding, MutableList<Any>) -> Unit)? = null,
    private val onBindPlaceHolder: ((T?, Int, ViewDataBinding) -> Unit)? = null,
    private val itemSame: (T, T) -> Boolean = { old, new -> old == new },
    private val contentSame: (T, T) -> Boolean = { old, new -> old == new },
    private val getChangePayload: ((T, T) -> Any?)? = null
) : PagingDataAdapter<T, BaseViewHolder<T>>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return itemSame.invoke(oldItem, newItem)
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return contentSame.invoke(oldItem, newItem)
    }

    override fun getChangePayload(oldItem: T, newItem: T): Any? {
        return getChangePayload?.invoke(oldItem, newItem)
    }
}) {

    lateinit var context: Context

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
//        val item = getItem(position)
//        if (null != item) {
//            holder.bind(item)
//        } else {
//            onBindPlaceHolder?.run {
//                this.invoke(item, position, holder.binding)
//            }
//        }
//        onBindViewHolder?.run {
//            this.invoke(item, position, holder)
//        }
//        onBindDataBinding?.run {
//            this.invoke(item, position, holder.binding)
//        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<T>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        val item = getItem(position)
        if (null != item) {
            holder.bind(item)
        } else {
            onBindPlaceHolder?.run {
                this.invoke(item, position, holder.binding)
            }
        }
        onBindViewHolder?.run {
            this.invoke(item, position, holder, payloads)
        }
        onBindDataBinding?.run {
            this.invoke(item, position, holder.binding, payloads)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        context = parent.context
//        val inflater=LayoutInflater.from(context).cloneInContext(context)
//        inflater.factory2 = SkinInflaterFactory
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(context),
            layoutId,
            parent,
            false
        )
        return BaseViewHolder(binding, variableId)
    }

}