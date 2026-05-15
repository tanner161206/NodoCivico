package com.nodocivico.app.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.R
import com.nodocivico.app.adapters.ReportAdapter
import com.nodocivico.app.databinding.FragmentCreateReportBinding
import com.nodocivico.app.databinding.FragmentReportListBinding
import com.nodocivico.app.domain.model.Priority
import com.nodocivico.app.domain.model.UiState
import com.nodocivico.app.utils.*
import com.nodocivico.app.viewmodel.ReportViewModel
import com.nodocivico.app.viewmodel.ViewModelFactory

// ---------------------------------------------------------------------------
// ReportListFragment — lista completa con filtros y FAB
// ---------------------------------------------------------------------------
class ReportListFragment : Fragment() {

    private var _binding: FragmentReportListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reportRepository = app.reportRepository)
    }

    private lateinit var adapter: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupFilters()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = ReportAdapter(
            onItemClick = { report ->
                val action = ReportListFragmentDirections
                    .actionReportListToReportDetail(report.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { report ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.confirm_delete_title)
                    .setMessage(R.string.confirm_delete_msg)
                    .setNegativeButton(R.string.btn_cancel, null)
                    .setPositiveButton(R.string.btn_delete) { _, _ ->
                        viewModel.deleteReport(report)
                    }
                    .show()
            }
        )
        binding.rvReports.apply {
            adapter = this@ReportListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            // La sincronización real se realiza en SyncStatusFragment/Entregable 3
            binding.root.showSnackbar("Lista actualizada")
        }
    }

    private fun setupObservers() {
        viewModel.reports.observe(viewLifecycleOwner) { reports ->
            binding.progressBar.gone()
            if (reports.isEmpty()) {
                binding.layoutEmpty.visible()
                binding.rvReports.gone()
                binding.layoutError.gone()
            } else {
                binding.layoutEmpty.gone()
                binding.layoutError.gone()
                binding.rvReports.visible()
                adapter.submitList(reports)
            }
        }

        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> binding.root.showSnackbarError(state.message)
                is UiState.Success<*> -> binding.root.showSnackbarSuccess("Operación completada")
                else -> {}
            }
        }
    }

    private fun setupFilters() {
        binding.chipAll.setOnClickListener {
            viewModel.reports.value?.let { adapter.submitList(it) }
        }
        binding.chipOpen.setOnClickListener {
            viewModel.reports.value?.let { list ->
                adapter.submitList(list.filter { it.statusId == 1 })
            }
        }
        binding.chipInProgress.setOnClickListener {
            viewModel.reports.value?.let { list ->
                adapter.submitList(list.filter { it.statusId == 2 })
            }
        }
        binding.chipClosed.setOnClickListener {
            viewModel.reports.value?.let { list ->
                adapter.submitList(list.filter { it.statusId == 3 })
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabCreate.setOnClickListener {
            findNavController().navigate(R.id.action_reportList_to_createReport)
        }
        binding.btnCreateFirst.setOnClickListener {
            findNavController().navigate(R.id.action_reportList_to_createReport)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ---------------------------------------------------------------------------
// CreateReportFragment — formulario completo con validaciones
// ---------------------------------------------------------------------------
class CreateReportFragment : Fragment() {

    private var _binding: FragmentCreateReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reportRepository = app.reportRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSave.isEnabled = false
                    binding.btnSaveOffline.isEnabled = false
                }
                is UiState.Success<*> -> {
                    binding.progressBar.gone()
                    binding.root.showSnackbarSuccess("Reporte guardado correctamente")
                    findNavController().popBackStack()
                }
                is UiState.Error -> {
                    binding.progressBar.gone()
                    binding.btnSave.isEnabled = true
                    binding.btnSaveOffline.isEnabled = true
                    binding.tilTitle.error = if (state.message.contains("título")) state.message else null
                    binding.tilDescription.error = if (state.message.contains("descripción")) state.message else null
                    binding.tilLocation.error = if (state.message.contains("ubicación")) state.message else null
                    binding.root.showSnackbarError(state.message)
                }
                else -> {
                    binding.progressBar.gone()
                    binding.btnSave.isEnabled = true
                    binding.btnSaveOffline.isEnabled = true
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener { saveReport() }
        binding.btnSaveOffline.setOnClickListener { saveReport() }
    }

    private fun saveReport() {
        // Limpiar errores previos
        binding.tilTitle.error = null
        binding.tilDescription.error = null
        binding.tilLocation.error = null

        val title       = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val location    = binding.etLocation.text.toString()
        val categoryId  = binding.spinnerCategory.selectedItemPosition + 1
        val priorityLabel = binding.spinnerPriority.selectedItem.toString()
        val priority    = Priority.fromLabel(priorityLabel)

        viewModel.createReport(title, description, categoryId, priority, location)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
