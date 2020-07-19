package com.example.mobile_final_project


import androidx.lifecycle.LiveData

class CellRepository(private val cellDao : CellDao) {

    val allCells: LiveData<List<Cell>> = cellDao.getAllCellInfo()

    suspend fun insert(cell: Cell) {
        cellDao.insert(cell)
    }
}
