package com.nodocivico.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.R
import com.nodocivico.app.databinding.FragmentProfileBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val app = requireActivity().application as NodoCivicoApp
        lifecycleScope.launch {
            binding.tvName.text  = app.userPreferences.userName.first().ifBlank { "Ciudadano" }
            binding.tvEmail.text = app.userPreferences.userEmail.first().ifBlank { "—" }
            binding.tvZone.text  = app.userPreferences.userZone.first().ifBlank { "Sin zona asignada" }
        }
    }

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener { findNavController().navigate(R.id.settingsFragment) }
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_logout_title)
                .setMessage(R.string.confirm_logout_msg)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_login) { _, _ -> doLogout() }
                .show()
        }
    }

    private fun doLogout() {
        val app = requireActivity().application as NodoCivicoApp
        lifecycleScope.launch {
            app.userPreferences.clearSession()
            app.authRepository.logout()
            findNavController().navigate(R.id.loginFragment, null,
                androidx.navigation.NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build())
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
