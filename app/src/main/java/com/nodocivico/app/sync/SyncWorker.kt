package com.nodocivico.app.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.nodocivico.app.NodoCivicoApp
import com.nodocivico.app.utils.NetworkUtils
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * SyncWorker — ejecuta sincronización en segundo plano con WorkManager.
 * Se programa periódicamente y también se dispara cuando hay conexión.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME_PERIODIC = "NodoCivico_SyncPeriodic"
        const val WORK_NAME_ONE_SHOT = "NodoCivico_SyncOneShot"
        const val TAG = "SyncWorker"

        /** Programa sync periódico cada 15 minutos cuando hay red */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "Sync periódico programado")
        }

        /** Dispara sync inmediato una sola vez */
        fun scheduleOneShot(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_SHOT,
                ExistingWorkPolicy.REPLACE,
                request
            )
            Log.d(TAG, "Sync inmediato programado")
        }
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as NodoCivicoApp

        // Verificar conectividad
        if (!NetworkUtils.isConnected(applicationContext)) {
            Log.d(TAG, "Sin conexión, reintentando más tarde")
            return Result.retry()
        }

        return try {
            val token = app.userPreferences.authToken.first()
            if (token.isBlank()) {
                Log.d(TAG, "Sin token, omitiendo sync")
                return Result.success()
            }

            val result = app.reportRepository.syncPendingReports(token)
            when (result) {
                is com.nodocivico.app.domain.model.Result.Success -> {
                    Log.d(TAG, "Sync exitoso: ${result.data} reportes sincronizados")
                    Result.success()
                }
                is com.nodocivico.app.domain.model.Result.Error -> {
                    Log.e(TAG, "Error sync: ${result.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en sync: ${e.message}")
            Result.retry()
        }
    }
}
