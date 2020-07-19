package com.example.mobile_final_project

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Cell::class), version = 1, exportSchema = false)
abstract class CellRoomDatabase : RoomDatabase() {

    abstract fun LTECellDao(): CellDao

    private class CellDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    var LTECellDao = database.LTECellDao()

                    // Delete all content here.
                    LTECellDao.deleteAll()

                    // Add sample words.
                    var LTE_Cell = Cell(1, "1", "1", "1", "1", "1", 51.3890.toFloat(), 35.6892.toFloat(), "LTE", "1 AM")
                    LTECellDao.insert(LTE_Cell)
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CellRoomDatabase? = null

        fun getDatabase(
            context: Context
            // scope: CoroutineScope
        ): CellRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CellRoomDatabase::class.java,
                    "cell_database"
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