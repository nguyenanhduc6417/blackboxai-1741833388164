package com.example.myapp.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupSearch()
        setupFab()
        loadContacts()
    }

    private fun setupViews() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            // Add adapter implementation later
        }

        // Initially show loading
        showLoading(true)
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterContacts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterContacts(newText)
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabAddContact.setOnClickListener {
            // Implement add contact action
        }
    }

    private fun filterContacts(query: String?) {
        // Implement contact filtering
        if (query.isNullOrEmpty()) {
            loadContacts()
        } else {
            // Filter contacts based on query
            // For now, just show loading and empty state
            showLoading(true)
            binding.root.postDelayed({
                showLoading(false)
                showEmptyState(true)
            }, 500)
        }
    }

    private fun loadContacts() {
        // Simulate loading contacts
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
