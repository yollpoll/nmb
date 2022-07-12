package com.yollpoll.nmb.view.widgets.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yollpoll.nmb.R

/**
 * Created by 鹏祺 on 2017/6/15.
 */
class WordEmojiAdapter(
    private val list: List<String>,
    private val onItemClick: ((View, Int) -> Unit)?
) :
    RecyclerView.Adapter<WordEmojiAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_word_emoji, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvWordEomji.text = item
        holder.tvWordEomji.setOnClickListener { v ->
            onItemClick?.invoke(
                v,
                holder.adapterPosition
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvWordEomji: TextView

        init {
            tvWordEomji = itemView.findViewById<View>(R.id.tv_word_emoji) as TextView
        }
    }
}