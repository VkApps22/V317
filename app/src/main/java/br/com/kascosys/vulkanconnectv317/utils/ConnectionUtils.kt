package br.com.kascosys.vulkanconnectv317.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.DEFAULT_MODBUS_HOST
import br.com.kascosys.vulkanconnectv317.constants.DEFAULT_MODBUS_PORT
import br.com.kascosys.vulkanconnectv317.constants.HOST_SHARED_KEY
import br.com.kascosys.vulkanconnectv317.constants.PORT_SHARED_KEY
import java.util.*

class ConnectionUtils {
    companion object {
        fun getIpPrefix(wifiManager: WifiManager): String {
//            val wifiManager: WifiManager =
//                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            Log.i("ConnectionUtils", "getIpPrefix-----------------------")

//            val wifiInfo = wifiManager.connectionInfo
            val ipLong = wifiManager.dhcpInfo.ipAddress
//            val ipAddress = wifiInfo.ipAddress
            val ipString = String.format(
                Locale.US, "%d.%d.%d.%d",
                ipLong and 0xff,
                ipLong shr 8 and 0xff,
                ipLong shr 16 and 0xff,
                ipLong shr 24 and 0xff
            );

            Log.i("ConnectionUtils", "getIpPrefix raw ip $ipLong")

//            val ipString = Formatter.formatIpAddress(ipAddress)
//            val ipString = InetAddress.getLocalHost().hostAddress
//            val ipString = InetAddress.getByAddress(ipAddress).hostAddress

            Log.i("ConnectionUtils", "getIpPrefix ip $ipString")

            val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)

            Log.i("ConnectionUtils", "getIpPrefix prefix $prefix")

            return prefix
        }

        fun getIpHost(wifiManager: WifiManager): String {
//            val wifiManager: WifiManager =
//                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            Log.i("ConnectionUtils", "getIpPrefix-----------------------")

//            val wifiInfo = wifiManager.connectionInfo
            val ipLong = wifiManager.dhcpInfo.ipAddress
//            val ipAddress = wifiInfo.ipAddress
            val ipString = String.format(
                Locale.US, "%d.%d.%d.%d",
                ipLong and 0xff,
                ipLong shr 8 and 0xff,
                ipLong shr 16 and 0xff,
                ipLong shr 24 and 0xff
            );

            Log.i("ConnectionUtils", "getIpPrefix raw ip $ipLong")

//            val ipString = Formatter.formatIpAddress(ipAddress)
//            val ipString = InetAddress.getLocalHost().hostAddress
//            val ipString = InetAddress.getByAddress(ipAddress).hostAddress

            Log.i("ConnectionUtils", "getIpPrefix ip $ipString")

            val suffix = ipString.substring(ipString.lastIndexOf(".") + 1)

            Log.i("ConnectionUtils", "getIpHost suffix $suffix")

            return suffix
        }

        fun getIp(wifiManager: WifiManager): String {
//            val wifiManager: WifiManager =
//                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            Log.i("ConnectionUtils", "getIpPrefix-----------------------")

//            val wifiInfo = wifiManager.connectionInfo
            val ipLong = wifiManager.dhcpInfo.ipAddress
//            val ipAddress = wifiInfo.ipAddress
            val ipString = String.format(
                Locale.US, "%d.%d.%d.%d",
                ipLong and 0xff,
                ipLong shr 8 and 0xff,
                ipLong shr 16 and 0xff,
                ipLong shr 24 and 0xff
            );

            Log.i("ConnectionUtils", "getIpPrefix raw ip $ipLong")

//            val ipString = Formatter.formatIpAddress(ipAddress)
//            val ipString = InetAddress.getLocalHost().hostAddress
//            val ipString = InetAddress.getByAddress(ipAddress).hostAddress

            return ipString
        }

        fun isValidIpPrefix(ipPrefix: String): Boolean {
            when (ipPrefix) {
                "" -> return false
                "0.0.0." -> return false
                "." -> return false
                else -> {
                    Log.i("ConnectionUtils", "isValidIpPrefix valid $ipPrefix")
                }
            }

            return true
        }

        fun isConnected(wifiManager: WifiManager): Boolean {
            return isValidIpPrefix(getIpPrefix(wifiManager))
        }

        fun getActiveSsId(wifiManager: WifiManager, context: Context): String {
            Log.i("ConnectionUtils", "getActiveSsId --------------------------")

            if(!wifiManager.isWifiEnabled){
                return context.getString(R.string.warning_wifi_off)
            }

            var ssId = wifiManager.connectionInfo.ssid

            if (ssId.contains("\"")) {
                var splitSsId = ssId.split("\"")[1]

                Log.i("ConnectionUtils", "getActiveSsId will return split $splitSsId")

                return splitSsId
            }

            if (ssId == "0x"){
                ssId = "Not connected!"
            }


            Log.i("ConnectionUtils", "getActiveSsId will return $ssId")

            return ssId
        }

        fun getHostFromPreferences(context: Context): Int {
            Log.i("ConnectionUtils", "getHostFromPreferences $context-----------------")

            return context.applicationContext.getSharedPreferences(
                HOST_SHARED_KEY,
                Context.MODE_PRIVATE
            )
                .getInt(
                    HOST_SHARED_KEY, DEFAULT_MODBUS_HOST
                )
        }

        fun getPortFromPreferences(context: Context): Int {
            Log.i("ConnectionUtils", "getPortFromPreferences $context-----------------")

            return context.applicationContext.getSharedPreferences(
                PORT_SHARED_KEY,
                Context.MODE_PRIVATE
            )
                .getInt(
                    PORT_SHARED_KEY, DEFAULT_MODBUS_PORT
                )
        }
    }
}