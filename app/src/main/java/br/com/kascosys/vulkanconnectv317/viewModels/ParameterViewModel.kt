package br.com.kascosys.vulkanconnectv317.viewModels

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.C000
import br.com.kascosys.vulkanconnectv317.constants.P128
import br.com.kascosys.vulkanconnectv317.constants.P171
import br.com.kascosys.vulkanconnectv317.enums.ParameterState
import br.com.kascosys.vulkanconnectv317.enums.UserPermission
import br.com.kascosys.vulkanconnectv317.interfaces.ParameterListItem
import br.com.kascosys.vulkanconnectv317.managers.ParameterManager
import br.com.kascosys.vulkanconnectv317.models.*
import kotlin.math.absoluteValue
import kotlin.random.Random

class ParameterViewModel : ViewModel() {

    private val maxInt = 2147483648.0
    private val range = 100

    private var basicSection = 0
    private var advancedSection = 5

    private val _permission = MutableLiveData<Boolean>(false)
    val permission: LiveData<Boolean>
        get() = _permission

    private val _basicParameterList: MutableList<ParameterListItem>

    private val _advancedParameterList: MutableList<ParameterListItem>

    private val _generalParameterState = MutableLiveData<ParameterState>(ParameterState.UNEDITED)
    val generalParameterState: LiveData<ParameterState>
        get() {
            return _generalParameterState
        }

    private var _fullParameterList: MutableList<ParameterListItem>

    private val _basicHeaderList: MutableList<ParameterListItem>

    init {
        _permission.value = false

        _basicParameterList = mutableListOf()
        ParameterManager
            .getInstance()?.getAllBasic()?.forEach {
                _basicParameterList.add(
                    ParameterItem(
                        it,
                        basicSection,
                        ParameterListItem.permissionConstants.BASIC
                    )
                )
            }

        _basicHeaderList = mutableListOf(
            ParameterHeader(
                basicSection,
                ParameterListItem.permissionConstants.BASIC
            )
        )

        advancedSection = _basicParameterList.size + 1

        _advancedParameterList = mutableListOf(
            ParameterHeader(
                advancedSection,
                ParameterListItem.permissionConstants.ADVANCED
            )
        )
        ParameterManager
            .getInstance()?.getAllAdvanced()?.forEach {
                _advancedParameterList.add(
                    ParameterItem(
                        it,
                        advancedSection,
                        ParameterListItem.permissionConstants.ADVANCED
                    )
                )
            }

        _fullParameterList =
            (_basicParameterList).toMutableList()

    }

    fun setPermission(value: Boolean) {
        if (value && !_permission.value!!) {
            notifyPermissionChanged(UserPermission.ADVANCED)
        }

        _permission.value = value
    }


    fun setGeneralState(value: ParameterState) {
        _generalParameterState.value = value
    }


//    private val _basicParameterList: MutableList<ParameterListItem> = mutableListOf(
//        ParameterItem(
//            ParametersModel(
//                "Economic Current Reference", P128,
//                false, 0.0, range.toDouble(), getRandomDouble(range)
//            ), basicSection, ParameterListItem.permissionConstants.BASIC
//        ),
//        ParameterItem(
//            ParametersModel(
//                "Call Current Time", "P171",
//                false, 0.0, range.toDouble(), getRandomDouble(range)
//            ), basicSection, ParameterListItem.permissionConstants.BASIC
//        ),
//        ParameterItem(
//            ParametersModel(
//                "Brake Opening Confirmation Time", "P177",
//                false, 0.0, range.toDouble(), getRandomDouble(range)
//            ), basicSection, ParameterListItem.permissionConstants.BASIC
//        ),
//        ParameterItem(
//            ParametersModel(
//                "Rated Call Current", "C000",
//                false, 0.0, range.toDouble(), getRandomDouble(range)
//            ), basicSection, ParameterListItem.permissionConstants.BASIC
//        )
//    )

//    private val _advancedParameterList: MutableList<ParameterListItem> = mutableListOf(
//
//        ParameterHeader(
//            advancedSection,
//            ParameterListItem.permissionConstants.ADVANCED
//        ),
//        ParameterItem(
//            ParametersModel(
//                "Current Loop Proportional Gain", "P100",
//                true, 0.0, range.toDouble(), getRandomDouble(range)
//            ), advancedSection, ParameterListItem.permissionConstants.ADVANCED
//        ),
//        ParameterItem(
//            ParametersModel(
//                "Current loop Integral time with discontinuous current conduction", "P101",
//                true, 0.0, range.toDouble(), getRandomDouble(range)
//            ), advancedSection, ParameterListItem.permissionConstants.ADVANCED
//        ),
//        ParameterItem(
//            ParametersModel(
//                "Current loop Integral time with continuous current conduction", "P102",
//                true, 0.0, range.toDouble(), getRandomDouble(range)
//            ), advancedSection, ParameterListItem.permissionConstants.ADVANCED
//        ),
//        ParameterItem(
//            ParametersModel(
//                "Minimum Firing Angle", "P230",
//                true, 0.0, range.toDouble(), getRandomDouble(range)
//            ), advancedSection, ParameterListItem.permissionConstants.ADVANCED
//        ),
//        ParameterItem(
//            ParametersModel(
//                "Maximum Firing Angle", "P231",
//                true, 0.0, range.toDouble(), getRandomDouble(range)
//            ), advancedSection, ParameterListItem.permissionConstants.ADVANCED
//        ),
//        ParameterItem(
//            ParametersModel(
//                "Average Current in Full Cycle", "C001",
//                true, 0.0, range.toDouble(), getRandomDouble(range)
//            ), advancedSection, ParameterListItem.permissionConstants.ADVANCED
//        )
//    )

    val parameterList: MutableList<ParameterListItem>
        get() {
//            return if (permission.value!!) {
//                Log.i("ParameterViewModel", "parameterList.get adv")
//                _fullParameterList
//            } else {
//                Log.i("ParameterViewModel", "parameterList.get bas")
//                _basicParameterList
//            }

            return _fullParameterList
        }

//    private val _parameterList = ParameterList()
//
//    fun getParameterById(id: String): NewParameterModel {
//        return _parameterList.parameterList.first {
//            it.id == id
//        }
//    }

    fun notifyPermissionChanged(permission: UserPermission) {
        if(permission == UserPermission.ADVANCED) {
            _fullParameterList.add(0, _basicHeaderList[0])
            _advancedParameterList.forEach {
                _fullParameterList.add(it)
            }
        } else {
            _fullParameterList.remove(_basicHeaderList[0])
            _advancedParameterList.forEach {
                _fullParameterList.remove(it)
            }
        }
    }

    fun notifyGraphParametersChanged() {
        val graphParameters = listOf(C000, P128, P171)

        graphParameters.forEach { parameter ->
            val item = _basicParameterList.first {
                if (it is ParameterItem) {
                    it.data.id == parameter
                } else false
            } as ParameterItem
            item.data.value = ParameterManager.getInstance()?.getBy(parameter)?.value
        }
    }

    private fun getRandomDouble(range: Int): Double {
        return Random.nextInt().absoluteValue / maxInt * range
    }

    fun setParameterValue(id: String, value: Double) {
        val index = parameterList.indexOfFirst {
            it.listItemType == ParameterListItem.typeConstants.ITEM &&
                    (it as ParameterItem).data.id == id
        }
        (_fullParameterList[index] as ParameterItem).data.value = value

    }

}