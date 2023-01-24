package br.com.kascosys.vulkanconnectv317.database.firebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.databinding.ActivityConnectionBinding

class AlertsFirebaseActivity : AppCompatActivity() {
    private val viewModel: AlertsFirebaseViewModel by lazy {
        ViewModelProvider(this)[AlertsFirebaseViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityConnectionBinding = DataBindingUtil.setContentView(this, R.layout.fragment_alarms)
        binding.lifecycleOwner = this

        viewModel.fetchAlertsFirebase()
    }
}
