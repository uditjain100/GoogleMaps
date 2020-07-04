package udit.programmer.co.mapswork

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.JsonObject
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
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
import com.mapbox.mapboxsdk.plugins.traffic.TrafficPlugin
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.activity_search_pick.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.ref.WeakReference

class SearchPickActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private lateinit var firebaseDatabase: DatabaseReference

    private lateinit var mapView: MapView
    var mapboxMap: MapboxMap? = null
    private lateinit var home: CarmenFeature
    private lateinit var work: CarmenFeature
    private val geojsonSourceLayerId = "geojsonSourceLayerId"
    private val symbolIconId = "symbolIconId"
    private val REQUEST_CODE_AUTOCOMPLETE = 1

    private val DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID"
    private var hoveringMarker: ImageView? = null
    private var droppedMarkerLayer: Layer? = null

    var currentLat = 0.0
    var currentLng = 0.0

    var lat: Double = 0.0
    var lng: Double = 0.0

    private lateinit var permissionsManager: PermissionsManager

    private var locationEngine: LocationEngine? = null
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    private lateinit var request: LocationEngineRequest
    private var callback = SearchPickActivityLocationCallback(this)

    private var navigationMapRoute: NavigationMapRoute? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_search_pick)
        mapView = findViewById(R.id.mapView_000)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        firebaseDatabase = FirebaseDatabase.getInstance().getReference("LatLng")

        fab_done_btn.setOnClickListener {
            locationEngine!!.removeLocationUpdates(callback)
            val map = mutableMapOf<String, Double>()
            map["Latitude"] = lat
            map["Longitude"] = lng
            firebaseDatabase.child("000")
                .setValue(map).addOnSuccessListener {
                    Toast.makeText(this, "Location Stored", Toast.LENGTH_LONG).show()
                    Thread.sleep(2000)
                    onBackPressed()
                }.addOnFailureListener {
                    Toast.makeText(this, "FAILED : $it", Toast.LENGTH_LONG).show()
                }
        }

        fab_navigate_pick_btn.setOnClickListener {
            locationEngine!!.requestLocationUpdates(request, callback, mainLooper)
        }

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.OUTDOORS) { style ->
            TrafficPlugin(mapView, mapboxMap, style).setVisibility(true)

            enableLocationComponent(style)

            initSearchFab()
            addUserLocation()

            val drawable = ResourcesCompat.getDrawable(
                resources, R.mipmap.location_green, null
            )
            val bitmap = BitmapUtils.getBitmapFromDrawable(drawable)
            style.addImage(symbolIconId, bitmap!!)

            setUpSource(style)
            setUpLayer(style)

            hoveringMarker = ImageView(this)
            hoveringMarker!!.setImageResource(R.mipmap.location)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER
            )
            hoveringMarker!!.layoutParams = params
            mapView!!.addView(hoveringMarker)

            initDroppedMarker(style)

            fab_location_pick_btn.setOnClickListener {
                locationEngine!!.removeLocationUpdates(callback)
                if (hoveringMarker!!.visibility == View.VISIBLE) {
                    val mapTargetLatLng = mapboxMap.cameraPosition.target;

                    hoveringMarker!!.visibility = View.INVISIBLE;
                    fab_location_pick_btn.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.BlueViolet)
                    );
                    fab_location_pick_btn.text = "Cancel";
                    Toast.makeText(this, "Selected :)", Toast.LENGTH_LONG).show()
                    fab_done_btn.isClickable = true

                    lat = mapTargetLatLng.latitude
                    lng = mapTargetLatLng.longitude

//                    if (lat == 0.0 || lng == 0.0 || currentLat == 0.0 || currentLng == 0.0) {
//                        Toast.makeText(this, "Select Destination", Toast.LENGTH_LONG).show()
//                    } else {
//                        var origin = Point.fromLngLat(currentLat, currentLng)
//                        var dest = Point.fromLngLat(lat, lng)
//                        getRoute(origin, dest)
//                    }

                    if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                        val source = style.getSourceAs<GeoJsonSource>("dropped-marker-source-id");
                        source?.setGeoJson(
                            Point.fromLngLat(
                                mapTargetLatLng.longitude,
                                mapTargetLatLng.latitude
                            )
                        )
                        droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID)
                    }

                } else {
                    fab_location_pick_btn.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.DodgerBlue)
                    )
                    fab_location_pick_btn.text = "PICK";
                    hoveringMarker!!.visibility = View.VISIBLE;
                    droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID);
                    fab_done_btn.isClickable = true
                }
            }

        }
    }

//    private fun getRoute(origin: Point, dest: Point) {
//        NavigationRoute.builder(this).accessToken(Mapbox.getAccessToken()!!).origin(origin)
//            .destination(dest).build().getRoute(object : Callback<DirectionsResponse> {
//                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {}
//                override fun onResponse(
//                    call: Call<DirectionsResponse>,
//                    response: Response<DirectionsResponse>
//                ) {
//                    if (response.body() == null || response.body()!!.routes().size == 0) {
//                        Toast.makeText(
//                            this@SearchPickActivity, "No routes found", Toast.LENGTH_LONG
//                        ).show()
//                        return
//                    }
//                    val route = response.body()!!.routes()[0]
//                    if (navigationMapRoute != null) {
//                        navigationMapRoute!!.removeRoute()
//                    } else {
//                        navigationMapRoute = NavigationMapRoute(null, mapView, mapboxMap!!)
//                    }
//                    navigationMapRoute!!.addRoute(route)
//                }
//            })
//    }

    private fun initDroppedMarker(it: Style) {

        val drawable = ResourcesCompat.getDrawable(
            resources, R.mipmap.location_blue, null
        )
        val bitmap = BitmapUtils.getBitmapFromDrawable(drawable)
        it.addImage("dropped-icon-image", bitmap!!)

        it.addSource(GeoJsonSource("dropped-marker-source-id"))
        it.addLayer(
            SymbolLayer(
                DROPPED_MARKER_LAYER_ID,
                "dropped-marker-source-id"
            ).withProperties(
                PropertyFactory.iconImage("dropped-icon-image"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
        )
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
            activity.currentLat = location.latitude
            activity.currentLng = location.longitude
            Toast.makeText(
                activity, "lat : ${activity.currentLat} , lng : ${activity.currentLng}",
                Toast.LENGTH_SHORT
            ).show()
            if (activity.mapboxMap != null && result.lastLocation != null) {
                activity.mapboxMap!!.locationComponent
                    .forceLocationUpdate(result.lastLocation)
                activity.mapboxMap!!.animateCamera(
                    com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(activity.currentLat, activity.currentLng)).zoom(14.0)
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
