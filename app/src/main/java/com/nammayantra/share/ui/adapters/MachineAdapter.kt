package com.nammayantra.share.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammayantra.share.R
import com.nammayantra.share.data.model.MachineUi
import com.nammayantra.share.databinding.ItemMachineBinding
import java.util.Locale

class MachineAdapter(
    private val onClick: (MachineUi) -> Unit
) : ListAdapter<MachineUi, MachineAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMachineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: ItemMachineBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MachineUi) {
            val m = item.machine
            val ctx = b.root.context

            b.tvMachineEmoji.text = m.emoji()
            b.tvMachineName.text = m.name
            b.tvOwnerName.text = "by ${m.ownerName}"
            b.tvRate.text = "₹${m.hourlyRate.toInt()}/hr"
            b.tvDistance.text = String.format(Locale.US, "%.1f km", item.distanceKm)

            // Condition stars
            val stars = "⭐".repeat(m.condition)
            b.tvCondition.text = stars

            // Availability badge
            if (m.isAvailable) {
                b.tvAvailability.text = "Available"
                b.tvAvailability.setTextColor(ContextCompat.getColor(ctx, R.color.status_accepted))
                b.tvAvailability.setBackgroundResource(R.drawable.bg_status_accepted)
            } else {
                b.tvAvailability.text = "Booked"
                b.tvAvailability.setTextColor(ContextCompat.getColor(ctx, R.color.status_declined))
                b.tvAvailability.setBackgroundResource(R.drawable.bg_status_declined)
            }

            b.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MachineUi>() {
            override fun areItemsTheSame(a: MachineUi, b: MachineUi) = a.machine.id == b.machine.id
            override fun areContentsTheSame(a: MachineUi, b: MachineUi) = a == b
        }
    }
}
