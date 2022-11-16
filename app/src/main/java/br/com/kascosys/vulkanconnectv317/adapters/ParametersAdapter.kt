package br.com.kascosys.vulkanconnectv317.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.P171
import br.com.kascosys.vulkanconnectv317.constants.P177
import br.com.kascosys.vulkanconnectv317.constants.READ_ERROR
import br.com.kascosys.vulkanconnectv317.enums.DataType
import br.com.kascosys.vulkanconnectv317.enums.ParameterState
import br.com.kascosys.vulkanconnectv317.enums.UserPermission
import br.com.kascosys.vulkanconnectv317.interfaces.GraphClickListener
import br.com.kascosys.vulkanconnectv317.interfaces.OnSlide
import br.com.kascosys.vulkanconnectv317.interfaces.ParameterListItem
import br.com.kascosys.vulkanconnectv317.managers.DriveManager
import br.com.kascosys.vulkanconnectv317.managers.PermissionManager
import br.com.kascosys.vulkanconnectv317.models.NewParameterModel
import br.com.kascosys.vulkanconnectv317.models.ParameterItem
import br.com.kascosys.vulkanconnectv317.utils.DataUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.shuhart.stickyheader.StickyAdapter
import java.text.DecimalFormatSymbols
import java.text.NumberFormat


