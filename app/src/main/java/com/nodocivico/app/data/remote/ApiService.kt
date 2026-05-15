package com.nodocivico.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

// ---------------------------------------------------------------------------
// DTOs (Data Transfer Objects) para la API REST
// ---------------------------------------------------------------------------

data class ReportDto(
    @SerializedName("id")          val id: Long = 0,
    @SerializedName("title")       val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("status_id")   val statusId: Int = 1,
    @SerializedName("priority")    val priority: String,
    @SerializedName("location")    val location: String,
    @SerializedName("date")        val date: Long = System.currentTimeMillis(),
    @SerializedName("image_uri")   val imageUri: String? = null,
    @SerializedName("created_by")  val createdBy: String = ""
)

data class CategoryDto(
    @SerializedName("id")   val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String
)

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name")     val name: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("zone")     val zone: String = ""
)

data class AuthResponse(
    @SerializedName("token")   val token: String,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("name")    val name: String,
    @SerializedName("email")   val email: String,
    @SerializedName("zone")    val zone: String = "",
    @SerializedName("role")    val role: String = "citizen"
)

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data")    val data: T?,
    @SerializedName("message") val message: String = ""
)

// ---------------------------------------------------------------------------
// Interfaz de la API
// ---------------------------------------------------------------------------
interface NodoCivicoApiService {

    // --- Autenticación ---
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // --- Reportes ---
    @GET("reports")
    suspend fun getReports(
        @Header("Authorization") token: String
    ): Response<List<ReportDto>>

    @GET("reports/{id}")
    suspend fun getReportById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ReportDto>

    @POST("reports")
    suspend fun createReport(
        @Header("Authorization") token: String,
        @Body report: ReportDto
    ): Response<ReportDto>

    @PUT("reports/{id}")
    suspend fun updateReport(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body report: ReportDto
    ): Response<ReportDto>

    @DELETE("reports/{id}")
    suspend fun deleteReport(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>

    // --- Categorías ---
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
}
