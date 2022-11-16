package br.com.kascosys.vulkanconnectv317.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.kascosys.vulkanconnectv317.models.MonitoringModel
import br.com.kascosys.vulkanconnectv317.models.NewMonitoringModel
import br.com.kascosys.vulkanconnectv317.models.NewParameterModel
import com.github.mikephil.charting.data.Entry

@Entity(tableName = "device_data_table")
data class DeviceMinimalData(
    @ColumnInfo(name = "device_ssid")
    val deviceSsId: String = "Device SSID",

    @ColumnInfo(name = "device_size")
    val deviceSize: Int = 0,

    @ColumnInfo(name = "device_nick")
    val deviceNickname: String = "Device nickname",

    @ColumnInfo(name = "last_active")
    var lastActiveTime: Long = 0L
)