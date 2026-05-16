package com.nammayantra.share.ui.detail

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nammayantra.share.R
import com.nammayantra.share.data.model.Machine
import com.nammayantra.share.databinding.ActivityEquipmentDetailBinding
import com.nammayantra.share.util.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EquipmentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEquipmentDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    private var selectedDateMillis = 0L
    private var currentDurationType = "hourly"
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipmentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val machineId = intent.getStringExtra("machineId") ?: run {
            Toast.makeText(this, "Machine not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load user profile and machine concurrently
        viewModel.loadUser()
        viewModel.loadMachine(machineId)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // Date picker
        binding.cardDate.setOnClickListener {
            val cal = Calendar.getInstance()
            // If date already selected, pre-populate picker
            if (selectedDateMillis > 0) cal.timeInMillis = selectedDateMillis

            DatePickerDialog(
                this,
                R.style.Theme_NammaYantraShare,
                { _, year, month, day ->
                    cal.set(year, month, day, 8, 0, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    selectedDateMillis = cal.timeInMillis
                    binding.tvDate.text = dateFormat.format(cal.time)
                    recalculate()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        // Duration type chips
        binding.chipHourly.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                currentDurationType = "hourly"
                recalculate()
            }
        }
        binding.chipDaily.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                currentDurationType = "daily"
                recalculate()
            }
        }

        // Duration input
        binding.etDuration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                recalculate()
            }
        })

        // Book button
        binding.btnRequest.setOnClickListener {
            val duration = binding.etDuration.text.toString().toIntOrNull() ?: 0
            viewModel.requestBooking(selectedDateMillis, duration, currentDurationType)
        }
    }

    private fun recalculate() {
        val duration = binding.etDuration.text.toString().toIntOrNull() ?: 0
        viewModel.calculatePrice(duration, currentDurationType)
    }

    private fun observeViewModel() {
        viewModel.machine.observe(this) { res ->
            when (res) {
                is Resource.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.btnRequest.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progress.visibility = View.GONE
                    binding.btnRequest.isEnabled = true
                    populateMachine(res.data)
                }
                is Resource.Error -> {
                    binding.progress.visibility = View.GONE
                    Toast.makeText(this, res.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.totalPrice.observe(this) { price ->
            binding.tvTotal.text = "₹${price.toInt()}"
        }

        viewModel.bookingResult.observe(this) { res ->
            when (res) {
                is Resource.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.btnRequest.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progress.visibility = View.GONE
                    binding.btnRequest.isEnabled = true
                    Toast.makeText(this, getString(R.string.booking_success), Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.btnRequest.isEnabled = true
                    Toast.makeText(this, res.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateMachine(m: Machine) {
        binding.tvHeaderEmoji.text = m.emoji()
        binding.tvName.text = m.name
        binding.tvOwnerLabel.text = "by ${m.ownerName}"
        binding.tvDescription.text = m.description.ifBlank { "No description provided." }
        binding.tvHourlyRate.text = "₹${m.hourlyRate.toInt()}"
        binding.tvDailyRate.text = "₹${m.dailyRate.toInt()}"

        // Availability
        if (m.isAvailable) {
            binding.tvStatus.text = getString(R.string.available)
            binding.tvStatus.setTextColor(getColor(R.color.status_accepted))
        } else {
            binding.tvStatus.text = getString(R.string.unavailable)
            binding.tvStatus.setTextColor(getColor(R.color.status_declined))
            binding.btnRequest.isEnabled = false
            binding.btnRequest.alpha = 0.5f
        }

        // isIndicator is already set to true in XML — just set the rating value
        binding.ratingBar.rating = m.condition.coerceIn(0, 5).toFloat()

        // Last service
        binding.tvService.text = m.lastServiceDate.ifBlank { "N/A" }
    }
}
