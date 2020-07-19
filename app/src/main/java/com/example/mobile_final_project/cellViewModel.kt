package com.example.mobile_final_project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CellViewModel(application: Application) : AndroidViewModel(application) {

    private val LTErepository: CellRepository

    val LTE_allCells: LiveData<List<Cell>>

    init {
        val LTE_cellsDao = CellRoomDatabase.getDatabase(application).LTECellDao()
        LTErepository = CellRepository(LTE_cellsDao)
        LTE_allCells = LTErepository.allCells
    }

    fun LTEinsert(cell: Cell) = viewModelScope.launch(Dispatchers.IO) {
        LTErepository.insert(cell)
    }

}