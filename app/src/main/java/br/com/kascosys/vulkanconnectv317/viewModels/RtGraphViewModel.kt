package br.com.kascosys.vulkanconnectv317.viewModels

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.LineData

class RtGraphViewModel : ViewModel() {
    private val _graphData = MutableLiveData<LineData>()
    var data: LineData
        get() {
            Log.i("RtGraphViewModel","getData----------")

            if (_graphData.value != null) {
                Log.i("RtGraphViewModel","getData will return ${_graphData.value}")

                return _graphData.value!!
            }

            Log.i("RtGraphViewModel","getData will return empty")

            _graphData.value = LineData()
            return _graphData.value!!
        }
        set(value) {
            Log.i("RtGraphViewModel","setData $value----------")

            _graphData.value = value
        }

    private val _initialTime = MutableLiveData<Long>()
    var initialTime: Long
        get() {
            Log.i("RtGraphViewModel","getInitialTime----------")

            if(_initialTime.value != null){
                Log.i("RtGraphViewModel","getInitialTime will return ${_initialTime.value}")

                return _initialTime.value!!
            }

            Log.i("RtGraphViewModel","getInitialTime will return empty")

            _initialTime.value = SystemClock.elapsedRealtime()
            return _initialTime.value!!
        }
        set(value) {
            Log.i("RtGraphViewModel","setInitialValue $value----------")

            _initialTime.value = value
        }

    fun clearData(){
        _graphData.value = LineData()
    }

    private val _graphPlayState = MutableLiveData<Boolean>(true)
    var graphPlayState: Boolean
        get() =  _graphPlayState.value ?: run {
            _graphPlayState.value = false
            false
        }
        set(value) {
            _graphPlayState.value  = value
        }
}