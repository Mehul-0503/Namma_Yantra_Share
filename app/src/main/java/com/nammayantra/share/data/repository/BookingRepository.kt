package com.nammayantra.share.data.repository

import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.nammayantra.share.data.model.Booking
import com.nammayantra.share.util.Constants
import kotlinx.coroutines.tasks.await

object BookingRepository {
    private val ref = Firebase.firestore.collection("bookings")

    private val mockBookings = mutableListOf(
        Booking("b1", "m1", "John Deere 5050D", "owner_1", "demo_uid", "Demo User", System.currentTimeMillis() - 86400000, System.currentTimeMillis() - 80000000, 4, "hourly", 2000.0, "Accepted"),
        Booking("b2", "m4", "Kubota Harvester", "owner_3", "demo_uid", "Demo User", System.currentTimeMillis() + 86400000, System.currentTimeMillis() + 100000000, 2, "daily", 16000.0, "Pending"),
        Booking("b3", "m2", "Sonalika Worldtrac", "demo_uid", "farmer_2", "Manju Patil", System.currentTimeMillis() + 172800000, System.currentTimeMillis() + 200000000, 1, "daily", 4500.0, "Pending")
    )

    suspend fun requestBooking(booking: Booking): Result<Unit> = runCatching {
        if (Constants.DEMO_MODE) {
            val hasConflict = mockBookings.any {
                it.machineId == booking.machineId &&
                it.status in listOf("Pending", "Accepted") &&
                it.startTimeMillis < booking.endTimeMillis && booking.startTimeMillis < it.endTimeMillis
            }
            if (hasConflict) throw IllegalStateException("Machine already booked for selected slot")
            val id = "bk_${System.currentTimeMillis()}"
            mockBookings.add(booking.copy(id = id))
            return@runCatching
        }
        val existing = ref.whereEqualTo("machineId", booking.machineId)
            .whereIn("status", listOf("Pending", "Accepted"))
            .get().await().documents.mapNotNull { it.toObject<Booking>() }

        val hasConflict = existing.any {
            it.startTimeMillis < booking.endTimeMillis && booking.startTimeMillis < it.endTimeMillis
        }
        if (hasConflict) throw IllegalStateException("Machine already booked for selected slot")

        val id = ref.document().id
        ref.document(id).set(booking.copy(id = id)).await()
    }

    suspend fun getBookingsByUser(userId: String): Result<List<Booking>> = runCatching {
        if (Constants.DEMO_MODE) return@runCatching mockBookings.filter { it.userId == userId }.sortedByDescending { it.startTimeMillis }
        ref.whereEqualTo("userId", userId).get().await().documents
            .mapNotNull { it.toObject<Booking>()?.copy(id = it.id) }
            .sortedByDescending { it.startTimeMillis }
    }

    suspend fun getBookingsForOwner(ownerId: String): Result<List<Booking>> = runCatching {
        if (Constants.DEMO_MODE) return@runCatching mockBookings.filter { it.ownerId == ownerId }.sortedByDescending { it.startTimeMillis }
        ref.whereEqualTo("ownerId", ownerId).get().await().documents
            .mapNotNull { it.toObject<Booking>()?.copy(id = it.id) }
            .sortedByDescending { it.startTimeMillis }
    }

    suspend fun updateStatus(bookingId: String, status: String): Result<Unit> = runCatching {
        require(status in setOf("Pending", "Accepted", "Declined")) { "Invalid booking status" }
        if (Constants.DEMO_MODE) {
            val index = mockBookings.indexOfFirst { it.id == bookingId }
            if (index != -1) mockBookings[index] = mockBookings[index].copy(status = status)
            return@runCatching
        }
        ref.document(bookingId).update("status", status).await()
    }
}
