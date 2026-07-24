package com.adam.ecolens.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.adam.ecolens.R
import com.adam.ecolens.databinding.FragmentHomeBinding
import com.adam.ecolens.ui.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDailyTip.text = viewModel.getDailyTip()

        // Navigation shortcuts
        binding.btnBannerScan.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_scan)
        }
        binding.cardShortcutScan.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_scan)
        }
        binding.cardShortcutLearn.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_learn)
        }
        binding.cardShortcutQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_quiz)
        }

        // Observe active user data
        val userFlow = viewModel.getActiveUserFlow()
        if (userFlow != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                userFlow.collectLatest { user ->
                    user?.let {
                        binding.tvStudentName.text = it.fullName
                        binding.tvUserPoints.text = "${it.totalPoints} XP"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
