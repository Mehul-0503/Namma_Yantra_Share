package com.nammayantra.share.util

import com.nammayantra.share.data.model.Machine

object PriceCalculator {
    fun total(machine: Machine, duration: Int, durationType: String): Double {
        if (duration <= 0) return 0.0
        return if (durationType == "daily") machine.dailyRate * duration else machine.hourlyRate * duration
    }
}
