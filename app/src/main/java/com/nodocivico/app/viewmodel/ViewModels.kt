package com.nodocivico.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nodocivico.app.data.repository.ReportRepository
import com.nodocivico.app.domain.model.*
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// HomeViewModel
// ---------------------------------------------------------------------------
class HomeViewModel(private val reportRepository: ReportRepository) : ViewModel() {
    val totalReports: LiveData<Int>      = reportRepository.totalCount
    val unsyncedCount: LiveData<Int>     = reportRepository.unsyncedCount
    val recentReports: LiveData<List<Report>> = reportRepository.allReports
}

// ---------------------------------------------------------------------------
// ReportViewModel
// ---------------------------------------------------------------------------
class ReportViewModel(
    private val reportRepository: ReportRepository
) : ViewModel() {

    val reports: LiveData<List<Report>>  = reportRepository.allReports
    val unsyncedCount: LiveData<Int>     = reportRepository.unsyncedCount

    private val _operationState = MutableLiveData<UiState<Any>>()
    val operationState: LiveData<UiState<Any>> = _operationState

    private val _selectedReport = MutableLiveData<Report?>()
    val selectedReport: LiveData<Report?> = _selectedReport

    private val _syncState = MutableLiveData<UiState<Int>>()
    val syncState: LiveData<UiState<Int>> = _syncState

    fun createReport(
        title: String, description: String, categoryId: Int,
        priority: Priority, location: String,
        imageUri: String? = null, createdBy: String = ""
    ) {
        if (!validateFields(title, description, location)) return
        _operationState.value = UiState.Loading
        viewModelScope.launch {
            val report = Report(
                title = title.trim(), description = description.trim(),
                categoryId = categoryId, priority = priority,
                location = location.trim(), imageUri = imageUri, createdBy = createdBy
            )
            when (val r = reportRepository.createReport(report)) {
                is Result.Success -> _operationState.value = UiState.Success(r.data)
                is Result.Error   -> _operationState.value = UiState.Error(r.message)
            }
        }
    }

    fun updateReport(
        report: Report, title: String, description: String,
        location: String, priority: Priority, statusId: Int
    ) {
        if (!validateFields(title, description, location)) return
        _operationState.value = UiState.Loading
        viewModelScope.launch {
            val updated = report.copy(
                title = title.trim(), description = description.trim(),
                location = location.trim(), priority = priority,
                statusId = statusId, synced = false
            )
            when (val r = reportRepository.updateReport(updated)) {
                is Result.Success -> _operationState.value = UiState.Success(Unit)
                is Result.Error   -> _operationState.value = UiState.Error(r.message)
            }
        }
    }

    fun deleteReport(report: Report) {
        _operationState.value = UiState.Loading
        viewModelScope.launch {
            when (val r = reportRepository.deleteReport(report)) {
                is Result.Success -> _operationState.value = UiState.Success(Unit)
                is Result.Error   -> _operationState.value = UiState.Error(r.message)
            }
        }
    }

    fun loadReportById(id: Long) {
        viewModelScope.launch { _selectedReport.value = reportRepository.getReportById(id) }
    }

    fun syncReports(token: String) {
        _syncState.value = UiState.Loading
        viewModelScope.launch {
            when (val r = reportRepository.syncPendingReports(token)) {
                is Result.Success -> _syncState.value = UiState.Success(r.data)
                is Result.Error   -> _syncState.value = UiState.Error(r.message)
            }
        }
    }

    private fun validateFields(title: String, description: String, location: String): Boolean {
        return when {
            title.isBlank() -> {
                _operationState.value = UiState.Error("El título no puede estar vacío"); false
            }
            title.length < 5 -> {
                _operationState.value = UiState.Error("El título debe tener al menos 5 caracteres"); false
            }
            description.isBlank() -> {
                _operationState.value = UiState.Error("La descripción no puede estar vacía"); false
            }
            description.length < 10 -> {
                _operationState.value = UiState.Error("La descripción debe tener al menos 10 caracteres"); false
            }
            location.isBlank() -> {
                _operationState.value = UiState.Error("La ubicación no puede estar vacía"); false
            }
            else -> true
        }
    }

    fun clearOperationState() { _operationState.value = null }
}
