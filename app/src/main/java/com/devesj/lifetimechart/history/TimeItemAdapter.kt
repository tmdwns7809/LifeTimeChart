package com.devesj.lifetimechart.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devesj.lifetimechart.R
import com.devesj.lifetimechart.db.Time
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeItemAdapter(private var times: List<Time>) :
    RecyclerView.Adapter<TimeItemAdapter.TimeItemViewHolder>() {

    class TimeItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val startTimeTextView: TextView = itemView.findViewById(R.id.startTimeTextView)
        val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
        val endTimeTextView: TextView = itemView.findViewById(R.id.endTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_item_card, parent, false)
        return TimeItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeItemViewHolder, position: Int) {
        val time = times[position]
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val durationFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        holder.nameTextView.text = time.name
        holder.startTimeTextView.text = "시작시간: ${timeFormat.format(Date(time.startTime))}"
        holder.durationTextView.text = "소요시간: ${durationFormat.format(Date(time.elapsedTime))}"
        holder.endTimeTextView.text = "종료시간: ${timeFormat.format(Date(time.endTime))}"
    }

    override fun getItemCount(): Int = times.size

    fun updateItems(newTimes: List<Time>) {
        this.times = newTimes
        notifyDataSetChanged()
    }
}