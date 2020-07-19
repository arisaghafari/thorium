package com.example.mobile_final_project

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CellDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(cell : Cell)

    @Update
    fun updateUsers(cell : Cell)

    @Delete
    fun deleteUsers(cell : Cell)

    @Query("SELECT * from LTE_table ORDER BY cellId ASC")
    fun getAllCellInfo(): LiveData<List<Cell>>

    @Query("SELECT * from LTE_table ORDER BY cellId ASC")
    fun AllCell(): List<Cell>

    @Query("DELETE FROM LTE_table")
    fun deleteAll()
}