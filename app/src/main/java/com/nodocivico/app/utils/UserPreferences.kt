package com.nodocivico.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nodo_civico_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_TOKEN        = stringPreferencesKey("auth_token")
        val KEY_USER_ID      = longPreferencesKey("user_id")
        val KEY_USER_NAME    = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL   = stringPreferencesKey("user_email")
        val KEY_USER_ZONE    = stringPreferencesKey("user_zone")
        val KEY_THEME        = stringPreferencesKey("theme")           // "system" | "light" | "dark"
        val KEY_NOTIFICATIONS = stringPreferencesKey("notifications")  // "active" | "silent" | "off"
        val KEY_FILTER_STATUS = stringPreferencesKey("filter_status")  // "" | "1" | "2" | "3"
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    // ----- Lecturas -----

    val authToken: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_TOKEN] ?: "" }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_IS_LOGGED_IN] ?: false }

    val userName: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_USER_NAME] ?: "" }

    val userEmail: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_USER_EMAIL] ?: "" }

    val userZone: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_USER_ZONE] ?: "" }

    val theme: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_THEME] ?: "system" }

    val notifications: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_NOTIFICATIONS] ?: "active" }

    val filterStatus: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_FILTER_STATUS] ?: "" }

    // ----- Escrituras -----

    suspend fun saveSession(token: String, userId: Long, name: String, email: String, zone: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]       = token
            prefs[KEY_USER_ID]     = userId
            prefs[KEY_USER_NAME]   = name
            prefs[KEY_USER_EMAIL]  = email
            prefs[KEY_USER_ZONE]   = zone
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
            prefs[KEY_IS_LOGGED_IN] = false
        }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun saveNotifications(value: String) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS] = value }
    }

    suspend fun saveFilterStatus(value: String) {
        context.dataStore.edit { it[KEY_FILTER_STATUS] = value }
    }
}
