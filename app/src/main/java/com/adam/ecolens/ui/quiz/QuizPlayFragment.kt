package com.adam.ecolens.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adam.ecolens.R
import com.adam.ecolens.databinding.FragmentQuizPlayBinding
import com.adam.ecolens.ui.ViewModelFactory

/**
 * Active quiz play screen.
 *
 * On creation, [QuizPlayViewModel.loadLevel] is called, which fetches the level from the
 * in-memory Firestore cache (or triggers a network fetch on the first visit). A spinner
 * is shown while loading. If the fetch fails, a kid-friendly error panel with a retry
 * button is shown. Once loaded, the quiz runs entirely in-memory with no additional
 * network calls until the quiz is submitted.
 */
class QuizPlayFragment : Fragment() {

    private var _binding: FragmentQuizPlayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizPlayViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    /** Stored as a field so the retry click listener can call loadLevel() without re-reading args. */
    private var levelId: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizPlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        levelId = arguments?.getInt("levelId", 1) ?: 1
        viewModel.loadLevel(levelId)

        setupOptionListeners()
        setupRetry()
        observeViewModel()
    }

    private fun setupOptionListeners() {
        val radioButtons = listOf(
            binding.rbOption0,
            binding.rbOption1,
            binding.rbOption2,
            binding.rbOption3
        )

        radioButtons.forEachIndexed { index, radioButton ->
            radioButton.setOnClickListener {
                viewModel.selectAnswer(index)
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (viewModel.isAnswerSubmitted.value == true) {
                viewModel.nextQuestion()
            } else {
                if (viewModel.selectedAnswerIndex.value == null) {
                    return@setOnClickListener
                }
                viewModel.submitAnswer()
            }
        }
    }

    private fun setupRetry() {
        binding.btnPlayRetry.setOnClickListener {
            viewModel.loadLevel(levelId)
        }
    }

    private fun observeViewModel() {
        // Loading state: show spinner, hide content and error panel
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarQuizPlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.scrollContent.visibility = View.GONE
                binding.layoutPlayError.visibility = View.GONE
            }
        }

        // Error state: hide spinner + content, show error panel with message
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                binding.layoutPlayError.visibility = View.VISIBLE
                binding.tvPlayErrorMessage.text = msg
                binding.scrollContent.visibility = View.GONE
                binding.progressBarQuizPlay.visibility = View.GONE
            } else {
                binding.layoutPlayError.visibility = View.GONE
            }
        }

        viewModel.questionProgressText.observe(viewLifecycleOwner) { text ->
            binding.tvProgress.text = text
            val currentProgress = text.filter { it.isDigit() }.firstOrNull()?.toString()?.toIntOrNull() ?: 1
            binding.pbQuestionProgress.progress = currentProgress
        }

        viewModel.currentQuestion.observe(viewLifecycleOwner) { q ->
            // Show content scroll view once we have a question
            binding.scrollContent.visibility = View.VISIBLE

            binding.tvQuestionText.text = q.questionText
            binding.rgOptions.clearCheck()
            binding.cardExplanation.visibility = View.GONE
            binding.btnSubmit.text = "Periksa Jawaban"

            val radioButtons = listOf(
                binding.rbOption0,
                binding.rbOption1,
                binding.rbOption2,
                binding.rbOption3
            )

            radioButtons.forEachIndexed { i, rb ->
                if (i < q.options.size) {
                    rb.visibility = View.VISIBLE
                    rb.text = q.options[i]
                    rb.isEnabled = true
                    rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                } else {
                    rb.visibility = View.GONE
                }
            }
        }

        viewModel.selectedAnswerIndex.observe(viewLifecycleOwner) { selectedIndex ->
            binding.btnSubmit.isEnabled = selectedIndex != null || viewModel.isAnswerSubmitted.value == true
        }

        viewModel.isAnswerSubmitted.observe(viewLifecycleOwner) { isSubmitted ->
            if (isSubmitted) {
                val q = viewModel.currentQuestion.value ?: return@observe
                val selected = viewModel.selectedAnswerIndex.value ?: return@observe
                val isCorrect = selected == q.correctAnswerIndex

                binding.cardExplanation.visibility = View.VISIBLE
                if (isCorrect) {
                    binding.tvAnswerStatus.text = "Jawabanmu Benar! 🎉 (+20 XP)"
                    binding.tvAnswerStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                } else {
                    binding.tvAnswerStatus.text = "Jawaban Kurang Tepat 😅"
                    binding.tvAnswerStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.category_b3))
                }
                binding.tvExplanationText.text = q.explanation

                val radioButtons = listOf(binding.rbOption0, binding.rbOption1, binding.rbOption2, binding.rbOption3)
                radioButtons.forEachIndexed { idx, rb ->
                    rb.isEnabled = false
                    if (idx == q.correctAnswerIndex) {
                        rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                    } else if (idx == selected) {
                        rb.setTextColor(ContextCompat.getColor(requireContext(), R.color.category_b3))
                    }
                }

                binding.btnSubmit.text = "Lanjut ke Soal Berikutnya →"
                binding.btnSubmit.isEnabled = true
            }
        }

        viewModel.quizCompletedState.observe(viewLifecycleOwner) { state ->
            state?.let {
                showResultDialog(it)
            }
        }
    }

    private fun showResultDialog(state: QuizCompletedState) {
        val starsText = "★".repeat(state.stars) + "☆".repeat(3 - state.stars)
        val message = "Skor Akhir: ${state.score} / 100\nBintang: $starsText\n" +
                if (state.isLevelUnlocked) "Selamat! Kamu berhasil menyelesaikan level ini! 🎉"
                else "Tetap semangat! Coba lagi untuk membuka level berikutnya!"

        AlertDialog.Builder(requireContext())
            .setTitle("Kuis Selesai!")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ke Daftar Level") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
