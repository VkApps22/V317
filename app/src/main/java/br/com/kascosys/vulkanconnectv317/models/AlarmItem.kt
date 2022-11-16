package br.com.kascosys.vulkanconnectv317.models

import android.util.Log
import br.com.kascosys.vulkanconnectv317.interfaces.AlarmListItem

class AlarmItem(
    val data: AlarmModel,
    startSection: Int
) : AlarmListItem {

    override var state: Int = AlarmListItem.stateConstants.INACTIVE
        set(value) {
            when (value) {
                AlarmListItem.stateConstants.ACTIVE -> field = value
                AlarmListItem.stateConstants.INACTIVE -> field = value
                else -> Log.e("AlarmHeader", "setState state out of range")
            }
        }

    override val listItemType: Int
        get() = AlarmListItem.typeConstants.ITEM


    override var section: Int = startSection
}