package com.adam.ecolens.ui.learn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adam.ecolens.data.model.EncyclopediaItem
import com.adam.ecolens.databinding.ItemEncyclopediaBinding

class EncyclopediaAdapter(
    private var items: List<EncyclopediaItem> = emptyList()
) : RecyclerView.Adapter<EncyclopediaAdapter.ViewHolder>() {

    private val expandedStates = mutableSetOf<String>()

    fun submitList(newItems: List<EncyclopediaItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemEncyclopediaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEncyclopediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.binding.tvEmoji.text = item.iconEmoji
        holder.binding.tvCategoryName.text = item.category.displayName
        holder.binding.tvCategoryName.setTextColor(ContextCompat.getColor(context, item.category.colorResId))
        holder.binding.tvTitle.text = item.title
        holder.binding.tvShortDesc.text = item.shortDesc
        holder.binding.tvFullContent.text = item.fullContent

        val isExpanded = expandedStates.contains(item.id)
        holder.binding.tvFullContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.binding.tvToggleRead.text = if (isExpanded) "Tutup ▴" else "Baca Selengkapnya ▾"

        holder.binding.cardEncyclopedia.setOnClickListener {
            if (isExpanded) {
                expandedStates.remove(item.id)
            } else {
                expandedStates.add(item.id)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = items.size
}
