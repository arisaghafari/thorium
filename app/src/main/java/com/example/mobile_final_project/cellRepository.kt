package com.example.mobile_final_project


import androidx.lifecycle.LiveData

class CellRepository(private val cellDao : CellDao) {

    val allCells: LiveData<List<Cell>> = cellDao.getAllCellInfo()

    suspend fun insert(cell: Cell) {
        cellDao.insert(cell)
    }
}

class RoutRepository(private val routDao : routDao) {

    val allRouts: LiveData<List<Rout>> = routDao.getAllRoutInfo()

    suspend fun insert(rout : Rout) {
        routDao.insert(rout)
    }
}
