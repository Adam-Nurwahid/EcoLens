package com.adam.ecolens.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adam.ecolens.R
import com.adam.ecolens.databinding.FragmentQuizBinding
import com.adam.ecolens.ui.ViewModelFactory
import com.adam.ecolens.ui.quiz.adapter.LeaderboardAdapter
import com.adam.ecolens.ui.quiz.adapter.LevelAdapter

/**
 * Quiz list screen — shows unlocked levels and the leaderboard.
 *
 * Data is loaded from Firestore via [QuizViewModel]. While loading, a spinner is shown.
 * If the fetch fails (e.g. no internet), a kid-friendly error panel with a retry button
 * is displayed instead of the content.
 */
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var levelAdapter: LevelAdapter
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupRetry()
        observeViewModel()
    }

    private fun setupAdapters() {
        levelAdapter = LevelAdapter { selectedLevel ->
            val bundle = Bundle().apply {
                putInt("levelId", selectedLevel.levelId)
            }
            findNavController().navigate(R.id.action_quiz_to_play, bundle)
        }
        binding.rvLevels.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = levelAdapter
        }

        leaderboardAdapter = LeaderboardAdapter()
        binding.rvLeaderboard.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaderboardAdapter
        }
    }

    private fun setupRetry() {
        binding.btnRetry.setOnClickListener {
            viewModel.loadData()
        }
    }

    private fun observeViewModel() {
        // Loading state: show spinner, hide content and error panel
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarQuiz.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.scrollContent.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
            }
        }

        // Error state: hide spinner + content, show error panel
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                binding.layoutError.visibility = View.VISIBLE
                binding.tvErrorMessage.text = msg
                binding.scrollContent.visibility = View.GONE
                binding.progressBarQuiz.visibility = View.GONE
            } else {
                binding.layoutError.visibility = View.GONE
            }
        }

        // Data: show content once levels arrive (also clears spinner via isLoading observer)
        viewModel.levels.observe(viewLifecycleOwner) { levels ->
            levelAdapter.submitList(levels)
            if (levels.isNotEmpty()) {
                binding.scrollContent.visibility = View.VISIBLE
            }
        }

        viewModel.leaderboard.observe(viewLifecycleOwner) { leaderboard ->
            leaderboardAdapter.submitList(leaderboard)
        }
    }

    /**
     * Bug 3 fix: re-fetch level data every time this screen becomes visible again.
     * This covers the case where the user returns from QuizPlayFragment — the ViewModel
     * is NOT recreated on back-navigation, so init{} won't re-run. Calling loadData()
     * here guarantees the star display is always up to date.
     */
    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
