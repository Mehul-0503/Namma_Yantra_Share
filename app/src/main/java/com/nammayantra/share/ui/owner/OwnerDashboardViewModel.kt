package com.nammayantra.share.ui.owner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammayantra.share.data.model.Booking
import com.nammayantra.share.data.model.Machine
import com.nammayantra.share.data.repository.BookingRepository
import com.nammayantra.share.data.repository.MachineRepository
import com.nammayantra.share.util.Resource
import kotlinx.coroutines.launch

class OwnerDashboardViewModel : ViewModel() {

    private val bookingRepo = BookingRepository
    private val machineRepo = MachineRepository

    private val _bookings = MutableLiveData<Resource<List<Booking>>>()
    val bookings: LiveData<Resource<List<Booking>>> = _bookings

    private val _machines = MutableLiveData<Resource<List<Machine>>>()
    val machines: LiveData<Resource<List<Machine>>> = _machines

    private val _updateResult = MutableLiveData<Resource<Unit>>()
    val updateResult: LiveData<Resource<Unit>> = _updateResult

    fun loadRequests(ownerId: String) {
        _bookings.value = Resource.Loading
        viewModelScope.launch {
            bookingRepo.getBookingsForOwner(ownerId).fold(
                onSuccess = { _bookings.postValue(Resource.Success(it)) },
                onFailure = { _bookings.postValue(Resource.Error(it.message ?: "Failed to load requests")) }
            )
        }
    }

    fun loadMyMachines(ownerId: String) {
        _machines.value = Resource.Loading
        viewModelScope.launch {
            machineRepo.getMachinesByOwner(ownerId).fold(
                onSuccess = { _machines.postValue(Resource.Success(it)) },
                onFailure = { _machines.postValue(Resource.Error(it.message ?: "Failed to load equipment")) }
            )
        }
    }

    fun updateStatus(bookingId: String, status: String, ownerId: String) {
        _updateResult.value = Resource.Loading
        viewModelScope.launch {
            bookingRepo.updateStatus(bookingId, status).fold(
                onSuccess = {
                    _updateResult.postValue(Resource.Success(Unit))
                    loadRequests(ownerId)
                },
                onFailure = {
                    _updateResult.postValue(Resource.Error(it.message ?: "Failed to update status"))
                }
            )
        }
    }

    /** Count bookings by status for dashboard stats */
    fun countByStatus(bookings: List<Booking>, status: String): Int {
        return bookings.count { it.status == status }
    }
}
