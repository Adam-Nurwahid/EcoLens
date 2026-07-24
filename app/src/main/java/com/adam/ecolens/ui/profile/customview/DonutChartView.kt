package com.adam.ecolens.ui.profile.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.adam.ecolens.R
import com.adam.ecolens.data.model.CategoryStat
import com.adam.ecolens.data.model.WasteCategory

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var stats: List<CategoryStat> = emptyList()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 36f
    }

    private val rectF = RectF()

    fun setCategoryStats(newStats: List<CategoryStat>) {
        stats = newStats
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val size = minOf(width, height)
        if (size <= 0) return

        val stroke = 36f
        paint.strokeWidth = stroke
        val margin = stroke / 2f + 10f
        rectF.set(margin, margin, size - margin, size - margin)

        val totalCount = stats.sumOf { it.count }

        if (totalCount == 0) {
            // Draw empty placeholder ring
            paint.color = ContextCompat.getColor(context, R.color.divider)
            canvas.drawArc(rectF, 0f, 360f, false, paint)
            return
        }

        var startAngle = -90f
        stats.forEach { stat ->
            val sweepAngle = (stat.count.toFloat() / totalCount.toFloat()) * 360f
            if (sweepAngle > 0) {
                paint.color = ContextCompat.getColor(context, stat.category.colorResId)
                canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
                startAngle += sweepAngle
            }
        }
    }
}
