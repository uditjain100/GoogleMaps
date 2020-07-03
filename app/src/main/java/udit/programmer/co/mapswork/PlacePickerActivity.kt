package udit.programmer.co.mapswork

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.core.exceptions.ServicesException
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_place_picker.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class PlacePickerActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private val DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID"
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var permissionsManager: PermissionsManager? = null
    private var hoveringMarker: ImageView? = null
    private var droppedMarkerLayer: Layer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_place_picker)

        mapView = findViewById(R.id.mapView_0000)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.OUTDOORS) { style ->
            enableLocationComponent(style)

            hoveringMarker = ImageView(this)
            hoveringMarker!!.setImageResource(R.mipmap.location)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER
            )
            hoveringMarker!!.layoutParams = params
            mapView!!.addView(hoveringMarker)

            initDroppedMarker(style)

            select_location_button.setOnClickListener {
                if (hoveringMarker!!.visibility == View.VISIBLE) {
                    val mapTargetLatLng = mapboxMap.cameraPosition.target;

                    hoveringMarker!!.visibility = View.INVISIBLE;
                    select_location_button.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.BlueViolet)
                    );
                    select_location_button.text = "Cancel";

                    Toast.makeText(
                        this,
                        "lat : ${mapTargetLatLng.latitude} , lng : ${mapTargetLatLng.longitude},",
                        Toast.LENGTH_LONG
                    ).show()

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
                    select_location_button.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.Blue)
                    )
                    select_location_button.text = "Select another Location";
                    hoveringMarker!!.visibility = View.VISIBLE;
                    droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID);
                }
            }
        }
    }

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
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager!!.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "user_location_permission_explanation", Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted && mapboxMap != null) {
            val style = mapboxMap!!.style
            style?.let { enableLocationComponent(it) }
        } else {
            Toast.makeText(this, "Permissions Not Granted", Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

}
