package com.nodocivico.app.domain.usecase

import com.nodocivico.app.data.repository.*
import com.nodocivico.app.domain.model.*

// ─────────────────────────────────────────────────────────────────────────────
// Validación de campos de reporte
// ─────────────────────────────────────────────────────────────────────────────
class ValidateReportUseCase {
    sealed class Out {
        object Valid : Out()
        data class Invalid(val field: String, val message: String) : Out()
    }

    operator fun invoke(title: String, description: String, location: String, priority: String): Out =
        when {
            title.isBlank()                           -> Out.Invalid("title",       "El título no puede estar vacío")
            title.trim().length < 5                   -> Out.Invalid("title",       "El título debe tener al menos 5 caracteres")
            title.trim().length > 120                 -> Out.Invalid("title",       "El título no puede superar 120 caracteres")
            description.isBlank()                    -> Out.Invalid("description", "La descripción no puede estar vacía")
            description.trim().length < 10           -> Out.Invalid("description", "La descripción debe tener al menos 10 caracteres")
            description.trim().length > 1000         -> Out.Invalid("description", "La descripción no puede superar 1000 caracteres")
            location.isBlank()                       -> Out.Invalid("location",    "La ubicación no puede estar vacía")
            priority !in listOf("Baja","Media","Alta") -> Out.Invalid("priority",  "Prioridad inválida")
            else                                     -> Out.Valid
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// CRUD de reportes
// ─────────────────────────────────────────────────────────────────────────────
class CreateReportUseCase(
    private val repo: ReportRepository,
    private val validate: ValidateReportUseCase = ValidateReportUseCase()
) {
    suspend operator fun invoke(
        title: String, description: String, categoryId: Int,
        priority: Priority, location: String,
        imageUri: String? = null, createdBy: String = ""
    ): Result<Long> {
        val v = validate(title, description, location, priority.label)
        if (v is ValidateReportUseCase.Out.Invalid)
            return Result.Error(Exception(v.message), v.message)
        return repo.createReport(
            Report(
                title = title.trim(), description = description.trim(),
                categoryId = categoryId, priority = priority,
                location = location.trim(), imageUri = imageUri,
                createdBy = createdBy, synced = false
            )
        )
    }
}

class UpdateReportUseCase(
    private val repo: ReportRepository,
    private val validate: ValidateReportUseCase = ValidateReportUseCase()
) {
    suspend operator fun invoke(
        original: Report, title: String, description: String,
        location: String, priority: Priority, statusId: Int
    ): Result<Unit> {
        val v = validate(title, description, location, priority.label)
        if (v is ValidateReportUseCase.Out.Invalid)
            return Result.Error(Exception(v.message), v.message)
        return repo.updateReport(
            original.copy(
                title = title.trim(), description = description.trim(),
                location = location.trim(), priority = priority,
                statusId = statusId, synced = false
            )
        )
    }
}

class DeleteReportUseCase(private val repo: ReportRepository) {
    suspend operator fun invoke(report: Report): Result<Unit> = repo.deleteReport(report)
}

class GetReportByIdUseCase(private val repo: ReportRepository) {
    suspend operator fun invoke(id: Long): Report? = repo.getReportById(id)
}

// ─────────────────────────────────────────────────────────────────────────────
// Recordatorios
// ─────────────────────────────────────────────────────────────────────────────
class AddReminderUseCase(private val repo: ReminderRepository) {
    suspend operator fun invoke(reportId: Long, dateMillis: Long, message: String): Result<Long> {
        if (message.isBlank())
            return Result.Error(Exception("Vacío"), "El mensaje no puede estar vacío")
        if (dateMillis <= System.currentTimeMillis())
            return Result.Error(Exception("Fecha pasada"), "La fecha debe ser futura")
        return repo.add(Reminder(reportId = reportId, reminderDate = dateMillis, message = message.trim()))
    }
}
