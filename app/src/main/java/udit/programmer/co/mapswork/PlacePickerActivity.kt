package udit.programmer.co.mapswork

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions
import kotlinx.android.synthetic.main.activity_place_picker.*

class PlacePickerActivity : AppCompatActivity() {

    private val REQUEST_CODE = 5678

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_place_picker)
        gotoPickerActivity()
    }

    private fun gotoPickerActivity() {
        startActivityForResult(
            PlacePicker.IntentBuilder()
                .accessToken(getString(R.string.mapbox_access_token))
                .placeOptions(
                    PlacePickerOptions.builder().statingCameraPosition(
                        CameraPosition.Builder().target(LatLng(40.7544, -73.9862)).zoom(16.0)
                            .build()
                    ).build()
                ).build(this), REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            go_to_picker_button.visibility = View.VISIBLE
            go_to_picker_button.setOnClickListener {
                gotoPickerActivity()
            }
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val carmenFeature = PlacePicker.getPlace(data)
            if (carmenFeature != null) {
                selected_location_info_textview.text =
                    String.format("qwertyu", carmenFeature.toJson())
            }
        }
    }
}