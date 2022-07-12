package com.yollpoll.nmb.view.widgets.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.yollpoll.nmb.R

/**
 * Created by 鹏祺 on 2017/6/15.
 */
class PicEmojiAdapter(
    private val list: List<Int>,
    private val onItemClick: ((View, Int) -> Unit)?
) :
    RecyclerView.Adapter<PicEmojiAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_pic_emoji, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.imgPicEmoji.setImageResource(item)
        holder.imgPicEmoji.setOnClickListener { v ->
            onItemClick?.invoke(
                v,
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgPicEmoji: ImageView

        init {
            imgPicEmoji = itemView.findViewById<View>(R.id.img_pic_emoji) as ImageView
        }
    }
}