package br.com.kascosys.vulkanconnectv317.managers

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.text.format.Formatter
import android.util.Log
import br.com.kascosys.vulkanconnectv317.activities.PairingActivity
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.fragments.HomeFragment
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusUtils
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusValues
import br.com.kascosys.vulkanconnectv317.utils.singleton.SingletonHolder

@Deprecated("Remove modBus from Singleton")
class ConnectionManager private constructor(private val context: Context) {

    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val port =
        context.applicationContext.getSharedPreferences(PORT_SHARED_KEY, Context.MODE_PRIVATE)
            .getInt(
                PORT_SHARED_KEY, DEFAULT_MODBUS_PORT
            )

    private val host =
        context.applicationContext.getSharedPreferences(HOST_SHARED_KEY, Context.MODE_PRIVATE)
            .getInt(
                HOST_SHARED_KEY, DEFAULT_MODBUS_HOST
            )

    var modBusUtils: ModBusUtils
        private set

    var isConnected = false
        private set

    var inProgress = false
        private set

    init {
        Log.i("ConnectionManager", "init-----------------------")

        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress

        Log.i("ConnectionManager", "init $ipAddress")

        val ipString = Formatter.formatIpAddress(ipAddress)

        val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)

        Log.i("ConnectionManager", "init $prefix $port $host")

        modBusUtils = ModBusInit(this, prefix, host, port).execute().get()

//        run connectLoop@{
//            for (i in 0 until CONNECTION_TRIALS - 1)
//                if (!modBusUtils.isConnected()) {
//                    modBusUtils = ModBusInit().execute(prefix, port.toString()).get()
//                } else {
//                    isConnected = true
//                    return@connectLoop
//                }
//        }
//        if (modBusUtils.isConnected()) {
//            isConnected = true
//        }

        Log.i("ConnectionManager", "init end-----------------------")
        //            modBusUtils = ModBusInit().execute(prefix, port.toString()).get()
    }


    fun reconnect() {
        Log.i("ConnectionManager", "reconnect-----------------------")

        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress

        Log.i("ConnectionManager", "init $ipAddress")

        val ipString = Formatter.formatIpAddress(ipAddress)

        val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)

        modBusUtils = ModBusInit(this, prefix, host, port).execute().get()
    }


    fun disconnect(): Boolean {
        return ModBusDisconnect(this, modBusUtils).execute().get()
    }

    fun readParameter(id: String): Number? {
        val ratio = ParameterManager
            .getInstance(context.applicationContext).getBy(id).ratio

        if (ratio != null) {
            return ModBusReadParameter(modBusUtils, ratio).execute(id).get()
        }

        Log.e("ConnectionManager", "readParameter $id ratio is null!")

        return null
    }

    fun programParameter(id: String, value: Number): Boolean {
        val ratio = ParameterManager
            .getInstance(context.applicationContext).getBy(id).ratio

        if (ratio != null) {
            return ModBusProgramParameter(modBusUtils, value, ratio).execute(id).get()
        }

        Log.e("ConnectionManager", "readParameter $id ratio is null!")

        return false
    }

    fun saveParameter(id: String): Boolean {
        val ratio = ParameterManager
            .getInstance(context.applicationContext).getBy(id).ratio

        if (ratio != null) {
            return ModBusSaveParameter(modBusUtils).execute(id).get()
        }

        Log.e("ConnectionManager", "readParameter $id ratio is null!")

        return false
    }


    private class ModBusInit(
        val parent: ConnectionManager,
        val prefix: String,
        val host: Int,
        val port: Int
    ) :
        AsyncTask<String, Boolean, ModBusUtils>() {

        override fun onPreExecute() {
            Log.i("ModBusInit", "onPreExecute-------------------")
            parent.inProgress = true

            super.onPreExecute()
        }

        override fun doInBackground(vararg p0: String?): ModBusUtils? {
            Log.i("ModBusInit", "doInBackground $p0")

//            if (p0[0] != null && p0[1] != null) {
//                return ModBusUtils(p0[0]!!, p0[1]!!.toInt())
//            }
//
//            return null
            return ModBusUtils(prefix, host, port)
        }

        override fun onPostExecute(result: ModBusUtils?) {
            Log.i("ModBusInit", "onPostExecute-------------------")
            if (result != null) {
                parent.isConnected = result.isConnected()
            } else {
                parent.isConnected = false
            }
            parent.inProgress = false

            super.onPostExecute(result)
        }
    }

    private class ModBusReadParameter(val modBusUtils: ModBusUtils, val ratio: Number) :
        AsyncTask<String, Boolean, Number?>() {

        override fun doInBackground(vararg p0: String?): Number? {
            Log.i("ModBusReadParameter", "doInBackground $p0")

            if (p0.size == 1 && p0[0] != null) {
                return modBusUtils.readParameter(p0[0]!!, ratio)
            }

            return null
        }

    }

    private class ModBusProgramParameter(
        val modBusUtils: ModBusUtils,
        val value: Number,
        val ratio: Number
    ) : AsyncTask<String, Boolean, Boolean>() {

        override fun doInBackground(vararg idVar: String?): Boolean {
            Log.i("ModBusProgramParameter", "doInBackground $idVar")

            if (idVar.size == 1 && idVar[0] != null) {
                return modBusUtils.programParameter(idVar[0]!!, value, ratio)
            }

            return false
        }

    }

    private class ModBusSaveParameter(
        val modBusUtils: ModBusUtils
    ) : AsyncTask<String, Boolean, Boolean>() {

        override fun doInBackground(vararg idVar: String?): Boolean {
            Log.i("ModBusSaveParameter", "doInBackground $idVar")


            if (idVar.size == 1 && idVar[0] != null) {
                return modBusUtils.saveParameter(idVar[0]!!)
            }

            return false
        }

    }

    private class ModBusDisconnect(val parent: ConnectionManager, val modBusUtils: ModBusUtils) :
        AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg p0: Void?): Boolean {
            Log.i("ModBusDisconnect", "doInBackground $p0")

            if (p0.isEmpty()) {
                modBusUtils.disconnect()
                return true
            }

            return false
        }

        override fun onPostExecute(result: Boolean?) {
            parent.isConnected = modBusUtils.isConnected()

            super.onPostExecute(result)
        }

    }


    companion object : SingletonHolder<ConnectionManager, Context>(::ConnectionManager)
}