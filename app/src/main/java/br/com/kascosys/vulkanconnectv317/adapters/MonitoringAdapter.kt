package br.com.kascosys.vulkanconnectv317.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.enums.DataType
import br.com.kascosys.vulkanconnectv317.interfaces.GraphClickListener
import br.com.kascosys.vulkanconnectv317.models.MonitoringModel
import br.com.kascosys.vulkanconnectv317.utils.DataUtils

class MonitoringAdapter(
    val graphClickListener: GraphClickListener,
    var myDataset: MutableList<MonitoringModel>,
    val driveSize: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val MONITORING_BIT_TYPE = 0
    private val MONITORING_GENERIC_TYPE = 1

    override fun getItemViewType(position: Int): Int {
        return when (DataUtils.getExhibitType(myDataset[position].unit)) {
            DataType.BIT8_DATA -> MONITORING_BIT_TYPE
            DataType.BIT6_DATA -> MONITORING_BIT_TYPE
            else -> MONITORING_GENERIC_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return GenericViewHolder.from(parent)
        when (viewType) {
            MONITORING_GENERIC_TYPE -> return GenericViewHolder.from(parent)
            MONITORING_BIT_TYPE -> return BitViewHolder.from(parent)
            else -> Log.e(
                "MonitoringAdapter",
                "onCreateViewHolder Type out of bounds"
            )
        }

        return GenericViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return myDataset.size
    }

    override fun onBindViewHolder(monitoringHolder: RecyclerView.ViewHolder, position: Int) {
//        Log.i("MonitoringAdapter", "onBindViewHolder autoMode $autoMode")

        val item = myDataset[position]
//        (monitoringHolder as GenericViewHolder).bind(
//            item,
//            graphClickListener,
//            driveSize
//        )
        when (monitoringHolder) {
            is GenericViewHolder -> monitoringHolder.bind(
                item,
                graphClickListener,
                driveSize
            )
            is BitViewHolder -> monitoringHolder.bind(
                item
            )
        }
    }


    class GenericViewHolder private constructor(itemView: View, val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val monitoringName: TextView = itemView.findViewById(R.id.monitoring_name_text)
        private val monitoringNumberName: TextView =
            itemView.findViewById(R.id.monitoring_number_name_text)
        private val parameterValue: TextView = itemView.findViewById(R.id.monitoring_value_text)
        private val graphButton: ImageButton =
            itemView.findViewById(R.id.monitoring_graph_imageButton)

        companion object {
            fun from(parent: ViewGroup): GenericViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.monitoring_card, parent, false)

                return GenericViewHolder(itemView, parent.context)
            }
        }

        fun bind(
            itemModel: MonitoringModel,
            graphClickListener: GraphClickListener,
            driveSize: Int
        ) {

            val id = itemModel.idNumber

            monitoringName.text = itemModel.name

            monitoringNumberName.text = if (id != OPEN_ID && id != CLOSE_ID) {
                itemModel.idNumber
            } else {
                ""
            }

            val exhibitType = DataUtils.getExhibitType(itemModel.unit)


            if (itemModel.value.toFloat() < READ_ERROR.toFloat()) {

                Log.i(
                    "GenericViewHolder",
                    "bind ${itemModel.idNumber} ${itemModel.unit} $exhibitType"
                )

                when (exhibitType) {
                    DataType.NUMBER_DATA -> {
                        when {
                            itemModel.ratio.toLong() > 10 -> parameterValue.text =
                                "${"%.2f".format(itemModel.value.toFloat())} ${itemModel.unit}"
                            itemModel.ratio.toLong() > 1 -> parameterValue.text =
                                "${"%.1f".format(itemModel.value.toFloat())} ${itemModel.unit}"
                            else -> parameterValue.text =
                                "${"%.1f".format(itemModel.value.toFloat())} ${itemModel.unit}"
                        }

                    }
                    DataType.TIME_DATA -> parameterValue.text =
                        DataUtils.getTimeString(itemModel.value.toInt())
                    DataType.BIT6_DATA -> parameterValue.text =
                        "%8s".format(
                            Integer.toBinaryString(
                                itemModel.value.toInt().and(0xFF) + 0x100
                            )
                                .substring(1, 6)
                        )

                    DataType.BIT8_DATA -> parameterValue.text =
                        "%8s".format(
                            Integer.toBinaryString(
                                itemModel.value.toInt().and(0xFF) + 0x100
                            )
                                .substring(1)
                        )
                    DataType.PHASE_DATA -> parameterValue.text =
                        DataUtils.getPhase(itemModel.value.toInt())
                    null -> parameterValue.text = context.resources.getString(R.string.value_error)
                    DataType.INTEGER_DATA -> parameterValue.text =
                        "${"%d".format(itemModel.value.toInt())} ${itemModel.unit}"
                    DataType.CURRENT_DATA -> parameterValue.text =
                        "${"%.2f"
                            .format(itemModel.value.toFloat() * driveSize)} ${itemModel
                            .unit.removePrefix("%")}"
                    else -> parameterValue.text =
                        "${"%d".format(itemModel.value.toInt())} ${itemModel.unit}"
                }

            } else {
                parameterValue.text = context.resources.getString(R.string.value_error)
            }

            if (exhibitType != DataType.BIT6_DATA
                && exhibitType != DataType.BIT8_DATA
                && exhibitType != DataType.PHASE_DATA
                && exhibitType != DataType.TIME_DATA
                && id != OPEN_ID && id != CLOSE_ID
            ) {
                graphButton.setOnClickListener {
                    graphClickListener.onGraphButtonClick(adapterPosition)
                }
                graphButton.visibility = View.VISIBLE
            } else {
                graphButton.visibility = View.INVISIBLE
            }


        }

    }

    class BitViewHolder private constructor(itemView: View, val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val monitoringName: TextView = itemView.findViewById(R.id.monitoring_name_text)
        private val monitoringNumberName: TextView =
            itemView.findViewById(R.id.monitoring_number_name_text)
        private val monitoringValue: TextView = itemView.findViewById(R.id.monitoring_value_text)

        private val cardArrow: ImageView = itemView.findViewById(R.id.monitoring_show_detail)

        private val subName1: TextView = itemView.findViewById(R.id.monitoring_sub_name_text1)
        private val subName2: TextView = itemView.findViewById(R.id.monitoring_sub_name_text2)
        private val subName3: TextView = itemView.findViewById(R.id.monitoring_sub_name_text3)
        private val subName4: TextView = itemView.findViewById(R.id.monitoring_sub_name_text4)
        private val subName5: TextView = itemView.findViewById(R.id.monitoring_sub_name_text5)
        private val subName6: TextView = itemView.findViewById(R.id.monitoring_sub_name_text6)
        private val subName7: TextView = itemView.findViewById(R.id.monitoring_sub_name_text7)
        private val subName8: TextView = itemView.findViewById(R.id.monitoring_sub_name_text8)

        private val subNumber1: TextView = itemView.findViewById(R.id.monitoring_sub_number_text1)
        private val subNumber2: TextView = itemView.findViewById(R.id.monitoring_sub_number_text2)
        private val subNumber3: TextView = itemView.findViewById(R.id.monitoring_sub_number_text3)
        private val subNumber4: TextView = itemView.findViewById(R.id.monitoring_sub_number_text4)
        private val subNumber5: TextView = itemView.findViewById(R.id.monitoring_sub_number_text5)
        private val subNumber6: TextView = itemView.findViewById(R.id.monitoring_sub_number_text6)
        private val subNumber7: TextView = itemView.findViewById(R.id.monitoring_sub_number_text7)
        private val subNumber8: TextView = itemView.findViewById(R.id.monitoring_sub_number_text8)

        private val subValue1: TextView = itemView.findViewById(R.id.monitoring_sub_value_text1)
        private val subValue2: TextView = itemView.findViewById(R.id.monitoring_sub_value_text2)
        private val subValue3: TextView = itemView.findViewById(R.id.monitoring_sub_value_text3)
        private val subValue4: TextView = itemView.findViewById(R.id.monitoring_sub_value_text4)
        private val subValue5: TextView = itemView.findViewById(R.id.monitoring_sub_value_text5)
        private val subValue6: TextView = itemView.findViewById(R.id.monitoring_sub_value_text6)
        private val subValue7: TextView = itemView.findViewById(R.id.monitoring_sub_value_text7)
        private val subValue8: TextView = itemView.findViewById(R.id.monitoring_sub_value_text8)

        private var cardOpened = false

        private var numberSubs = 0

        private val bit5NumberSums = 1
        private val bit8NumberSums = 3

        companion object {
            fun from(parent: ViewGroup): BitViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.monitoring_card2, parent, false)

                return BitViewHolder(itemView, parent.context)
            }
        }

        private fun changeCardOpenState() {
            Log.i(
                "MonitoringAdapter",
                "changeCardOpenState $numberSubs $cardOpened"
            )

            cardOpened = if (!cardOpened) {
                setAllVisible()
                true
            } else {
                setAllGone()
                false
            }
        }

        private fun setAllGone() {
            when (numberSubs) {
                bit5NumberSums -> {
                    cardArrow.setImageResource(R.drawable.ic_arrow_down)
                    set5Gone()
                }
                bit8NumberSums -> {
                    cardArrow.setImageResource(R.drawable.ic_arrow_down)
                    set8Gone()
                }
                else -> Log.e(
                    "MonitoringAdapter",
                    "setAllGone Invalid number of bits"
                )
            }
        }

        private fun setAllVisible() {
            when (numberSubs) {
                bit5NumberSums -> {
                    cardArrow.setImageResource(R.drawable.ic_arrow_up)
                    set5Visible()
                }
                bit8NumberSums -> {
                    cardArrow.setImageResource(R.drawable.ic_arrow_up)
                    set8Visible()
                }
                else -> Log.e(
                    "MonitoringAdapter",
                    "setAllVisible Invalid number of bits"
                )
            }
        }

        private fun set5Visible() {
            subName1.visibility = View.VISIBLE
            subName2.visibility = View.VISIBLE
            subName3.visibility = View.VISIBLE
            subName4.visibility = View.VISIBLE
            subName5.visibility = View.VISIBLE

            subNumber1.visibility = View.VISIBLE
            subNumber2.visibility = View.VISIBLE
            subNumber3.visibility = View.VISIBLE
            subNumber4.visibility = View.VISIBLE
            subNumber5.visibility = View.VISIBLE

            subValue1.visibility = View.VISIBLE
            subValue2.visibility = View.VISIBLE
            subValue3.visibility = View.VISIBLE
            subValue4.visibility = View.VISIBLE
            subValue5.visibility = View.VISIBLE
        }

        private fun set8Visible() {
            subName1.visibility = View.VISIBLE
            subName2.visibility = View.VISIBLE
            subName3.visibility = View.VISIBLE
            subName4.visibility = View.VISIBLE
            subName5.visibility = View.VISIBLE
            subName6.visibility = View.VISIBLE
            subName7.visibility = View.VISIBLE
            subName8.visibility = View.VISIBLE

            subNumber1.visibility = View.VISIBLE
            subNumber2.visibility = View.VISIBLE
            subNumber3.visibility = View.VISIBLE
            subNumber4.visibility = View.VISIBLE
            subNumber5.visibility = View.VISIBLE
            subNumber6.visibility = View.VISIBLE
            subNumber7.visibility = View.VISIBLE
            subNumber8.visibility = View.VISIBLE

            subValue1.visibility = View.VISIBLE
            subValue2.visibility = View.VISIBLE
            subValue3.visibility = View.VISIBLE
            subValue4.visibility = View.VISIBLE
            subValue5.visibility = View.VISIBLE
            subValue6.visibility = View.VISIBLE
            subValue7.visibility = View.VISIBLE
            subValue8.visibility = View.VISIBLE
        }

        private fun set5Gone() {
            subName1.visibility = View.GONE
            subName2.visibility = View.GONE
            subName3.visibility = View.GONE
            subName4.visibility = View.GONE
            subName5.visibility = View.GONE

            subNumber1.visibility = View.GONE
            subNumber2.visibility = View.GONE
            subNumber3.visibility = View.GONE
            subNumber4.visibility = View.GONE
            subNumber5.visibility = View.GONE

            subValue1.visibility = View.GONE
            subValue2.visibility = View.GONE
            subValue3.visibility = View.GONE
            subValue4.visibility = View.GONE
            subValue5.visibility = View.GONE
        }

        private fun set8Gone() {
            subName1.visibility = View.GONE
            subName2.visibility = View.GONE
            subName3.visibility = View.GONE
            subName4.visibility = View.GONE
            subName5.visibility = View.GONE
            subName6.visibility = View.GONE
            subName7.visibility = View.GONE
            subName8.visibility = View.GONE

            subNumber1.visibility = View.GONE
            subNumber2.visibility = View.GONE
            subNumber3.visibility = View.GONE
            subNumber4.visibility = View.GONE
            subNumber5.visibility = View.GONE
            subNumber6.visibility = View.GONE
            subNumber7.visibility = View.GONE
            subNumber8.visibility = View.GONE

            subValue1.visibility = View.GONE
            subValue2.visibility = View.GONE
            subValue3.visibility = View.GONE
            subValue4.visibility = View.GONE
            subValue5.visibility = View.GONE
            subValue6.visibility = View.GONE
            subValue7.visibility = View.GONE
            subValue8.visibility = View.GONE
        }

        fun bind(
            itemModel: MonitoringModel
        ) {

            monitoringName.text = itemModel.name
            monitoringNumberName.text = itemModel.idNumber

            val exhibitType = DataUtils.getExhibitType(itemModel.unit)

            numberSubs = when (exhibitType) {
                DataType.BIT6_DATA -> {
                    bit5NumberSums
                }
                DataType.BIT8_DATA -> {
                    bit8NumberSums
                }
                else -> 0
            }

            setSubTexts()

            if (itemModel.value.toFloat() < READ_ERROR.toFloat()) {

                when (exhibitType) {
                    DataType.BIT6_DATA -> {
                        monitoringValue.text =
                            "%8s".format(
                                Integer.toBinaryString(
                                    itemModel.value.toInt().and(0xFF) + 0x100
                                )
                                    .substring(1, 6)
                            )
                        set5SubValues(itemModel.value.toInt())
                    }

                    DataType.BIT8_DATA -> {
                        monitoringValue.text =
                            "%8s".format(
                                Integer.toBinaryString(
                                    itemModel.value.toInt().and(0xFF) + 0x100
                                )
                                    .substring(1)
                            )
                        set8SubValues(itemModel.value.toInt())
                    }
                    else -> monitoringValue.text =
                        "%8s".format(
                            Integer.toBinaryString(
                                itemModel.value.toInt().and(0xFF) + 0x100
                            )
                                .substring(1)
                        )
                }

            } else {
                monitoringValue.text = context.resources.getString(R.string.value_error)
            }

            itemView.setOnClickListener {
                changeCardOpenState()
            }

            monitoringName.setOnClickListener {
                changeCardOpenState()
            }

            monitoringNumberName.setOnClickListener {
                changeCardOpenState()
            }

            monitoringValue.setOnClickListener {
                changeCardOpenState()
            }

            cardArrow.setOnClickListener {
                changeCardOpenState()
            }


        }

        private fun set5SubValues(value: Int) {
            subValue1.text = DataUtils.getBitFromMask(value, DIG_OUT_MDO1_MASK).toString()
            subValue2.text = DataUtils.getBitFromMask(value, DIG_OUT_MDO2_MASK).toString()
            subValue3.text = DataUtils.getBitFromMask(value, DIG_OUT_MDO3_MASK).toString()
            subValue4.text = DataUtils.getBitFromMask(value, DIG_OUT_MDO4_MASK).toString()
            subValue5.text = DataUtils.getBitFromMask(value, DIG_OUT_MDO5_MASK).toString()
        }

        private fun set8SubValues(value: Int) {
            subValue1.text = DataUtils.getBitFromMask(value, DIG_IN_ENABLE_MASK).toString()
            subValue2.text = DataUtils.getBitFromMask(value, DIG_IN_START_MASK).toString()
            subValue3.text = DataUtils.getBitFromMask(value, DIG_IN_MDI1_MASK).toString()
            subValue4.text = DataUtils.getBitFromMask(value, DIG_IN_MDI2_MASK).toString()
            subValue5.text = DataUtils.getBitFromMask(value, DIG_IN_MDI3_MASK).toString()
            subValue6.text = DataUtils.getBitFromMask(value, DIG_IN_MDI4_MASK).toString()
            subValue7.text = DataUtils.getBitFromMask(value, DIG_IN_MDI5_MASK).toString()
            subValue8.text = DataUtils.getBitFromMask(value, DIG_IN_MDI6_MASK).toString()
        }

        private fun setSubTexts() {
            when (numberSubs) {
                bit5NumberSums -> set5SubsTexts()
                bit8NumberSums -> set8SubsTexts()
            }
        }

        private fun set5SubsTexts() {
            subName1.text = context.resources.getString(R.string.m022_mdo1_label)
            subName2.text = context.resources.getString(R.string.m022_mdo2_label)
            subName3.text = context.resources.getString(R.string.m022_mdo3_label)
            subName4.text = context.resources.getString(R.string.m022_mdo4_label)
            subName5.text = context.resources.getString(R.string.m022_mdo5_label)

            subNumber1.text = MDO1
            subNumber2.text = MDO2
            subNumber3.text = MDO3
            subNumber4.text = MDO4
            subNumber5.text = MDO5
        }

        private fun set8SubsTexts() {
            subName1.text = context.resources.getString(R.string.m021_enable_label)
            subName2.text = context.resources.getString(R.string.m021_start_label)
            subName3.text = context.resources.getString(R.string.m021_mdi1_label)
            subName4.text = context.resources.getString(R.string.m021_mdi2_label)
            subName5.text = context.resources.getString(R.string.m021_mdi3_label)
            subName6.text = context.resources.getString(R.string.m021_mdi4_label)
            subName7.text = context.resources.getString(R.string.m021_mdi5_label)
            subName8.text = context.resources.getString(R.string.m021_mdi6_label)

            subNumber1.text = ENABLE
            subNumber2.text = START
            subNumber3.text = MDI1
            subNumber4.text = MDI2
            subNumber5.text = MDI3
            subNumber6.text = MDI4
            subNumber7.text = MDI5
            subNumber8.text = MDI6
        }

    }
}