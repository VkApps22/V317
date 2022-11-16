package br.com.kascosys.vulkanconnectv317.utils

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import br.com.kascosys.vulkanconnectv317.R

class Util{
    companion object{
        fun titleForDialog(title: String, context: Context): TextView{

            val backgroundColor = ContextCompat.getColor(context, R.color.colorAccent)

            val titleTextView = TextView(context)
            titleTextView.text = title
            titleTextView.setPadding(20, 30, 20, 30)
            titleTextView.textSize = 20f
            titleTextView.setBackgroundColor(backgroundColor)
            titleTextView.setTextColor(Color.WHITE)

            return titleTextView
        }

        fun readBasicParameterJson(context: Context): String{
            Log.i("Util","readBasicParameterJson----------------------")

            val stream = context.resources.openRawResource(R.raw.basic_parameter_table)

            val size = stream.available()

            val buffer = ByteArray(size)

            stream.read(buffer)

            stream.close()

            val fileContent = String(buffer)

            Log.i("Util","readBasicParameterJson fileContent $fileContent")

            return fileContent
        }

        fun readAdvancedParameterJson(context: Context): String{
            Log.i("Util","readBasicParameterJson----------------------")

            val stream = context.resources.openRawResource(R.raw.advanced_parameter_table)

            val size = stream.available()

            val buffer = ByteArray(size)

            stream.read(buffer)

            stream.close()

            val fileContent = String(buffer)

            Log.i("Util","readBasicParameterJson fileContent $fileContent")

            return fileContent
        }

        fun readMonitoringJson(context: Context): String{
            Log.i("Util","readMonitoringJson----------------------")

            val stream = context.resources.openRawResource(R.raw.monitoring_table)

            val size = stream.available()

            val buffer = ByteArray(size)

            stream.read(buffer)

            stream.close()

            val fileContent = String(buffer)

            Log.i("Util","readMonitoringJson fileContent $fileContent")

            return fileContent
        }
    }
}