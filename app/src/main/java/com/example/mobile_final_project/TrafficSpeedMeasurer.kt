package com.example.mobile_final_project

import android.net.TrafficStats
import android.os.*


class TrafficSpeedMeasurer(private val mTrafficType: TrafficType) {
    private var mTrafficSpeedListener: ITrafficSpeedListener? = null
    private val mHandler: SamplingHandler
    private var mLastTimeReading: Long = 0
    private var mPreviousUpStream: Long = -1
    private var mPreviousDownStream: Long = -1
    fun registerListener(iTrafficSpeedListener: ITrafficSpeedListener?) {
        mTrafficSpeedListener = iTrafficSpeedListener
    }

    fun removeListener(iTrafficSpeedListener: ITrafficSpeedListener?) {
        mTrafficSpeedListener = iTrafficSpeedListener
    }

    fun startMeasuring() {
        mHandler.startSamplingThread()
        mLastTimeReading = SystemClock.elapsedRealtime()
    }

    fun stopMeasuring() {
        mHandler.stopSamplingThread()
        finalReadTrafficStats()
    }

    private fun readTrafficStats() {
        val newBytesUpStream =
            (if (mTrafficType == TrafficType.MOBILE) TrafficStats.getMobileTxBytes() else TrafficStats.getTotalTxBytes()) * 1024
        val newBytesDownStream =
            (if (mTrafficType == TrafficType.MOBILE) TrafficStats.getMobileRxBytes() else TrafficStats.getTotalRxBytes()) * 1024
        val byteDiffUpStream = newBytesUpStream - mPreviousUpStream
        val byteDiffDownStream = newBytesDownStream - mPreviousDownStream
        synchronized(this) {
            val currentTime = SystemClock.elapsedRealtime()
            var bandwidthUpStream = 0.0
            var bandwidthDownStream = 0.0
            if (mPreviousUpStream >= 0) {
                bandwidthUpStream = byteDiffUpStream * 1.0 / (currentTime - mLastTimeReading)
            }
            if (mPreviousDownStream >= 0) {
                bandwidthDownStream = byteDiffDownStream * 1.0 / (currentTime - mLastTimeReading)
            }
            if (mTrafficSpeedListener != null) {
                mTrafficSpeedListener!!.onTrafficSpeedMeasured(
                    bandwidthUpStream,
                    bandwidthDownStream
                )
            }
            mLastTimeReading = currentTime
        }
        mPreviousDownStream = newBytesDownStream
        mPreviousUpStream = newBytesUpStream
    }

    private fun finalReadTrafficStats() {
        readTrafficStats()
        mPreviousUpStream = -1
        mPreviousDownStream = -1
    }
    companion object {
        private const val SAMPLE_TIME: Long = 1000
        private const val MSG_START = 1
    }
    private inner class SamplingHandler(looper: Looper) :
        Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Companion.MSG_START -> {
                    readTrafficStats()
                    sendEmptyMessageDelayed(
                        Companion.MSG_START,
                        Companion.SAMPLE_TIME
                    )
                }
                else -> throw IllegalArgumentException("Unknown what=" + msg.what)
            }
        }

        fun startSamplingThread() {
            sendEmptyMessage(Companion.MSG_START)
        }

        fun stopSamplingThread() {
            removeMessages(Companion.MSG_START)
        }


    }

    enum class TrafficType {
        MOBILE, ALL
    }

    init {
        val thread = HandlerThread("ParseThread")
        thread.start()
        mHandler = SamplingHandler(thread.looper)
    }
}