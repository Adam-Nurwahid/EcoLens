package com.adam.ecolens.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adam.ecolens.R
import com.adam.ecolens.data.local.SessionManager
import com.adam.ecolens.databinding.FragmentOnboardingBinding
import com.adam.ecolens.ui.ViewModelFactory
import com.google.android.material.button.MaterialButton

/**
 * First-time onboarding survey.
 *
 * Shown exactly once — after a successful login — to gauge the user's baseline
 * knowledge about waste and recycling. Answers and the completion flag are
 * persisted via [SessionManager] (SharedPreferences) before navigating to Home.
 *
 * No ViewModel is needed: all state is local to this fragment because the data
 * only needs to survive a single session transition, not configuration changes
 * that would require the user to redo the survey.
 */
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    // ---------------------------------------------------------------------------
    // Survey definition — 5 questions with 3 options each (Bahasa Indonesia)
    // ---------------------------------------------------------------------------
    private data class Question(val text: String, val options: List<String>)

    private val questions = listOf(
        Question(
            "Apakah kamu tahu perbedaan sampah organik dan anorganik?",
            listOf("✅  Ya, tahu", "🤔  Kurang yakin", "❌  Belum tahu")
        ),
        Question(
            "Apakah kamu sudah terbiasa memilah sampah di rumah?",
            listOf("✅  Selalu", "🔄  Kadang-kadang", "❌  Belum pernah")
        ),
        Question(
            "Seberapa sering kamu mendaur ulang sampah?",
            listOf("✅  Sering", "🔄  Jarang", "❌  Tidak pernah")
        ),
        Question(
            "Apakah kamu tahu cara menangani sampah B3 (berbahaya)?",
            listOf("✅  Ya, tahu", "🤔  Pernah dengar", "❌  Belum tahu")
        ),
        Question(
            "Apa tujuanmu menggunakan EcoLens?",
            listOf("📚  Belajar tentang lingkungan", "🔍  Mengidentifikasi sampah", "🎮  Bermain kuis")
        )
    )

    private var currentStep = 0
    private val selectedAnswers = MutableList(questions.size) { "" }
    private val viewModel: OnboardingViewModel by viewModels { ViewModelFactory(requireContext()) }

    // Poin per opsi (index sesuai urutan `options`). Q1–Q4 menilai pemahaman
// (opsi paling paham = poin terbesar); Q5 cuma preferensi → poin rata.
    private val pointsPerQuestion = listOf(
        listOf(10, 5, 2),
        listOf(10, 5, 2),
        listOf(10, 5, 2),
        listOf(10, 5, 2),
        listOf(5, 5, 5)
    )
    // ---------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Restore state after configuration change
        savedInstanceState?.let {
            currentStep = it.getInt(STATE_STEP, 0)
            it.getStringArray(STATE_ANSWERS)?.forEachIndexed { i, ans ->
                selectedAnswers[i] = ans
            }
        }

        renderStep()

        binding.btnNext.setOnClickListener { onNextClicked() }
        binding.tvSkip.setOnClickListener { finishOnboarding() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_STEP, currentStep)
        outState.putStringArray(STATE_ANSWERS, selectedAnswers.toTypedArray())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------------------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------------------

    private fun renderStep() {
        val question = questions[currentStep]
        val total = questions.size
        val stepNumber = currentStep + 1

        // Progress bar
        binding.progressIndicator.max = total
        binding.progressIndicator.progress = stepNumber
        binding.tvStepCounter.text = "Pertanyaan $stepNumber dari $total"

        // Question text
        binding.tvQuestion.text = question.text

        // Rebuild answer buttons dynamically
        binding.optionsContainer.removeAllViews()
        question.options.forEachIndexed { index, optionText ->
            val btn = buildOptionButton(optionText, index == getSelectedOptionIndex())
            binding.optionsContainer.addView(btn)
        }

        // Next / Finish button label
        val isLastStep = currentStep == total - 1
        binding.btnNext.text = if (isLastStep) "Mulai Belajar 🌿" else "Lanjut  →"
        binding.btnNext.isEnabled = selectedAnswers[currentStep].isNotEmpty()

        // On last step, hide skip link (the "Mulai Belajar" button already advances)
        binding.tvSkip.visibility = if (isLastStep) View.INVISIBLE else View.VISIBLE
    }

    private fun getSelectedOptionIndex(): Int {
        val current = selectedAnswers[currentStep]
        if (current.isEmpty()) return -1
        return questions[currentStep].options.indexOf(current)
    }

    private fun buildOptionButton(label: String, isSelected: Boolean): MaterialButton {
        val context = requireContext()
        val btn = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            strokeWidth = 2
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = resources.getDimensionPixelSize(R.dimen.spacing_small) }
            layoutParams = params
            setPadding(
                resources.getDimensionPixelSize(R.dimen.spacing_medium),
                resources.getDimensionPixelSize(R.dimen.spacing_small),
                resources.getDimensionPixelSize(R.dimen.spacing_medium),
                resources.getDimensionPixelSize(R.dimen.spacing_small)
            )
            cornerRadius = resources.getDimensionPixelSize(R.dimen.card_corner_radius_medium)
            if (isSelected) {
                setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
                setTextColor(ContextCompat.getColor(context, R.color.white))
                strokeColor = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.primary)
                )
            } else {
                setBackgroundColor(ContextCompat.getColor(context, R.color.card_surface))
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                strokeColor = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.divider)
                )
            }
        }
        btn.setOnClickListener {
            selectedAnswers[currentStep] = label
            renderStep()
        }
        return btn
    }

    // ---------------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------------

    private fun onNextClicked() {
        if (currentStep < questions.size - 1) {
            currentStep++
            renderStep()
        } else {
            finishOnboarding()
        }
    }
    private fun calculateBonusPoints(): Int =
        selectedAnswers.mapIndexed { qIndex, answer ->
            val optionIndex = questions[qIndex].options.indexOf(answer)
            if (optionIndex >= 0) pointsPerQuestion[qIndex][optionIndex] else 0
        }.sum()

    private fun finishOnboarding() {
        val answers = selectedAnswers.joinToString("|")
        val bonusPoints = calculateBonusPoints()

        sessionManager.setOnboardingCompleted(answers)
        viewModel.submitOnboarding(selectedAnswers.toList(), bonusPoints)

        Toast.makeText(requireContext(), "+$bonusPoints poin bonus dari survey! 🎉", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_onboarding_to_home)
    }

    companion object {
        private const val STATE_STEP = "onboarding_step"
        private const val STATE_ANSWERS = "onboarding_answers"
    }
}
