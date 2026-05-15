package com.nodocivico.app.ui.home

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.R
import com.nodocivico.app.databinding.FragmentSyncStatusBinding
import com.nodocivico.app.domain.model.UiState
import com.nodocivico.app.receivers.ConnectivityReceiver
import com.nodocivico.app.utils.*
import com.nodocivico.app.viewmodel.ReportViewModel
import com.nodocivico.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// SyncStatusFragment — estado de sincronización y cola pendiente
// ---------------------------------------------------------------------------
class SyncStatusFragment : Fragment() {

    private var _binding: FragmentSyncStatusBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reportRepository = app.reportRepository)
    }

    private val connectivityReceiver = ConnectivityReceiver()
    private var isOnline = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        updateConnectivity(NetworkUtils.isConnected(requireContext()))

        ConnectivityReceiver.onConnectivityChanged = { connected ->
            requireActivity().runOnUiThread {
                isOnline = connected
                updateConnectivity(connected)
            }
        }
    }

    private fun setupObservers() {
        viewModel.unsyncedCount.observe(viewLifecycleOwner) { count ->
            binding.tvPendingCount.text = if (count == 0)
                getString(R.string.sync_none)
            else
                getString(R.string.sync_pending, count)

            binding.btnSync.isEnabled = count > 0
        }

        viewModel.syncState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSync.isEnabled = false
                }
                is UiState.Success<*> -> {
                    binding.progressBar.gone()
                    val synced = (state.data as? Int) ?: 0
                    binding.root.showSnackbarSuccess("$synced reportes sincronizados ✓")
                }
                is UiState.Error -> {
                    binding.progressBar.gone()
                    binding.btnSync.isEnabled = true
                    binding.root.showSnackbarError(state.message)
                }
                else -> binding.progressBar.gone()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSync.setOnClickListener {
            if (!isOnline) {
                binding.root.showSnackbarError(getString(R.string.error_no_connection))
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val app = requireActivity().application as NodoCivicoApp
                val token = app.userPreferences.authToken.first()
                viewModel.syncReports(token)
            }
        }
    }

    private fun updateConnectivity(connected: Boolean) {
        isOnline = connected
        if (!isAdded) return
        if (connected) {
            binding.viewDot.setBackgroundResource(R.drawable.shape_dot_green)
            binding.tvConnLabel.text = getString(R.string.status_connected)
        } else {
            binding.viewDot.setBackgroundResource(R.drawable.shape_dot_red)
            binding.tvConnLabel.text = getString(R.string.status_offline)
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireActivity().registerReceiver(connectivityReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(connectivityReceiver)
        ConnectivityReceiver.onConnectivityChanged = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ---------------------------------------------------------------------------
// MapZoneFragment — mapa / zona comunitaria (Entregable 3 con Google Maps)
// ---------------------------------------------------------------------------
class MapZoneFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_map_zone, container, false)
}
