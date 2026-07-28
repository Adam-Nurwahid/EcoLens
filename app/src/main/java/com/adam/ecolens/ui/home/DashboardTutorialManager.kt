package com.adam.ecolens.ui.home

import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adam.ecolens.R
import com.adam.ecolens.data.local.SessionManager

/**
 * Manages the one-time Dashboard coach-marks tutorial.
 *
 * Call [showIfNeeded] from [HomeFragment.onViewCreated] (after a layout pass)
 * to start the walkthrough. The tutorial is never shown again once dismissed
 * or completed — the flag is persisted via [SessionManager].
 *
 * Step order:
 *  1. XP score badge   — explains the points system
 *  2. Belajar card     — learning materials
 *  3. Pindai AI card   — waste scanning
 *  4. Kuis Seru card   — quiz
 *  5. Hero banner      — main CTA shortcut
 */
class DashboardTutorialManager(
    private val fragment: Fragment,
    private val sessionManager: SessionManager
) {

    // ---------------------------------------------------------------------------
    // Step definitions
    // ---------------------------------------------------------------------------

    private data class TutorialStep(
        val targetViewId: Int,
        val tooltipText: String,
        val isLast: Boolean = false
    )

    private val steps = listOf(
        TutorialStep(
            R.id.cardUserPoints,
            "Ini poin XP-mu! Semakin banyak belajar dan ikut kuis, semakin tinggi skormu. Semangat! 🌟"
        ),
        TutorialStep(
            R.id.cardShortcutLearn,
            "Mulai Belajar — Temukan materi seru tentang jenis-jenis sampah dan cara mengelolanya dengan benar. 📚"
        ),
        TutorialStep(
            R.id.cardShortcutScan,
            "Pindai AI — Arahkan kameramu ke sampah dan biarkan AI mendeteksi jenisnya secara instan! 🔍"
        ),
        TutorialStep(
            R.id.cardShortcutQuiz,
            "Kuis Seru — Uji pengetahuanmu tentang sampah dan lingkungan, lalu kumpulkan XP sebanyak-banyaknya! 🎮"
        ),
        TutorialStep(
            R.id.cardHeroBanner,
            "Banner utama — Ketuk kapan saja untuk langsung memulai perjalanan belajarmu bersama EcoLens. 🌿",
            isLast = true
        )
    )

    private var currentStepIndex = 0
    private var overlay: CoachMarkOverlay? = null

    // ---------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------

    /**
     * Attaches the overlay to the activity's DecorView and starts the tutorial,
     * but ONLY if the tutorial has not been completed yet.
     *
     * Must be called after the Fragment's views are fully laid out so that
     * [View.getGlobalVisibleRect] returns accurate coordinates.
     */
    fun showIfNeeded() {
        if (sessionManager.hasTutorialCompleted()) return
        currentStepIndex = 0
        showCurrentStep()
    }

    // ---------------------------------------------------------------------------
    // Step navigation
    // ---------------------------------------------------------------------------

    private fun showCurrentStep() {
        val step = steps.getOrNull(currentStepIndex) ?: run {
            dismiss(markComplete = true)
            return
        }

        val activity = fragment.activity ?: return
        val rootView = activity.window.decorView as ViewGroup

        // Find the target view
        val targetView = fragment.view?.findViewById<View>(step.targetViewId) ?: run {
            // If the view isn't found, skip to next
            currentStepIndex++
            showCurrentStep()
            return
        }

        // Compute screen-space rect of the target
        val targetRect = getScreenRect(targetView)

        // Remove any existing overlay
        overlay?.let { rootView.removeView(it) }

        // Create and configure the overlay
        val newOverlay = CoachMarkOverlay(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.targetRect = targetRect
            this.tooltipText = step.tooltipText
            this.currentStep = currentStepIndex + 1
            this.totalSteps = steps.size
            this.primaryButtonLabel = if (step.isLast) "Mulai Sekarang! 🌿" else "Berikutnya →"

            onPrimaryClick = {
                if (step.isLast) {
                    dismiss(markComplete = true)
                } else {
                    currentStepIndex++
                    showCurrentStep()
                }
            }

            onSkipClick = { dismiss(markComplete = true) }
        }

        overlay = newOverlay
        rootView.addView(newOverlay)

        // Spotlight entrance animation
        val spotRadius = (targetRect.width().coerceAtLeast(targetRect.height()) / 2f) + 28f * activity.resources.displayMetrics.density
        newOverlay.animateIn(spotRadius)
    }

    // ---------------------------------------------------------------------------
    // Dismiss
    // ---------------------------------------------------------------------------

    private fun dismiss(markComplete: Boolean) {
        val activity = fragment.activity ?: return
        val rootView = activity.window.decorView as ViewGroup
        overlay?.let { rootView.removeView(it) }
        overlay = null
        if (markComplete) {
            sessionManager.setTutorialCompleted()
        }
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    /**
     * Returns the screen-space [RectF] of [view], accounting for scroll offsets,
     * decorations, and any translation the view may have.
     */
    private fun getScreenRect(view: View): RectF {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return RectF(
            location[0].toFloat(),
            location[1].toFloat(),
            (location[0] + view.width).toFloat(),
            (location[1] + view.height).toFloat()
        )
    }
}
