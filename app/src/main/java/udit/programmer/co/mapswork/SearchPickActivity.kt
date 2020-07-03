package udit.programmer.co.mapswork

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.gson.JsonObject
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions
import com.mapbox.mapboxsdk.plugins.traffic.TrafficPlugin
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_search_pick.*
import java.lang.Exception
import java.lang.ref.WeakReference

class SearchPickActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private lateinit var mapView: MapView
    var mapboxMap: MapboxMap? = null
    private lateinit var home: CarmenFeature
    private lateinit var work: CarmenFeature
    private val geojsonSourceLayerId = "geojsonSourceLayerId"
    private val symbolIconId = "symbolIconId"
    private val REQUEST_CODE_AUTOCOMPLETE = 1
    private val REQUEST_CODE = 5678

    var lat: Double = 0.0
    var lng: Double = 0.0

    private lateinit var permissionsManager: PermissionsManager

    private var locationEngine: LocationEngine? = null
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    private lateinit var request: LocationEngineRequest
    private var callback = SearchPickActivityLocationCallback(this)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_search_pick)
        mapView = findViewById(R.id.mapView_000)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fab_location_pick_btn.setOnClickListener {
            locationEngine!!.removeLocationUpdates(callback)
            gotoPickerActivity()
        }

        fab_navigate_pick_btn.setOnClickListener {
            locationEngine!!.requestLocationUpdates(request, callback, mainLooper)
        }

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.OUTDOORS) {
            TrafficPlugin(mapView, mapboxMap, it).setVisibility(true)

            enableLocationComponent(it)

            initSearchFab()
            addUserLocation()

            val drawable = ResourcesCompat.getDrawable(
                resources, R.drawable.ic_baseline_contact_phone_24, null
            )
            val bitmap = BitmapUtils.getBitmapFromDrawable(drawable)
            it.addImage(symbolIconId, bitmap!!)

            setUpSource(it)
            setUpLayer(it)
        }
    }

    @SuppressLint("LogNotTimber", "MissingPermission")
    private fun enableLocationComponent(it: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponent = mapboxMap!!.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, it).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine!!.requestLocationUpdates(request, callback, mainLooper);
        locationEngine!!.getLastLocation(callback)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "Explanation Needed", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) mapboxMap!!.getStyle { enableLocationComponent(it) }
        else Toast.makeText(this, "Permissions Not Granted", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE && resultCode == Activity.RESULT_OK) {
            locationEngine!!.removeLocationUpdates(callback)
            val selectedCameraFeature = PlaceAutocomplete.getPlace(data)
            if (mapboxMap != null) {
                if (mapboxMap!!.style != null) {
                    if (mapboxMap!!.style!!.getSourceAs<GeoJsonSource>(geojsonSourceLayerId) != null) {
                        mapboxMap!!.style!!.getSourceAs<GeoJsonSource>(geojsonSourceLayerId)!!
                            .setGeoJson(
                                FeatureCollection.fromFeature(
                                    com.mapbox.geojson.Feature.fromJson(
                                        selectedCameraFeature.toJson()
                                    )
                                )
                            )
                    }

                    lat = (selectedCameraFeature.geometry() as com.mapbox.geojson.Point).latitude()
                    lng = (selectedCameraFeature.geometry() as com.mapbox.geojson.Point).longitude()
                    mapboxMap!!.animateCamera(
                        com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(LatLng(lat, lng)).zoom(14.0)
                                .build()
                        ), 4000
                    )

                }
            }

        }
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val carmenFeature = PlacePicker.getPlace(data)
            Toast.makeText(this, "Location Selected", Toast.LENGTH_LONG).show()
            locationEngine!!.requestLocationUpdates(request, callback, mainLooper)
            if (carmenFeature != null) {
                selected_location_info_tv.text = String.format("qwertyu", carmenFeature.toJson())
            }
        }
    }

    private fun gotoPickerActivity() {
        startActivityForResult(
            PlacePicker.IntentBuilder()
                .accessToken(getString(R.string.mapbox_access_token))
                .placeOptions(
                    PlacePickerOptions.builder().statingCameraPosition(
                        CameraPosition.Builder().target(LatLng(lat, lng)).zoom(16.0)
                            .build()
                    ).build()
                ).build(this), REQUEST_CODE
        )
    }

    private fun setUpLayer(it: Style) {
        it.addLayer(
            SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                PropertyFactory.iconImage(symbolIconId),
                PropertyFactory.iconOffset(arrayOf(0f, -8f))
            )
        )
    }

    private fun setUpSource(it: Style) {
        it.addSource(GeoJsonSource(geojsonSourceLayerId))
    }

    private fun addUserLocation() {
        home = CarmenFeature.builder().text("Mapbox SF Office")
            .geometry(com.mapbox.geojson.Point.fromLngLat(-122.3964485, 37.7912561))
            .placeName("50 Beale St, San Francisco, CA")
            .id("mapbox-sf")
            .properties(JsonObject())
            .build()

        work = CarmenFeature.builder().text("Mapbox DC Office")
            .placeName("740 15th Street NW, Washington DC")
            .geometry(com.mapbox.geojson.Point.fromLngLat(-77.0338348, 38.899750))
            .id("mapbox-dc")
            .properties(JsonObject())
            .build()
    }

    private fun initSearchFab() {
        fab_location_search_btn.setOnClickListener {
            locationEngine!!.removeLocationUpdates(callback)
            val intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(
                    if (Mapbox.getAccessToken() != null) Mapbox.getAccessToken()!! else getString(
                        R.string.mapbox_access_token
                    )
                ).placeOptions(
                    PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .addInjectedFeature(home)
                        .addInjectedFeature(work)
                        .build(PlaceOptions.MODE_CARDS)
                ).build(this)
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

}

class SearchPickActivityLocationCallback(activity: SearchPickActivity?) :
    LocationEngineCallback<LocationEngineResult?> {
    private val activityWeakReference: WeakReference<SearchPickActivity?>?

    init {
        activityWeakReference = WeakReference(activity)
    }

    override fun onSuccess(result: LocationEngineResult?) {
        val activity: SearchPickActivity = activityWeakReference!!.get()!!
        if (activity != null) {
            val location = result!!.lastLocation ?: return
            activity.lat = location.latitude
            activity.lng = location.longitude
            Toast.makeText(
                activity, "lat : ${activity.lat} , lng : ${activity.lng}",
                Toast.LENGTH_SHORT
            ).show()
            if (activity.mapboxMap != null && result.lastLocation != null) {
                activity.mapboxMap!!.locationComponent
                    .forceLocationUpdate(result.lastLocation)
                activity.mapboxMap!!.animateCamera(
                    com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(activity.lat, activity.lng)).zoom(14.0)
                            .build()
                    ), 4000
                )
            }
        }
    }

    override fun onFailure(exception: Exception) {
        Log.d("Ceased Meteor", exception!!.localizedMessage)
        val activity: SearchPickActivity = activityWeakReference!!.get()!!
        if (activity != null) {
            Toast.makeText(
                activity, exception.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
