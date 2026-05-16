package com.nammayantra.share.ui.bookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nammayantra.share.data.repository.AuthRepository
import com.nammayantra.share.databinding.FragmentMyBookingsBinding
import com.nammayantra.share.ui.adapters.BookingAdapter
import com.nammayantra.share.util.Resource

class MyBookingsFragment : Fragment() {

    private var _binding: FragmentMyBookingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyBookingsViewModel by viewModels()
    private lateinit var adapter: BookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BookingAdapter(showActions = false)

        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            loadBookings()
        }

        viewModel.bookings.observe(viewLifecycleOwner) { res ->
            binding.swipeRefresh.isRefreshing = false
            when (res) {
                is Resource.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvBookings.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progress.visibility = View.GONE
                    if (res.data.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvBookings.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvBookings.visibility = View.VISIBLE
                        adapter.submitList(res.data)
                    }
                }
                is Resource.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadBookings()
    }

    private fun loadBookings() {
        // Use AuthRepository so DEMO_MODE is handled correctly
        val uid = AuthRepository.currentUserId() ?: return
        viewModel.loadBookings(uid)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
