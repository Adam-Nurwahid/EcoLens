package com.adam.ecolens.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adam.ecolens.R
import com.adam.ecolens.databinding.FragmentQuizBinding
import com.adam.ecolens.ui.ViewModelFactory
import com.adam.ecolens.ui.quiz.adapter.LeaderboardAdapter
import com.adam.ecolens.ui.quiz.adapter.LevelAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

    private fun observeViewModel() {
        val levelsFlow = viewModel.getLevelsFlow()
        if (levelsFlow != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                levelsFlow.collectLatest { levels ->
                    levelAdapter.submitList(levels)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getLeaderboardFlow().collectLatest { leaderboard ->
                leaderboardAdapter.submitList(leaderboard)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
