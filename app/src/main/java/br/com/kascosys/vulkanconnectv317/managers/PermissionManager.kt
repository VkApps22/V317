package br.com.kascosys.vulkanconnectv317.managers

import android.content.Context
import br.com.kascosys.vulkanconnectv317.enums.UserPermission
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusValues
import br.com.kascosys.vulkanconnectv317.utils.singleton.SingletonHolder

class PermissionManager private constructor(context: Context){

    private var _permission: UserPermission

    init{
        _permission = UserPermission.BASIC
    }

    var permission: UserPermission
        get() = _permission
        set(value) {
            _permission = value
        }

    companion object : SingletonHolder<PermissionManager, Context>(::PermissionManager)
}