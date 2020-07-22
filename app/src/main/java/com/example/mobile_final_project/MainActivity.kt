package com.example.mobile_final_project
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class MainActivity : AppCompatActivity() {
    private var mTrafficSpeedMeasurer: TrafficSpeedMeasurer? = null
    private var mTextView: TextView? = null
    private var db: CellRoomDatabase? = null
    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTextView = findViewById(R.id.connection_class)
        mTrafficSpeedMeasurer = TrafficSpeedMeasurer(TrafficSpeedMeasurer.TrafficType.ALL)
        mTrafficSpeedMeasurer!!.startMeasuring()

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                prameters()
                executeCommand()
                mainHandler.postDelayed(this, 5000)
            }
        })
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
                    "$upStreamSpeed \n\n$downStreamSpeed"
            }
        }
    }

    companion object {
        private const val SHOW_SPEED_IN_BITS = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prameters() {
        var RSRP_SCView = findViewById<TextView>(R.id.RSRP_SC)
        var RSRP_NCView = findViewById<TextView>(R.id.RSRP_NC)
        var RSRQ_SCView = findViewById<TextView>(R.id.RSRQ_SC)
        var RSRQ_NCView = findViewById<TextView>(R.id.RSRQ_NC)
        var CINR_SCView = findViewById<TextView>(R.id.CINR_SC)
        var CINR_NCView = findViewById<TextView>(R.id.CINR_NC)
        var ACView = findViewById<TextView>(R.id.AC)
        var PLMNView = findViewById<TextView>(R.id.PLMN)
        var CellIdView = findViewById<TextView>(R.id.Cell_Id)
        var NetworkTypeView = findViewById<TextView>(R.id.NetworkType)
        var currentTime = findViewById<TextView>(R.id.current_time)
        var latView = findViewById<TextView>(R.id.lat)
        var lonView = findViewById<TextView>(R.id.lon)

        var servingCellSignalStrength = 0
        var servingCellSignalQuality = 0
        var servingCellSignalnoise = 0
        var neighborCellSignalStrength = 0
        var neighborCellSignalQuality = 0
        var neighborCellSignalnoise = 0
        var servingCellTAC = 0
        var servingCellLAC = 0
        var servingCellPLMN = ""
        var servingCellRAC = 0
        var servingCellId = 0


        //Set an instance of telephony manager
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        //Check location permission
        val permission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
        }

        //Check type of network and assign parameters to signal strength and quality
        val cellInfoList = tm.allCellInfo
        for (cellInfo in cellInfoList) {
            if (cellInfo.isRegistered) {
                if (cellInfo is CellInfoLte) {
                    servingCellSignalStrength = cellInfo.cellSignalStrength.rsrp
                    servingCellSignalQuality = cellInfo.cellSignalStrength.rsrq
                    servingCellSignalnoise = cellInfo.cellSignalStrength.rssnr
                    servingCellTAC = cellInfo.cellIdentity.tac
                    servingCellPLMN = tm.networkOperator
                    servingCellId = cellInfo.cellIdentity.ci
                } else if (cellInfo is CellInfoWcdma && servingCellSignalStrength == 0) {
                    val b = cellInfo.cellSignalStrength.dbm
                    servingCellSignalStrength = b
                    servingCellLAC = cellInfo.cellIdentity.lac
                    servingCellPLMN = tm.networkOperator
                    servingCellId = cellInfo.cellIdentity.cid
                } else if (cellInfo is CellInfoGsm && servingCellSignalStrength == 0) {
                    val gsm = cellInfo.cellSignalStrength
                    servingCellSignalStrength = gsm.dbm
                    servingCellPLMN = tm.networkOperator
                    servingCellLAC = cellInfo.cellIdentity.lac
                    servingCellId = cellInfo.cellIdentity.cid
                }

            } else {
                if (cellInfo is CellInfoLte) {
                    neighborCellSignalStrength = cellInfo.cellSignalStrength.rsrp
                    neighborCellSignalQuality = cellInfo.cellSignalStrength.rsrq
                    neighborCellSignalnoise = cellInfo.cellSignalStrength.rssnr
                } else if (cellInfo is CellInfoWcdma && neighborCellSignalStrength == 0) {
                    val b = cellInfo.cellSignalStrength.dbm
                    neighborCellSignalStrength = b
                } else if (cellInfo is CellInfoGsm && neighborCellSignalStrength == 0) {
                    val gsm = cellInfo.cellSignalStrength
                    neighborCellSignalStrength = gsm.dbm
                }
            }
        }
        RSRP_SCView.text = servingCellSignalStrength.toString()
        RSRP_NCView.text = neighborCellSignalStrength.toString()
        RSRQ_SCView.text = servingCellSignalQuality.toString()
        RSRQ_NCView.text = neighborCellSignalQuality.toString()
        CINR_SCView.text = servingCellSignalnoise.toString()
        CINR_NCView.text = neighborCellSignalnoise.toString()
        PLMNView.text = servingCellPLMN
        CellIdView.text = servingCellId.toString()
        NetworkTypeView.text = getNetworkType()
        // latView.text = "latitude : " + lat.toString()
        // lonView.text = "longitude : " + lon.toString()

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = current.format(formatter)
        currentTime.text = formatted.toString()

        if (servingCellLAC != 0) {
            ACView.text = servingCellLAC.toString()
        } else if (servingCellTAC != 0) {
            ACView.text = servingCellTAC.toString()
        } else if (servingCellRAC != 0) {
            ACView.text = servingCellRAC.toString()
        }
        //getLastLocation()
        val info = Cell(
            cellId = servingCellId.toLong(),
            RSRP = servingCellSignalStrength.toString(),
            RSRQ = servingCellSignalQuality.toString()
            ,
            CINR = servingCellSignalnoise.toString(),
            AC = ACView.toString(),
            PLMN = servingCellPLMN,
            currentTime = currentTime.toString()
            ,
            altitude = 0.toDouble(),
            longtitude = 0.toDouble(),
            cellType = NetworkTypeView.toString()
        )
        db?.CellDao()?.insert(info)
        //var list = db?.CellDao()?.AllCell()
    }

    fun getNetworkType(): String {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var textV1: String = ""
        when (telephonyManager.networkType) {
            7 -> textV1 = "1xRTT"
            4 -> textV1 = "CDMA"
            2 -> textV1 = "EDGE"
            14 -> textV1 = "eHRPD"
            5 -> textV1 = "EVDO rev. 0"
            6 -> textV1 = "EVDO rev. A"
            12 -> textV1 = "EVDO rev. B"
            1 -> textV1 = "GPRS"
            8 -> textV1 = "HSDPA"
            10 -> textV1 = "HSPA"
            15 -> textV1 = "HSPA+"
            9 -> textV1 = "HSUPA"
            11 -> textV1 = "iDen"
            13 -> textV1 = "LTE"
            3 -> textV1 = "UMTS"
            0 -> textV1 = "Unknown"
        }
        return textV1
    }

    private fun executeCommand(): Boolean {
        var latencyView = findViewById<TextView>(R.id.latency)
        var time1 = System.currentTimeMillis()
        println("executeCommand")
        val runtime = Runtime.getRuntime()
        try {
            val mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val mExitValue = mIpAddrProcess.waitFor()
            var time2 = System.currentTimeMillis()
            latencyView.text = (time2 - time1).toString() + " ms"
            println(" mExitValue $mExitValue")
            return mExitValue == 0

        } catch (ignore: InterruptedException) {
            ignore.printStackTrace()
            println(" Exception:$ignore")
        } catch (e: IOException) {
            e.printStackTrace()
            println(" Exception:$e")
        }
        return false
    }

    @Throws(UnknownHostException::class, IOException::class)
    fun sendPingRequest(ipAddress: String) {
        val geek = InetAddress.getByName(ipAddress)
        println("Sending Ping Request to $ipAddress")
        if (geek.isReachable(5000)) {
            println("Host is reachable")
        }
        else {
            println("Sorry ! We can't reach to this host")
        }
    }
}