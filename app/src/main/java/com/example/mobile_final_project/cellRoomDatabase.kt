package com.example.mobile_final_project

import android.content.Context
import android.provider.SyncStateContract.Helpers.insert
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Cell::class, Rout::class), version = 1, exportSchema = false)
abstract class CellRoomDatabase : RoomDatabase() {

    abstract fun CellDao(): CellDao
    abstract fun RoutDao(): routDao

    private class CellDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    var CellDao = database.CellDao()
                    var RoutDao = database.RoutDao()

                    // Delete all content here.
                    CellDao.deleteAll()
                    RoutDao.deleteAll()

                    var Cell = Cell(1, 1, "1", "1", "1", "1", "1", 51.3890, 35.6892, "LTE", "1 AM")
                    CellDao.insert(Cell)

                    var rout = Rout(1, "1")
                    RoutDao.insert(rout)
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CellRoomDatabase? = null

        fun getDatabase(
            context: Context
        ): CellRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CellRoomDatabase::class.java,
                    "database"
                )
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                return instance
                //instance
            }
        }
    }
}