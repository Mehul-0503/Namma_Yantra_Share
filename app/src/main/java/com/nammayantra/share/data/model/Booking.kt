package com.nammayantra.share.data.model

data class Booking(
    val id: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val ownerId: String = "",
    val userId: String = "",
    val userName: String = "",
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val duration: Int = 0,
    val durationType: String = "hourly",
    val totalPrice: Double = 0.0,
    val status: String = "Pending"
)
