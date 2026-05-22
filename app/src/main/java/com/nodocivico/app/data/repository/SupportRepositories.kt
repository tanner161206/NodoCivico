package com.nodocivico.app.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.nodocivico.app.data.local.dao.CategoryDao
import com.nodocivico.app.data.local.dao.FollowUpDao
import com.nodocivico.app.data.local.dao.ReminderDao
import com.nodocivico.app.data.local.entity.CategoryEntity
import com.nodocivico.app.data.local.entity.FollowUpEntity
import com.nodocivico.app.data.local.entity.ReminderEntity
import com.nodocivico.app.data.remote.NodoCivicoApiService
import com.nodocivico.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────────────────────
// CategoryRepository
// ─────────────────────────────────────────────────────────────────────────────
class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val apiService: NodoCivicoApiService
) {
    val allCategories: LiveData<List<Category>> =
        categoryDao.getAllCategories().map { it.map { e -> e.toDomain() } }

    suspend fun getAllList(): List<Category> = withContext(Dispatchers.IO) {
        categoryDao.getAllCategoriesList().map { it.toDomain() }
    }

    suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val r = apiService.getCategories()
            if (r.isSuccessful) {
                r.body()?.let { dtos ->
                    categoryDao.insertAll(dtos.map { CategoryEntity(it.id, it.name, it.icon) })
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "No se pudieron actualizar las categorías")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ReminderRepository
// ─────────────────────────────────────────────────────────────────────────────
class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getForReport(reportId: Long): LiveData<List<Reminder>> =
        reminderDao.getRemindersForReport(reportId).map { it.map { e -> e.toDomain() } }

    suspend fun getUpcoming(): List<Reminder> = withContext(Dispatchers.IO) {
        reminderDao.getUpcomingReminders().map { it.toDomain() }
    }

    suspend fun add(reminder: Reminder): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = reminderDao.insertReminder(ReminderEntity.fromDomain(reminder))
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e, "Error al guardar el recordatorio")
        }
    }

    suspend fun delete(reminder: Reminder): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            reminderDao.deleteReminder(
                ReminderEntity(reminder.id, reminder.reportId, reminder.reminderDate, reminder.message)
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Error al eliminar el recordatorio")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FollowUpRepository
// ─────────────────────────────────────────────────────────────────────────────
class FollowUpRepository(private val followUpDao: FollowUpDao) {

    fun getForReport(reportId: Long): LiveData<List<FollowUp>> =
        followUpDao.getFollowUpsForReport(reportId).map { it.map { e -> e.toDomain() } }

    suspend fun add(reportId: Long, comment: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (comment.isBlank())
                return@withContext Result.Error(Exception("Vacío"), "El comentario no puede estar vacío")
            val id = followUpDao.insertFollowUp(
                FollowUpEntity(reportId = reportId, comment = comment.trim())
            )
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e, "Error al guardar el seguimiento")
        }
    }
}
