package br.com.kascosys.vulkanconnectv317.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.kascosys.vulkanconnectv317.models.NewMonitoringModel
import br.com.kascosys.vulkanconnectv317.models.NewParameterModel
import com.github.mikephil.charting.data.Entry
import java.util.*

@Entity(tableName = "device_data_table", indices = [Index(value = ["device_nick"], unique = true)])
data class DeviceData(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "device_ssid")
    var deviceSsId: String = "Device SSID",

    @ColumnInfo(name = "device_size")
    var deviceSize: Int = 0,

    @ColumnInfo(name = "device_nick")
    var deviceNickname: String = "Device nickname",

    @ColumnInfo(name = "last_active")
    var lastActiveTime: Long = Date().time,

    @ColumnInfo(name = "parameter_list")
    var parameterList: MutableList<NewParameterModel> = mutableListOf(),

    @ColumnInfo(name = "monitoring_list")
    var monitoringList: MutableList<NewMonitoringModel> = mutableListOf(),

    @ColumnInfo(name = "last_alarm_id")
    var lastActiveAlarm: String = "Last alarm ID",

    @ColumnInfo(name = "last_graph_data")
    var lastGeneratedGraph: MutableList<Entry> = mutableListOf(),

    @ColumnInfo(name = "last_graph_id")
    var lastGraphId: String = "Last graph ID"
)