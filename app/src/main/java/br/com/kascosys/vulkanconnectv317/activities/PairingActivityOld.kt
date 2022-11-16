package br.com.kascosys.vulkanconnectv317.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.EXTRA_RESULTS_UPDATED
import android.net.wifi.WifiNetworkSpecifier
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.utils.Util
import br.com.kascosys.vulkanconnectv317.adapters.DeviceAdapter
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.databinding.ActivityPairingBinding
import br.com.kascosys.vulkanconnectv317.interfaces.OnDeviceClick
import br.com.kascosys.vulkanconnectv317.managers.ConnectionManager
import br.com.kascosys.vulkanconnectv317.models.PermissionData
import br.com.kascosys.vulkanconnectv317.utils.ConnectionUtils
import br.com.kascosys.vulkanconnectv317.viewModels.PairingViewModel
import kotlinx.android.synthetic.main.activity_pairing.*
import kotlinx.android.synthetic.main.fragment_home.*

class PairingActivityOld : AppCompatActivity(), OnDeviceClick {

    private lateinit var binding: ActivityPairingBinding

    private lateinit var viewModel: PairingViewModel

    private lateinit var wifiManager: WifiManager

    private lateinit var adapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar
        supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_pairing
        )

        pairingProgressBar.visibility = View.INVISIBLE

        Log.i("PairingActivity", "Called ViewModelProviders.of")
        viewModel = ViewModelProviders.of(this).get(PairingViewModel::class.java)

        val deviceList = viewModel.deviceList

        adapter = DeviceAdapter(deviceList, this, this)
        binding.recyclerDevices.adapter = adapter

        binding.configButton.setOnClickListener {

            Log.i("PairingActivity", "configButton onClick--------------")

//            Toast.makeText(this, "QR Code not yet implemented", Toast.LENGTH_SHORT).show()

//            ParameterList(applicationContext)

//            ModbusTest(wifiManager).execute()

            inflateConfigDialog()

        }

        binding.deviceButton.setOnClickListener {
            //            binding.pairingProgressBar.visibility = View.VISIBLE
//
//            Handler().postDelayed({
//                adapter.shuffleAndRefresh()
//                binding.pairingProgressBar.visibility = View.INVISIBLE
//            }, 1500)

            lookForDevices()

//            val wifiInfo = wifiManager.connectionInfo
//            val ipAddress = wifiInfo.ipAddress
//
////        Log.i("PairingActivity","$ipAddress")
//
//            val ipString = Formatter.formatIpAddress(ipAddress)
//
//            val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)
//
//
//            val retroFit = Retrofit.Builder()
//                .baseUrl("http://${prefix}1")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//
//            val service = retroFit.create(TestAPI::class.java)
//
//            val call = service.getPingServerL()
//
//            call.enqueue(object : Callback<APIResponse> {
//                override fun onFailure(call: Call<APIResponse>?, t: Throwable?) {
//                    Log.d("PairingActivity", "onFailure Request failed ${t?.message}")
//                }
//
//                override fun onResponse(
//                    call: Call<APIResponse>?,
//                    response: Response<APIResponse>?
//                ) {
//                    Log.i("PairingActivity", "onResponse Request success!")
//                }
//
//            })

//            ConnectionManager.getInstance(applicationContext)

        }

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!ConnectionUtils.isConnected(wifiManager)) {
            Log.i("PairingActivity", "onCreate will call system wifi")
            callSystemWifi()

        } else {
            Log.i("PairingActivity", "onCreate wifi already connected")
        }
//
////        NetworkTest(wifiManager).execute()
//        val wifiInfo = wifiManager.connectionInfo
//        val ipAddress = wifiInfo.ipAddress
//
////        Log.i("PairingActivity","$ipAddress")
//
//        val ipString = Formatter.formatIpAddress(ipAddress)
//
//        val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)
//
//
//        val retroFit = Retrofit.Builder()
//            .baseUrl("http://${prefix}1")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val service = retroFit.create(TestAPI::class.java)
//
//        val call = service.getPingServerH()
//
//        call.enqueue(object : Callback<APIResponse> {
//            override fun onFailure(call: Call<APIResponse>?, t: Throwable?) {
//                Log.d("PairingActivity", "onFailure Request failed ${t?.message}")
//
//
//            }
//
//            override fun onResponse(call: Call<APIResponse>?, response: Response<APIResponse>?) {
//                Log.i("PairingActivity", "onResponse Request success!")
//            }
//
//        })

