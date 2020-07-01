package udit.programmer.co.mapswork

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        map_box_btn.setOnClickListener {
            startActivity(Intent(this, MapBoxActivity::class.java))
        }

        table_btn.setOnClickListener {
            startActivity(Intent(this, TableActivity::class.java))
        }

        map_btn.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        navigation_btn.setOnClickListener {
            startActivity(Intent(this, NavigationActivity::class.java))
        }

    }
}