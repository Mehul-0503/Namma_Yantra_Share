package com.nammayantra.share.data.model

data class Machine(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val description: String = "",
    val hourlyRate: Double = 0.0,
    val dailyRate: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val condition: Int = 3,
    val lastServiceDate: String = "",
    val isAvailable: Boolean = true
) {
    /** Returns an emoji based on machine name for visual identification */
    fun emoji(): String = when {
        name.contains("tractor", true) -> "🚜"
        name.contains("harvester", true) -> "🌾"
        name.contains("plough", true) || name.contains("plow", true) -> "⚙️"
        name.contains("sprayer", true) -> "💧"
        name.contains("seeder", true) || name.contains("drill", true) -> "🌱"
        name.contains("thresher", true) -> "🔧"
        name.contains("rotavator", true) || name.contains("cultivator", true) -> "🛠️"
        name.contains("trailer", true) -> "🚛"
        name.contains("pump", true) -> "🔩"
        else -> "🚜"
    }
}
