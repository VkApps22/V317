package br.com.kascosys.vulkanconnectv317.models

class AlertsFirebaseModel {
    var language: String? = ""
    var alerts: MutableList<AlertModel> = mutableListOf()

    fun isEmpty() : Boolean {
        return alerts.isEmpty()
    }
}
data class AlertModel(
    var id: String = "",
    var description: String = "",
    var label: String = ""
)