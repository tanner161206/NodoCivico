package com.nodocivico.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nodocivico.app.data.repository.*
import com.nodocivico.app.domain.usecase.*
import com.nodocivico.app.utils.UserPreferences

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val reportRepository: ReportRepository?     = null,
    private val authRepository: AuthRepository?         = null,
    private val reminderRepository: ReminderRepository? = null,
    private val followUpRepository: FollowUpRepository? = null,
    private val userPreferences: UserPreferences?       = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(ReportViewModel::class.java) ->
            ReportViewModel(
                reportRepository!!,
                CreateReportUseCase(reportRepository),
                UpdateReportUseCase(reportRepository),
                DeleteReportUseCase(reportRepository),
                GetReportByIdUseCase(reportRepository)
            ) as T
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(authRepository!!, userPreferences!!) as T
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(reportRepository!!) as T
        modelClass.isAssignableFrom(EditReportViewModel::class.java) ->
            EditReportViewModel(
                UpdateReportUseCase(reportRepository!!),
                GetReportByIdUseCase(reportRepository)
            ) as T
        modelClass.isAssignableFrom(DetailViewModel::class.java) ->
            DetailViewModel(
                reportRepository!!,
                followUpRepository!!,
                GetReportByIdUseCase(reportRepository)
            ) as T
        modelClass.isAssignableFrom(CalendarViewModel::class.java) ->
            CalendarViewModel(reminderRepository!!, AddReminderUseCase(reminderRepository)) as T
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
