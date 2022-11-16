package br.com.kascosys.vulkanconnectv317.managers

import android.content.Context
import android.os.SystemClock
import br.com.kascosys.vulkanconnectv317.constants.CONNECTION_TRIALS
import br.com.kascosys.vulkanconnectv317.utils.singleton.SingletonHolder

class OnlineManager private constructor(context: Context){

    private var _onlineMode: Boolean

    private var _trials: Int

    private var _lastActionTime: Long

    init{
        _onlineMode = true

        _trials = CONNECTION_TRIALS

        _lastActionTime = 0L
    }

    var onlineModeOn: Boolean
        get() = _onlineMode
        private set(value) {
            _onlineMode = value
        }

    var trials: Int
        get() = _trials
        set(value) {
            _trials = value
        }

    var lastTime: Long
        get() {
            if(_lastActionTime == 0L){
                _lastActionTime = SystemClock.elapsedRealtime()
            }

            return _lastActionTime
        }
        set(value) {
            _lastActionTime = value
        }

    fun resetTrials() {
        _trials = 0
    }

    fun goOnline() {
        onlineModeOn = true
    }

    fun goOffline() {
        onlineModeOn = false
    }

    companion object : SingletonHolder<OnlineManager, Context>(::OnlineManager)
}