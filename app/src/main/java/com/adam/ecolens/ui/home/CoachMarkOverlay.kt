package com.adam.ecolens.ui.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.adam.ecolens.R

/**
 * A full-screen overlay view that implements coach-mark / spotlight behaviour.
 *
 * Usage
 * -----
 * 1. Add to your activity's DecorView with MATCH_PARENT dimensions.
 * 2. Call [showStep] with the screen-space [RectF] of the element to highlight
 *    and the [tooltipText] to display.
 * 3. Call [animateIn] to play the entrance animation.
 *
 * The overlay draws:
 * - A semi-transparent scrim over the whole screen.
 * - A circular spotlight centred on the target, punched through the scrim.
 * - A rounded-rect tooltip bubble (above or below the spotlight).
 * - Step counter text, a "Berikutnya" button, and a "Lewati" link.
 *
 * All styling uses the app's existing color tokens pulled from resources.
 */
class CoachMarkOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ---------------------------------------------------------------------------
    // Public state — set before calling invalidate()
    // ---------------------------------------------------------------------------

    /** Screen-space rect of the element being highlighted. */
    var targetRect: RectF = RectF()

    /** Tooltip body text (Bahasa Indonesia). */
    var tooltipText: String = ""

    /** Label for the primary action button (e.g. "Berikutnya →"). */
    var primaryButtonLabel: String = "Berikutnya →"

    /** Current step and total steps for the "X dari Y" counter. */
    var currentStep: Int = 1
    var totalSteps: Int = 5

    /** Callbacks fired when the user taps the primary or skip button. */
    var onPrimaryClick: (() -> Unit)? = null
    var onSkipClick: (() -> Unit)? = null

    // ---------------------------------------------------------------------------
    // Internal drawing state
    // ---------------------------------------------------------------------------

    /** Animated radius of the spotlight circle (0 → spotlightRadius). */
    private var animatedRadius: Float = 0f

    private val spotlightPadding = 28f.dp
    private val tooltipPadding = 20f.dp
    private val tooltipRadius = 20f.dp
    private val buttonHeight = 48f.dp
    private val buttonRadius = 50f.dp
    /** Gap between the bottom of the wrapped text block and the top of the primary button. */
    private val textToButtonGap = 16f.dp

    // Color tokens matching the app's design system
    private val colorScrim = Color.parseColor("#CC000000")        // 80% black scrim
    private val colorSpotlight = Color.TRANSPARENT                 // punched-through hole
    private val colorTooltipBg = Color.WHITE
    private val colorPrimary = ContextCompat.getColor(context, R.color.primary)
    private val colorPrimaryDark = ContextCompat.getColor(context, R.color.primary_dark)
    private val colorTextPrimary = ContextCompat.getColor(context, R.color.text_primary)
    private val colorTextSecondary = ContextCompat.getColor(context, R.color.text_secondary)
    private val colorButtonText = Color.WHITE
    private val colorSkipText = Color.parseColor("#AAFFFFFF")     // translucent white on scrim

    // Paints
    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorScrim
    }
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        color = Color.TRANSPARENT
    }
    private val spotlightRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 3f.dp
        alpha = 180
    }
    private val tooltipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorTooltipBg
        setShadowLayer(12f, 0f, 4f, Color.parseColor("#33000000"))
    }
    private val tooltipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorTextPrimary
        textSize = 14f.sp
        isAntiAlias = true
    }
    private val stepCounterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPrimary
        textSize = 12f.sp
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    private val buttonBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPrimary
    }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorButtonText
        textSize = 14f.sp
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    private val skipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorSkipText
        textSize = 13f.sp
        isAntiAlias = true
    }

    // Offline bitmap for the scrim+spotlight (so PorterDuff CLEAR works correctly)
    private var overlayBitmap: Bitmap? = null
    private var overlayCanvas: Canvas? = null

    // Click hit-testing rects (computed each draw pass)
    private var primaryButtonRect = RectF()
    private var skipRect = RectF()

    // ---------------------------------------------------------------------------
    // Init
    // ---------------------------------------------------------------------------

    init {
        // Required so PorterDuff CLEAR works against the view's own canvas
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = true
        isFocusable = true
    }

    // ---------------------------------------------------------------------------
    // Animation
    // ---------------------------------------------------------------------------

    fun animateIn(targetRadius: Float) {
        ValueAnimator.ofFloat(0f, targetRadius).apply {
            duration = 350
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                animatedRadius = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // ---------------------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------------------

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        overlayBitmap?.recycle()
        overlayBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        overlayCanvas = Canvas(overlayBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = overlayBitmap ?: return
        val offCanvas = overlayCanvas ?: return

        // 1 — Clear the offline bitmap
        offCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // 2 — Draw full scrim
        offCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)

        // 3 — Punch spotlight hole
        val cx = targetRect.centerX()
        val cy = targetRect.centerY()
        val radius = (targetRect.width().coerceAtLeast(targetRect.height()) / 2f) + spotlightPadding
        offCanvas.drawCircle(cx, cy, animatedRadius.coerceAtMost(radius), clearPaint)

        // 4 — Draw spotlight ring
        offCanvas.drawCircle(cx, cy, animatedRadius.coerceAtMost(radius), spotlightRingPaint)

        // 5 — Blit offline bitmap onto real canvas
        canvas.drawBitmap(bmp, 0f, 0f, null)

        // 6 — Draw tooltip bubble (above or below spotlight)
        drawTooltip(canvas, cx, cy, radius)

        // 7 — Draw step counter
        drawStepCounter(canvas)

        // 8 — Draw "Lewati" skip link at bottom
        drawSkipLink(canvas)
    }

    private fun drawTooltip(canvas: Canvas, cx: Float, cy: Float, spotRadius: Float) {
        val maxTooltipWidth = (width * 0.82f)
        val textLines = wrapText(tooltipText, tooltipTextPaint, maxTooltipWidth - tooltipPadding * 2)
        val lineHeight = tooltipTextPaint.fontSpacing
        val textBlockHeight = textLines.size * lineHeight

        val tooltipWidth = maxTooltipWidth
        // Step-counter occupies: tooltipPadding (top) + stepCounterPaint.textSize + 4f gap before body text.
        // Body text occupies: textBlockHeight.
        // Below body text: textToButtonGap + buttonHeight + tooltipPadding (bottom).
        val stepCounterOffset = tooltipPadding + stepCounterPaint.textSize + 4f
        val tooltipHeight = stepCounterOffset + textBlockHeight + textToButtonGap + buttonHeight + tooltipPadding

        val tooltipLeft = (width - tooltipWidth) / 2f
        val spotBottom = cy + spotRadius
        val spotTop = cy - spotRadius

        // Decide: place tooltip below if there's room, otherwise above
        val placeBelow = spotBottom + 24f + tooltipHeight < height
        val tooltipTop = if (placeBelow) spotBottom + 24f else spotTop - 24f - tooltipHeight

        val tooltipRect = RectF(tooltipLeft, tooltipTop, tooltipLeft + tooltipWidth, tooltipTop + tooltipHeight)

        // Background
        canvas.drawRoundRect(tooltipRect, tooltipRadius, tooltipRadius, tooltipBgPaint)

        // Step counter inside tooltip
        val counterText = "Langkah $currentStep dari $totalSteps"
        canvas.drawText(counterText, tooltipRect.left + tooltipPadding, tooltipRect.top + tooltipPadding + stepCounterPaint.textSize, stepCounterPaint)

        // Tooltip body text — starts after the step-counter offset
        var textY = tooltipRect.top + stepCounterOffset + lineHeight * 0.8f
        for (line in textLines) {
            canvas.drawText(line, tooltipRect.left + tooltipPadding, textY, tooltipTextPaint)
            textY += lineHeight
        }

        // Primary button — sits textToButtonGap below the text block, tooltipPadding above the bubble bottom
        val btnTop = tooltipRect.bottom - tooltipPadding - buttonHeight
        val btnLeft = tooltipRect.left + tooltipPadding
        val btnRight = tooltipRect.right - tooltipPadding
        primaryButtonRect.set(btnLeft, btnTop, btnRight, btnTop + buttonHeight)
        canvas.drawRoundRect(primaryButtonRect, buttonRadius, buttonRadius, buttonBgPaint)

        val btnText = primaryButtonLabel
        val btnTextWidth = buttonTextPaint.measureText(btnText)
        val btnTextX = primaryButtonRect.centerX() - btnTextWidth / 2f
        val btnTextY = primaryButtonRect.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2f
        canvas.drawText(btnText, btnTextX, btnTextY, buttonTextPaint)
    }

    private fun drawStepCounter(canvas: Canvas) {
        // Dot indicators at the top-center of the screen
        val dotRadius = 4f.dp
        val dotSpacing = 12f.dp
        val totalDotWidth = (totalSteps - 1) * dotSpacing + dotRadius * 2
        var dotX = (width - totalDotWidth) / 2f + dotRadius
        val dotY = 56f.dp

        val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 80
        }
        for (i in 1..totalSteps) {
            canvas.drawCircle(dotX, dotY, if (i == currentStep) dotRadius + 2f else dotRadius, if (i == currentStep) activePaint else inactivePaint)
            dotX += dotSpacing
        }
    }

    private fun drawSkipLink(canvas: Canvas) {
        val skipText = "Lewati semua"
        val skipTextWidth = skipTextPaint.measureText(skipText)
        val skipX = (width - skipTextWidth) / 2f
        val skipY = height - 48f.dp
        skipRect.set(skipX - 16f, skipY - 24f, skipX + skipTextWidth + 16f, skipY + 8f)
        canvas.drawText(skipText, skipX, skipY, skipTextPaint)
    }

    // ---------------------------------------------------------------------------
    // Touch handling
    // ---------------------------------------------------------------------------

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        if (event.action == android.view.MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y
            when {
                primaryButtonRect.contains(x, y) -> {
                    onPrimaryClick?.invoke()
                    return true
                }
                skipRect.contains(x, y) -> {
                    onSkipClick?.invoke()
                    return true
                }
            }
        }
        // Consume all touches so the underlying screen is blocked
        return true
    }

    // ---------------------------------------------------------------------------
    // Text wrapping helper
    // ---------------------------------------------------------------------------

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        for (word in words) {
            val probe = if (currentLine.isEmpty()) word else "${currentLine} $word"
            if (paint.measureText(probe) <= maxWidth) {
                currentLine = StringBuilder(probe)
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    // ---------------------------------------------------------------------------
    // Density helpers
    // ---------------------------------------------------------------------------

    private val Float.dp: Float get() = this * resources.displayMetrics.density
    private val Float.sp: Float get() = this * resources.displayMetrics.scaledDensity

    // ---------------------------------------------------------------------------
    // Cleanup
    // ---------------------------------------------------------------------------

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        overlayBitmap?.recycle()
        overlayBitmap = null
    }
}
