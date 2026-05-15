package com.nodocivico.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.R
import com.nodocivico.app.databinding.FragmentLoginBinding
import com.nodocivico.app.databinding.FragmentRegisterBinding
import com.nodocivico.app.domain.model.UiState
import com.nodocivico.app.utils.gone
import com.nodocivico.app.utils.visible
import com.nodocivico.app.viewmodel.AuthViewModel
import com.nodocivico.app.viewmodel.ViewModelFactory

// ---------------------------------------------------------------------------
// LoginFragment
// ---------------------------------------------------------------------------
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(authRepository = app.authRepository, userPreferences = app.userPreferences)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnLogin.isEnabled = false
                    binding.tvError.gone()
                }
                is UiState.Success -> {
                    binding.progressBar.gone()
                    binding.btnLogin.isEnabled = true
                    findNavController().navigate(R.id.action_login_to_home)
                }
                is UiState.Error -> {
                    binding.progressBar.gone()
                    binding.btnLogin.isEnabled = true
                    binding.tvError.text = state.message
                    binding.tvError.visible()
                }
                else -> {
                    binding.progressBar.gone()
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ---------------------------------------------------------------------------
// RegisterFragment
// ---------------------------------------------------------------------------
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as NodoCivicoApp
        ViewModelFactory(authRepository = app.authRepository, userPreferences = app.userPreferences)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnRegister.isEnabled = false
                    binding.tvError.gone()
                }
                is UiState.Success -> {
                    binding.progressBar.gone()
                    findNavController().navigate(R.id.action_register_to_home)
                }
                is UiState.Error -> {
                    binding.progressBar.gone()
                    binding.btnRegister.isEnabled = true
                    binding.tvError.text = state.message
                    binding.tvError.visible()
                }
                else -> {
                    binding.progressBar.gone()
                    binding.btnRegister.isEnabled = true
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name     = binding.etName.text.toString().trim()
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val zone     = binding.etZone.text.toString().trim()
            viewModel.register(name, email, password, zone)
        }

        binding.tvGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
