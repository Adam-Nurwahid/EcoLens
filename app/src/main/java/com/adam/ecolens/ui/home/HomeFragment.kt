package com.adam.ecolens.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adam.ecolens.R
import com.adam.ecolens.data.local.SessionManager
import com.adam.ecolens.databinding.FragmentHomeBinding
import com.adam.ecolens.ui.ViewModelFactory

/**
 * Home screen — displays user name, total points, and daily tip.
 * Data is loaded from Firestore via [HomeViewModel].
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    /** One-time Dashboard coach-marks tutorial — lazily created, safe to access post-layout. */
    private val tutorialManager by lazy {
        DashboardTutorialManager(
            fragment = this,
            sessionManager = SessionManager(requireContext())
        )
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
            findNavController().navigate(R.id.action_home_to_learn)
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

        // Observe Firestore user profile (LiveData — replaces Room Flow)
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.tvStudentName.text = it.name
                binding.tvUserPoints.text = "${it.totalPoints} XP"
            }
        }

        // Start the one-time Dashboard walkthrough after the first complete layout pass
        // (so every target view has real on-screen coordinates from getLocationOnScreen).
        val vto = binding.root.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                tutorialManager.showIfNeeded()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
