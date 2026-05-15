package com.nodocivico.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nodocivico.app.data.local.dao.*
import com.nodocivico.app.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ReportEntity::class,
        CategoryEntity::class,
        ReportStatusEntity::class,
        UserEntity::class,
        ReminderEntity::class,
        FollowUpEntity::class,
        SyncEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NodoCivicoDatabase : RoomDatabase() {

    abstract fun reportDao(): ReportDao
    abstract fun categoryDao(): CategoryDao
    abstract fun reportStatusDao(): ReportStatusDao
    abstract fun userDao(): UserDao
    abstract fun reminderDao(): ReminderDao
    abstract fun syncEventDao(): SyncEventDao
    abstract fun followUpDao(): FollowUpDao

    companion object {
        @Volatile
        private var INSTANCE: NodoCivicoDatabase? = null

        fun getDatabase(context: Context): NodoCivicoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NodoCivicoDatabase::class.java,
                    "nodo_civico_db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback para insertar datos iniciales (categorías y estados) al crear la DB.
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: NodoCivicoDatabase) {
            // Categorías iniciales
            db.categoryDao().insertAll(
                listOf(
                    CategoryEntity(1, "Alumbrado", "ic_lightbulb"),
                    CategoryEntity(2, "Aseo", "ic_trash"),
                    CategoryEntity(3, "Seguridad", "ic_shield"),
                    CategoryEntity(4, "Servicios públicos", "ic_water"),
                    CategoryEntity(5, "Vías", "ic_road"),
                    CategoryEntity(6, "Espacios comunes", "ic_park"),
                    CategoryEntity(7, "Ruido", "ic_sound"),
                    CategoryEntity(8, "Otro", "ic_flag")
                )
            )

            // Estados de reporte
            db.reportStatusDao().insertAll(
                listOf(
                    ReportStatusEntity(1, "Abierto"),
                    ReportStatusEntity(2, "En proceso"),
                    ReportStatusEntity(3, "Cerrado"),
                    ReportStatusEntity(4, "Rechazado")
                )
            )
        }
    }
}
