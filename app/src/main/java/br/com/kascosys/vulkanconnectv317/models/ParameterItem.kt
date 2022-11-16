package br.com.kascosys.vulkanconnectv317.models

import android.util.Log
import br.com.kascosys.vulkanconnectv317.interfaces.ParameterListItem

class ParameterItem(
//    val data: ParametersModel,
    val data: NewParameterModel,
    startSection: Int,
    override val kind: Int) : ParameterListItem {

//    override var kind: Int = ParameterListItem.permissionConstants.ADVANCED
//        set(value) {
//            when (value) {
//                ParameterListItem.permissionConstants.BASIC -> field = value
//                ParameterListItem.permissionConstants.ADVANCED -> field = value
//                else -> Log.e("ParameterHeader", "setState state out of range")
//            }
//        }

    override val listItemType: Int
        get() = ParameterListItem.typeConstants.ITEM


    override var section: Int = startSection
}