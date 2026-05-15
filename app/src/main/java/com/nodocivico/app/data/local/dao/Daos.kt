package com.nodocivico.app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nodocivico.app.data.local.entity.*

// ---------------------------------------------------------------------------
// ReportDao
// ---------------------------------------------------------------------------
@Dao
interface ReportDao {

    @Query("SELECT * FROM reports ORDER BY date DESC")
    fun getAllReports(): LiveData<List<ReportEntity>>

    @Query("SELECT * FROM reports ORDER BY date DESC")
    suspend fun getAllReportsList(): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReportById(id: Long): ReportEntity?

    @Query("SELECT * FROM reports WHERE synced = 0 ORDER BY date ASC")
    suspend fun getUnsyncedReports(): List<ReportEntity>

    @Query("SELECT COUNT(*) FROM reports WHERE synced = 0")
    fun getUnsyncedCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM reports")
    fun getTotalCount(): LiveData<Int>

    @Query("SELECT * FROM reports WHERE statusId = :statusId ORDER BY date DESC")
    fun getReportsByStatus(statusId: Int): LiveData<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getReportsByCategory(categoryId: Int): LiveData<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Delete
    suspend fun deleteReport(report: ReportEntity)

    @Query("UPDATE reports SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE reports SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAllSynced(ids: List<Long>)
}

// ---------------------------------------------------------------------------
// CategoryDao
// ---------------------------------------------------------------------------
@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories")
    fun getAllCategories(): LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesList(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)
}

// ---------------------------------------------------------------------------
// ReportStatusDao
// ---------------------------------------------------------------------------
@Dao
interface ReportStatusDao {

    @Query("SELECT * FROM report_status")
    fun getAllStatuses(): LiveData<List<ReportStatusEntity>>

    @Query("SELECT * FROM report_status")
    suspend fun getAllStatusesList(): List<ReportStatusEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statuses: List<ReportStatusEntity>)
}

// ---------------------------------------------------------------------------
// UserDao
// ---------------------------------------------------------------------------
@Dao
interface UserDao {

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

// ---------------------------------------------------------------------------
// ReminderDao
// ---------------------------------------------------------------------------
@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE reportId = :reportId")
    fun getRemindersForReport(reportId: Long): LiveData<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE reminderDate >= :from ORDER BY reminderDate ASC")
    suspend fun getUpcomingReminders(from: Long = System.currentTimeMillis()): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE reportId = :reportId")
    suspend fun deleteRemindersForReport(reportId: Long)
}

// ---------------------------------------------------------------------------
// SyncEventDao
// ---------------------------------------------------------------------------
@Dao
interface SyncEventDao {

    @Query("SELECT * FROM sync_events WHERE status = 'PENDING' ORDER BY syncDate ASC")
    suspend fun getPendingEvents(): List<SyncEventEntity>

    @Query("SELECT COUNT(*) FROM sync_events WHERE status = 'PENDING'")
    fun getPendingCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SyncEventEntity): Long

    @Query("UPDATE sync_events SET status = :status WHERE id = :id")
    suspend fun updateEventStatus(id: Long, status: String)

    @Query("DELETE FROM sync_events WHERE status = 'DONE'")
    suspend fun clearDoneEvents()
}

// ---------------------------------------------------------------------------
// FollowUpDao
// ---------------------------------------------------------------------------
@Dao
interface FollowUpDao {

    @Query("SELECT * FROM follow_ups WHERE reportId = :reportId ORDER BY createdAt DESC")
    fun getFollowUpsForReport(reportId: Long): LiveData<List<FollowUpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUpEntity): Long
}
