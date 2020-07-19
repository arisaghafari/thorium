package com.example.mobile_final_project

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "LTE_table", indices = arrayOf(
    Index(value = ["altitude", "longtitude"],
        unique = true)
))
data class Cell(
    @PrimaryKey(autoGenerate = true)
    var ID: Long?=null,
    var cellId: Long?=null,
    var RSRP: String?,
    var RSRQ: String?,
    var CINR: String?,
    var AC: String?,
    var PLMN: String?,
    var altitude: Float,
    var longtitude: Float,
    //@ColumnInfo(defaultValue = "false")
    //var pointer: Boolean,
    var cellType: String?,
    var currentTime : String?
)