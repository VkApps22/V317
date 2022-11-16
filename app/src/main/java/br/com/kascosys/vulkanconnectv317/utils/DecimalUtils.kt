package br.com.kascosys.vulkanconnectv317.utils

import android.util.Log
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import kotlin.math.floor

class DecimalUtils {
    companion object {
        fun getDigits(value: Float): Int {
            Log.i("DecimalUtils", "getDigits $value-------------------")

            val maxDigits = 9

            var newValue = (value - floor(value)) * 10
            var lastIndex = 0
            for (i in 1..maxDigits) {
                Log.i("DecimalUtils", "getDigits $newValue $i $lastIndex")

                if (newValue >= 1) {
                    lastIndex = i
                    newValue -= floor(newValue)
                }
                newValue *= 10
            }

            Log.i("DecimalUtils", "getDigits will return $lastIndex")

            return lastIndex
        }

        fun getNumberFromInputString(inputString: String, unitString: String = ""): Number? {
            val valueString =
                inputString
                    .removeSuffix(unitString)
                    .removePrefix("+")

            if (valueString == "") {
                return null
            }

            val separator = DecimalFormatSymbols.getInstance()
                .decimalSeparator

            val sepValueString = if (separator == ',') {
                valueString.replace('.', separator)
            } else {
                valueString.replace(',', separator)
            }

            Log.i(
                "DecimalUtils",
                "getNumberFromInputString corrected separator valueString $sepValueString"
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
                    "DecimalUtils",
                    "getNumberFromInputString subs separator $testValueString $sepCount"
                )

                sepCount++
            }

            if (sepCount > 1) {
                finalValueString = ""
            }

            return if (finalValueString != ""
                && finalValueString.lastIndexOf('-') < 1
                && finalValueString != separator.toString()
            ) {
                NumberFormat.getInstance().parse(
                    finalValueString
                )
            } else null
        }
    }
}