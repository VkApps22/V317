package br.com.kascosys.vulkanconnectv317.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import br.com.kascosys.vulkanconnectv317.managers.MonitoringManager
import br.com.kascosys.vulkanconnectv317.models.MainsModel
import br.com.kascosys.vulkanconnectv317.models.MonitoringModel
import kotlin.coroutines.coroutineContext

class MonitoringViewModel: ViewModel() {

    private var _monitoringList: MutableList<MonitoringModel>

    init {
        val monitoringManager = MonitoringManager.getInstance()

        _monitoringList = mutableListOf()
        monitoringManager?.getAll()?.forEach {
            monitoringList.add(
                MonitoringModel(
                    "",
                    it.id!!,
                    it.modBusAddress!!,
                    it.checked,
                    it.ratio!!,
                    it.minRange!!,
                    it.maxRange!!,
                    it.value!!,
                    it.unit!!
                )
            )
        }
    }

    val monitoringList: MutableList<MonitoringModel>
        get() = _monitoringList

    private val _mains = MainsModel()
    val mains: MainsModel
        get() = _mains

}