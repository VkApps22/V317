package br.com.kascosys.vulkanconnectv317.models

import android.util.Log
import br.com.kascosys.vulkanconnectv317.interfaces.AlarmListItem
import br.com.kascosys.vulkanconnectv317.interfaces.ParameterListItem

class ParameterHeader(startSection: Int, override val kind: Int) :
    ParameterListItem {


//    override var kind: Int = headerType
//        set(value) {
//            when (value) {
//                ParameterListItem.permissionConstants.BASIC -> field = value
//                ParameterListItem.permissionConstants.ADVANCED -> field = value
//                else -> Log.e("AlarmHeader", "setState state out of range")
//            }
//        }

    override val listItemType: Int
        get() = ParameterListItem.typeConstants.HEADER

    override var section: Int = startSection
}