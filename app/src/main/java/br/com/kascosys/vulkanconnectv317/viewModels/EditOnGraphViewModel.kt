package br.com.kascosys.vulkanconnectv317.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.kascosys.vulkanconnectv317.constants.C000
import br.com.kascosys.vulkanconnectv317.constants.P128
import br.com.kascosys.vulkanconnectv317.constants.P171
import br.com.kascosys.vulkanconnectv317.constants.READ_ERROR
import br.com.kascosys.vulkanconnectv317.enums.ParameterState
import br.com.kascosys.vulkanconnectv317.managers.MonitoringManager
import br.com.kascosys.vulkanconnectv317.managers.ParameterManager
import kotlin.math.absoluteValue
import kotlin.random.Random

class EditOnGraphViewModel: ViewModel() {
    val maxInt = 2147483648.0
    val range = 100

    private val _c000val = MutableLiveData<Double>()
    val c000val: LiveData<Double>
        get() = _c000val

    private val _p128val = MutableLiveData<Double>()
    val p128val: LiveData<Double>
        get() = _p128val

    private val _p171val = MutableLiveData<Double>()
    val p171val: LiveData<Double>
        get() = _p171val

    private val _generalState = MutableLiveData<ParameterState>()
    var generalState: ParameterState
        get() = _generalState.value!!
        set(value) {
            _generalState.value = value
        }

    init {
//        _c000val.value = Random.nextInt().absoluteValue / maxInt * range
//        _p128val.value = Random.nextInt().absoluteValue / maxInt * range
//        _p171val.value = Random.nextInt().absoluteValue / maxInt * range
        _c000val.value = ParameterManager.getInstance()?.getBy(C000)?.value?.toDouble()
        _p128val.value = ParameterManager.getInstance()?.getBy(P128)?.value?.toDouble()
        _p171val.value = ParameterManager.getInstance()?.getBy(P171)?.value?.toDouble()

        _generalState.value = ParameterState.UNEDITED
    }

    fun setC000(value: Double) {
//        if (value <= range) {
            _c000val.value = value
//        }
    }

    fun setP128(value: Double) {
//        if (value <= range) {
            _p128val.value = value
//        }
    }

    fun setP171(value: Double) {
//        if (value <= range) {
            _p171val.value = value
//        }
    }

    fun applyDigitsFilter(value: Double): String {
        return "%.2f".format(value)
    }

    fun getUnit(id: String): String {
        val unit = ParameterManager.getInstance()?.getBy(id)?.unit
        if(unit != null){
            return unit
        }

        return ""
    }

}