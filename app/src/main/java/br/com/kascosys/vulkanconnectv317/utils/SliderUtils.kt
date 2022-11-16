package br.com.kascosys.vulkanconnectv317.utils

import android.util.Log

@Deprecated("Slider not used anymore")
class SliderUtils {
    companion object {
        private const val sliderFactor1 = 10
        private const val sliderFactor2 = 100

        fun getSliderAmplitude(amplitude: Float): Int {
            return (amplitude * sliderFactor2).toInt()
        }

        @Deprecated("SliderUtils.change_amplitude")
        fun getSliderVal(realVal: Float): Int {
            Log.i("SliderUtils", "getSliderVal realVal $realVal")

            return (realVal * sliderFactor2).toInt()
        }

        fun getSliderVal(realVal: Float, minVal: Float): Int {
            Log.i("SliderUtils", "getSliderVal realVal $realVal")

            return ((realVal - minVal) * sliderFactor2).toInt()
        }

        @Deprecated("SliderUtils.change_amplitude")
        fun getVal2Decimals(sliderVal: Int): Float {
            Log.i("SliderUtils", "getVal2Decimals sliderVal $sliderVal")

            return sliderVal / sliderFactor2.toFloat()
        }

        fun getVal2Decimals(sliderVal: Int, minVal: Float): Float {
            Log.i("SliderUtils", "getVal2Decimals sliderVal $sliderVal")

            return (sliderVal) / sliderFactor2.toFloat() + minVal
        }

        @Deprecated("SliderUtils.change_amplitude")
        fun getVal1Decimals(sliderVal: Int): Float {
            Log.i("SliderUtils", "getVal2Decimals sliderVal $sliderVal")

            return sliderVal / sliderFactor1.toFloat()
        }

        fun getVal1Decimals(sliderVal: Int, minVal: Float): Float {
            Log.i("SliderUtils", "getVal2Decimals sliderVal $sliderVal")

            return (sliderVal) / sliderFactor1.toFloat() + minVal
        }

        @Deprecated("SliderUtils.change_amplitude")
        fun getVal0Decimals(sliderVal: Int): Float {
            Log.i("SliderUtils", "getVal2Decimals sliderVal $sliderVal")

            return sliderVal.toFloat()
        }

        fun getVal0Decimals(sliderVal: Int, minVal: Float): Float {
            Log.i("SliderUtils", "getVal2Decimals sliderVal $sliderVal")

            return (sliderVal).toFloat() + minVal
        }
    }
}