package com.adam.ecolens.ui.quiz.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adam.ecolens.data.model.LeaderboardItem
import com.adam.ecolens.databinding.ItemLeaderboardBinding

class LeaderboardAdapter(
    private var items: List<LeaderboardItem> = emptyList()
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    fun submitList(newItems: List<LeaderboardItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvRankNumber.text = "${item.rank}"
        holder.binding.tvLeaderboardName.text = item.fullName
        holder.binding.tvLeaderboardLevel.text = "Mencapai Level ${item.currentLevel}"
        holder.binding.tvLeaderboardPoints.text = "${item.totalPoints} XP"
    }

    override fun getItemCount(): Int = items.size
}
