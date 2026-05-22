package com.nodocivico.app.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.R
import com.nodocivico.app.adapters.FollowUpAdapter
import com.nodocivico.app.databinding.FragmentReportDetailBinding
import com.nodocivico.app.domain.model.UiState
import com.nodocivico.app.utils.*
import com.nodocivico.app.viewmodel.DetailViewModel
import com.nodocivico.app.viewmodel.ViewModelFactory

class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!
    private val args: ReportDetailFragmentArgs by navArgs()

    private val viewModel: DetailViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reportRepository = app.reportRepository, followUpRepository = app.followUpRepository)
    }

    private lateinit var followUpAdapter: FollowUpAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFollowUps()
        viewModel.loadReport(args.reportId)
        setupObservers()
        setupClickListeners()
    }

    private fun setupFollowUps() {
        followUpAdapter = FollowUpAdapter()
        binding.rvFollowUps.apply {
            adapter = followUpAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        viewModel.report.observe(viewLifecycleOwner) { report ->
            report ?: return@observe
            binding.tvTitle.text       = report.title
            binding.tvDescription.text = report.description
            binding.tvLocation.text    = report.location
            binding.tvDate.text        = report.date.toFormattedDate()
            binding.tvPriority.text    = report.priority.label
            binding.tvSynced.text      = if (report.synced) "✓ Sincronizado" else "⏳ Pendiente sync"

            val (statusText, bgColor) = when (report.statusId) {
                1    -> "Abierto"    to 0x1A2563EB
                2    -> "En proceso" to 0x1AF59E0B
                3    -> "Cerrado"    to 0x1A16A34A
                else -> "Rechazado"  to 0x1ADC2626
            }
            binding.chipStatus.text = statusText
            binding.chipStatus.chipBackgroundColor =
                android.content.res.ColorStateList.valueOf(bgColor)
        }

        viewModel.followUps.observe(viewLifecycleOwner) { list ->
            followUpAdapter.submitList(list)
            binding.tvNoFollowUps.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success<*> -> {
                    if (state.data == "deleted") {
                        binding.root.showSnackbarSuccess("Reporte eliminado")
                        findNavController().popBackStack()
                    } else {
                        binding.root.showSnackbarSuccess("Seguimiento agregado")
                        binding.etFollowUp.text?.clear()
                    }
                    viewModel.clearState()
                }
                is UiState.Error -> { binding.root.showSnackbarError(state.message); viewModel.clearState() }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                ReportDetailFragmentDirections.actionReportDetailToEditReport(args.reportId)
            )
        }
        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_msg)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_delete) { _, _ ->
                    viewModel.report.value?.let { viewModel.deleteReport(it) }
                }.show()
        }
        binding.btnAddFollowUp.setOnClickListener {
            viewModel.addFollowUp(args.reportId, binding.etFollowUp.text.toString())
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
