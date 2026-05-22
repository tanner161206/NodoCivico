package com.nodocivico.app.ui.reports

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.adapters.ReminderAdapter
import com.nodocivico.app.databinding.FragmentCalendarRemindersBinding
import com.nodocivico.app.domain.model.Reminder
import com.nodocivico.app.domain.model.UiState
import com.nodocivico.app.notifications.NotificationScheduler
import com.nodocivico.app.utils.*
import com.nodocivico.app.viewmodel.CalendarViewModel
import com.nodocivico.app.viewmodel.ViewModelFactory
import java.util.Calendar

class CalendarRemindersFragment : Fragment() {

    private var _binding: FragmentCalendarRemindersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reminderRepository = app.reminderRepository)
    }

    private lateinit var adapter: ReminderAdapter
    private var selectedMillis = 0L
    private val cal = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupObservers()
        setupClickListeners()
        viewModel.loadUpcoming()
    }

    private fun setupList() {
        adapter = ReminderAdapter { reminder ->
            NotificationScheduler.cancel(requireContext(), reminder.id)
            viewModel.deleteReminder(reminder)
        }
        binding.rvReminders.apply {
            this.adapter = this@CalendarRemindersFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.reminders.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvReminders.visibility = if (list.isEmpty()) View.GONE   else View.VISIBLE
        }

        viewModel.reminderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success<*> -> {
                    binding.root.showSnackbarSuccess("Recordatorio programado ✓")
                    binding.etReminderMessage.text?.clear()
                    binding.etReportId.text?.clear()
                    binding.tvSelectedDate.text = "Sin fecha seleccionada"
                    selectedMillis = 0L
                    viewModel.clearState()
                }
                is UiState.Error -> { binding.root.showSnackbarError(state.message); viewModel.clearState() }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnPickDate.setOnClickListener { showDatePicker() }

        binding.btnAddReminder.setOnClickListener {
            if (selectedMillis == 0L) {
                binding.root.showSnackbarError("Selecciona una fecha y hora")
                return@setOnClickListener
            }
            val reportId = binding.etReportId.text.toString().toLongOrNull() ?: 0L
            val message  = binding.etReminderMessage.text.toString()

            viewModel.addReminder(reportId, selectedMillis, message)

            // Programar la alarma localmente con AlarmManager
            if (selectedMillis > System.currentTimeMillis()) {
                NotificationScheduler.schedule(
                    requireContext(),
                    Reminder(reportId = reportId, reminderDate = selectedMillis, message = message)
                )
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d)
            showTimePicker()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            .also { it.datePicker.minDate = System.currentTimeMillis() }
            .show()
    }

    private fun showTimePicker() {
        TimePickerDialog(requireContext(), { _, h, min ->
            cal.set(Calendar.HOUR_OF_DAY, h)
            cal.set(Calendar.MINUTE, min)
            cal.set(Calendar.SECOND, 0)
            selectedMillis = cal.timeInMillis
            binding.tvSelectedDate.text = selectedMillis.toFormattedDate("dd/MM/yyyy HH:mm")
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
