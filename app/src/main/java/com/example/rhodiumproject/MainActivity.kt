package com.example.rhodiumproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var map: MapView? = null
    private var lat = 0.0
    private var lon = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
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


        val roadManager: RoadManager = OSRMRoadManager(this)
        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(startPoint)
        val endPoint = GeoPoint(35.705909, 51.406097)
        waypoints.add(endPoint)
        val road = roadManager.getRoad(waypoints)
        if (road.mStatus != Road.STATUS_OK){
            val startMarker = Marker(map)
            startMarker.position = startPoint
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            startMarker.setOnMarkerClickListener { marker, mapView ->
                onMarkerClickDefault(marker)
                mapView.controller.animateTo(marker.position)
                true
            }
            map!!.overlays.add(startMarker)
            map!!.invalidate()
        }else{
            val startMarker = Marker(map)
            startMarker.position = startPoint
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            startMarker.setOnMarkerClickListener { marker, mapView ->
                onMarkerClickDefault(marker)
                mapView.controller.animateTo(marker.position)
                true
            }
            val roadOverlay: Polyline = RoadManager.buildRoadOverlay(road)
            val overlays = listOf<Overlay>(roadOverlay, startMarker)
            map!!.overlays.addAll(overlays)
            map!!.invalidate()
        }
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                Toast.makeText(this@MainActivity, "latitude is : $lat longitude is : $lon", Toast.LENGTH_LONG).show()
                mainHandler.postDelayed(this, 3000)
            }
        })
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

    override fun onResume() {
        super.onResume()
        map?.onResume()

    }

    override fun onPause() {
        super.onPause()
        map?.onPause()
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
    private fun onMarkerClickDefault(marker: Marker): Boolean {
        val intent = Intent(this@MainActivity, DetailActivity::class.java)
        startActivity(intent)
        return true
    }
}
