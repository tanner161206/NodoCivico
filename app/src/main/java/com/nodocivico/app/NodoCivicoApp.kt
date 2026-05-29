package com.nodocivico.app

import android.app.Application
import com.nodocivico.app.data.local.NodoCivicoDatabase
import com.nodocivico.app.data.remote.RetrofitClient
import com.nodocivico.app.data.repository.*
import com.nodocivico.app.sync.SyncWorker
import com.nodocivico.app.utils.UserPreferences

class NodoCivicoApp : Application() {

    val database: NodoCivicoDatabase by lazy { NodoCivicoDatabase.getDatabase(this) }
    val userPreferences: UserPreferences by lazy { UserPreferences(this) }

    val reportRepository: ReportRepository by lazy {
        ReportRepository(database.reportDao(), database.syncEventDao(), RetrofitClient.apiService)
    }
    val authRepository: AuthRepository by lazy {
        AuthRepository(database.userDao(), RetrofitClient.apiService)
    }
    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao(), RetrofitClient.apiService)
    }
    val reminderRepository: ReminderRepository by lazy {
        ReminderRepository(database.reminderDao())
    }
    val followUpRepository: FollowUpRepository by lazy {
        FollowUpRepository(database.followUpDao())
    }

    override fun onCreate() {
        super.onCreate()
        SyncWorker.schedulePeriodic(this)
    }
}
