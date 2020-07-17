package com.example.mobile_final_project
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var mTrafficSpeedMeasurer: TrafficSpeedMeasurer? = null
    private var mTextView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTextView = findViewById(R.id.connection_class)
        mTrafficSpeedMeasurer = TrafficSpeedMeasurer(TrafficSpeedMeasurer.TrafficType.ALL)
        mTrafficSpeedMeasurer!!.startMeasuring()
    }

    override fun onDestroy() {
        super.onDestroy()
        mTrafficSpeedMeasurer?.stopMeasuring()
    }

    override fun onPause() {
        super.onPause()
        mTrafficSpeedMeasurer?.removeListener(mStreamSpeedListener)
    }

    override fun onResume() {
        super.onResume()
        mTrafficSpeedMeasurer?.registerListener(mStreamSpeedListener)
    }

    private val mStreamSpeedListener: ITrafficSpeedListener = object : ITrafficSpeedListener {
        override fun onTrafficSpeedMeasured(
            upStream: Double,
            downStream: Double
        ) {
            runOnUiThread {
                val upStreamSpeed: String =
                    Utils.parseSpeed(upStream, SHOW_SPEED_IN_BITS)
                val downStreamSpeed: String =
                    Utils.parseSpeed(downStream, SHOW_SPEED_IN_BITS)
                mTextView!!.text =
                    "Up Stream Speed: $upStreamSpeed\nDown Stream Speed: $downStreamSpeed"
            }
        }
    }

    companion object {
        private const val SHOW_SPEED_IN_BITS = false
    }
}