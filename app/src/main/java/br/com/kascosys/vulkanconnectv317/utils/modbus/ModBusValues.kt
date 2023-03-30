package br.com.kascosys.vulkanconnectv317.utils.modbus

import android.util.Log
import br.com.kascosys.vulkanconnectv317.constants.*

class ModBusValues {

    companion object {

        fun mapParIdToAddress(id: String): Int {
            when (id) {
                P007 -> return 57
                P008 -> return 58
                P128 -> return 158
                P171 -> return 191
                P177 -> return 197
                C000 -> return 310
                SAVE_PARAMETER -> return 472
                ALARM_NUMBER -> return 8
                DRIVE_SIZE -> return 481
                MAINS_VOLTAGE -> return 19
                MAINS_FREQUENCY -> return 18
                ADVANCED_KEY_ID -> return 50
                else -> Log.i("ModBusValues", "Parameter $id")
            }

            return -1
        }

        fun mapParAddressToId(address: Int): String {
            when (address) {
                8 -> return ALARM_NUMBER
                50 -> return ADVANCED_KEY_ID
                57 -> P007
                58 -> P008
                158 -> return P128
                191 -> return P171
                197 -> return P177
                310 -> return C000
                472 -> return SAVE_PARAMETER
                481 -> return DRIVE_SIZE
                else -> Log.i("ModBusValues", "Address $address")
            }

            return ""
        }

        fun mapAlarmNumberToId(number: Int): String {
            Log.i("ModBusValues", "mapAlarmNumberToId $number-----------------")

            return when {
                number == 0 -> DRIVE_OK
//                number <= 33 -> "A%03d".format(number)
//                else -> WXXX
                listOf(2, 3, 6, 7, 12, 13, 16, 17, 20, 21, 24, 29, 32, 33).indexOf(number) > -1 -> "A%03d".format(number)
                number == 37 -> "W%03d".format(number - 33)
                else -> "A033"
            }
        }

        private const val OVERFLOW_16BITS = 0x10000

        fun getModBusVal(id: String, realVal: Number, ratio: Number): Int? {
            Log.i("ModBusValues", "getModBusVal $id $realVal $ratio -------------")


//                val ratio = manager.getBy(id).ratio

            val converted = (realVal.toFloat() * ratio.toFloat()).toInt()

            Log.i("ModBusValues", "getModBusVal $id $converted")

            return converted
        }

        fun getRealVal(
            id: String,
            modBusVal: Int,
            ratio: Number,
            isSigned: Boolean = true
        ): Number? {
            Log.i("ModBusValues", "getRealVal $id $modBusVal $ratio -------------")
//                val ratio = manager.getBy(id).ratio

            var value = modBusVal
            if (isSigned) {
                value = complementOf216Bits(value)
            }

            val converted = (value / ratio.toFloat())


            Log.i("ModBusValues", "getRealVal $id $converted")

            return converted
        }

        private fun complementOf216Bits(value: Int): Int {
            return if (value >= OVERFLOW_16BITS / 2) value - OVERFLOW_16BITS else value
        }
    }


}

