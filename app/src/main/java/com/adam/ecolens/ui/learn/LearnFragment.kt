package com.adam.ecolens.ui.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.adam.ecolens.databinding.FragmentLearnBinding
import com.adam.ecolens.ui.ViewModelFactory
import com.adam.ecolens.ui.learn.adapter.EncyclopediaAdapter

class LearnFragment : Fragment() {

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LearnViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var encyclopediaAdapter: EncyclopediaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        encyclopediaAdapter = EncyclopediaAdapter()
        binding.rvEncyclopedia.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = encyclopediaAdapter
        }

    }

    private fun observeViewModel() {
        viewModel.encyclopediaItems.observe(viewLifecycleOwner) { items ->
            encyclopediaAdapter.submitList(items)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
