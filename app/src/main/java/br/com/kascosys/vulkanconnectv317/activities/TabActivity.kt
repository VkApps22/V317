package br.com.kascosys.vulkanconnectv317.activities

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.MINIMUM_TAB_RELOAD_MILLIS
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class TabActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("TabActivity", "onCreate called")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navController = Navigation.findNavController(this, R.id.home_fragment)

        var lastClickTime = 0L

        val context = this

        bottom_navigation.setupWithNavController(navController)
        bottom_navigation.setOnNavigationItemSelectedListener {
            Log.i(
                "TabActivity",
                "bottom_navigation.onNavigationItemSelectedListener ${
                it.title
                } $lastClickTime ${
                SystemClock.elapsedRealtime()
                }----------------------"
            )

            if (lastClickTime == 0L ||
                lastClickTime + MINIMUM_TAB_RELOAD_MILLIS < SystemClock.elapsedRealtime()
            ) {
                Log.i(
                    "TabActivity",
                    "bottom_navigation.onNavigationItemSelectedListener will load tab"
                )

                lastClickTime = SystemClock.elapsedRealtime()

                NavigationUI.onNavDestinationSelected(it, navController)

                Lingver.getInstance().setLocale(context, Locale.getDefault())

                return@setOnNavigationItemSelectedListener true
            }

            Log.i(
                "TabActivity",
                "bottom_navigation.onNavigationItemSelectedListener time not elapsed yet "
            )

            false
        }

        val appBarConfiguration = AppBarConfiguration
            .Builder(
                R.id.homeFragment,
                R.id.alarmFragment,
                R.id.monitoringFragment
            )
            .build()

        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            appBarConfiguration
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

}