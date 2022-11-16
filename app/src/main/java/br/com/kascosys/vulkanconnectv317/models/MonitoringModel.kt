package br.com.kascosys.vulkanconnectv317.models

import br.com.kascosys.vulkanconnectv317.enums.ParameterState
import com.google.gson.annotations.SerializedName

class MonitoringModel(
    val name: String,
    val idNumber: String,
    val address: Int,
    var checked: Boolean,
    var ratio: Number,
    var minRange: Number,
    var maxRange: Number,
    var value: Number,
    val unit: String
)

class NewMonitoringModel {

    @SerializedName("id")
    var id: String? = null

    @SerializedName("shortName")
    var shortName: String? = null

    @SerializedName("nameResId")
    var nameResId: Int = 0

    @SerializedName("address")
    var modBusAddress: Int? = null

    @SerializedName("description")
    var descriptionResId: Int = 0

    @SerializedName("minVal")
    var minRange: Number? = null

    @SerializedName("maxVal")
    var maxRange: Number? = null

    @SerializedName("unit")
    var unit: String? = null

    @SerializedName("ratio")
    var ratio: Number? = null

    @SerializedName("value")
    var value: Number? = null

    @SerializedName("checked")
    var checked = false
}