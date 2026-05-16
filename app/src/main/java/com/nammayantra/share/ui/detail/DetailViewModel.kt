package com.nammayantra.share.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammayantra.share.data.model.Booking
import com.nammayantra.share.data.model.Machine
import com.nammayantra.share.data.model.User
import com.nammayantra.share.data.repository.AuthRepository
import com.nammayantra.share.data.repository.BookingRepository
import com.nammayantra.share.data.repository.MachineRepository
import com.nammayantra.share.util.PriceCalculator
import com.nammayantra.share.util.Resource
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val machineRepo = MachineRepository
    private val bookingRepo = BookingRepository
    private val authRepo = AuthRepository

    private val _machine = MutableLiveData<Resource<Machine>>()
    val machine: LiveData<Resource<Machine>> = _machine

    private val _user = MutableLiveData<Resource<User>>()
    val user: LiveData<Resource<User>> = _user

    private val _bookingResult = MutableLiveData<Resource<Unit>>()
    val bookingResult: LiveData<Resource<Unit>> = _bookingResult

    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    private var cachedMachine: Machine? = null
    private var cachedUser: User? = null

    fun loadMachine(id: String) {
        _machine.value = Resource.Loading
        viewModelScope.launch {
            machineRepo.getMachineById(id).fold(
                onSuccess = {
                    cachedMachine = it
                    _machine.postValue(Resource.Success(it))
                },
                onFailure = {
                    _machine.postValue(Resource.Error(it.message ?: "Failed to load machine"))
                }
            )
        }
    }

    fun loadUser() {
        val uid = authRepo.currentUserId() ?: return
        _user.value = Resource.Loading
        viewModelScope.launch {
            authRepo.getUser(uid).fold(
                onSuccess = {
                    cachedUser = it
                    _user.postValue(Resource.Success(it))
                },
                onFailure = {
                    _user.postValue(Resource.Error(it.message ?: "User not found"))
                }
            )
        }
    }

    fun calculatePrice(duration: Int, durationType: String) {
        cachedMachine?.let {
            _totalPrice.value = PriceCalculator.total(it, duration, durationType)
        }
    }

    fun requestBooking(startMillis: Long, duration: Int, durationType: String) {
        val m = cachedMachine ?: run {
            _bookingResult.value = Resource.Error("Machine data not loaded yet")
            return
        }

        if (!m.isAvailable) {
            _bookingResult.value = Resource.Error("This machine is currently unavailable")
            return
        }
        if (startMillis <= 0L) {
            _bookingResult.value = Resource.Error("Please select a date")
            return
        }
        if (duration <= 0) {
            _bookingResult.value = Resource.Error("Please enter a valid duration")
            return
        }

        // Try to use cached user; if not yet loaded, fetch inline
        _bookingResult.value = Resource.Loading
        viewModelScope.launch {
            val user = cachedUser ?: run {
                val uid = authRepo.currentUserId() ?: run {
                    _bookingResult.postValue(Resource.Error("Not logged in"))
                    return@launch
                }
                authRepo.getUser(uid).getOrElse {
                    _bookingResult.postValue(Resource.Error("Could not load user profile"))
                    return@launch
                }
            }
            cachedUser = user

            val durationMillis = if (durationType == "daily") {
                duration.toLong() * 24 * 60 * 60 * 1000
            } else {
                duration.toLong() * 60 * 60 * 1000
            }
            val endMillis = startMillis + durationMillis
            val total = PriceCalculator.total(m, duration, durationType)

            val booking = Booking(
                machineId = m.id,
                machineName = m.name,
                ownerId = m.ownerId,
                userId = user.id,
                userName = user.name,
                startTimeMillis = startMillis,
                endTimeMillis = endMillis,
                duration = duration,
                durationType = durationType,
                totalPrice = total,
                status = "Pending"
            )

            bookingRepo.requestBooking(booking).fold(
                onSuccess = { _bookingResult.postValue(Resource.Success(Unit)) },
                onFailure = { _bookingResult.postValue(Resource.Error(it.message ?: "Booking failed")) }
            )
        }
    }
}
