package br.com.kascosys.vulkanconnectv317.database

import androidx.room.TypeConverter
import br.com.kascosys.vulkanconnectv317.models.NewMonitoringModel
import br.com.kascosys.vulkanconnectv317.models.NewParameterModel
import com.github.mikephil.charting.data.Entry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class DeviceDataConverter {
    private val gson = Gson()

    @TypeConverter
    fun toEntryList(data: String?): List<Entry> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<Entry>>() {}.type

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun toParameterList(data: String?): List<NewParameterModel> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<NewParameterModel>>() {}.type

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun toMonitoringList(data: String?): List<NewMonitoringModel> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<NewMonitoringModel>>() {}.type

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun entryListToString(list: List<Entry>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun parameterListToString(list: List<NewParameterModel>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun monitoringListToString(list: List<NewMonitoringModel>): String {
        return gson.toJson(list)
    }
}