package br.com.kascosys.vulkanconnectv317.interfaces

interface ParameterListItem {

    object typeConstants{
        val HEADER = 0
        val ITEM = 1
        val CUSTOM_HEADER = 2
    }

    object permissionConstants{
        val BASIC = 0
        val ADVANCED = 1
    }

    val listItemType: Int
    val kind: Int
    var section: Int
}