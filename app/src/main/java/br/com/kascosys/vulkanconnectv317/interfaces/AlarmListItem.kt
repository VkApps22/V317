package br.com.kascosys.vulkanconnectv317.interfaces

interface AlarmListItem {

    object typeConstants{
        val HEADER = 0
        val ITEM = 1
        val CUSTOM_HEADER = 2
    }

    object stateConstants{
        val ACTIVE = 0
        val INACTIVE = 1
    }

    val listItemType: Int
    var state: Int
    var section: Int
}