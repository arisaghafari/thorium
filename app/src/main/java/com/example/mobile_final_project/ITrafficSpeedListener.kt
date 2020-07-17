package com.example.mobile_final_project

interface ITrafficSpeedListener {
    fun onTrafficSpeedMeasured(upStream: Double, downStream: Double)
}