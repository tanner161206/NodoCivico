package com.nodocivico.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nodocivico.app.data.repository.AuthRepository
import com.nodocivico.app.data.repository.ReportRepository
import com.nodocivico.app.utils.UserPreferences

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val reportRepository: ReportRepository? = null,
    private val authRepository: AuthRepository? = null,
    private val userPreferences: UserPreferences? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ReportViewModel::class.java) ->
                ReportViewModel(reportRepository!!) as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(authRepository!!, userPreferences!!) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(reportRepository!!) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
