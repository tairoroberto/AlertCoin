package com.trmamobilesolutions.alertcoin.home.view

import android.content.Context
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.trmamobilesolutions.alertcoin.R
import com.trmamobilesolutions.alertcoin.base.extension.loadImage
import com.trmamobilesolutions.alertcoin.home.model.domain.ExchangesItem


/**
 * Created by tairo on 12/12/17.
 */
class HomeRecyclerAdapter(private val context: Context?,
                          private var list: List<ExchangesItem>?,
                          private val onClick: (exchangesItem: ExchangesItem, imageView: ImageView) -> Unit) : RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder>() {

    private var lastPosition = -1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list?.get(position)
        if (item != null) {
            holder.bind(item)
            holder.itemView.setOnClickListener({
                onClick(item, holder.imageView)
            })
        }
        //setAnimation(holder.itemView, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item, parent, false))
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > 0) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    override fun getItemCount(): Int = list?.size as Int

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        private val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        private val textViewOverview: TextView = view.findViewById(R.id.textViewOverview)
        private val progressImage: ProgressBar = view.findViewById(R.id.progressImage)
        private val tag1: Button = view.findViewById(R.id.tag1)
        private val tag2: Button = view.findViewById(R.id.tag2)
        private val tag3: Button = view.findViewById(R.id.tag3)

        fun bind(exchange: ExchangesItem) {
            imageView.loadImage(exchange.legend, progressImage, false)

            textViewTitle.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(exchange.name, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(exchange.name)
            }

            textViewOverview.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(exchange.legend, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(exchange.legend)
            }

            tag1.text = exchange.high.toString()
            tag2.text = exchange.low.toString()
            tag3.text = exchange.trades.toString()
        }
    }

    fun update(items: List<ExchangesItem>?) {
        this.list = items
        notifyDataSetChanged()
    }
}