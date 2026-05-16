package com.nammayantra.share.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammayantra.share.data.model.Machine
import com.nammayantra.share.databinding.ItemOwnerMachineBinding

class OwnerMachineAdapter : ListAdapter<Machine, OwnerMachineAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOwnerMachineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: ItemOwnerMachineBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(machine: Machine) {
            b.tvEmoji.text = machine.emoji()
            b.tvMachineName.text = machine.name
            b.tvMachineRate.text = "₹${machine.hourlyRate.toInt()}/hr"
            b.tvMachineStatus.text = if (machine.isAvailable) "✅ Available" else "🔴 Booked"
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Machine>() {
            override fun areItemsTheSame(a: Machine, b: Machine) = a.id == b.id
            override fun areContentsTheSame(a: Machine, b: Machine) = a == b
        }
    }
}
