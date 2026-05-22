package com.nodocivico.app.viewmodel

import androidx.lifecycle.*
import com.nodocivico.app.data.repository.*
import com.nodocivico.app.domain.model.*
import com.nodocivico.app.domain.usecase.*
import kotlinx.coroutines.launch

// ─── HomeViewModel ────────────────────────────────────────────────────────────
class HomeViewModel(repo: ReportRepository) : ViewModel() {
    val totalReports: LiveData<Int>           = repo.totalCount
    val unsyncedCount: LiveData<Int>          = repo.unsyncedCount
    val recentReports: LiveData<List<Report>> = repo.allReports
}

// ─── ReportViewModel ─────────────────────────────────────────────────────────
class ReportViewModel(
    private val repo: ReportRepository,
    private val createUC: CreateReportUseCase,
    private val updateUC: UpdateReportUseCase,
    private val deleteUC: DeleteReportUseCase,
    private val getByIdUC: GetReportByIdUseCase
) : ViewModel() {

    val reports: LiveData<List<Report>> = repo.allReports
    val unsyncedCount: LiveData<Int>    = repo.unsyncedCount

    private val _opState   = MutableLiveData<UiState<Any>>()
    val operationState: LiveData<UiState<Any>> = _opState

    private val _selected  = MutableLiveData<Report?>()
    val selectedReport: LiveData<Report?> = _selected

    private val _syncState = MutableLiveData<UiState<Int>>()
    val syncState: LiveData<UiState<Int>> = _syncState

    fun createReport(title: String, description: String, categoryId: Int,
                     priority: Priority, location: String,
                     imageUri: String? = null, createdBy: String = "") {
        _opState.value = UiState.Loading
        viewModelScope.launch {
            _opState.value = when (val r = createUC(title, description, categoryId, priority, location, imageUri, createdBy)) {
                is Result.Success -> UiState.Success(r.data)
                is Result.Error   -> UiState.Error(r.message)
            }
        }
    }

    fun updateReport(report: Report, title: String, description: String,
                     location: String, priority: Priority, statusId: Int) {
        _opState.value = UiState.Loading
        viewModelScope.launch {
            _opState.value = when (val r = updateUC(report, title, description, location, priority, statusId)) {
                is Result.Success -> UiState.Success(Unit)
                is Result.Error   -> UiState.Error(r.message)
            }
        }
    }

    fun deleteReport(report: Report) {
        _opState.value = UiState.Loading
        viewModelScope.launch {
            _opState.value = when (val r = deleteUC(report)) {
                is Result.Success -> UiState.Success(Unit)
                is Result.Error   -> UiState.Error(r.message)
            }
        }
    }

    fun loadReportById(id: Long) {
        viewModelScope.launch { _selected.value = getByIdUC(id) }
    }

    fun syncReports(token: String) {
        _syncState.value = UiState.Loading
        viewModelScope.launch {
            _syncState.value = when (val r = repo.syncPendingReports(token)) {
                is Result.Success -> UiState.Success(r.data)
                is Result.Error   -> UiState.Error(r.message)
            }
        }
    }

    fun clearOperationState() { _opState.value = null }
}

// ─── EditReportViewModel ──────────────────────────────────────────────────────
class EditReportViewModel(
    private val updateUC: UpdateReportUseCase,
    private val getByIdUC: GetReportByIdUseCase
) : ViewModel() {

    private val _report      = MutableLiveData<Report?>()
    val report: LiveData<Report?> = _report

    private val _updateState = MutableLiveData<UiState<Unit>>()
    val updateState: LiveData<UiState<Unit>> = _updateState

    fun loadReport(id: Long) {
        viewModelScope.launch { _report.value = getByIdUC(id) }
    }

    fun updateReport(title: String, description: String,
                     location: String, priority: Priority, statusId: Int) {
        val original = _report.value ?: return
        _updateState.value = UiState.Loading
        viewModelScope.launch {
            _updateState.value = when (val r = updateUC(original, title, description, location, priority, statusId)) {
                is Result.Success -> UiState.Success(Unit)
                is Result.Error   -> UiState.Error(r.message)
            }
        }
    }

    fun clearState() { _updateState.value = null }
}

// ─── DetailViewModel ─────────────────────────────────────────────────────────
class DetailViewModel(
    private val reportRepo: ReportRepository,
    private val followUpRepo: FollowUpRepository,
    private val getByIdUC: GetReportByIdUseCase
) : ViewModel() {

    private val _report    = MutableLiveData<Report?>()
    val report: LiveData<Report?> = _report

    private val _followUps = MutableLiveData<List<FollowUp>>()
    val followUps: LiveData<List<FollowUp>> = _followUps

    private val _opState   = MutableLiveData<UiState<Any>>()
    val operationState: LiveData<UiState<Any>> = _opState

    fun loadReport(id: Long) {
        viewModelScope.launch { _report.value = getByIdUC(id) }
        followUpRepo.getForReport(id).observeForever { _followUps.value = it }
    }

    fun addFollowUp(reportId: Long, comment: String) {
        viewModelScope.launch {
            _opState.value = when (val r = followUpRepo.add(reportId, comment)) {
                is Result.Success -> UiState.Success(Unit)
                is Result.Error   -> UiState.Error(r.message)
            }
        }
    }

    fun deleteReport(report: Report) {
        _opState.value = UiState.Loading
        viewModelScope.launch {
            _opState.value = when (val r = reportRepo.deleteReport(report)) {
                is Result.Success -> UiState.Success("deleted")
                is Result.Error   -> UiState.Error(r.message)
            }
        }
    }

    fun clearState() { _opState.value = null }
}

// ─── CalendarViewModel ───────────────────────────────────────────────────────
class CalendarViewModel(
    private val reminderRepo: ReminderRepository,
    private val addUC: AddReminderUseCase
) : ViewModel() {

    private val _reminders     = MutableLiveData<List<Reminder>>()
    val reminders: LiveData<List<Reminder>> = _reminders

    private val _reminderState = MutableLiveData<UiState<Long>>()
    val reminderState: LiveData<UiState<Long>> = _reminderState

    fun loadUpcoming() {
        viewModelScope.launch { _reminders.value = reminderRepo.getUpcoming() }
    }

    fun addReminder(reportId: Long, dateMillis: Long, message: String) {
        _reminderState.value = UiState.Loading
        viewModelScope.launch {
            _reminderState.value = when (val r = addUC(reportId, dateMillis, message)) {
                is Result.Success -> { loadUpcoming(); UiState.Success(r.data) }
                is Result.Error   -> UiState.Error(r.message)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { reminderRepo.delete(reminder); loadUpcoming() }
    }

    fun clearState() { _reminderState.value = null }
}
