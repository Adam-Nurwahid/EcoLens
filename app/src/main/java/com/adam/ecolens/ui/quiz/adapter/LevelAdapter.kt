package com.adam.ecolens.ui.quiz.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adam.ecolens.R
import com.adam.ecolens.data.model.QuizLevel
import com.adam.ecolens.databinding.ItemQuizLevelBinding

class LevelAdapter(
    private var levels: List<QuizLevel> = emptyList(),
    private val onLevelClick: (QuizLevel) -> Unit
) : RecyclerView.Adapter<LevelAdapter.ViewHolder>() {

    fun submitList(newLevels: List<QuizLevel>) {
        levels = newLevels
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemQuizLevelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuizLevelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val level = levels[position]
        val context = holder.itemView.context

        holder.binding.tvLevelTitle.text = level.title
        holder.binding.tvLevelSubtitle.text = level.subtitle

        if (level.isUnlocked) {
            holder.binding.tvLevelBadge.text = "${level.levelId}"
            holder.binding.tvLevelBadge.visibility = View.VISIBLE
            holder.binding.imgLockIcon.visibility = View.GONE
            holder.binding.cardLevel.alpha = 1.0f

            val goldColor = ContextCompat.getColor(context, R.color.star_active)
            val inactiveColor = ContextCompat.getColor(context, R.color.star_inactive)
            holder.binding.tvStar1.setTextColor(if (level.starsAchieved >= 1) goldColor else inactiveColor)
            holder.binding.tvStar2.setTextColor(if (level.starsAchieved >= 2) goldColor else inactiveColor)
            holder.binding.tvStar3.setTextColor(if (level.starsAchieved >= 3) goldColor else inactiveColor)

            holder.binding.cardLevel.setOnClickListener {
                onLevelClick(level)
            }
        } else {
            holder.binding.tvLevelBadge.visibility = View.INVISIBLE
            holder.binding.imgLockIcon.visibility = View.VISIBLE
            holder.binding.cardLevel.alpha = 0.6f
            holder.binding.cardLevel.setOnClickListener {
                // Locked level
            }
        }
    }

    override fun getItemCount(): Int = levels.size
}
