package br.com.kascosys.vulkanconnectv317.managers

import android.content.Context
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.text.format.Formatter
import android.util.Log
import br.com.kascosys.vulkanconnectv317.constants.PARAMETER_NAME_SUFFIX
import br.com.kascosys.vulkanconnectv317.constants.READ_ERROR
import br.com.kascosys.vulkanconnectv317.models.NewParameterModel
import br.com.kascosys.vulkanconnectv317.utils.Util
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusUtils
import br.com.kascosys.vulkanconnectv317.utils.singleton.SingletonHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ParameterManager private constructor(context: Context) {

    private var basicParameterList: ArrayList<NewParameterModel>

    private var advancedParameterList: ArrayList<NewParameterModel>

    private lateinit var modBusUtils: ModBusUtils

    init {
        // Init using context argument

//        getInstance(context)

        Log.i("ParameterManager", "init----------------------")

        val groupListType = object : TypeToken<ArrayList<NewParameterModel>>() {

        }.type

        basicParameterList = Gson().fromJson(Util.readBasicParameterJson(context), groupListType)

        advancedParameterList =
            Gson().fromJson(Util.readAdvancedParameterJson(context), groupListType)

//        ConnectionManager.getInstance(context)
        val wifiManager: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        modBusUtils = ParameterModBusInit(wifiManager).execute().get()

//        val modBusConnected = ParameterIsConnected(modBusUtils).execute().get()

        for (i in 0 until basicParameterList.size) {
            val id = basicParameterList[i].id
            val ratio = basicParameterList[i].ratio

            Log.i(
                "ParameterManager", "init bas $i $id ${
                basicParameterList[i].unit
                }"
            )

//            basicParameterList[i].value = basicParameterList[i].defaultVal


//            basicParameterList[i].value =
//                ConnectionManager.getInstance(context).readParameter(basicParameterList[i].id!!)

//            if (modBusConnected != null && modBusConnected) {
//                if (id != null && ratio != null) {
//                    basicParameterList[i].value =
//                        ParameterReadParameters(id, ratio, modBusUtils).execute().get()
//                } else {
//                    Log.e("ParameterManager", "id $id ratio $ratio")
//                }
//            } else {
//                Log.e("ParameterManager", "ModBus not connected! id $id")
//            }
            basicParameterList[i].value = READ_ERROR
            basicParameterList[i].lastVal = basicParameterList[i].value

            val resName = id?.toLowerCase() + PARAMETER_NAME_SUFFIX
//            Log.d("ParameterManager","init resName $resName")


            val labelId = context.resources.getIdentifier(
                resName,
                "string", context.packageName
            )
//            Log.d("ParameterManager","init labelId $labelId")

//            var nameResId: String? = null
//            if(labelId != 0) {
//                nameResId = context.resources.getString(labelId)
//            }
//            Log.d("ParameterManager","init parName $nameResId")

            basicParameterList[i].nameResId = labelId
        }

        for(i in advancedParameterList.indices){
            val id = advancedParameterList[i].id
            val ratio = advancedParameterList[i].ratio

            Log.i(
                "ParameterManager", "init adv $i $id ${
                advancedParameterList[i].unit
                }"
            )

            advancedParameterList[i].value = READ_ERROR
            advancedParameterList[i].lastVal = advancedParameterList[i].value

            val resName = id?.toLowerCase() + PARAMETER_NAME_SUFFIX
//            Log.d("ParameterManager","init resName $resName")


            val labelId = context.resources.getIdentifier(
                resName,
                "string", context.packageName
            )

            advancedParameterList[i].nameResId = labelId
        }
    }

    fun getBy(id: String): NewParameterModel {
//        return basicParameterList.first { it.id == id }
        return try {
            basicParameterList.first { it.id == id }
        } catch (e: NoSuchElementException){
            advancedParameterList.first { it.id == id }
        }
    }

    fun getAllBasic(): ArrayList<NewParameterModel> {
        return basicParameterList
    }

    fun getAllAdvanced(): ArrayList<NewParameterModel> {
        return advancedParameterList
    }

    private class ParameterModBusInit(val wifiManager: WifiManager) :
        AsyncTask<String, Boolean, ModBusUtils>() {

        val port = 8899
        val host = 254

        override fun doInBackground(vararg p0: String?): ModBusUtils? {
            Log.i("ParameterModBusInit", "doInBackground $wifiManager")

            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress

            Log.i("ConnectionManager", "init $ipAddress")

            val ipString = Formatter.formatIpAddress(ipAddress)

            val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)

            return ModBusUtils(prefix, host, port)

        }


    }

    private class ParameterIsConnected(
        val modBusUtils: ModBusUtils
    ) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg p0: Void?): Boolean {
            return modBusUtils.isConnected()
        }

    }

    fun readAll(): ArrayList<NewParameterModel>? {
        Log.i("ParameterManager", "readAll------------------")

        if (modBusUtils.isConnected()) {
            Log.i("ParameterManager", "readAll modBus connected")

            for (i in 0 until basicParameterList.size) {
                val id = basicParameterList[i].id
                val ratio = basicParameterList[i].ratio

                if (id != null && ratio != null) {
                    basicParameterList[i].value =
                        ParameterReadParameters(id, ratio, modBusUtils).execute().get()
                    basicParameterList[i].lastVal = basicParameterList[i].value
                }
            }

            return basicParameterList
        }

        Log.e("ParameterManager", "readAll ModBus not connected!")

        return null
    }

    private class ParameterReadParameters(
        val id: String,
        val ratio: Number,
        val modBusUtils: ModBusUtils
    ) : AsyncTask<Void, Void, Number?>() {

        override fun doInBackground(vararg p0: Void?): Number? {
            val read = modBusUtils.readParameter(id, ratio)

            if (read != null) {
                return read
            }

            Log.e("ParameterReadParameters", "readParameter $id ratio is null!")

            return null
        }

    }

    companion object : SingletonHolder<ParameterManager, Context>(::ParameterManager)
}