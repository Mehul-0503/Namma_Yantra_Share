package com.nammayantra.share.ui.owner

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.nammayantra.share.R
import com.nammayantra.share.data.model.Machine
import com.nammayantra.share.data.repository.AuthRepository
import com.nammayantra.share.data.repository.MachineRepository
import com.nammayantra.share.databinding.ActivityAddMachineBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddMachineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMachineBinding
    private val machineRepo = MachineRepository
    private val authRepo = AuthRepository
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMachineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Service date picker
        binding.etServiceDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                R.style.Theme_NammaYantraShare,
                { _, year, month, day ->
                    cal.set(year, month, day)
                    binding.etServiceDate.setText(dateFormat.format(cal.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSave.setOnClickListener {
            saveMachine()
        }
    }

    private fun saveMachine() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val hourlyRate = binding.etHourlyRate.text.toString().toDoubleOrNull()
        val dailyRate = binding.etDailyRate.text.toString().toDoubleOrNull()
        val condition = binding.etCondition.text.toString().toIntOrNull()
        val serviceDate = binding.etServiceDate.text.toString().trim()

        // Use AuthRepository - works in both DEMO_MODE and real Firebase
        val uid = authRepo.currentUserId()

        if (name.isBlank()) {
            Toast.makeText(this, "Please enter machine name", Toast.LENGTH_SHORT).show()
            return
        }
        if (hourlyRate == null || hourlyRate <= 0) {
            Toast.makeText(this, "Please enter a valid hourly rate", Toast.LENGTH_SHORT).show()
            return
        }
        if (dailyRate == null || dailyRate <= 0) {
            Toast.makeText(this, "Please enter a valid daily rate", Toast.LENGTH_SHORT).show()
            return
        }
        if (condition == null || condition !in 1..5) {
            Toast.makeText(this, "Condition must be between 1 and 5", Toast.LENGTH_SHORT).show()
            return
        }
        if (uid == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        // Fetch owner profile then save machine
        lifecycleScope.launch {
            // Try Firestore profile first; fall back to Firebase Auth displayName or "Owner"
            val ownerName = authRepo.getUser(uid).getOrNull()?.name
                ?: FirebaseAuth.getInstance().currentUser?.displayName
                ?: "Owner"

            val machine = Machine(
                name = name,
                ownerId = uid,
                ownerName = ownerName,
                description = description,
                hourlyRate = hourlyRate,
                dailyRate = dailyRate,
                // Randomize location slightly around Bangalore for demo
                latitude = 12.9716 + (Math.random() * 0.1 - 0.05),
                longitude = 77.5946 + (Math.random() * 0.1 - 0.05),
                condition = condition,
                lastServiceDate = serviceDate,
                isAvailable = true
            )

            machineRepo.addMachine(machine).fold(
                onSuccess = {
                    binding.progress.visibility = View.GONE
                    Toast.makeText(this@AddMachineActivity, getString(R.string.machine_added), Toast.LENGTH_LONG).show()
                    finish()
                },
                onFailure = {
                    binding.progress.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this@AddMachineActivity, it.message ?: "Failed to add machine", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
