package com.nodocivico.app.domain.model

/**
 * Modelo de dominio para un reporte ciudadano.
 * Independiente de Room o Retrofit.
 */
data class Report(
    val id: Long = 0,
    val title: String,
    val description: String,
    val categoryId: Int,
    val statusId: Int = 1,
    val priority: Priority,
    val location: String,
    val date: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val synced: Boolean = false,
    val createdBy: String = ""
)

enum class Priority(val label: String) {
    LOW("Baja"),
    MEDIUM("Media"),
    HIGH("Alta");

    companion object {
        fun fromLabel(label: String): Priority =
            entries.firstOrNull { it.label == label } ?: MEDIUM
    }
}

data class Category(
    val id: Int,
    val name: String,
    val icon: String
)

data class ReportStatus(
    val id: Int,
    val name: String
)

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val zone: String = "",
    val role: String = "citizen",
    val token: String = ""
)

data class Reminder(
    val id: Long = 0,
    val reportId: Long,
    val reminderDate: Long,
    val message: String
)

data class FollowUp(
    val id: Long = 0,
    val reportId: Long,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

/** Estado de la UI para cualquier operación asíncrona */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

/** Resultado de operaciones del repositorio */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String = exception.message ?: "Error desconocido") : Result<Nothing>()
}
