package br.com.kascosys.vulkanconnectv317.utils

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.C000
import br.com.kascosys.vulkanconnectv317.constants.P128
import br.com.kascosys.vulkanconnectv317.constants.P171
import br.com.kascosys.vulkanconnectv317.constants.READ_ERROR
import br.com.kascosys.vulkanconnectv317.enums.DataType
import java.text.DecimalFormat


class DataUtils {

    companion object {
        fun getExhibitType(unit: String): DataType? {
            when (unit) {
                "A" -> return DataType.NUMBER_DATA
                "s" -> return DataType.NUMBER_DATA
                "ms" -> return DataType.NUMBER_DATA
                "V" -> return DataType.NUMBER_DATA
                "%" -> return DataType.NUMBER_DATA
                "%A" -> return DataType.CURRENT_DATA
                "Hz" -> return DataType.NUMBER_DATA
                "°" -> return DataType.NUMBER_DATA
                "8bit" -> return DataType.BIT8_DATA
                "6bit" -> return DataType.BIT6_DATA
                "time" -> return DataType.TIME_DATA
                "phaseSeq" -> return DataType.PHASE_DATA
                "bps" -> return DataType.BIT_RATE_DATA
                "YesNo" -> return DataType.YES_NO_DATA
                "Excluded" -> return DataType.EXCLUDED_DATA
                "Parity" -> return DataType.PARITY_DATA
                "Kp" -> return DataType.LOOP_GAIN_DATA
                "times" -> return DataType.TIMES_DATA
                "anConf" -> return DataType.ANALOG_CONFIG_DATA
                "" -> return DataType.INTEGER_DATA
            }

            return null
        }

        fun getFormatted(value: Number, unit: String, driveSize: Int = 100): String {
            val dataType = getExhibitType(unit)

//            return when (dataType) {
//                DataType.NUMBER_DATA -> "%.2f".format(value.toFloat())
//                DataType.BIT8_DATA -> "%8s".format(
//                    Integer.toBinaryString(
//                        value.toInt().and(0xFF) + 0x100
//                    )
//                        .substring(1)
//                )
//                DataType.BIT6_DATA -> "%8s".format(
//                    Integer.toBinaryString(
//                        value.toInt().and(0xFF) + 0x100
//                    )
//                        .substring(1, 6)
//                )
//                DataType.TIME_DATA -> getTimeString(value.toInt())
//                DataType.PHASE_DATA -> "%.2f".format(value.toFloat())
//                DataType.INTEGER_DATA -> "%d".format(value.toInt())
//                DataType.BIT_RATE_DATA -> "%d".format(mapToBitrate(value))
//                DataType.CURRENT_DATA -> "%.2f".format(value.toFloat() / 100.0 * driveSize)
//                DataType.TIMES_DATA -> "%d".format(value.toInt())
//                DataType.YES_NO_DATA -> "%d".format(value.toInt())
//                DataType.EXCLUDED_DATA -> "%d".format(value.toInt())
//                DataType.PARITY_DATA -> "%d".format(value.toInt())
//                DataType.LOOP_GAIN_DATA -> "%.3f".format(value.toFloat())
//                null -> "%.2f".format(value.toFloat())
////                else -> "%.2f".format(value.toFloat())
//            }

            val formatter2decimals = DecimalFormat("0.00")
            val formatter3decimals = DecimalFormat("0.00#")

            return when (dataType) {
                DataType.NUMBER_DATA -> formatter2decimals.format(value.toFloat())
                DataType.BIT8_DATA -> "%8s".format(
                    Integer.toBinaryString(
                        value.toInt().and(0xFF) + 0x100
                    )
                        .substring(1)
                )
                DataType.BIT6_DATA -> "%8s".format(
                    Integer.toBinaryString(
                        value.toInt().and(0xFF) + 0x100
                    )
                        .substring(1, 6)
                )
                DataType.TIME_DATA -> getTimeString(value.toInt())
                DataType.PHASE_DATA -> formatter2decimals.format(value.toFloat())
                DataType.INTEGER_DATA -> "%d".format(value.toInt())
                DataType.BIT_RATE_DATA -> "%d".format(mapToBitrate(value))
                DataType.CURRENT_DATA -> formatter2decimals.format(
                    value.toFloat() / 100.0 * driveSize
                )
                DataType.TIMES_DATA -> "%d".format(value.toInt())
                DataType.YES_NO_DATA -> "%d".format(value.toInt())
                DataType.EXCLUDED_DATA -> "%d".format(value.toInt())
                DataType.PARITY_DATA -> "%d".format(value.toInt())
                DataType.LOOP_GAIN_DATA -> formatter3decimals.format(value.toFloat())
                DataType.ANALOG_CONFIG_DATA -> "%d".format(value.toInt())
                null -> formatter2decimals.format(value.toFloat())
//                else -> "%.2f".format(value.toFloat())
            }

        }

        fun getFormattedUnit(unit: String, value: Int = 0, context: Context? = null): String {

            return when (getExhibitType(unit)) {
                DataType.NUMBER_DATA -> unit
                DataType.BIT8_DATA -> ""
                DataType.BIT6_DATA -> ""
                DataType.TIME_DATA -> ""
                DataType.PHASE_DATA -> unit
                DataType.INTEGER_DATA -> unit
                DataType.BIT_RATE_DATA -> unit
                DataType.CURRENT_DATA -> "A"
                DataType.YES_NO_DATA -> ""
                DataType.EXCLUDED_DATA -> ""
                DataType.PARITY_DATA -> ""
                DataType.LOOP_GAIN_DATA -> ""
                DataType.ANALOG_CONFIG_DATA -> ""
                DataType.TIMES_DATA -> context?.resources?.getQuantityString(
                    R.plurals.parameter_card_times_unit,
                    value
                ) ?: unit
                null -> ""
            }
        }

        fun getPercentageFromValue(value: Number, amplitude: Number): Number {
            return 100 * value.toDouble() / amplitude.toDouble()
        }

        fun getValueFromPercentage(percentage: Number, amplitude: Number): Number {
            return percentage.toDouble() * amplitude.toDouble() / 100.0
        }

        fun mapToBitrate(value: Number): Int {
            Log.i("DataUtils", "mapToBitrate $value-------------------")


            return when (value.toFloat()) {
                0f -> 1200
                1f -> 2400
                2f -> 4800
                3f -> 9600
                4f -> 19200
                5f -> 38400
                6f -> 57600
                7f -> 128000
                else -> READ_ERROR
            }
        }

        fun mapFromBitrate(value: Int): Number {
            Log.i("DataUtils", "mapToBitrate $value-------------------")


            return when (value) {
                1200 -> 0f
                2400 -> 1f
                4800 -> 2f
                9600 -> 3f
                19200 -> 4f
                38400 -> 5f
                57600 -> 6f
                128000 -> 7f
                else -> READ_ERROR
            }
        }

        fun isValidBitrate(value: Int): Boolean {
            return when (value) {
                1200 -> true
                2400 -> true
                4800 -> true
                9600 -> true
                19200 -> true
                38400 -> true
                57600 -> true
                128000 -> true
                else -> false
            }
        }

        fun getBitFromMask(value: Int, mask: Int): Int {
            return value.and(mask) / mask
        }

        fun mapFromYesNo(value: String): Number {
            Log.i("DataUtils", "mapFromYesNo $value-------------------")

            return when {
                value.contains("0") -> return 0
                value.contains("1") -> return 1
                else -> READ_ERROR
            }
        }

        fun mapFromExcluded(value: String): Number {
            Log.i("DataUtils", "mapFromExcluded $value-------------------")

            return when {
                value.contains("0") -> return 0
                value.contains("1") -> return 1
                else -> READ_ERROR
            }
        }

        fun mapFromParity(value: String): Number {
            Log.i("DataUtils", "mapFromParity $value-------------------")

            return when {
                value.contains("0") -> return 0
                value.contains("1") -> return 1
                value.contains("2") -> return 2
                else -> READ_ERROR
            }
        }

        fun mapFromAnalogConfig(value: String): Number {
            Log.i("DataUtils", "mapFromParity $value-------------------")

            return when {
                value.contains("10") -> return 10
                value.contains("11") -> return 11
                value.contains("12") -> return 12
                value.contains("13") -> return 13
                value.contains("0") -> return 0
                value.contains("1") -> return 1
                value.contains("2") -> return 2
                value.contains("3") -> return 3
                value.contains("4") -> return 4
                value.contains("5") -> return 5
                value.contains("6") -> return 6
                value.contains("7") -> return 7
                value.contains("8") -> return 8
                value.contains("9") -> return 9
                else -> READ_ERROR
            }
        }


        fun getTimeString(timeSeconds: Int): String {
            Log.i("DataUtils", "getTimeString $timeSeconds-------------------")

            var remainingSeconds = timeSeconds

            val factorSecondMinute = 60
            val factorMinuteHour = 60
            val factorHourDay = 24

            val factorSecondHour = factorSecondMinute * factorMinuteHour
            val factorSecondDay = factorSecondHour * factorHourDay

            val days = remainingSeconds / factorSecondDay
            remainingSeconds -= days * factorSecondDay

            val hours = remainingSeconds / factorSecondHour
            remainingSeconds -= hours * factorSecondHour

            val minutes = remainingSeconds / factorSecondMinute
            remainingSeconds -= minutes * factorSecondMinute

            val seconds = remainingSeconds

            val returnString = "${days}d ${hours}h ${minutes}m ${seconds}s"

            Log.i("DataUtils", "getTimeString will return $returnString")

            return returnString
        }

        fun getPhase(value: Int): String {
            return when (value) {
                0 -> "RST"
                1 -> "TSR"
                else -> "Error"
            }
        }

        fun isGraphParameter(id: String): Boolean {
            return when (id) {
                P128 -> true
                P171 -> true
                C000 -> true
                else -> false
            }
        }
    }

}