package com.nodocivico.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nodocivico.app.databinding.ItemReminderBinding
import com.nodocivico.app.domain.model.Reminder
import com.nodocivico.app.utils.toFormattedDate

class ReminderAdapter(
    private val onDelete: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemReminderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: Reminder) {
            b.tvReminderMessage.text = r.message
            b.tvReminderDate.text    = r.reminderDate.toFormattedDate("dd/MM/yyyy HH:mm")
            b.tvReportId.text        = "Reporte #${r.reportId}"
            b.btnDeleteReminder.setOnClickListener { onDelete(r) }
        }
    }

    class Diff : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(a: Reminder, b: Reminder) = a.id == b.id
        override fun areContentsTheSame(a: Reminder, b: Reminder) = a == b
    }
}
