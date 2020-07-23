package com.example.mobile_final_project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CellViewModel(application: Application) : AndroidViewModel(application) {

    private val repository1: CellRepository
    private val repository2: RoutRepository

    val allCells: LiveData<List<Cell>>
    val allRout: LiveData<List<Rout>>

    init {
        val cellsDao = CellRoomDatabase.getDatabase(application).CellDao()
        repository1 = CellRepository(cellsDao)
        allCells = repository1.allCells
        val routsDao = CellRoomDatabase.getDatabase(application).RoutDao()
        repository2 = RoutRepository(routsDao)
        allRout = repository2.allRouts
    }

    fun insertCell(cell: Cell) = viewModelScope.launch(Dispatchers.IO) {
        repository1.insert(cell)
    }

    fun insertRout(rout: Rout) = viewModelScope.launch(Dispatchers.IO) {
        repository2.insert(rout)
    }
}