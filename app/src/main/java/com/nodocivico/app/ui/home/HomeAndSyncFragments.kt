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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.R
import com.nodocivico.app.adapters.ReportAdapter
import com.nodocivico.app.databinding.FragmentHomeBinding
import com.nodocivico.app.databinding.FragmentMapZoneBinding
import com.nodocivico.app.databinding.FragmentSyncStatusBinding
import com.nodocivico.app.domain.model.UiState
import com.nodocivico.app.receivers.ConnectivityReceiver
import com.nodocivico.app.utils.NetworkUtils
import com.nodocivico.app.utils.gone
import com.nodocivico.app.utils.showSnackbarError
import com.nodocivico.app.utils.showSnackbarSuccess
import com.nodocivico.app.utils.visible
import com.nodocivico.app.viewmodel.HomeViewModel
import com.nodocivico.app.viewmodel.ReportViewModel
import com.nodocivico.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ─── HomeFragment ─────────────────────────────────────────────────────────────
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reportRepository = app.reportRepository)
    }

    private lateinit var recentAdapter: ReportAdapter
    private val connectivityReceiver = ConnectivityReceiver()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        updateConnectivityStatus()
        loadUserName()
    }

    private fun setupRecyclerView() {
        recentAdapter = ReportAdapter(
            onItemClick = { _ ->
                findNavController().navigate(R.id.action_home_to_reportList)
            }
        )
        binding.rvRecentReports.apply {
            adapter = recentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        viewModel.totalReports.observe(viewLifecycleOwner) { count ->
            binding.tvTotalCount.text = count.toString()
        }
        viewModel.unsyncedCount.observe(viewLifecycleOwner) { count ->
            binding.tvUnsyncedCount.text = count.toString()
        }
        viewModel.recentReports.observe(viewLifecycleOwner) { reports ->
            val recent = reports.take(5)
            if (recent.isEmpty()) {
                binding.layoutEmpty.visible()
                binding.rvRecentReports.gone()
            } else {
                binding.layoutEmpty.gone()
                binding.rvRecentReports.visible()
                recentAdapter.submitList(recent)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnNewReport.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_createReport)
        }
        binding.btnMyReports.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_reportList)
        }
    }

    private fun updateConnectivityStatus() {
        updateConnectivityUI(NetworkUtils.isConnected(requireContext()))
        ConnectivityReceiver.onConnectivityChanged = { connected ->
            requireActivity().runOnUiThread { updateConnectivityUI(connected) }
        }
    }

    private fun updateConnectivityUI(connected: Boolean) {
        if (!isAdded) return
        if (connected) {
            binding.viewConnDot.setBackgroundResource(R.drawable.shape_dot_green)
            binding.tvConnStatus.setText(R.string.status_connected)
        } else {
            binding.viewConnDot.setBackgroundResource(R.drawable.shape_dot_red)
            binding.tvConnStatus.setText(R.string.status_offline)
        }
    }

    private fun loadUserName() {
        lifecycleScope.launch {
            val app = requireActivity().application as NodoCivicoApp
            val name = app.userPreferences.userName.first()
            if (name.isNotBlank()) binding.tvUserName.text = name
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

// ─── SyncStatusFragment ───────────────────────────────────────────────────────
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
                if (connected) triggerAutoSync()
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
                    val count = (state.data as? Int) ?: 0
                    binding.root.showSnackbarSuccess("$count reportes sincronizados ✓")
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
            triggerSync()
        }
    }

    private fun triggerSync() {
        lifecycleScope.launch {
            val app = requireActivity().application as NodoCivicoApp
            val token = app.userPreferences.authToken.first()
            viewModel.syncReports(token)
        }
    }

    private fun triggerAutoSync() {
        lifecycleScope.launch {
            val app = requireActivity().application as NodoCivicoApp
            val pending = app.reportRepository.unsyncedCount.value ?: 0
            if (pending > 0) triggerSync()
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

// ─── MapZoneFragment ──────────────────────────────────────────────────────────
class MapZoneFragment : Fragment() {

    private var _binding: FragmentMapZoneBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapZoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadZoneInfo()
    }

    private fun loadZoneInfo() {
        lifecycleScope.launch {
            val app = requireActivity().application as NodoCivicoApp
            val zone = app.userPreferences.userZone.first()
            binding.tvZoneName.text = zone.ifBlank { "Sin zona asignada" }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}