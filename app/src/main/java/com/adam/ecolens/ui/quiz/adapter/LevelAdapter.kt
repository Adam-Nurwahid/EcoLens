package com.adam.ecolens.ui.quiz.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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

        holder.binding.tvLevelTitle.text = level.title
        holder.binding.tvLevelSubtitle.text = level.subtitle

        if (level.isUnlocked) {
            holder.binding.tvLevelBadge.text = "${level.levelId}"
            holder.binding.tvLevelBadge.visibility = View.VISIBLE
            holder.binding.imgLockIcon.visibility = View.GONE
            holder.binding.cardLevel.alpha = 1.0f

            // Dynamic stars: one star per question, filled (gold) if answered correctly,
            // empty (gray) otherwise. Never attempted -> show nothing.
            val total = level.totalQuestionsAttempted
            val correct = level.correctCount
            holder.binding.tvStars.text = if (total > 0) {
                buildStarSpannable(holder.itemView.context, correct, total)
            } else {
                ""
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

    /**
     * Builds a SpannableString of [total] star characters where the first [correct]
     * stars are filled (★, colored gold via R.color.star_active) and the rest are
     * empty (☆, colored gray via R.color.star_inactive).
     */
    private fun buildStarSpannable(context: Context, correct: Int, total: Int): SpannableString {
        val starsText = "★".repeat(correct) + "☆".repeat(total - correct)
        val spannable = SpannableString(starsText)

        val activeColor = ContextCompat.getColor(context, R.color.star_active)
        val inactiveColor = ContextCompat.getColor(context, R.color.star_inactive)

        for (i in starsText.indices) {
            val color = if (i < correct) activeColor else inactiveColor
            spannable.setSpan(
                ForegroundColorSpan(color),
                i,
                i + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}