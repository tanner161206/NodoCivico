package com.nodocivico.app.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.databinding.FragmentEditReportBinding
import com.nodocivico.app.domain.model.Priority
import com.nodocivico.app.domain.model.UiState
import com.nodocivico.app.utils.*
import com.nodocivico.app.viewmodel.EditReportViewModel
import com.nodocivico.app.viewmodel.ViewModelFactory

class EditReportFragment : Fragment() {

    private var _binding: FragmentEditReportBinding? = null
    private val binding get() = _binding!!
    private val args: EditReportFragmentArgs by navArgs()

    private val viewModel: EditReportViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reportRepository = app.reportRepository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        viewModel.loadReport(args.reportId)
        setupObservers()
        setupClickListeners()
    }

    private fun setupSpinners() {
        binding.spinnerPriority.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            listOf("Baja", "Media", "Alta")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerStatus.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            listOf("Abierto", "En proceso", "Cerrado", "Rechazado")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun setupObservers() {
        viewModel.report.observe(viewLifecycleOwner) { report ->
            report ?: return@observe
            binding.etTitle.setText(report.title)
            binding.etDescription.setText(report.description)
            binding.etLocation.setText(report.location)
            binding.spinnerPriority.setSelection(
                when (report.priority) { Priority.LOW -> 0; Priority.MEDIUM -> 1; Priority.HIGH -> 2 }
            )
            binding.spinnerStatus.setSelection((report.statusId - 1).coerceIn(0, 3))
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSave.isEnabled = false
                }
                is UiState.Success<*> -> {
                    binding.progressBar.gone()
                    binding.root.showSnackbarSuccess("Reporte actualizado correctamente")
                    findNavController().popBackStack()
                }
                is UiState.Error -> {
                    binding.progressBar.gone()
                    binding.btnSave.isEnabled = true
                    when {
                        state.message.contains("título")       -> binding.tilTitle.error = state.message
                        state.message.contains("descripción") -> binding.tilDescription.error = state.message
                        state.message.contains("ubicación")   -> binding.tilLocation.error = state.message
                        else -> binding.root.showSnackbarError(state.message)
                    }
                }
                else -> { binding.progressBar.gone(); binding.btnSave.isEnabled = true }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            binding.tilTitle.error = null
            binding.tilDescription.error = null
            binding.tilLocation.error = null
            viewModel.updateReport(
                title       = binding.etTitle.text.toString(),
                description = binding.etDescription.text.toString(),
                location    = binding.etLocation.text.toString(),
                priority    = Priority.fromLabel(binding.spinnerPriority.selectedItem.toString()),
                statusId    = binding.spinnerStatus.selectedItemPosition + 1
            )
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
