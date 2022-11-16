package br.com.kascosys.vulkanconnectv317.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import br.com.kascosys.vulkanconnectv317.R

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val splashMilliseconds: Long = 3000

        // Hide action bar
        supportActionBar?.hide()

        Handler().postDelayed({
            runMyApp()
        }, splashMilliseconds)
    }

    private fun runMyApp() {
        val intent = Intent(this, PairingActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
