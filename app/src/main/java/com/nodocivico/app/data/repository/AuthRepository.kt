package com.nodocivico.app.data.repository

import com.nodocivico.app.data.local.dao.UserDao
import com.nodocivico.app.data.local.entity.UserEntity
import com.nodocivico.app.data.remote.LoginRequest
import com.nodocivico.app.data.remote.NodoCivicoApiService
import com.nodocivico.app.data.remote.RegisterRequest
import com.nodocivico.app.domain.model.Result
import com.nodocivico.app.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val userDao: UserDao,
    private val apiService: NodoCivicoApiService
) {

    suspend fun login(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    val user = User(
                        id    = body.userId,
                        name  = body.name,
                        email = body.email,
                        zone  = body.zone,
                        role  = body.role,
                        token = body.token
                    )
                    userDao.deleteAll()
                    userDao.insertUser(UserEntity.fromDomain(user))
                    Result.Success(user)
                } else {
                    Result.Error(Exception("Error ${response.code()}"), "Email o contraseña incorrectos")
                }
            } catch (e: Exception) {
                // Fallback offline
                val localUser = userDao.getCurrentUser()
                if (localUser != null) {
                    Result.Success(localUser.toDomain())
                } else {
                    Result.Error(e, "No se pudo conectar al servidor: ${e.message}")
                }
            }
        }

    suspend fun register(name: String, email: String, password: String, zone: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(RegisterRequest(name, email, password, zone))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    val user = User(
                        id    = body.userId,
                        name  = body.name,
                        email = body.email,
                        zone  = body.zone,
                        role  = body.role,
                        token = body.token
                    )
                    userDao.deleteAll()
                    userDao.insertUser(UserEntity.fromDomain(user))
                    Result.Success(user)
                } else {
                    Result.Error(Exception("Error ${response.code()}"), "No se pudo crear la cuenta")
                }
            } catch (e: Exception) {
                Result.Error(e, "No se pudo conectar al servidor: ${e.message}")
            }
        }

    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        userDao.getCurrentUser()?.toDomain()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.deleteAll()
    }
}