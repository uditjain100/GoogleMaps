package udit.programmer.co.mapswork

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_location.*
import java.lang.Exception
import java.util.*

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btn_search.setOnClickListener {
            geoLocate()
        }

//        search_input.setOnEditorActionListener { _, actionId, event ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
//                actionId == EditorInfo.IME_ACTION_DONE ||
//                event!!.action == KeyEvent.ACTION_DOWN ||
//                event.action == KeyEvent.KEYCODE_ENTER
//            ) {
//                Log.d("Ceased Meteor", "geoLocation is ON")
//                geoLocate()
//            }
//            Log.d("Ceased Meteor", "geoLocation is OFF")
//            false
//        }

    }

    private fun geoLocate() {
        var searchString = search_input.text.toString()
        var geocoder = Geocoder(this)
        var list = mutableListOf<Address>()
        try {
            list.addAll(geocoder.getFromLocationName(searchString, 1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (list.size > 0) {
            var address = list[0]
            moveCamera(address.latitude, address.longitude)
        }

    }

    private fun moveCamera(latitude: Double, longitude: Double) {
        mMap.addMarker(
            MarkerOptions().position(LatLng(latitude, longitude)).title("Location Found")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
    }

    override fun onStart() {
        requestFineLocation()
        super.onStart()
        if (isFineLocationGranted()) {
            if (isLocationEnabled()) {
                my_location_search.setOnClickListener {
                    setUpLocationListener()
                }
            } else {
                showGPS_NotDialog()
            }
        } else {
            this.requestFineLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpLocationListener() {
        var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var locationRequest = LocationRequest()
            .setInterval(2000)
            .setFastestInterval(2000)
            .setSmallestDisplacement(1f)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        val curr = LatLng(location.latitude, location.longitude)
                        if (::mMap.isInitialized) {
                            mMap.addMarker(MarkerOptions().position(curr).title("Current Position"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(curr))
                        }
                    }
                }
            },
            Looper.myLooper()
        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            999 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    my_location_search.setOnClickListener {
                        setUpLocationListener()
                    }
                } else {
                    showGPS_NotDialog()
                }
            } else {
                Toast.makeText(this, "Permissions Not Granted", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isFineLocationGranted(): Boolean {
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestFineLocation() {
        this.requestPermissions(
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 999
        )
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showGPS_NotDialog() {
        AlertDialog.Builder(this)
            .setMessage("GPS should be Enabled")
            .setTitle("GPS Enabled")
            .setCancelable(false)
            .setPositiveButton("Enable Now") { dialogInterface: DialogInterface?, _: Int ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialogInterface?.dismiss()
            }.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = true
        }
        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.isTrafficEnabled
        mMap.focusedBuilding

        search_input.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event!!.action == KeyEvent.ACTION_DOWN ||
                event.action == KeyEvent.KEYCODE_ENTER
            ) {
                geoLocate()
            }
            false
        }

//        mMap.setMaxZoomPreference(40f)
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}