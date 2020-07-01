package udit.programmer.co.mapswork

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.*
import com.mapbox.mapboxsdk.style.layers.HillshadeLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.RasterDemSource

class MapBoxActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private val LAYER_ID = "hillshade-layer"
    private val SOURCE_ID = "hillshade-source"
    private val SOURCE_URL = "mapbox://mapbox.terrain-rgb"
    private val HILLSHADE_HIGHLIGHT_COLOR = "#008924"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(
            this,
            "pk.eyJ1IjoidWRpdGphaW4xMDAiLCJhIjoiY2tjM21odzJoMTN6YTJybGZ3Y21hNmUybyJ9.j_BjCEd8MUiFY_A9hA-phQ"
        )
        val options = MapboxMapOptions.createFromAttributes(this, null)
            .camera(CameraPosition.Builder().target(LatLng(43.7383, 7.7094)).zoom(5.00).build())

        mapView = MapView(this, options)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        setContentView(mapView)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.OUTDOORS) {
            it.addSource(RasterDemSource(SOURCE_ID, SOURCE_URL))
            val hillShadeLayer = HillshadeLayer(LAYER_ID, SOURCE_ID)
                .withProperties(
                    PropertyFactory.hillshadeHighlightColor(
                        Color.parseColor(
                            HILLSHADE_HIGHLIGHT_COLOR
                        )
                    ),
                    PropertyFactory.hillshadeShadowColor(Color.BLACK)
                )
            it.addLayerBelow(hillShadeLayer, "aerialway")
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
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