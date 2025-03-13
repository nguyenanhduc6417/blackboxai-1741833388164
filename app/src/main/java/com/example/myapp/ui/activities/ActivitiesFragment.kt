package com.example.myapp.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.FragmentActivitiesBinding
import com.google.android.material.chip.Chip

class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupSwipeRefresh()
        setupChipGroup()
        loadActivities()
    }

    private fun setupViews() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            // Add adapter implementation later
        }

        // Initially show loading
        showLoading(true)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadActivities()
        }
    }

    private fun setupChipGroup() {
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            // Get selected chip and filter activities
            val selectedChip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            when (selectedChip?.id) {
                binding.chipAll.id -> loadActivities("all")
                binding.chipToday.id -> loadActivities("today")
                binding.chipWeek.id -> loadActivities("week")
            }
        }
    }

    private fun loadActivities(filter: String = "all") {
        // Simulate loading activities with filter
        showLoading(true)
        binding.root.postDelayed({
            showLoading(false)
            // For now, show empty state
            showEmptyState(true)
        }, 1500)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyState.visibility = View.GONE
        }
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    fun scrollToTop() {
        binding.recyclerView.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
