package com.nammayantra.share.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nammayantra.share.R
import com.nammayantra.share.data.model.Booking
import com.nammayantra.share.databinding.ItemBookingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingAdapter(
    private val showActions: Boolean = false,
    private val onAccept: ((Booking) -> Unit)? = null,
    private val onDecline: ((Booking) -> Unit)? = null
) : ListAdapter<Booking, BookingAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: ItemBookingBinding) : RecyclerView.ViewHolder(b.root) {
        private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        fun bind(booking: Booking) {
            val ctx = b.root.context

            b.tvTitle.text = booking.machineName.ifBlank { "Machine Booking" }

            // Date and duration info
            val dateStr = if (booking.startTimeMillis > 0) {
                dateFormat.format(Date(booking.startTimeMillis))
            } else "—"
            val unitLabel = if (booking.durationType == "daily") "days" else "hours"
            b.tvMeta.text = "$dateStr • ${booking.duration} $unitLabel"

            // Price
            b.tvPrice.text = "₹${booking.totalPrice.toInt()}"

            // Status badge with correct background + padding
            b.tvStatus.text = booking.status
            when (booking.status) {
                "Pending" -> {
                    b.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_pending))
                    b.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                "Accepted" -> {
                    b.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_accepted))
                    b.tvStatus.setBackgroundResource(R.drawable.bg_status_accepted)
                }
                "Declined" -> {
                    b.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_declined))
                    b.tvStatus.setBackgroundResource(R.drawable.bg_status_declined)
                }
                else -> {
                    b.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
                    b.tvStatus.background = null
                }
            }

            // Owner actions (Accept/Decline) - only for Pending bookings in dashboard
            if (showActions && booking.status == "Pending") {
                b.layoutActions.visibility = View.VISIBLE
                b.btnAccept.setOnClickListener { onAccept?.invoke(booking) }
                b.btnDecline.setOnClickListener { onDecline?.invoke(booking) }
            } else {
                b.layoutActions.visibility = View.GONE
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Booking>() {
            override fun areItemsTheSame(a: Booking, b: Booking) = a.id == b.id
            override fun areContentsTheSame(a: Booking, b: Booking) = a == b
        }
    }
}
