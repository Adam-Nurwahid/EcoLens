package com.adam.ecolens.ui.learn.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.adam.ecolens.data.model.NewsItem
import com.adam.ecolens.databinding.ItemNewsBinding

class NewsAdapter(
    private var items: List<NewsItem> = emptyList()
) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    fun submitList(newItems: List<NewsItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvNewsTitle.text = item.title
        holder.binding.tvNewsExcerpt.text = item.excerpt
        holder.binding.tvNewsMeta.text = "${item.date} • ${item.readTime} • ${item.author}"

        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Membuka: ${item.title}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = items.size
}
