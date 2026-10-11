package com.example.criminalintent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.databinding.ListItemCrimeBinding
import java.text.DateFormat
import java.util.Date

class CrimeAdapter(private val click: (Crime) -> Unit) : ListAdapter<Crime, CrimeAdapter.Holder>(Diff) {
    init { stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY }
    class Holder(val binding: ListItemCrimeBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ListItemCrimeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val crime = getItem(position)
        holder.binding.apply {
            crimeTitle.text = crime.title.ifBlank { root.context.getString(R.string.untitled) }
            crimeDate.text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(crime.dateMillis))
            crimeStatus.setText(if (crime.solved) R.string.solved_status else R.string.open_status)
            root.setOnClickListener { click(crime) }
        }
    }
    object Diff : DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Crime, newItem: Crime) = oldItem == newItem
    }
}
