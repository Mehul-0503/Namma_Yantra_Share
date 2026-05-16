package com.nammayantra.share.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nammayantra.share.data.model.Machine
import com.nammayantra.share.util.Constants
import kotlinx.coroutines.tasks.await

object MachineRepository {
    private val ref = Firebase.firestore.collection("machines")

    // Rich sample data for Karnataka farmers — used in DEMO_MODE AND as seed data for Firestore
    private val sampleMachines = listOf(
        Machine("m1", "John Deere 5050D Tractor", "owner_seed_1", "Ravi Gowda", "Reliable 50HP tractor suitable for all farm tasks including ploughing, tilling and transport. Well maintained with recent service.", 500.0, 3000.0, 12.9716, 77.5946, 5, "15 Mar 2026", true),
        Machine("m2", "Sonalika Worldtrac 90 Tractor", "owner_seed_1", "Ravi Gowda", "Heavy duty 90HP tractor ideal for deep plowing and large field operations. Excellent condition.", 700.0, 4500.0, 12.9816, 77.6046, 4, "01 Apr 2026", true),
        Machine("m3", "Mahindra Arjun 555 Tractor", "owner_seed_2", "Suresh Kumar", "Fuel efficient and powerful 55HP tractor. Perfect for medium sized farms. Very good condition.", 450.0, 2800.0, 12.9616, 77.5846, 4, "20 Feb 2026", true),
        Machine("m4", "Kubota DC-60 Combine Harvester", "owner_seed_3", "Lokesh M", "Efficient combine harvester with minimal grain loss. Handles paddy, wheat and maize. Top condition.", 1200.0, 8000.0, 12.9516, 77.5746, 5, "10 Apr 2026", true),
        Machine("m5", "CLAAS Crop Tiger Harvester", "owner_seed_3", "Lokesh M", "Compact combine harvester perfect for small and medium paddy fields. Easy to maneuver.", 900.0, 6000.0, 12.9416, 77.5646, 4, "05 Mar 2026", true),
        Machine("m6", "Indo Farm Rotavator 200", "owner_seed_2", "Suresh Kumar", "Heavy duty rotavator for soil preparation. Breaks clods efficiently and prepares seedbed in one pass.", 300.0, 1800.0, 12.9916, 77.6146, 4, "12 Feb 2026", true),
        Machine("m7", "Kirloskar Water Pump 10HP", "owner_seed_4", "Manjunath B", "10HP diesel water pump for irrigation. Suitable for bore well and open well. High discharge rate.", 200.0, 1200.0, 12.9316, 77.5546, 3, "01 Jan 2026", true),
        Machine("m8", "VST Shakti Power Tiller", "owner_seed_4", "Manjunath B", "14HP power tiller ideal for small farms and paddy fields. Easy to operate, very economical.", 250.0, 1500.0, 13.0016, 77.6246, 4, "18 Mar 2026", true),
        Machine("m9", "Fieldking Potato Planter", "owner_seed_5", "Anand Patil", "4-row potato planter with fertiliser attachment. Saves time and labour significantly.", 400.0, 2400.0, 12.9216, 77.5446, 4, "22 Feb 2026", true),
        Machine("m10", "Mahindra Gyrovator Tiller", "owner_seed_5", "Anand Patil", "Sturdy gyrovator for deep tilling and mixing of soil. Compatible with most 35HP+ tractors.", 350.0, 2000.0, 13.0116, 77.6346, 3, "10 Jan 2026", true)
    )

    private val mockMachines = sampleMachines.toMutableList()

    suspend fun getMachinesByOwner(ownerId: String): Result<List<Machine>> = runCatching {
        if (Constants.DEMO_MODE) return@runCatching mockMachines.filter { it.ownerId == ownerId }
        ref.whereEqualTo("ownerId", ownerId).get().await()
            .documents.mapNotNull { it.toObject<Machine>()?.copy(id = it.id) }
    }

    suspend fun getMachines(): Result<List<Machine>> = runCatching {
        if (Constants.DEMO_MODE) return@runCatching mockMachines

        val docs = ref.get().await().documents
        val machines = docs.mapNotNull { it.toObject<Machine>()?.copy(id = it.id) }

        // Auto-seed Firestore with sample data if collection is empty
        if (machines.isEmpty()) {
            seedFirestore()
            return@runCatching ref.get().await().documents
                .mapNotNull { it.toObject<Machine>()?.copy(id = it.id) }
        }

        machines
    }

    suspend fun getMachineById(id: String): Result<Machine> = runCatching {
        if (Constants.DEMO_MODE) return@runCatching mockMachines.find { it.id == id }
            ?: throw IllegalStateException("Machine not found")
        val doc = ref.document(id).get().await()
        doc.toObject<Machine>()?.copy(id = doc.id) ?: throw IllegalStateException("Machine not found")
    }

    suspend fun addMachine(machine: Machine): Result<String> = runCatching {
        if (Constants.DEMO_MODE) {
            val id = "mock_${System.currentTimeMillis()}"
            mockMachines.add(machine.copy(id = id))
            return@runCatching id
        }
        val id = ref.document().id
        ref.document(id).set(machine.copy(id = id)).await()
        id
    }

    suspend fun updateAvailability(machineId: String, isAvailable: Boolean): Result<Unit> = runCatching {
        if (Constants.DEMO_MODE) {
            val index = mockMachines.indexOfFirst { it.id == machineId }
            if (index != -1) mockMachines[index] = mockMachines[index].copy(isAvailable = isAvailable)
            return@runCatching
        }
        ref.document(machineId).update("isAvailable", isAvailable).await()
    }

    /** Seeds Firestore with sample machines so new users see data immediately */
    private suspend fun seedFirestore() {
        val batch = Firebase.firestore.batch()
        sampleMachines.forEach { machine ->
            val docRef = ref.document(machine.id)
            batch.set(docRef, machine)
        }
        batch.commit().await()
    }
}
