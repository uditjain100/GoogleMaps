package udit.programmer.co.mapswork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Gallery
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import udit.programmer.co.mapswork.ui.Dashboard
import udit.programmer.co.mapswork.ui.Home
import udit.programmer.co.mapswork.ui.Notification

class NavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener
    , BottomNavigationView.OnNavigationItemReselectedListener {

    private lateinit var appBarConfiguration1: AppBarConfiguration
    private lateinit var appBarConfiguration2: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration1 = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_dashboard, R.id.nav_notifications
            ), drawer_layout
        )
        appBarConfiguration2 = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_dashboard, R.id.nav_notifications
            ), drawer_layout
        )
        setupActionBarWithNavController(navController, appBarConfiguration1)
        setupActionBarWithNavController(navController, appBarConfiguration2)
        side_nav_view.setupWithNavController(navController)
        bottom_nav_view.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration1) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(it: MenuItem): Boolean {
        when (it.itemId) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, Home())
            }
            R.id.nav_dashboard -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, Dashboard())
            }
            R.id.nav_notifications -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, Notification())
            }
        }
        return true
    }

    override fun onNavigationItemReselected(it: MenuItem) {
        when (it.itemId) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, Home())
            }
            R.id.nav_dashboard -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, Dashboard())
            }
            R.id.nav_notifications -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, Notification())
            }
        }
    }

}