package udit.programmer.co.mapswork

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.gson.JsonObject
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity(), com.mapbox.mapboxsdk.maps.OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var mapboxMap: MapboxMap? = null
    private lateinit var home: CarmenFeature
    private lateinit var work: CarmenFeature
    private val geojsonSourceLayerId = "geojsonSourceLayerId"
    private val symbolIconId = "symbolIconId"
    private val REQUEST_CODE_AUTOCOMPLETE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_search)
        mapView = findViewById(R.id.mapView_001)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.OUTDOORS) {
            initSearchFab()
            addUserLocation()

            val drawable =
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_contact_phone_24,
                    null
                )
            val bitmap = BitmapUtils.getBitmapFromDrawable(drawable)
            it.addImage(symbolIconId, bitmap!!)

            setUpSource(it)
            setUpLayer(it)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE && resultCode == Activity.RESULT_OK) {
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

                    val lat =
                        (selectedCameraFeature.geometry() as com.mapbox.geojson.Point).latitude()
                    val lng =
                        (selectedCameraFeature.geometry() as com.mapbox.geojson.Point).longitude()
                    mapboxMap!!.animateCamera(
                        com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(
                            com.mapbox.mapboxsdk.camera.CameraPosition.Builder()
                                .target(com.mapbox.mapboxsdk.geometry.LatLng(lat, lng)).zoom(14.0)
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
        fab_location_search.setOnClickListener {
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