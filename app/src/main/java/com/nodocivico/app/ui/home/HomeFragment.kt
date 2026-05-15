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
import com.nodocivico.app.receivers.ConnectivityReceiver
import com.nodocivico.app.utils.NetworkUtils
import com.nodocivico.app.utils.gone
import com.nodocivico.app.utils.visible
import com.nodocivico.app.viewmodel.HomeViewModel
import com.nodocivico.app.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(reportRepository = app.reportRepository)
    }

    private lateinit var recentAdapter: ReportAdapter

    // BroadcastReceiver para detectar cambios de conectividad
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
            onItemClick = { report ->
                val action = HomeFragmentDirections.actionHomeToReportList()
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
        // Total de reportes
        viewModel.totalReports.observe(viewLifecycleOwner) { count ->
            binding.tvTotalCount.text = count.toString()
        }

        // Reportes sin sincronizar
        viewModel.unsyncedCount.observe(viewLifecycleOwner) { count ->
            binding.tvUnsyncedCount.text = count.toString()
        }

        // Lista de reportes recientes (máx 5)
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
        val isConnected = NetworkUtils.isConnected(requireContext())
        updateConnectivityUI(isConnected)

        // Escucha cambios en tiempo real
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
            if (name.isNotBlank()) {
                binding.tvUserName.text = name
            }
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
