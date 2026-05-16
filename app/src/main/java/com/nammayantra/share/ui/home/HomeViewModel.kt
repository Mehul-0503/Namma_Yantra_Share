package com.nammayantra.share.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammayantra.share.data.model.MachineUi
import com.nammayantra.share.data.repository.MachineRepository
import com.nammayantra.share.util.DistanceUtils
import com.nammayantra.share.util.Resource
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repo = MachineRepository

    private val _machines = MutableLiveData<Resource<List<MachineUi>>>()
    val machines: LiveData<Resource<List<MachineUi>>> = _machines

    private var allMachines: List<MachineUi> = emptyList()
    private var activeCategory = ""
    private var activeSearch = ""

    // Default location (Bangalore); updated when user grants location permission
    private var userLat = 12.9716
    private var userLng = 77.5946

    fun setUserLocation(lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
    }

    fun loadMachines() {
        _machines.value = Resource.Loading
        viewModelScope.launch {
            repo.getMachines().fold(
                onSuccess = { list ->
                    allMachines = list.map { machine ->
                        val dist = DistanceUtils.haversineKm(
                            userLat, userLng, machine.latitude, machine.longitude
                        )
                        MachineUi(machine, dist)
                    }.sortedBy { it.distanceKm }
                    applyFilters()
                },
                onFailure = {
                    _machines.postValue(Resource.Error(it.message ?: "Failed to load machines"))
                }
            )
        }
    }

    fun search(query: String) {
        activeSearch = query
        activeCategory = "" // Clear category when searching
        applyFilters()
    }

    /** Filter by category keyword (empty = show all) */
    fun filterByCategory(category: String) {
        activeCategory = category
        activeSearch = "" // Clear search when filtering by category
        applyFilters()
    }

    fun filterByDistance(maxKm: Double) {
        val filtered = allMachines.filter { it.distanceKm <= maxKm }
        _machines.value = Resource.Success(filtered)
    }

    private fun applyFilters() {
        var result = allMachines

        if (activeSearch.isNotBlank()) {
            val q = activeSearch.lowercase()
            result = result.filter {
                it.machine.name.lowercase().contains(q) ||
                        it.machine.ownerName.lowercase().contains(q) ||
                        it.machine.description.lowercase().contains(q)
            }
        } else if (activeCategory.isNotBlank()) {
            val cat = activeCategory.lowercase()
            result = result.filter { machineUi ->
                when (cat) {
                    "tractor" -> machineUi.machine.name.lowercase().let {
                        it.contains("tractor") || it.contains("arjun") ||
                                it.contains("sonalika") || it.contains("mahindra") ||
                                it.contains("john deere") || it.contains("kubota") &&
                                !machineUi.machine.name.lowercase().contains("harvester")
                    }
                    "harvester" -> machineUi.machine.name.lowercase().let {
                        it.contains("harvester") || it.contains("combine") || it.contains("thresher")
                    }
                    "tool" -> machineUi.machine.name.lowercase().let {
                        it.contains("sprayer") || it.contains("seeder") || it.contains("pump") ||
                                it.contains("rotavator") || it.contains("cultivator") ||
                                it.contains("plough") || it.contains("trailer")
                    }
                    else -> true
                }
            }
        }

        _machines.postValue(Resource.Success(result))
    }
}
