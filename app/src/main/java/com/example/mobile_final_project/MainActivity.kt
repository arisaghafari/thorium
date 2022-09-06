package com.example.mobile_final_project
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager

import android.net.ConnectivityManager
import android.os.*
import android.preference.PreferenceManager
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
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.ArrayList
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.views.overlay.Overlay



class MainActivity : AppCompatActivity() {
    private var mTrafficSpeedMeasurer: TrafficSpeedMeasurer? = null
    private var mTextView: TextView? = null

    private var db:CellRoomDatabase? = null

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var map: MapView? = null
    private var lat = 0.0
    private var lon = 0.0
    private var downloadRate = ""
    private var uploadRate = ""
    private var latency = ""
    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = CellRoomDatabase.getDatabase(context = this)

        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        map = findViewById<MapView>(R.id.map)
        map?.setBuiltInZoomControls(true)
        map?.setMultiTouchControls(true)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        val startPoint = GeoPoint(35.705328, 51.408065)
        val mapController = map!!.controller
        mapController.setZoom(12)
        mapController.setCenter(startPoint)

        val loc = GpsMyLocationProvider(applicationContext)
        var mLocationOverlay = MyLocationNewOverlay(loc, map)
        mLocationOverlay.enableMyLocation()
        mLocationOverlay.enableFollowLocation()
        mLocationOverlay.isDrawAccuracyEnabled
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        mTextView = findViewById(R.id.connection_class)
        mTrafficSpeedMeasurer = TrafficSpeedMeasurer(TrafficSpeedMeasurer.TrafficType.ALL)
        mTrafficSpeedMeasurer!!.startMeasuring()
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                prameters()
                pointer()

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
        map?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mTrafficSpeedMeasurer?.registerListener(mStreamSpeedListener)
        map?.onResume()
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
                //mTextView!!.text = "$upStreamSpeed \n\n$downStreamSpeed"
                downloadRate = downStreamSpeed
                uploadRate = upStreamSpeed
            }
        }
    }

    companion object {
        private const val SHOW_SPEED_IN_BITS = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prameters() {
       /* var RSRP_SCView = findViewById<TextView>(R.id.RSRP_SC)
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
        var lonView = findViewById<TextView>(R.id.lon)*/

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
        /*RSRP_SCView.text = servingCellSignalStrength.toString()
        RSRP_NCView.text = neighborCellSignalStrength.toString()
        RSRQ_SCView.text = servingCellSignalQuality.toString()
        RSRQ_NCView.text = neighborCellSignalQuality.toString()
        CINR_SCView.text = servingCellSignalnoise.toString()
        CINR_NCView.text = neighborCellSignalnoise.toString()
        PLMNView.text = servingCellPLMN
        CellIdView.text = servingCellId.toString()
        NetworkTypeView.text = getNetworkType()*/
        // latView.text = "latitude : " + lat.toString()
        // lonView.text = "longitude : " + lon.toString()

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = current.format(formatter)
        //currentTime.text = formatted.toString()
        var AC = 0
        if (servingCellLAC != 0) {
            //ACView.text = servingCellLAC.toString()
            AC = servingCellLAC
        } else if (servingCellTAC != 0) {
            //ACView.text = servingCellTAC.toString()
            AC = servingCellTAC
        } else if (servingCellRAC != 0) {
            //ACView.text = servingCellRAC.toString()
            AC = servingCellRAC
        }
        //getLastLocation()
        executeCommand()
        val info = Cell(cellId = servingCellId.toLong(),
            RSRP = servingCellSignalStrength.toString(),
            RSRQ = servingCellSignalQuality.toString(),
            CINR = servingCellSignalnoise.toString(),
            AC = AC.toString(),
            PLMN = servingCellPLMN,
            currentTime = formatted.toString(),
            altitude = lat,
            longtitude = lon,
            cellType = getNetworkType(),
            downloadRate = downloadRate,
            uploadRate = uploadRate,
            latency = latency
        )

        var flag = true
        val templat = (lat * 100).toInt()
        var templon = (lon * 100).toInt()
        var list = db?.CellDao()?.AllCell()
        if (list != null) {
            for (cell in list){
                var tempCellAl = (cell.altitude * 100).toInt()
                var tempCellLon = (cell.longtitude * 100).toInt()
                if(tempCellAl == templat && tempCellLon == templon){
                    println("update $flag")
                    db?.CellDao()?.update(info)
                    flag = false
                    println("flag :  $flag")
                }

            }
        }
        println("flag 2 : $flag")
        if (lat == 0.0 && lon == 0.0) {
            flag = false
        }
        if (flag) {
            db?.CellDao()?.insert(info)
            println("insert $flag")
        }

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
            //latencyView.text = (time2 - time1).toString() + " ms"
            latency = (time2 - time1).toString() + " ms"
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
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun getLastLocation() {
        if (isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                task.result
                requestNewLocationData()
            }
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 9000
        mLocationRequest.fastestInterval = 5000

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            lat = mLastLocation.latitude
            lon = mLastLocation.longitude
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        val permissionsToRequest: ArrayList<String?> = ArrayList()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun onMarkerClickDefault(id : Long?): Boolean {
        val intent = Intent(this@MainActivity, DetailActivity::class.java)
        intent.putExtra("id", id.toString())
        startActivity(intent)
        return true
    }
    private fun pointer() {
        val list = db?.CellDao()?.AllCell()
        if (list != null) {
            for (cell in list) {
                val startPoint = GeoPoint(cell.altitude, cell.longtitude)
                val roadManager: RoadManager = OSRMRoadManager(this)
                val waypoints = ArrayList<GeoPoint>()
                val endPoint = GeoPoint(35.700978, 51.396865)
                waypoints.add(startPoint)
                waypoints.add(endPoint)
                val road = roadManager.getRoad(waypoints)
                if (road.mStatus != Road.STATUS_OK) {
                    val startMarker = Marker(map)
                    startMarker.position = startPoint
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    startMarker.setOnMarkerClickListener { marker, mapView ->
                        onMarkerClickDefault(cell.ID)
                        mapView.controller.animateTo(marker.position)
                        true
                    }
                    map!!.overlays.add(startMarker)
                    map!!.invalidate()
                } else {
                    val startMarker = Marker(map)
                    startMarker.position = startPoint
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    startMarker.setOnMarkerClickListener { marker, mapView ->
                        onMarkerClickDefault(cell.ID)
                        mapView.controller.animateTo(marker.position)
                        true
                    }
                    val roadOverlay: Polyline = RoadManager.buildRoadOverlay(road)
                    val overlays = listOf<Overlay>(roadOverlay, startMarker)
                    map!!.overlays.addAll(overlays)
                    map!!.invalidate()
                }
            }
        }
    }

}