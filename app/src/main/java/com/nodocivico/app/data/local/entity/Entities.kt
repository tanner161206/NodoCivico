package com.nodocivico.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nodocivico.app.domain.model.*

// ---------------------------------------------------------------------------
// ReportEntity
// ---------------------------------------------------------------------------
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val categoryId: Int,
    val statusId: Int = 1,
    val priority: String,           // "Baja" | "Media" | "Alta"
    val location: String,
    val date: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val synced: Boolean = false,
    val createdBy: String = ""
) {
    fun toDomain() = Report(
        id = id,
        title = title,
        description = description,
        categoryId = categoryId,
        statusId = statusId,
        priority = Priority.fromLabel(priority),
        location = location,
        date = date,
        imageUri = imageUri,
        synced = synced,
        createdBy = createdBy
    )

    companion object {
        fun fromDomain(r: Report) = ReportEntity(
            id = r.id,
            title = r.title,
            description = r.description,
            categoryId = r.categoryId,
            statusId = r.statusId,
            priority = r.priority.label,
            location = r.location,
            date = r.date,
            imageUri = r.imageUri,
            synced = r.synced,
            createdBy = r.createdBy
        )
    }
}

// ---------------------------------------------------------------------------
// CategoryEntity
// ---------------------------------------------------------------------------
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val icon: String
) {
    fun toDomain() = Category(id, name, icon)
}

// ---------------------------------------------------------------------------
// ReportStatusEntity
// ---------------------------------------------------------------------------
@Entity(tableName = "report_status")
data class ReportStatusEntity(
    @PrimaryKey val id: Int,
    val name: String
) {
    fun toDomain() = ReportStatus(id, name)
}

// ---------------------------------------------------------------------------
// UserEntity
// ---------------------------------------------------------------------------
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val zone: String = "",
    val role: String = "citizen",
    val token: String = ""
) {
    fun toDomain() = User(id, name, email, zone, role, token)

    companion object {
        fun fromDomain(u: User) = UserEntity(u.id, u.name, u.email, u.zone, u.role, u.token)
    }
}

// ---------------------------------------------------------------------------
// ReminderEntity
// ---------------------------------------------------------------------------
@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = ReportEntity::class,
        parentColumns = ["id"],
        childColumns = ["reportId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("reportId")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val reminderDate: Long,
    val message: String
) {
    fun toDomain() = Reminder(id, reportId, reminderDate, message)

    companion object {
        fun fromDomain(r: Reminder) = ReminderEntity(0, r.reportId, r.reminderDate, r.message)
    }
}

// ---------------------------------------------------------------------------
// FollowUpEntity
// ---------------------------------------------------------------------------
@Entity(
    tableName = "follow_ups",
    foreignKeys = [ForeignKey(
        entity = ReportEntity::class,
        parentColumns = ["id"],
        childColumns = ["reportId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("reportId")]
)
data class FollowUpEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = FollowUp(id, reportId, comment, createdAt)
}

// ---------------------------------------------------------------------------
// SyncEventEntity — cola de sincronización pendiente
// ---------------------------------------------------------------------------
@Entity(tableName = "sync_events")
data class SyncEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,         // "report" | "reminder"
    val entityId: Long,
    val actionType: String,         // "CREATE" | "UPDATE" | "DELETE"
    val syncDate: Long = System.currentTimeMillis(),
    val status: String = "PENDING"  // "PENDING" | "DONE" | "FAILED"
)
