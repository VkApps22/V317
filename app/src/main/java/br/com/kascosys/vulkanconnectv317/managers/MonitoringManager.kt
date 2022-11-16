package br.com.kascosys.vulkanconnectv317.managers

import android.content.Context
import android.os.SystemClock
import android.util.Log
import br.com.kascosys.vulkanconnectv317.constants.M004
import br.com.kascosys.vulkanconnectv317.constants.M004_RATIO_DIVIDER
import br.com.kascosys.vulkanconnectv317.constants.PARAMETER_NAME_SUFFIX
import br.com.kascosys.vulkanconnectv317.constants.READ_ERROR
import br.com.kascosys.vulkanconnectv317.enums.BrakeStatus
import br.com.kascosys.vulkanconnectv317.models.NewMonitoringModel
import br.com.kascosys.vulkanconnectv317.utils.Util
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusUtils
import br.com.kascosys.vulkanconnectv317.utils.singleton.SingletonHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MonitoringManager private constructor(context: Context) {

    private var monitoringList: ArrayList<NewMonitoringModel>

    private lateinit var modBusUtils: ModBusUtils

    private var _c2value = false

    private var _c3value = false

    private var openStart = 0L
    private var closeStart = 0L

    private var openEnd = 0L
    private var closeEnd = 0L

    private var _openTime = 0L
    val openTime
        get() = _openTime

    private var _closeTime = 0L
    val closeTime
        get() = _closeTime

    var c2
        get() = _c2value
        set(value) {
            when (val status =
                BrakeStatus.nextStatus(_c2value, _c3value, value, _c3value)
                ) {

                BrakeStatus.OPEN_START -> openStart = SystemClock.elapsedRealtime()
                BrakeStatus.CLOSE_START -> closeStart = SystemClock.elapsedRealtime()
                else -> {
                    Log.e(
                        "MonitoringManager",
                        "setC2 unexpected bit sequence $status value $value c2 $_c2value c3 $_c3value"
                    )
                }
            }

            _c2value = value
        }

    var c3
        get() = _c3value
        set(value) {
            when (val status =
                BrakeStatus.nextStatus(_c2value, _c3value, _c2value, value)
                ) {

                BrakeStatus.OPEN_FINISH -> {
                    openEnd = SystemClock.elapsedRealtime()
                    _openTime = openEnd - openStart
                }
                BrakeStatus.CLOSE_FINISH -> {
                    closeEnd = SystemClock.elapsedRealtime()
                    _closeTime = closeEnd - closeStart
                }
                else -> {
                    Log.e(
                        "MonitoringManager",
                        "setC3 unexpected bit sequence $status value $value c2 $_c2value c3 $_c3value"
                    )
                }
            }

            _c3value = value
        }

    init {
        // Init using context argument

        Log.i("MonitoringManager", "init----------------------")

        val groupListType = object : TypeToken<ArrayList<NewMonitoringModel>>() {

        }.type

        monitoringList = Gson().fromJson(Util.readMonitoringJson(context), groupListType)

        // Init monitoring list

        for (i in 0 until monitoringList.size) {
            val id = monitoringList[i].id
            val ratio = monitoringList[i].ratio

            Log.i(
                "MonitoringManager", "init $i $id ${
                monitoringList[i].unit
                } $ratio"
            )

            monitoringList[i].value = READ_ERROR

            val resName = id?.toLowerCase() + PARAMETER_NAME_SUFFIX

            val labelId = context.resources.getIdentifier(
                resName,
                "string", context.packageName
            )

            monitoringList[i].nameResId = labelId
        }
    }

    fun getBy(id: String): NewMonitoringModel {
        return monitoringList.first { it.id == id }
    }

    fun getAll(): ArrayList<NewMonitoringModel> {
        return monitoringList
    }

    fun updateM004ratio(size: Int) {
        getBy(M004).ratio = M004_RATIO_DIVIDER / size.toDouble()

        Log.i("MonitoringManager", "updateM004ratio $size ${getBy(M004).ratio}")
    }

    companion object : SingletonHolder<MonitoringManager, Context>(::MonitoringManager)
}