class ParametersAdapter(
    private val myParametersDataSet: MutableList<ParameterListItem>,
    private val slideListener: OnSlide,
    private val graphClickListener: GraphClickListener,
    private val permissionManager: PermissionManager,
    private val driveManager: DriveManager
) :
    StickyAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>() {

    private val ITEM_VIEW_TYPE2 = 1
    private val HEADER_VIEW_TYPE = 2

    private val HEADER = ParameterListItem.typeConstants.HEADER
    private val ITEM = ParameterListItem.typeConstants.ITEM
    private val CUSTOM_HEADER = ParameterListItem.typeConstants.CUSTOM_HEADER

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.i("ParametersAdapter", "onCreateViewHolder------viewType:$viewType")

        when (viewType) {
            HEADER_VIEW_TYPE -> return ParameterHeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE2 -> return ParameterItemViewHolder.from(parent)
            else -> Log.e(
                "ParametersAdapter",
                "onCreateViewHolder Type out of bounds"
            )
        }

        return ParameterItemViewHolder.from(parent)
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        Log.i("ParametersAdapter", "onCreateHeaderViewHolder------")
        return createViewHolder(parent, HEADER_VIEW_TYPE)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        Log.i("ParametersAdapter", "onBindViewHolder------ pos:$position")

        val sectionType = myParametersDataSet[position].kind

        when (viewHolder) {
            is ParameterItemViewHolder -> viewHolder.bind(
                (myParametersDataSet[position] as ParameterItem).data,
                slideListener, graphClickListener, permissionManager, driveManager
            )
            is ParameterHeaderViewHolder -> viewHolder.bind(sectionType)
            else -> Log.e(
                "ParametersAdapter",
                "onBindViewHolder Type out of bounds"
            )
        }
    }

    override fun onBindHeaderViewHolder(viewHolder: RecyclerView.ViewHolder?, i: Int) {
        Log.i("ParametersAdapter", "onBindHeaderViewHolder------pos $i")

        val sectionType = myParametersDataSet[i].kind

        (viewHolder as ParameterHeaderViewHolder).bind(sectionType)
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        Log.i("ParametersAdapter", "getHeaderPositionForItem------pos $itemPosition")

        return myParametersDataSet[itemPosition].section
    }


    override fun getItemViewType(position: Int): Int {

        Log.i("ParametersAdapter", "getItemViewType------pos $position")
        when (myParametersDataSet[position].listItemType) {
            HEADER, CUSTOM_HEADER -> return HEADER_VIEW_TYPE
            ITEM -> return ITEM_VIEW_TYPE2
            else -> Log.e(
                "ParametersAdapter",
                "onCreateViewHolder Type out of bounds"
            )
        }

        return HEADER_VIEW_TYPE
    }

    override fun getItemCount(): Int {
        Log.i("ParametersAdapter", "getItemCount------size ${myParametersDataSet.size}")


        return myParametersDataSet.size
    }

    class ParameterItemViewHolder private constructor(itemView: View, val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val parameterName: TextView =
            itemView.findViewById(R.id.parameter_name_text)
        private val parameterNumberName: TextView =
            itemView.findViewById(R.id.parameter_name_number_text)
        private val parameterValue: TextInputEditText =
            itemView.findViewById(R.id.parameter_value_edittext)
        private val parameterDefaultText: TextView =
            itemView.findViewById(R.id.parameter_card_default_text)

        private val parameterField: TextInputLayout =
            itemView.findViewById(R.id.parameter_value_text)

        private val minText: TextView =
            itemView.findViewById(R.id.minText)
        private val maxText: TextView =
            itemView.findViewById(R.id.maxText)

        private val graphIcon: ImageView = itemView.findViewById(R.id.parameter_card_graph)

        companion object {
            fun from(parent: ViewGroup): ParameterItemViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.parameter_card2,
                        parent,
                        false
                    )

                return ParameterItemViewHolder(itemView, parent.context)
            }
        }


        fun bind(
            parametersModel: NewParameterModel,
            slideListener: OnSlide,
            graphClickListener: GraphClickListener,
            permissionManager: PermissionManager,
            driveManager: DriveManager
        ) {
            Log.i(
                "ParameterItemViewHolder",
                "bind advancedParameter ${parametersModel.advancedParameter}"
            )

            if (DataUtils.isGraphParameter(parametersModel.id!!)) {
                graphIcon.visibility = View.VISIBLE
                graphIcon.setOnClickListener {
                    graphClickListener.onGraphButtonClick(adapterPosition)
                }
            } else {
                graphIcon.visibility = View.INVISIBLE
            }

            val defaultVal = when (driveManager.size) {
                40 -> parametersModel.defaultVal40
                70 -> parametersModel.defaultVal70
                27 -> parametersModel.defaultVal40
                else -> null
            }

            val exhibitType = DataUtils.getExhibitType(parametersModel.unit!!)


            if (parametersModel.id == P177) {
                parameterDefaultText.visibility = View.VISIBLE
                parameterDefaultText.text =
                    getDefaultCharSequence(
                        exhibitType,
                        defaultVal ?: 0,
                        parametersModel,
                        driveManager.size
                    )
            } else {
                parameterDefaultText.visibility = View.GONE
            }

            val minWrite = if (permissionManager.permission == UserPermission.BASIC) {
                parametersModel.minWrite
            } else parametersModel.minAdvanced
            val maxWrite = if (permissionManager.permission == UserPermission.BASIC) {
                parametersModel.maxWrite
            } else parametersModel.maxAdvanced

            val minString = "$minWrite"
            val maxString = "$maxWrite"

//            val unitString = " ${parametersModel.unit!!}"
            var valueUnitString = " ${DataUtils.getFormattedUnit(
                parametersModel.unit!!,
                parametersModel.value!!.toInt(),
                context
            )}"

            val minVal = getRangeFromNumber(
                minWrite ?: 0,
                exhibitType ?: DataType.NUMBER_DATA,
                driveManager.size
            )

            val maxVal = getRangeFromNumber(
                maxWrite ?: 0,
                exhibitType ?: DataType.NUMBER_DATA,
                driveManager.size
            )

            @SuppressLint("SetTextI18n")
            this.minText.text = minString + valueUnitString

            @SuppressLint("SetTextI18n")
            this.maxText.text = maxString + valueUnitString

            Log.d(
                "ParameterItemViewHolder", "bind value ${
                parametersModel.value
                }"
            )

            val value = parametersModel.value ?: READ_ERROR

            Log.d(
                "ParameterItemViewHolder",
                "bind slider vals $minVal $maxVal $value $READ_ERROR"
            )


            Log.d("ParameterItemViewHolder", "bind maxRange ${parametersModel.maxRange}")


            @SuppressLint("SetTextI18n")

            if (value != READ_ERROR) {
                Log.i("ParameterItemViewHolder", "bind value ok")

                parameterValue.setText(
                    DataUtils.getFormatted(
                        value,
                        parametersModel.unit!!,
                        driveManager.size
                    ) + valueUnitString
                )
            } else {
                Log.i("ParameterItemViewHolder", "bind value is from reading error")

                parameterValue.setText(R.string.value_error)
            }

            Log.d("ParameterItemViewHolder", "bind background")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                parameterField.boxBackgroundColor = context.resources.getColor(
                    when (parametersModel.state) {
                        ParameterState.UNEDITED -> R.color.colorWhite
                        ParameterState.EDITED -> R.color.colorAccentMid
                        ParameterState.UNSAVED -> R.color.colorAccentLight
                    }, context.resources.newTheme()
                )
            } else {
                @Suppress("DEPRECATION")
                parameterField.boxBackgroundColor = context.resources.getColor(
                    when (parametersModel.state) {
                        ParameterState.UNEDITED -> R.color.colorWhite
                        ParameterState.EDITED -> R.color.colorAccentMid
                        ParameterState.UNSAVED -> R.color.colorAccentLight
                    }
                )
            }

            this.parameterName.text = context.resources.getString(parametersModel.nameResId)
            this.parameterNumberName.text = parametersModel.id

            val inputManager: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            parameterValue.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {

                    if (parameterValue.text != null) {
                        if (parameterValue.text!!.contains(valueUnitString)) {
                            parameterValue.setText(parameterValue.text!!.split(valueUnitString)[0])
                        }
                    }

                    when (DataUtils.getExhibitType(parametersModel.unit!!)) {
                        DataType.BIT_RATE_DATA -> {
                            parameterValue.clearFocus()

                            val menu = PopupMenu(context, parameterValue)
                            menu.menuInflater.inflate(R.menu.menu_bps, menu.menu)
                            menu.setOnMenuItemClickListener {
                                val baudRate = it.title.removeSuffix(" $valueUnitString").toString()
                                parameterValue.setText(baudRate)

                                val valueFloat = DataUtils.mapFromBitrate(baudRate.toInt())
                                slideListener.slideListener(adapterPosition, valueFloat)

                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)

                                true
                            }
                            menu.setOnDismissListener {
                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)
                            }
                            menu.show()
                        }
                        DataType.YES_NO_DATA -> {
                            parameterValue.clearFocus()

                            val menu = PopupMenu(context, parameterValue)
                            menu.menuInflater.inflate(R.menu.menu_yes_no, menu.menu)
                            menu.setOnMenuItemClickListener {
                                val text = it.title
                                parameterValue.setText(text)

                                val valueFloat = DataUtils.mapFromYesNo(text.toString())
                                slideListener.slideListener(adapterPosition, valueFloat)

                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)

                                true
                            }
                            menu.setOnDismissListener {
                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)
                            }
                            menu.show()
                        }
                        DataType.EXCLUDED_DATA -> {
                            parameterValue.clearFocus()

                            val menu = PopupMenu(context, parameterValue)
                            menu.menuInflater.inflate(R.menu.menu_excluded, menu.menu)
                            menu.setOnMenuItemClickListener {
                                val text = it.title
                                parameterValue.setText(text)

                                val valueFloat = DataUtils.mapFromExcluded(text.toString())
                                slideListener.slideListener(adapterPosition, valueFloat)

                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)

                                true
                            }
                            menu.setOnDismissListener {
                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)
                            }
                            menu.show()
                        }
                        DataType.PARITY_DATA -> {
                            parameterValue.clearFocus()

                            val menu = PopupMenu(context, parameterValue)
                            menu.menuInflater.inflate(R.menu.menu_parity, menu.menu)
                            menu.setOnMenuItemClickListener {
                                val text = it.title
                                parameterValue.setText(text)

                                val valueFloat = DataUtils.mapFromParity(text.toString())
                                slideListener.slideListener(adapterPosition, valueFloat)

                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)

                                true
                            }
                            menu.setOnDismissListener {
                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)
                            }
                            menu.show()
                        }
                        DataType.ANALOG_CONFIG_DATA -> {
                            parameterValue.clearFocus()

                            val menu = PopupMenu(context, parameterValue)
                            menu.menuInflater.inflate(R.menu.menu_analog_config, menu.menu)
                            menu.setOnMenuItemClickListener {
                                val text = it.title
                                parameterValue.setText(text)

                                val valueFloat = DataUtils.mapFromAnalogConfig(text.toString())
                                slideListener.slideListener(adapterPosition, valueFloat)

                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)

                                true
                            }
                            menu.setOnDismissListener {
                                inputManager.hideSoftInputFromWindow(parameterValue.windowToken, 0)
                            }
                            menu.show()
                        }

                        else -> {
                            Log.i(
                                "ParameterItemViewHolder",
                                "onFocusChangeListener no menu"
                            )
                        }
                    }
                } else {

                    Log.i(
                        "ParameterItemViewHolder",
                        "onFocusChangeListener no focus ${parameterValue.text}"
                    )

                    valueUnitString = DataUtils.getFormattedUnit(
                        parametersModel.unit!!,
                        parameterValue.text.toString().toIntOrNull() ?: 0,
                        context
                    )

                    parameterValue.setText(
                        parameterValue.text.toString() + valueUnitString
                    )
                }
            }

            this.parameterValue.setOnEditorActionListener { textView, actionId, _ ->
                Log.i("ParameterItemViewHolder", "onEditorActionListener-------------")

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.i("ParameterItemViewHolder", "onEditorActionListener IME_ACTION_DONE")


                    Log.i("ParameterItemViewHolder", "onEditorActionListener will hide")


                    inputManager.hideSoftInputFromWindow(textView.windowToken, 0)

                    Log.d("ParameterItemViewHolder", "onEditorActionListener will get value")


                    val valueString =
                        parameterValue.text.toString()
                            .removeSuffix(valueUnitString)
                            .removePrefix("+")

                    val separator = DecimalFormatSymbols.getInstance()
                        .decimalSeparator

                    Log.i(
                        "ParameterItemViewHolder",
                        "onEditorActionListener separator $separator"
                    )

                    val sepValueString = if (separator == ',') {
                        valueString.replace('.', separator)
                    } else {
                        valueString.replace(',', separator)
                    }

                    Log.i(
                        "ParameterItemViewHolder",
                        "onEditorActionListener corrected separator valueString $sepValueString"
                    )

                    var finalValueString = sepValueString
                    var testValueString = sepValueString
                    var sepCount = 0
                    while (testValueString.contains(separator)) {
                        testValueString =
                            testValueString.substringBeforeLast(separator) + testValueString.substringAfterLast(
                                separator
                            )
                        Log.i(
                            "ParameterItemViewHolder",
                            "onEditorActionListener subs separator $testValueString $sepCount"
                        )

                        sepCount++
                    }

                    if (sepCount > 1) {
                        finalValueString = ""
                    }

                    Log.i(
                        "ParameterItemViewHolder",
                        "onEditorActionListener final valueString $finalValueString"
                    )

                    val valueNumber =
                        if (finalValueString != "-"
                            && finalValueString.lastIndexOf('-') < 1
                            && finalValueString != ""
                            && finalValueString != separator.toString()
                        ) {
                            NumberFormat.getInstance().parse(
                                finalValueString
                            )
                        } else null

                    var valueFloat = valueNumber?.toFloat()

                    Log.i("ParameterItemViewHolder", "onEditorActionListener will print value")
                    Log.i(
                        "ParameterItemViewHolder",
                        "onEditorActionListener raw value float $valueFloat"
                    )

                    if (valueFloat != null) {
                        when {
                            valueFloat > maxVal ->
                                valueFloat =
                                    maxVal
                            valueFloat < minVal ->
                                valueFloat =
                                    minVal
                        }
                    } else {
                        valueFloat = getRangeFromNumber(
                            parametersModel.lastVal!!,
                            exhibitType ?: DataType.NUMBER_DATA,
                            driveManager.size
                        )
                    }

                    Log.i(
                        "ParameterItemViewHolder",
                        "onEditorActionListener valueFloat $valueFloat"
                    )

                    parameterValue.setText(
                        DataUtils.getFormatted(
                            valueFloat,
                            parametersModel.unit!!
                        )
                    )
                    slideListener.slideListener(
                        adapterPosition,
                        valueFloat.toDefaultScale(
                            exhibitType ?: DataType.NUMBER_DATA,
                            driveManager.size
                        )
                    )

                    Log.i(
                        "ParameterItemViewHolder",
                        "onEditorActionListener value text ${parameterValue.text}"
                    )

                    parameterValue.clearFocus()

                    return@setOnEditorActionListener true
                }
                false
            }

            Log.i("ParameterItemViewHolder", "bind end----------------")

        }

        private fun getRangeFromString(
            rangeString: String,
            exhibitType: DataType,
            driveSize: Int
        ): Float {
            Log.i(
                "ParameterItemViewHolder",
                "getRangeFromString $rangeString $exhibitType $driveSize----------------"
            )

            val percentage =
                (NumberFormat.getInstance().parse(rangeString)?.toFloat() ?: 0.0f)

            return if (exhibitType == DataType.CURRENT_DATA) {
                DataUtils.getValueFromPercentage(percentage, driveSize).toFloat()
            } else percentage
        }

        private fun getRangeFromNumber(
            rangeNumber: Number,
            exhibitType: DataType,
            driveSize: Int
        ): Float {
            Log.i(
                "ParameterItemViewHolder",
                "getRangeFromString $rangeNumber $exhibitType $driveSize----------------"
            )

            val percentage =
                rangeNumber.toFloat()

            return if (exhibitType == DataType.CURRENT_DATA) {
                DataUtils.getValueFromPercentage(percentage, driveSize).toFloat()
            } else percentage
        }

        private fun getDefaultCharSequence(
            exhibitType: DataType?,
            defaultVal: Number,
            parametersModel: NewParameterModel,
            driveSize: Int
        ): CharSequence? {
            return when (exhibitType) {
                DataType.NUMBER_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_float_template,
                        0,
                        defaultVal.toFloat(),
                        DataUtils.getFormattedUnit(
                            parametersModel.unit!!,
                            defaultVal.toInt(),
                            context
                        )
                    )
                }
                DataType.BIT8_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_int_template,
                        0, defaultVal.toInt(), DataUtils.getFormattedUnit(
                            parametersModel.unit!!,
                            defaultVal.toInt(),
                            context
                        )
                    )
                }
                DataType.BIT6_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_int_template,
                        0, defaultVal.toInt(), DataUtils.getFormattedUnit(
                            parametersModel.unit!!,
                            defaultVal.toInt(),
                            context
                        )
                    )
                }
                DataType.PHASE_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_float_template,
                        0, defaultVal.toFloat(), DataUtils.getFormattedUnit(
                            parametersModel.unit!!,
                            defaultVal.toInt(),
                            context
                        )
                    )
                }
                DataType.INTEGER_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_int_template,
                        0, defaultVal.toInt(), DataUtils.getFormattedUnit(
                            parametersModel.unit!!,
                            defaultVal.toInt(),
                            context
                        )
                    )
                }
                DataType.BIT_RATE_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_int_template,
                        0, defaultVal.toInt(), DataUtils.getFormattedUnit(
                            parametersModel.unit!!,
                            defaultVal.toInt(),
                            context
                        )
                    )
                }
                DataType.CURRENT_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_float_template,
                        0,
                        DataUtils.getValueFromPercentage(defaultVal, driveSize),
                        DataUtils.getFormattedUnit(
                            parametersModel.unit!!,
                            defaultVal.toInt(),
                            context
                        )
                    )
                }
                DataType.YES_NO_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_string_template,
                        0, context.resources.getString(R.string.parameter_yes_option)
                    )
                }
                DataType.EXCLUDED_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_string_template,
                        0, context.resources.getString(R.string.parameter_excluded_option)
                    )
                }
                DataType.PARITY_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_string_template,
                        0, context.resources.getString(R.string.parameter_parity_none_option)
                    )
                }
                DataType.LOOP_GAIN_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_string_template,
                        0, "%.2f".format(defaultVal.toFloat())
                    )
                }
                DataType.TIMES_DATA -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_int_template,
                        defaultVal.toInt(),
                        defaultVal.toInt(),
                        DataUtils.getFormattedUnit(
                            DataUtils.getFormattedUnit(
                                parametersModel.unit!!,
                                defaultVal.toInt(),
                                context
                            ),
                            defaultVal.toInt(),
                            context
                        )
                    )
                }
                else -> {
                    context.resources.getQuantityString(
                        R.plurals.parameter_card_default_string_template,
                        0, context.resources.getString(R.string.value_error)
                    )
                }
            }
        }

        private fun Float.toDefaultScale(exhibitType: DataType, driveSize: Int = 1): Number {
            Log.i(
                "ParameterItemViewHolder",
                "toDefaultScale $this $exhibitType $driveSize-------------"
            )

            return (
                    if (exhibitType == DataType.CURRENT_DATA) {
                        DataUtils.getPercentageFromValue(this, driveSize)
                    } else {
                        this
                    })
        }
    }

    class ParameterHeaderViewHolder private constructor(itemView: View, val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val BASIC = ParameterListItem.permissionConstants.BASIC
        private val ADVANCED = ParameterListItem.permissionConstants.ADVANCED

        private val header: TextView =
            itemView.findViewById(R.id.parameter_header_title)

        companion object {
            fun from(parent: ViewGroup): ParameterHeaderViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.parameter_header,
                        parent,
                        false
                    )

                return ParameterHeaderViewHolder(itemView, parent.context)
            }
        }

        fun bind(kind: Int) {
            Log.i(
                "ParameterHeaderViewHold",
                "bind advancedParameter kind $kind"
            )

            header.text =
                when (kind) {
                    BASIC -> context.getString(R.string.basic_parameters_header)
                    ADVANCED -> context.getString(R.string.advanced_parameters_header)
                    else -> "Test"
                }
        }
    }

}


