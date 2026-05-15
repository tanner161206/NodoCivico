package com.nodocivico.app

import android.app.Application
import com.nodocivico.app.data.local.NodoCivicoDatabase
import com.nodocivico.app.data.remote.RetrofitClient
import com.nodocivico.app.data.repository.AuthRepository
import com.nodocivico.app.data.repository.ReportRepository
import com.nodocivico.app.utils.UserPreferences

class NodoCivicoApp : Application() {

    val database: NodoCivicoDatabase by lazy {
        NodoCivicoDatabase.getDatabase(this)
    }

    val userPreferences: UserPreferences by lazy {
        UserPreferences(this)
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepository(
            reportDao    = database.reportDao(),
            syncEventDao = database.syncEventDao(),
            apiService   = RetrofitClient.apiService
        )
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            userDao    = database.userDao(),
            apiService = RetrofitClient.apiService
        )
    }
}
