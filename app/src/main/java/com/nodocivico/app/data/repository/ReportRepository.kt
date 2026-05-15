package com.nodocivico.app.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.nodocivico.app.data.local.dao.ReportDao
import com.nodocivico.app.data.local.dao.SyncEventDao
import com.nodocivico.app.data.local.entity.ReportEntity
import com.nodocivico.app.data.local.entity.SyncEventEntity
import com.nodocivico.app.data.remote.NodoCivicoApiService
import com.nodocivico.app.data.remote.ReportDto
import com.nodocivico.app.domain.model.Report
import com.nodocivico.app.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepository(
    private val reportDao: ReportDao,
    private val syncEventDao: SyncEventDao,
    private val apiService: NodoCivicoApiService
) {

    // Observa todos los reportes como LiveData (Room los emite en tiempo real)
    val allReports: LiveData<List<Report>> =
        reportDao.getAllReports().map { list -> list.map { it.toDomain() } }

    val unsyncedCount: LiveData<Int> = reportDao.getUnsyncedCount()
    val totalCount: LiveData<Int> = reportDao.getTotalCount()

    // ---------------------------------------------------------------------------
    // CRUD local (offline-first)
    // ---------------------------------------------------------------------------

    suspend fun getReportById(id: Long): Report? = withContext(Dispatchers.IO) {
        reportDao.getReportById(id)?.toDomain()
    }

    suspend fun createReport(report: Report): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val entity = ReportEntity.fromDomain(report)
            val newId = reportDao.insertReport(entity)
            // Encola el evento para sincronizar cuando haya conexión
            syncEventDao.insertEvent(
                SyncEventEntity(entityType = "report", entityId = newId, actionType = "CREATE")
            )
            Result.Success(newId)
        } catch (e: Exception) {
            Result.Error(e, "Error al guardar el reporte localmente")
        }
    }

    suspend fun updateReport(report: Report): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            reportDao.updateReport(ReportEntity.fromDomain(report))
            syncEventDao.insertEvent(
                SyncEventEntity(entityType = "report", entityId = report.id, actionType = "UPDATE")
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Error al actualizar el reporte")
        }
    }

    suspend fun deleteReport(report: Report): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            reportDao.deleteReport(ReportEntity.fromDomain(report))
            syncEventDao.insertEvent(
                SyncEventEntity(entityType = "report", entityId = report.id, actionType = "DELETE")
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Error al eliminar el reporte")
        }
    }

    // ---------------------------------------------------------------------------
    // Sincronización con API REST
    // ---------------------------------------------------------------------------

    /**
     * Sube todos los reportes no sincronizados al servidor.
     * @return número de reportes sincronizados exitosamente.
     */
    suspend fun syncPendingReports(token: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val unsynced = reportDao.getUnsyncedReports()
            var syncedCount = 0

            unsynced.forEach { entity ->
                val dto = entity.toDto()
                val response = if (entity.id == 0L) {
                    apiService.createReport("Bearer $token", dto)
                } else {
                    apiService.updateReport("Bearer $token", entity.id, dto)
                }

                if (response.isSuccessful) {
                    reportDao.markAsSynced(entity.id)
                    syncedCount++
                }
            }

            // Descarga reportes del servidor
            val remoteResponse = apiService.getReports("Bearer $token")
            if (remoteResponse.isSuccessful) {
                remoteResponse.body()?.forEach { dto ->
                    reportDao.insertReport(dto.toEntity())
                }
            }

            Result.Success(syncedCount)
        } catch (e: Exception) {
            Result.Error(e, "Error durante la sincronización: ${e.message}")
        }
    }

    // ---------------------------------------------------------------------------
    // Extensiones de conversión DTO <-> Entity
    // ---------------------------------------------------------------------------

    private fun ReportEntity.toDto() = ReportDto(
        id = id,
        title = title,
        description = description,
        categoryId = categoryId,
        statusId = statusId,
        priority = priority,
        location = location,
        date = date,
        imageUri = imageUri,
        createdBy = createdBy
    )

    private fun ReportDto.toEntity() = ReportEntity(
        id = id,
        title = title,
        description = description,
        categoryId = categoryId,
        statusId = statusId,
        priority = priority,
        location = location,
        date = date,
        imageUri = imageUri,
        synced = true,
        createdBy = createdBy
    )
}
