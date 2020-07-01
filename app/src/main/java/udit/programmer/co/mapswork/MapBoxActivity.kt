package udit.programmer.co.mapswork

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.activity_map_box.*

class MapBoxActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

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
        mapView.getMapAsync {
            it.setStyle(Style.OUTDOORS) {

            }
        }
        setContentView(mapView)
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

}