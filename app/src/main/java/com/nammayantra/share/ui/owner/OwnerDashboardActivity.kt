package com.nammayantra.share.ui.owner

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.nammayantra.share.data.repository.AuthRepository
import com.nammayantra.share.databinding.ActivityOwnerDashboardBinding
import com.nammayantra.share.ui.adapters.BookingAdapter
import com.nammayantra.share.ui.adapters.OwnerMachineAdapter
import com.nammayantra.share.util.Resource

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerDashboardBinding
    private val viewModel: OwnerDashboardViewModel by viewModels()
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var machineAdapter: OwnerMachineAdapter

    private val ownerId: String by lazy {
        AuthRepository.currentUserId() ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // --- My Equipment RecyclerView (horizontal) ---
        machineAdapter = OwnerMachineAdapter()
        binding.rvMyMachines.layoutManager = LinearLayoutManager(this, HORIZONTAL, false)
        binding.rvMyMachines.adapter = machineAdapter

        // --- Booking Requests RecyclerView ---
        bookingAdapter = BookingAdapter(
            showActions = true,
            onAccept = { booking -> viewModel.updateStatus(booking.id, "Accepted", ownerId) },
            onDecline = { booking -> viewModel.updateStatus(booking.id, "Declined", ownerId) }
        )
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = bookingAdapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadRequests(ownerId)
            viewModel.loadMyMachines(ownerId)
        }

        // Observe My Equipment
        viewModel.machines.observe(this) { res ->
            when (res) {
                is Resource.Loading -> { /* silent load */ }
                is Resource.Success -> {
                    val list = res.data
                    if (list.isEmpty()) {
                        binding.rvMyMachines.visibility = View.GONE
                        binding.tvNoMachines.visibility = View.VISIBLE
                    } else {
                        binding.tvNoMachines.visibility = View.GONE
                        binding.rvMyMachines.visibility = View.VISIBLE
                        machineAdapter.submitList(list)
                    }
                }
                is Resource.Error -> {
                    binding.tvNoMachines.visibility = View.VISIBLE
                }
            }
        }

        // Observe Booking Requests
        viewModel.bookings.observe(this) { res ->
            binding.swipeRefresh.isRefreshing = false
            when (res) {
                is Resource.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progress.visibility = View.GONE
                    val list = res.data

                    binding.tvPendingCount.text = viewModel.countByStatus(list, "Pending").toString()
                    binding.tvAcceptedCount.text = viewModel.countByStatus(list, "Accepted").toString()
                    binding.tvDeclinedCount.text = viewModel.countByStatus(list, "Declined").toString()

                    if (list.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvRequests.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvRequests.visibility = View.VISIBLE
                        bookingAdapter.submitList(list)
                    }
                }
                is Resource.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    Toast.makeText(this, res.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.updateResult.observe(this) { res ->
            when (res) {
                is Resource.Success -> Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show()
                is Resource.Error -> Toast.makeText(this, res.message, Toast.LENGTH_LONG).show()
                is Resource.Loading -> { /* handled by reload */ }
            }
        }

        // Initial load
        viewModel.loadRequests(ownerId)
        viewModel.loadMyMachines(ownerId)
    }

    override fun onResume() {
        super.onResume()
        // Refresh equipment list when returning from AddMachineActivity
        viewModel.loadMyMachines(ownerId)
        viewModel.loadRequests(ownerId)
    }
}
