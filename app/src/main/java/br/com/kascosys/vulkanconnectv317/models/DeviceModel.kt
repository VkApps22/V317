package br.com.kascosys.vulkanconnectv317.models

import br.com.kascosys.vulkanconnectv317.enums.DeviceState

class DeviceModel(
    val deviceNickname: String,
    val modelSize: Int,
    val deviceSsId: String,
    var deviceState: DeviceState,
    val password: String,
    val lastConnected: Long
)