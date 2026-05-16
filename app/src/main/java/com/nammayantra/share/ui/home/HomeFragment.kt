package com.nammayantra.share.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.nammayantra.share.R
import com.nammayantra.share.databinding.FragmentHomeBinding
import com.nammayantra.share.ui.adapters.MachineAdapter
import com.nammayantra.share.ui.detail.EquipmentDetailActivity
import com.nammayantra.share.util.Resource

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: MachineAdapter

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchLocationThenLoad()
        } else {
            // No location permission – load without distance sorting
            viewModel.loadMachines()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MachineAdapter { machineUi ->
            startActivity(
                Intent(requireContext(), EquipmentDetailActivity::class.java)
                    .putExtra("machineId", machineUi.machine.id)
            )
        }

        binding.rvMachines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMachines.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadMachines()
        }

        // Search text watcher
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Clear category selection when searching
                viewModel.search(s.toString())
            }
        })

        // Category chip filter - FIX: wire up chip listeners
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            // Only handle if search bar is empty
            if (binding.etSearch.text.isNullOrBlank()) {
                when {
                    checkedIds.contains(R.id.chipAll) -> viewModel.filterByCategory("")
                    checkedIds.contains(R.id.chipTractors) -> viewModel.filterByCategory("tractor")
                    checkedIds.contains(R.id.chipHarvesters) -> viewModel.filterByCategory("harvester")
                    checkedIds.contains(R.id.chipTools) -> viewModel.filterByCategory("tool")
                }
            }
        }

        viewModel.machines.observe(viewLifecycleOwner) { res ->
            binding.swipeRefresh.isRefreshing = false
            when (res) {
                is Resource.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvMachines.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progress.visibility = View.GONE
                    if (res.data.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvMachines.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvMachines.visibility = View.VISIBLE
                        adapter.submitList(res.data)
                    }
                }
                is Resource.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = res.message
                }
            }
        }

        requestLocationAndLoad()
    }

    private fun requestLocationAndLoad() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocationThenLoad()
        } else {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @Suppress("MissingPermission")
    private fun fetchLocationThenLoad() {
        try {
            val client = LocationServices.getFusedLocationProviderClient(requireActivity())
            client.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    viewModel.setUserLocation(loc.latitude, loc.longitude)
                }
                // Load after location (or if location unavailable) - single call
                viewModel.loadMachines()
            }.addOnFailureListener {
                viewModel.loadMachines()
            }
        } catch (_: Exception) {
            viewModel.loadMachines()
        }
    }

    /** Called by MainActivity.onResume so the list stays fresh after AddMachineActivity finishes. */
    fun refreshIfVisible() {
        if (!isHidden) {
            viewModel.loadMachines()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
