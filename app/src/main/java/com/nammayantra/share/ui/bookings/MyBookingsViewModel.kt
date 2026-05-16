package com.nammayantra.share.ui.bookings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammayantra.share.data.model.Booking
import com.nammayantra.share.data.repository.BookingRepository
import com.nammayantra.share.util.Resource
import kotlinx.coroutines.launch

class MyBookingsViewModel : ViewModel() {

    private val repo = BookingRepository

    private val _bookings = MutableLiveData<Resource<List<Booking>>>()
    val bookings: LiveData<Resource<List<Booking>>> = _bookings

    fun loadBookings(userId: String) {
        _bookings.value = Resource.Loading
        viewModelScope.launch {
            repo.getBookingsByUser(userId).fold(
                onSuccess = { _bookings.postValue(Resource.Success(it)) },
                onFailure = { _bookings.postValue(Resource.Error(it.message ?: "Failed to load bookings")) }
            )
        }
    }
}
