package com.adam.ecolens.ui.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adam.ecolens.data.model.ScanRecord
import com.adam.ecolens.data.model.WasteCategory
import com.adam.ecolens.databinding.ItemScanHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter for the scan history list on the Profile screen.
 * Now backed by [ScanRecord] from Firestore instead of Room's ScanHistoryEntity.
 */
class ScanHistoryAdapter(
    private var items: List<ScanRecord> = emptyList()
) : RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))

    fun submitList(newItems: List<ScanRecord>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemScanHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScanHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val category = WasteCategory.fromLabel(item.category)

        holder.binding.tvHistoryCategoryName.text = category.displayName
        holder.binding.tvHistoryCategoryName.setTextColor(ContextCompat.getColor(context, category.colorResId))

        val emoji = when (category) {
            WasteCategory.ORGANIK -> "🌱"
            WasteCategory.ANORGANIK -> "🍾"
            WasteCategory.B3 -> "🔋"
        }
        holder.binding.tvHistoryCategoryBadge.text = emoji
        holder.binding.tvHistoryCategoryBadge.backgroundTintList =
            ContextCompat.getColorStateList(context, category.lightBgResId)

        holder.binding.tvHistoryConfidence.text = "%.1f%%".format(item.confidence)

        // Convert Firestore Timestamp to millis for date formatting
        val timestampMillis = item.timestamp?.toDate()?.time ?: 0L
        holder.binding.tvHistoryTimestamp.text = if (timestampMillis > 0) {
            dateFormat.format(Date(timestampMillis))
        } else {
            "-"
        }
    }

    override fun getItemCount(): Int = items.size
}
