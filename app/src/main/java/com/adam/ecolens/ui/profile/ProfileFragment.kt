package com.adam.ecolens.ui.profile

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
import com.adam.ecolens.data.model.WasteCategory
import com.adam.ecolens.databinding.FragmentProfileBinding
import com.adam.ecolens.ui.ViewModelFactory
import com.adam.ecolens.ui.profile.adapter.ScanHistoryAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var scanHistoryAdapter: ScanHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupLogout()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        scanHistoryAdapter = ScanHistoryAdapter()
        binding.rvScanHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scanHistoryAdapter
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profile_to_login)
        }
    }

    private fun observeViewModel() {
        val userFlow = viewModel.getActiveUserFlow()
        if (userFlow != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                userFlow.collectLatest { user ->
                    user?.let {
                        binding.tvProfileName.text = it.fullName
                        binding.tvProfileUsername.text = "@${it.username}"
                        binding.tvProfilePoints.text = "${it.totalPoints} XP"
                        binding.tvProfileLevel.text = "Level ${it.unlockedLevel}"
                    }
                }
            }
        }

        val historyFlow = viewModel.getScanHistoryFlow()
        if (historyFlow != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                historyFlow.collectLatest { history ->
                    if (history.isEmpty()) {
                        binding.tvEmptyHistory.visibility = View.VISIBLE
                        binding.rvScanHistory.visibility = View.GONE
                    } else {
                        binding.tvEmptyHistory.visibility = View.GONE
                        binding.rvScanHistory.visibility = View.VISIBLE
                        scanHistoryAdapter.submitList(history)
                    }
                }
            }
        }

        val statsFlow = viewModel.getCategoryStatsFlow()
        if (statsFlow != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                statsFlow.collectLatest { stats ->
                    binding.chartDonut.setCategoryStats(stats)

                    val map = stats.associateBy { it.category }
                    val org = map[WasteCategory.ORGANIK]
                    val anorg = map[WasteCategory.ANORGANIK]
                    val b3 = map[WasteCategory.B3]

                    binding.tvOrganikStat.text = "Organik: ${org?.count ?: 0} (%.0f%%)".format(org?.percentage ?: 0f)
                    binding.tvAnorganikStat.text = "Anorganik: ${anorg?.count ?: 0} (%.0f%%)".format(anorg?.percentage ?: 0f)
                    binding.tvB3Stat.text = "B3: ${b3?.count ?: 0} (%.0f%%)".format(b3?.percentage ?: 0f)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
