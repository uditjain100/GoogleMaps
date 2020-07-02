package udit.programmer.co.mapswork

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.*
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.plugins.building.BuildingPlugin
import com.mapbox.mapboxsdk.style.layers.HillshadeLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.RasterDemSource
import kotlinx.android.synthetic.main.activity_location.*

class MapBoxActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private val LAYER_ID = "hillshade-layer"
    private val SOURCE_ID = "hillshade-source"
    private val ICON_ID = "icon-source"
    private val SOURCE_URL = "mapbox://mapbox.terrain-rgb"
    private val HILLSHADE_HIGHLIGHT_COLOR = "#008924"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(
            this, getString(
                R.string.mapbox_access_token
            )
        )

        val options = MapboxMapOptions.createFromAttributes(this, null)
            .camera(CameraPosition.Builder().target(LatLng(43.7383, 7.7094)).zoom(5.00).build())

        mapView = MapView(this, options)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        setContentView(mapView)
    }

//    override fun onMapReady(mapboxMap: MapboxMap) {
//        mapboxMap.setStyle(Style.OUTDOORS) {
//            it.addSource(RasterDemSource(SOURCE_ID, SOURCE_URL))
//            val hillShadeLayer = HillshadeLayer(LAYER_ID, SOURCE_ID)
//                .withProperties(
//                    PropertyFactory.hillshadeHighlightColor(
//                        Color.parseColor(
//                            HILLSHADE_HIGHLIGHT_COLOR
//                        )
//                    ),
//                    PropertyFactory.hillshadeShadowColor(Color.BLACK)
//                )
//            it.addLayerBelow(hillShadeLayer, "aerialway")
//        }
//
//        mapboxMap.addMarker(
//            com.mapbox.mapboxsdk.annotations.MarkerOptions().position(LatLng(48.85819, 2.29458))
//                .title("Eiffel Tower")
//        )
//        mapboxMap.addMarker(
//            com.mapbox.mapboxsdk.annotations.MarkerOptions().position(LatLng(43.7383, 7.7094))
//                .title("New")
//        )
//        mapboxMap.addMarker(
//            com.mapbox.mapboxsdk.annotations.MarkerOptions().position(LatLng(45.522585, -122.685699))
//                .title("New")
//        )
//
//        mapboxMap.addPolyline(
//            PolylineOptions()
//                .add(LatLng(43.7383, 7.7094))
//                .add(LatLng(48.85819, 2.29458))
//                .color(Color.parseColor("#3bb2d0"))
//                .width(2f)
//        )
//
//        val polygonLatLngList = mutableListOf<LatLng>();
//
//        polygonLatLngList.add(LatLng(45.522585, -122.685699));
//        polygonLatLngList.add(LatLng(45.534611, -122.708873));
//        polygonLatLngList.add(LatLng(45.530883, -122.678833));
//        polygonLatLngList.add(LatLng(45.547115, -122.667503));
//        polygonLatLngList.add(LatLng(45.530643, -122.660121));
//        polygonLatLngList.add(LatLng(45.533529, -122.636260));
//        polygonLatLngList.add(LatLng(45.521743, -122.659091));
//        polygonLatLngList.add(LatLng(45.510677, -122.648792));
//        polygonLatLngList.add(LatLng(45.515008, -122.664070));
//        polygonLatLngList.add(LatLng(45.502496, -122.669048));
//        polygonLatLngList.add(LatLng(45.515369, -122.678489));
//        polygonLatLngList.add(LatLng(45.506346, -122.702007));
//        polygonLatLngList.add(LatLng(45.522585, -122.685699));
//
//        mapboxMap.addPolygon(
//            PolygonOptions()
//                .addAll(polygonLatLngList)
//                .fillColor(Color.parseColor("#3bb2d0"))
//        )
//
//    }

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

            val buildingPlugin = BuildingPlugin(mapView, mapboxMap, it)
            buildingPlugin.setMinZoomLevel(5f)
            buildingPlugin.setColor(Color.RED)
            buildingPlugin.setVisibility(true)

            val symbolManager = SymbolManager(mapView, mapboxMap, it)
            symbolManager.iconAllowOverlap = true
            symbolManager.iconIgnorePlacement = true

            val symbol = symbolManager.create(
                SymbolOptions().withLatLng(LatLng(60.169091, 24.939876)).withIconImage(ICON_ID)
                    .withIconSize(2.0f)
            )

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