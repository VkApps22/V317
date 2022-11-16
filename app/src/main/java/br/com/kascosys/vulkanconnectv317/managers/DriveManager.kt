package br.com.kascosys.vulkanconnectv317.managers

import android.content.Context
import android.util.Log
import br.com.kascosys.vulkanconnectv317.constants.MAX_DRIVE_SIZE
import br.com.kascosys.vulkanconnectv317.constants.MIN_DRIVE_SIZE
import br.com.kascosys.vulkanconnectv317.enums.LockState
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusValues
import br.com.kascosys.vulkanconnectv317.utils.singleton.SingletonHolder

class DriveManager private constructor(context: Context) {

    private var _driveSize = 10
    var size
        get() = _driveSize
        set(value) {
            if (value < MIN_DRIVE_SIZE || value > MAX_DRIVE_SIZE) {
                Log.e("DriveManager", "setSize out of range $value")

                return
            }

            _driveSize = value
        }

    fun getFormattedModel(): String {
        return "V317-${_driveSize}A"
    }

    private var _nickName = ""
    var name
        get() = _nickName
        set(value) {
            _nickName = value
        }

    private var _lockState = LockState.LOCKED
    val lockState
        get() = _lockState

    fun setLocked() {
        _lockState = LockState.LOCKED
    }

    fun setUnlocked() {
        _lockState = LockState.UNLOCKED
    }

    companion object : SingletonHolder<DriveManager, Context>(::DriveManager)
}