package udit.programmer.co.mapswork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.*
import kotlinx.android.synthetic.main.activity_main.*

class MapFragActivity : AppCompatActivity() {

    private var supportMapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_frag)
        Mapbox.getInstance(
            this,
            "pk.eyJ1IjoidWRpdGphaW4xMDAiLCJhIjoiY2tjM21odzJoMTN6YTJybGZ3Y21hNmUybyJ9.j_BjCEd8MUiFY_A9hA-phQ"
        )
        if (savedInstanceState == null) {
            val options = MapboxMapOptions.createFromAttributes(this, null)
                .camera(CameraPosition.Builder().target(LatLng(43.7383, 7.7094)).zoom(5.00).build())
            supportMapFragment = SupportMapFragment.newInstance(options)
            supportFragmentManager.beginTransaction()
                .add(R.id.map_view, supportMapFragment!!, "com.mapbox.map").commit()
        } else {
            supportMapFragment =
                supportFragmentManager.findFragmentByTag("com.mapbox.map") as SupportMapFragment
        }

        if (supportMapFragment != null)
            supportMapFragment!!.getMapAsync { it.setStyle(Style.OUTDOORS) {} }

    }
}