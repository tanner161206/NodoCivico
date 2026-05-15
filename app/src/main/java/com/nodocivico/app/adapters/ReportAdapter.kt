package com.nodocivico.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nodocivico.app.databinding.ItemReportBinding
import com.nodocivico.app.domain.model.Priority
import com.nodocivico.app.domain.model.Report
import com.nodocivico.app.utils.toRelativeTime

class ReportAdapter(
    private val onItemClick: (Report) -> Unit,
    private val onDeleteClick: (Report) -> Unit = {}
) : ListAdapter<Report, ReportAdapter.ReportViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(
        private val binding: ItemReportBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            binding.apply {
                tvReportTitle.text       = report.title
                tvReportDate.text        = report.date.toRelativeTime()
                tvReportLocation.text    = report.location.ifBlank { "Sin ubicación" }

                // Estado visual del badge de sincronización
                ivSyncStatus.setImageResource(
                    if (report.synced) android.R.drawable.presence_online
                    else android.R.drawable.presence_away
                )

                // Color de prioridad
                val priorityColor = when (report.priority) {
                    Priority.HIGH   -> 0xFFDC2626.toInt()
                    Priority.MEDIUM -> 0xFFF59E0B.toInt()
                    Priority.LOW    -> 0xFF16A34A.toInt()
                }
                viewPriorityBar.setBackgroundColor(priorityColor)
                tvPriority.text = report.priority.label

                // Chip de estado
                val (statusText, statusBg) = when (report.statusId) {
                    1    -> Pair("Abierto",     0x1A2563EB)
                    2    -> Pair("En proceso",  0x1AF59E0B)
                    3    -> Pair("Cerrado",     0x1A16A34A)
                    else -> Pair("Rechazado",   0x1ADC2626)
                }
                chipStatus.text = statusText
                chipStatus.chipBackgroundColor =
                    android.content.res.ColorStateList.valueOf(statusBg)

                // Clicks
                root.setOnClickListener { onItemClick(report) }
                btnDelete.setOnClickListener { onDeleteClick(report) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(a: Report, b: Report) = a.id == b.id
        override fun areContentsTheSame(a: Report, b: Report) = a == b
    }
}
