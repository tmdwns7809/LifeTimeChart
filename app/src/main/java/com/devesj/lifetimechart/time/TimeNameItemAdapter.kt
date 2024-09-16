package com.devesj.lifetimechart.time

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devesj.lifetimechart.R
import com.devesj.lifetimechart.db.Time
import com.devesj.lifetimechart.util.ColorUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeNameItemAdapter(private var names: List<String>,
                          private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<TimeNameItemAdapter.TimeNameItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(name: String, color: Int)
    }

    class TimeNameItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameItemTextView: TextView = itemView.findViewById(R.id.nameItemTextView)
        val itemLayout: FrameLayout = itemView.findViewById(R.id.itemLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeNameItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_name_item_card, parent, false)
        return TimeNameItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeNameItemViewHolder, position: Int) {
        val realPosition = position % names.size
        val name = names[realPosition]
        val color = ColorUtil.getColorForValue(realPosition, names.size - 1)
        holder.nameItemTextView.text = name
        holder.itemLayout.setBackgroundColor(color)
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(name, color)
        }
    }

    override fun getItemCount(): Int = names.size

    fun updateItems(newNames: List<String>) {
        this.names = newNames
        notifyDataSetChanged()
    }
}