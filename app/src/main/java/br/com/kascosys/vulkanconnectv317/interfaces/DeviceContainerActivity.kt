package br.com.kascosys.vulkanconnectv317.interfaces

import android.widget.ProgressBar
import br.com.kascosys.vulkanconnectv317.database.DeviceDatabase
import br.com.kascosys.vulkanconnectv317.database.DeviceMinimalData

interface DeviceContainerActivity {
    var deviceList: MutableList<DeviceMinimalData>

    val progressBar: ProgressBar

    val database: DeviceDatabase

    fun getScanResults()
}