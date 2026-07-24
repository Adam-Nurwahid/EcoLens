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
import com.adam.ecolens.ui.learn.adapter.NewsAdapter

class LearnFragment : Fragment() {

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LearnViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var encyclopediaAdapter: EncyclopediaAdapter
    private lateinit var newsAdapter: NewsAdapter

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

        newsAdapter = NewsAdapter()
        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.encyclopediaItems.observe(viewLifecycleOwner) { items ->
            encyclopediaAdapter.submitList(items)
        }

        viewModel.newsItems.observe(viewLifecycleOwner) { news ->
            newsAdapter.submitList(news)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