//        lookForDevices()
        ConnectionUtils.getIpPrefix(wifiManager)
//        NetworkTest(wifiManager).execute()
    }

    private fun callSystemWifi() {
        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    private fun inflateLoginDialog(name: String) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_login, null)

        val passwordEdit = dialogView.findViewById<EditText>(R.id.password_edit)
        passwordEdit.requestFocus()
        passwordEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.onPasswordChange(text.toString())
        }
        val password = passwordEdit.text.toString()

        val titleString = "Login to continue"

        val titleView = Util.titleForDialog(titleString, this)
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setMessage("Device: $name")
            .setPositiveButton("Login") { _, _ ->
                if (name.contains("Test")) {
                    if (
                        viewModel.onLogin(name)
//                    connectToDevice(nameResId, viewModel.password.value!!)
                    ) {
                        ConnectionManager.getInstance(applicationContext)
                        goToTabs(PermissionData(PermissionData.ADVANCED_PERMISSION))
                    } else {
//                        Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show()
                    }
                } else {

//                    if (
////                        viewModel.onLogin(name)
//                        connectToDevice(name, viewModel.password.value!!)
//                    ) {
//                        ConnectionManager.getInstance(applicationContext)
//                        goToTabs(PermissionData(PermissionData.ADVANCED_PERMISSION))
//                    } else {
//                        Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show()
//                    }
//                    connectToDevice(name, password)

                    connectToDevice(name, "")
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Basic Mode") { _, _ ->
                if (name.contains("Test")) {
                    goToTabs(PermissionData(PermissionData.BASIC_PERMISSION))
                } else {
                    connectToDevice(name, "")
                }


            }
            .setOnDismissListener {
                inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            }
            .show()

        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }

    private fun inflateConfigDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_config, null)

        val titleView = Util.titleForDialog(
            getString(R.string.config_title),
            this
        )
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager

        val hostPreferences = getSharedPreferences(
            HOST_SHARED_KEY,
            Context.MODE_PRIVATE
        )
        var hostResult = hostPreferences.getInt(HOST_SHARED_KEY, DEFAULT_MODBUS_HOST)


        val portPreferences = getSharedPreferences(
            PORT_SHARED_KEY,
            Context.MODE_PRIVATE
        )
        var portResult = portPreferences.getInt(PORT_SHARED_KEY, DEFAULT_MODBUS_PORT)

        val hostEdit = dialogView.findViewById<EditText>(R.id.host_edit)
        hostEdit.setText(hostResult.toString())
        hostEdit.requestFocus()

        val portEdit = dialogView.findViewById<EditText>(R.id.port_edit)
        portEdit.setText(portResult.toString())

        AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setPositiveButton("Ok") { _, _ ->

                val portString = portEdit.text.toString()
                val hostString = hostEdit.text.toString()

                val portInt = if (portString != "") portString.toInt() else DEFAULT_MODBUS_PORT
                val hostInt = if (hostString != "") hostString.toInt() else DEFAULT_MODBUS_HOST

                Log.i(
                    "PairingActivity",
                    "inflateConfigDialog on click $hostString $portString"
                )

                with(portPreferences.edit()) {
                    putInt(PORT_SHARED_KEY, portInt)
                    commit()
                }

                with(hostPreferences.edit()) {
                    putInt(HOST_SHARED_KEY, hostInt)
                    commit()
                }
            }
            .setOnDismissListener {
                inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            }
            .show()

        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }


    private fun goToTabs(permission: PermissionData) {
        val intent = Intent(this, TabActivity::class.java)
        intent.putExtra(
            PERMISSION_EXTRA,
            permission.permission
        )
        startActivity(intent)
    }

    private fun lookForDevices() {
        Log.i("PairingActivity", "lookForDevices----------")

        val context = this.applicationContext

        wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Automatically enable wifi for API 28 or lower
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !wifiManager.isWifiEnabled) {
            Toast.makeText(this, "Activating WiFI...", Toast.LENGTH_LONG).show()

            @Suppress("DEPRECATION")
            wifiManager.isWifiEnabled = true
        }

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                Log.i("PairingActivity", "wifiScanReceiver.onReceive----------")

                val success = intent.getBooleanExtra(
                    EXTRA_RESULTS_UPDATED,
                    false
                )
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }

