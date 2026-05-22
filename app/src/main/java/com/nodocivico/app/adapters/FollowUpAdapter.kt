package com.nodocivico.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nodocivico.app.databinding.ItemFollowUpBinding
import com.nodocivico.app.domain.model.FollowUp
import com.nodocivico.app.utils.toFormattedDate

class FollowUpAdapter : ListAdapter<FollowUp, FollowUpAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemFollowUpBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemFollowUpBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(f: FollowUp) {
            b.tvComment.text = f.comment
            b.tvDate.text    = f.createdAt.toFormattedDate()
        }
    }

    class Diff : DiffUtil.ItemCallback<FollowUp>() {
        override fun areItemsTheSame(a: FollowUp, b: FollowUp) = a.id == b.id
        override fun areContentsTheSame(a: FollowUp, b: FollowUp) = a == b
    }
}
