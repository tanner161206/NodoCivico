package com.nodocivico.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nodocivico.app.data.repository.AuthRepository
import com.nodocivico.app.domain.model.UiState
import com.nodocivico.app.domain.model.User
import com.nodocivico.app.domain.model.Result
import com.nodocivico.app.utils.UserPreferences
import com.nodocivico.app.utils.isValidEmail
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _loginState = MutableLiveData<UiState<User>>()
    val loginState: LiveData<UiState<User>> = _loginState

    private val _registerState = MutableLiveData<UiState<User>>()
    val registerState: LiveData<UiState<User>> = _registerState

    fun login(email: String, password: String) {
        if (email.isBlank() || !email.isValidEmail()) {
            _loginState.value = UiState.Error("Email inválido")
            return
        }
        if (password.isBlank()) {
            _loginState.value = UiState.Error("La contraseña no puede estar vacía")
            return
        }

        _loginState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    userPreferences.saveSession(
                        token  = result.data.token,
                        userId = result.data.id,
                        name   = result.data.name,
                        email  = result.data.email,
                        zone   = result.data.zone
                    )
                    _loginState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _loginState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, zone: String) {
        if (name.isBlank() || name.length < 2) {
            _registerState.value = UiState.Error("El nombre debe tener al menos 2 caracteres")
            return
        }
        if (!email.isValidEmail()) {
            _registerState.value = UiState.Error("Email inválido")
            return
        }
        if (password.isBlank()) {
            _registerState.value = UiState.Error("La contraseña no puede estar vacía")
            return
        }

        _registerState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.register(name, email, password, zone)) {
                is Result.Success -> {
                    userPreferences.saveSession(
                        token  = result.data.token,
                        userId = result.data.id,
                        name   = result.data.name,
                        email  = result.data.email,
                        zone   = result.data.zone
                    )
                    _registerState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _registerState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            userPreferences.clearSession()
        }
    }
}