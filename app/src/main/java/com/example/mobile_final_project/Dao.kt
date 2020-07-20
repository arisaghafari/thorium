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

    @Query("SELECT * from point_table ORDER BY cellId ASC")
    fun getAllCellInfo(): LiveData<List<Cell>>

    @Query("SELECT * from point_table ORDER BY cellId ASC")
    fun AllCell(): List<Cell>

    @Query("DELETE FROM point_table")
    fun deleteAll()
}
@Dao
interface routDao{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(rout : Rout)

    @Update
    fun updateUsers(rout : Rout)

    @Delete
    fun deleteUsers(rout : Rout)

    @Query("SELECT * from rout_table ORDER BY ID ASC")
    fun getAllRoutInfo(): LiveData<List<Rout>>

    @Query("SELECT * from rout_table ORDER BY ID ASC")
    fun AllCell(): List<Rout>

    @Query("DELETE FROM rout_table")
    fun deleteAll()
}