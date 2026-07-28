package com.adam.ecolens.ui.quiz.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

        holder.binding.tvLevelTitle.text = level.title
        holder.binding.tvLevelSubtitle.text = level.subtitle

        if (level.isUnlocked) {
            holder.binding.tvLevelBadge.text = "${level.levelId}"
            holder.binding.tvLevelBadge.visibility = View.VISIBLE
            holder.binding.imgLockIcon.visibility = View.GONE
            holder.binding.cardLevel.alpha = 1.0f

            // Dynamic stars: one star per question, filled if answered correctly
            val total = level.totalQuestionsAttempted
            val correct = level.correctCount
            holder.binding.tvStars.text = if (total > 0) {
                "★".repeat(correct) + "☆".repeat(total - correct)
            } else {
                "" // Never attempted — show nothing
            }

            holder.binding.cardLevel.setOnClickListener {
                onLevelClick(level)
            }
        } else {
            holder.binding.tvLevelBadge.visibility = View.INVISIBLE
            holder.binding.imgLockIcon.visibility = View.VISIBLE
            holder.binding.cardLevel.alpha = 0.6f
            holder.binding.tvStars.text = ""
            holder.binding.cardLevel.setOnClickListener {
                // Locked level — no action
            }
        }
    }

    override fun getItemCount(): Int = levels.size
}
