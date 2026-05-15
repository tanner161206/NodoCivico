package com.nodocivico.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.databinding.FragmentSettingsBinding
import com.nodocivico.app.utils.showSnackbarSuccess
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPreferences()
        setupClickListeners()
    }

    private fun loadPreferences() {
        val app = requireActivity().application as NodoCivicoApp
        lifecycleScope.launch {
            val theme  = app.userPreferences.theme.first()
            val notifs = app.userPreferences.notifications.first()
            binding.spinnerTheme.setSelection(when (theme) { "light" -> 1; "dark" -> 2; else -> 0 })
            binding.spinnerNotifications.setSelection(when (notifs) { "silent" -> 1; "off" -> 2; else -> 0 })
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            val app = requireActivity().application as NodoCivicoApp
            lifecycleScope.launch {
                val theme  = when (binding.spinnerTheme.selectedItemPosition) { 1 -> "light"; 2 -> "dark"; else -> "system" }
                val notifs = when (binding.spinnerNotifications.selectedItemPosition) { 1 -> "silent"; 2 -> "off"; else -> "active" }
                app.userPreferences.saveTheme(theme)
                app.userPreferences.saveNotifications(notifs)
                binding.root.showSnackbarSuccess("Preferencias guardadas")
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
