package br.com.kascosys.vulkanconnectv317.managers

import android.content.Context
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusValues
import br.com.kascosys.vulkanconnectv317.utils.singleton.SingletonHolder

class AlarmManager private constructor(context: Context){

    private var _alarmString = ""

    private var _oldAlarmString = ""

    val alarm: String
        get() = _alarmString

    val oldAlarm: String
        get() = _oldAlarmString

    fun setAlarmByNumber(number: Int) {
        _oldAlarmString = _alarmString
        _alarmString = ModBusValues.mapAlarmNumberToId(number)
    }

    fun setAlarm(string: String) {
        _oldAlarmString = _alarmString
        _alarmString = string
    }

    companion object : SingletonHolder<AlarmManager, Context>(::AlarmManager)
}