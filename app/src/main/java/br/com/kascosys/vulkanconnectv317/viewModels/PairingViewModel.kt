package br.com.kascosys.vulkanconnectv317.viewModels

import android.net.wifi.ScanResult
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.kascosys.vulkanconnectv317.adapters.DeviceAdapter
import br.com.kascosys.vulkanconnectv317.enums.DeviceState
import br.com.kascosys.vulkanconnectv317.models.DeviceModel
import java.util.*

class PairingViewModel : ViewModel() {

    private val _password = MutableLiveData<String>()
    val password: LiveData<String>
        get() = _password

    private val _deviceList: MutableList<DeviceModel> = mutableListOf(
        // TODO: FOR TEST ONLY
        DeviceModel(
            "Tester device",
            40,
            "AndroidTest",
            DeviceState.OFFLINE,
            "1111",
            500583221000
        )
    )
    val deviceList: List<DeviceModel>
        get() = _deviceList

    init {
        Log.i("PairingViewModel", "PairingViewModel created!")
        _password.value = ""
    }

    override fun onCleared() {
        super.onCleared()

        Log.i("PairingViewModel", "PairingViewModel cleared!")
    }

    fun onPasswordChange(newPassword: String) {
        Log.i("PairingViewModel", "onPasswordChange called")

        _password.value = newPassword

    }

    fun onLogin(deviceId: String): Boolean {
        Log.i("PairingViewModel", "Login requested")

        return password.value == deviceList.first { it.deviceSsId == deviceId }.password
    }

    fun onFindDevices(results: List<ScanResult>, adapter: DeviceAdapter) {
        val deviceResults = filterDevices(results)

        var listChanged = false

        deviceResults.forEach { result ->
            if (deviceList.indexOfFirst { it.deviceSsId == result.SSID } < 0) {
                _deviceList.add(
                    DeviceModel(
                        "",
                        0,
                        result.SSID,
                        DeviceState.ONLINE,
                        "password",
                        Date().time
                    )
                )
                listChanged = true
            }
        }

        if (listChanged) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun filterDevices(results: List<ScanResult>): List<ScanResult> {
        return results.filter {
            it.SSID.contains("", true)
        }
    }

    fun addListToDeviceList(newList: List<DeviceModel>) {
        newList.forEach {newItem ->
            if(deviceList.indexOfFirst { it.deviceNickname == newItem.deviceNickname } < 0) {
                _deviceList.add(newItem)
            }
        }
    }

}