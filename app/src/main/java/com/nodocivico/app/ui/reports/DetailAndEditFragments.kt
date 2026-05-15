package com.nodocivico.app.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.R
import com.nodocivico.app.databinding.FragmentReportDetailBinding
import com.nodocivico.app.domain.model.UiState
import com.nodocivico.app.utils.*
import com.nodocivico.app.viewmodel.ReportViewModel
import com.nodocivico.app.viewmodel.ViewModelFactory

// ---------------------------------------------------------------------------
// ReportDetailFragment — detalle de un reporte con acciones
// ---------------------------------------------------------------------------
class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!
    private val args: ReportDetailFragmentArgs by navArgs()

    private val viewModel: ReportViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reportRepository = app.reportRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadReportById(args.reportId)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.selectedReport.observe(viewLifecycleOwner) { report ->
            if (report == null) return@observe
            binding.tvTitle.text       = report.title
            binding.tvDescription.text = report.description
            binding.tvLocation.text    = report.location
            binding.tvDate.text        = report.date.toFormattedDate()
            binding.tvPriority.text    = report.priority.label
            binding.tvSynced.text      = if (report.synced) "✓ Sincronizado" else "⏳ Pendiente"

            val statusText = when (report.statusId) {
                1 -> "Abierto"; 2 -> "En proceso"; 3 -> "Cerrado"; else -> "Rechazado"
            }
            binding.chipStatus.text = statusText
        }

        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success<*> -> findNavController().popBackStack()
                is UiState.Error -> binding.root.showSnackbarError(state.message)
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            val action = ReportDetailFragmentDirections
                .actionReportDetailToEditReport(args.reportId)
            findNavController().navigate(action)
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_msg)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_delete) { _, _ ->
                    viewModel.selectedReport.value?.let { viewModel.deleteReport(it) }
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ---------------------------------------------------------------------------
// EditReportFragment — edición de un reporte existente
// ---------------------------------------------------------------------------
class EditReportFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!
    private val args: EditReportFragmentArgs by navArgs()

    private val viewModel: ReportViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reportRepository = app.reportRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadReportById(args.reportId)
        // La UI de edición completa va en el Entregable 2
        // con fragment_edit_report.xml propio y binding independiente
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ---------------------------------------------------------------------------
// CalendarRemindersFragment — lista de recordatorios (Entregable 3 completo)
// ---------------------------------------------------------------------------
class CalendarRemindersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_calendar_reminders, container, false)
}
