package com.marlin.webpagefinder.ui

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.marlin.webpagefinder.R
import com.marlin.webpagefinder.model.Page
import kotlinx.android.synthetic.main.list_row.view.*


class UrlAdapter(private var dataset: MutableList<Page>) :
        RecyclerView.Adapter<UrlAdapter.ViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlAdapter.ViewHolder {
        // create a new view
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_row, parent, false) as View

        return ViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.url.text = dataset[position].url
        holder.status.text = dataset[position].status.name
        holder.matches.text = dataset[position].matches.toString()
        holder.statusDescription.text = dataset[position].statusDescription
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataset.size

    fun update(newData: List<Page>) {
        this.dataset.clear()
        this.dataset.addAll(newData)
        notifyDataSetChanged()
    }

    fun reset() {
        this.dataset.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val url: TextView = view.title
        val status: TextView = view.status
        val matches: TextView = view.matches
        val statusDescription: TextView = view.statusDescription
    }
}