//                unregisterReceiver(this)
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        Log.i("PairingActivity", "lookForDevices will startScan")

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        @Suppress("DEPRECATION")
        val success = wifiManager.startScan()

        if (!success) {
            // scan failure handling
            scanFailure()
        }
//        } else {
//            scanFailure()
//        }

    }

    private fun scanSuccess() {
        Log.i("PairingActivity", "scanSuccess----------")
        val results = wifiManager.scanResults
        //... use new scan results ...

        Log.i("PairingActivity", "scanSuccess results: ${results.size}")
        results.forEach {
            Log.i("PairingActivity", "scanSuccess $it")
        }

        viewModel.onFindDevices(results, adapter)

//        val deviceResults = filterDevices(results)
//        Log.i("PairingActivity","scanFailure deviceResults: ${deviceResults.size}")
//        deviceResults.forEach {
//            Log.i("PairingActivity","scanSuccess $it")
//        }
    }

    private fun scanFailure() {
        Log.i("PairingActivity", "scanFailure----------")
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
        //... potentially use older scan results ...

        Log.i("PairingActivity", "scanFailure results: ${results.size}")
        results.forEach {
            Log.i("PairingActivity", "scanFailure $it")
        }

        viewModel.onFindDevices(results, adapter)

//        val deviceResults = filterDevices(results)
//        Log.i("PairingActivity","scanFailure deviceResults: ${deviceResults.size}")
//        deviceResults.forEach {
//            Log.i("PairingActivity","scanFailure $it")
//        }
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    private fun connectToDevice(ssId: String, password: String): Boolean {
        Log.i("PairingActivity", "connectToDevice-----------------")

        val context = this

        if (wifiManager.isWifiEnabled) {
            Log.i("PairingActivity", "connectToDevice wifi enabled")



            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                Log.i("PairingActivity", "connectToDevice API 28 or earlier")

                val cm =
                    applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                val linkIpPrefix = ConnectionUtils.getIpPrefix(wifiManager)
//                val activeNetwork = cm.activeNetworkInfo
//                val isAboutConnect = activeNetwork?.isConnectedOrConnecting
//                val isWifi = activeNetwork?.type == ConnectivityManager.TYPE_WIFI

                var currentSsId = ""
                if (
                    wifiManager.connectionInfo.ssid.isNotEmpty() &&
                    wifiManager.connectionInfo.ssid.contains("\"")
                ) {
                    currentSsId = wifiManager.connectionInfo.ssid.split("\"")[1]
                }

                Log.i("PairingActivity", "connectToDevice $currentSsId $ssId")

                if (ssId != currentSsId || !ConnectionUtils.isValidIpPrefix(linkIpPrefix)) {

                    val configuration = WifiConfiguration()
                    configuration.SSID = "\"${ssId}\""

                    if (password != "") {
                        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                        configuration.preSharedKey = "\"${password}\""
                    } else {
                        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    }


                    val netId = wifiManager.addNetwork(configuration)
                    wifiManager.disconnect()

                    var success = false

                    binding.pairingProgressBar.visibility = View.VISIBLE

                    val networkRequest = NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build()

                    var wentToTabs = false
                    val netCallback = object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)

                            val connectedSsId = wifiManager
                                .connectionInfo.ssid.split("\"")[1]

                            Log.i(
                                "PairingActivity",
                                "connectToDevice netCallback onAvailable $network $connectedSsId $ssId"
                            )

                            if (connectedSsId == ssId) {
                                Log.i(
                                    "PairingActivity",
                                    "connectToDevice netCallback will go to tabs"
                                )
//                                ConnectionManager.getInstance(applicationContext).reconnect()
                                val intent = Intent(CONNECTION_SUCCESS_FILTER)
                                applicationContext.sendBroadcast(intent)

                                if (!wentToTabs) {
                                    wentToTabs = true


                                    goToTabs(PermissionData(PermissionData.ADVANCED_PERMISSION))
                                }

                            } else {
                                Log.e(
                                    "PairingActivity",
                                    "connectToDevice netCallback wrong SSID!"
                                )
                                val intent = Intent(CONNECTED_TO_WRONG_AP_FILTER)
                                applicationContext.sendBroadcast(intent)

                                wifiManager.disconnect()
                            }

                            cm.unregisterNetworkCallback(this)
                        }

                        override fun onUnavailable() {
                            super.onUnavailable()

                            Log.i(
                                "PairingActivity",
                                "connectToDevice netCallback onUnavailable"
                            )

                            val intent = Intent(CONNECTION_UNAVAILABLE_FILTER)
                            applicationContext.sendBroadcast(intent)

                        }

                        override fun onLosing(network: Network, maxMsToLive: Int) {
                            super.onLosing(network, maxMsToLive)

                            Log.i(
                                "PairingActivity",
                                "connectToDevice netCallback onLosing $network $maxMsToLive"
                            )

                        }

                        override fun onLost(network: Network) {
                            super.onLost(network)

                            Log.i(
                                "PairingActivity",
                                "connectToDevice netCallback onLosing $network"
                            )

                            val intent = Intent(CONNECTION_LOST_FILTER)
                            applicationContext.sendBroadcast(intent)

                        }
                    }


                    Log.i(
                        "PairingActivity",
                        "connectToDevice will enable connect ${
                        configuration.SSID
                        } ${
                        configuration.preSharedKey
                        }"
                    )
                    wifiManager.enableNetwork(netId, true)

                    wifiManager.reconnect()

                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            Log.i(
                                "PairingActivity",
                                "connectToDevice receiver.onReceive $context $intent"
                            )

                            binding.pairingProgressBar.visibility = View.GONE

                            when (intent?.action) {
                                CONNECTION_SUCCESS_FILTER -> Log.i(
                                    "PairingActivity",
                                    "connectToDevice receiver.onReceive ${intent.action}"
                                )
                                else -> Toast.makeText(
                                    context,
                                    "Connection failed!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                            unregisterReceiver(this)
                        }
                    }

                    Log.i(
                        "PairingActivity",
                        "connectToDevice register receiver $receiver"
                    )

                    val successFilter = IntentFilter(CONNECTION_SUCCESS_FILTER)
                    val wrongApFilter = IntentFilter(CONNECTED_TO_WRONG_AP_FILTER)
                    val lostFilter = IntentFilter(CONNECTION_LOST_FILTER)
                    val unavailableFilter = IntentFilter(CONNECTION_UNAVAILABLE_FILTER)

                    registerReceiver(receiver, successFilter)
                    registerReceiver(receiver, wrongApFilter)
                    registerReceiver(receiver, lostFilter)
                    registerReceiver(receiver, unavailableFilter)

                    cm.registerNetworkCallback(networkRequest, netCallback)

                    return success

                }

                Log.i("PairingActivity", "connectToDevice already connected")

                goToTabs(PermissionData(PermissionData.ADVANCED_PERMISSION))

                return true


            } else {
                Log.i("PairingActivity", "connectToDevice new sdk")

                val specifier = WifiNetworkSpecifier.Builder()
                    .setSsid(ssId)
//                    .setWpa2Passphrase(password)
//                    .setWpa3Passphrase(password)
            }
        } else {
            Toast.makeText(this, "Please turn Wifi on!", Toast.LENGTH_LONG).show()
        }

        return false
    }

    override fun onStart() {
        super.onStart()

        Log.i("PairingActivity", "onStart called")
    }

    override fun onResume() {
        super.onResume()

        Log.i("PairingActivity", "onResume called")
    }

    override fun onPause() {
        super.onPause()

        Log.i("PairingActivity", "onPause called")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i("PairingActivity", "onDestroy called")
    }

    override fun onRestart() {
        super.onRestart()

        Log.i("PairingActivity", "onRestart called")
    }

    override fun onStop() {
        super.onStop()

        Log.i("PairingActivity", "onStop called")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        Log.i("PairingActivity", "onSaveInstanceState called")
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)

        Log.i("PairingActivity", "onRestoreInstanceState called")
    }

    override fun onDeviceClicked(pos: Int) {

    }

    private fun setProgressGone() {
        progressBar.visibility = View.GONE
    }


}