package br.com.kascosys.vulkanconnectv317.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.adapters.DeviceAdapter
import br.com.kascosys.vulkanconnectv317.adapters.LanguageAdapter
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.database.DeviceDatabase
import br.com.kascosys.vulkanconnectv317.database.DeviceMinimalData
import br.com.kascosys.vulkanconnectv317.database.firebase.AlertsFirebaseRepository
import br.com.kascosys.vulkanconnectv317.databinding.ActivityPairingBinding
import br.com.kascosys.vulkanconnectv317.enums.DeviceState
import br.com.kascosys.vulkanconnectv317.interfaces.DeviceContainerActivity
import br.com.kascosys.vulkanconnectv317.interfaces.LangClickListener
import br.com.kascosys.vulkanconnectv317.interfaces.OnDeviceClick
import br.com.kascosys.vulkanconnectv317.managers.DriveManager
import br.com.kascosys.vulkanconnectv317.managers.OnlineManager
import br.com.kascosys.vulkanconnectv317.models.DeviceModel
import br.com.kascosys.vulkanconnectv317.utils.ConnectionUtils
import br.com.kascosys.vulkanconnectv317.utils.Util
import br.com.kascosys.vulkanconnectv317.viewModels.PairingViewModel
import com.yariksoffice.lingver.Lingver
import java.util.*

class PairingActivity : AppCompatActivity(), OnDeviceClick, DeviceContainerActivity {


    private var retryAttempts = 0

    private lateinit var driveManager: DriveManager

    override lateinit var progressBar: ProgressBar

    private lateinit var binding: ActivityPairingBinding

    private lateinit var viewModel: PairingViewModel

    private lateinit var wifiManager: WifiManager

    private lateinit var adapter: DeviceAdapter

    override lateinit var database: DeviceDatabase

    override var deviceList: MutableList<DeviceMinimalData> = mutableListOf()

    private lateinit var onlineManager: OnlineManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar
        supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_pairing
        )

        progressBar = binding.pairingProgressBar
        progressBar.visibility = View.VISIBLE

        database = DeviceDatabase.getInstance(this)

        onlineManager = OnlineManager.getInstance(this)
        driveManager = DriveManager.getInstance(this)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        Log.i("PairingActivity", "Called ViewModelProviders.of")
        viewModel = ViewModelProviders.of(this).get(PairingViewModel::class.java)

        val deviceList = viewModel.deviceList

        adapter = DeviceAdapter(deviceList, this, this)
        binding.recyclerDevices.adapter = adapter


        binding.configButton.setOnClickListener {

            Log.i("PairingActivity", "configButton onClick--------------")

            inflateConfigDialog()

        }

        binding.deviceButton.setOnClickListener {
            //            lookForDevices()
            driveManager.name = getString(R.string.unnamed_device)
            callAnySystemWifi()
        }

//        binding.langButton.setOnClickListener {
//            inflateLangDialog()
//        }

        val locale = Locale.getDefault()
        Log.i("PairingActivity", "onCreate locale ${locale.language}")


//        binding.langButton.setImageResource(
//            when (locale.language) {
//                LANGUAGE_EN -> R.drawable.ic_gb
//                LANGUAGE_ES -> R.drawable.ic_es
//                LANGUAGE_PT -> R.drawable.ic_pt
//                else -> R.drawable.ic_gb
//            }
//        )

//        DatabaseGetMinimalAsync(this).execute()

//        ConnectionUtils.getIpPrefix(wifiManager)

    }


    private fun inflateLangDialog() {
        Log.i(
            "PairingActivity",
            "inflateLangDialog------------"
        )

        lateinit var dialog: AlertDialog

        val titleString = getString(R.string.select_language)
        val titleView = Util.titleForDialog(titleString, this)

        val dialogView = layoutInflater.inflate(R.layout.dialog_lang, null)

        val context = this

        val listener = object : LangClickListener {
            override fun onLanguageClicked(pos: Int) {
                Log.i(
                    "PairingActivity",
                    "inflateLangDialog listener.onLanguageClicked $pos ${languageList[pos]}"
                )
                Lingver.getInstance().setLocale(context, languageList[pos])

                dialog.dismiss()

                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }

        val adapter = LanguageAdapter(this, listener)

        dialogView.findViewById<RecyclerView>(R.id.dialog_lang_list).adapter = adapter

        dialog = AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .show()

    }


    private fun callAnySystemWifi() {
        progressBar.visibility = View.VISIBLE

        goToTabs()

        startActivity(
            Intent(Settings.ACTION_WIFI_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        )


    }

    private fun callSystemWifi(ssId: String) {


        progressBar.visibility = View.VISIBLE

        startActivity(
            Intent(Settings.ACTION_WIFI_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        )

        Toast.makeText(
            this,
            resources.getQuantityString(R.plurals.toast_please_connect, 0, ssId),
            Toast.LENGTH_SHORT
        ).show()

        listenToConnection(ssId)
    }


    private fun listenToConnection(
        ssId: String, fromActivity: Boolean = false
    ) {
        val currentSsid = ConnectionUtils.getActiveSsId(wifiManager, this)

//        var disconnected = !wifiManager.isWifiEnabled

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val netCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                Log.i(
                    "PairingActivity",
                    "listenToConnection netCallback.onAvailable $network"
                )

                val intent = Intent(CONNECTION_SUCCESS_FILTER)
                sendBroadcast(intent)

            }

            override fun onUnavailable() {
                super.onUnavailable()

                Log.i(
                    "PairingActivity",
                    "listenToConnection netCallback.onUnavailable"
                )

                val intent = Intent(CONNECTION_UNAVAILABLE_FILTER)
                sendBroadcast(intent)

            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)

                Log.i(
                    "PairingActivity",
                    "listenToConnection netCallback.onLosing $network $maxMsToLive"
                )

            }

            override fun onLost(network: Network) {
                super.onLost(network)

                Log.i(
                    "PairingActivity",
                    "listenToConnection netCallback.onLosing $network"
                )

                val intent = Intent(CONNECTION_LOST_FILTER)
                sendBroadcast(intent)

            }
        }

        val activity = this

        var failed = true

        var toastShown = false

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(
                    "PairingActivity",
                    "listenToConnection receiver.onReceive $context $intent"
                )

                when (intent?.action) {
                    CONNECTION_SUCCESS_FILTER -> {
                        Log.i(
                            "PairingActivity",
                            "listenToConnection receiver.onReceive ${intent.action}"
                        )

                        val connectedSsId =
                            ConnectionUtils.getActiveSsId(wifiManager, context!!)

                        Log.i(
                            "PairingActivity",
                            "listenToConnection receiver.onReceive $ssId $connectedSsId"
                        )

                        if (connectedSsId.contains(ssId)
//                            &&
//                            disconnected
                        ) {


//                            selectedDeviceName = if (selectedDeviceName != "") {
//                                selectedDeviceName
//                            } else connectedSsId
                            failed = false

                            progressBar.visibility = View.GONE

                            goToTabs()

                            context.unregisterReceiver(this)
                            connectivityManager.unregisterNetworkCallback(netCallback)
                        } else {
                            if (!toastShown) {
                                Handler().postDelayed({
                                    if (fromActivity && failed) {
                                        Toast.makeText(
                                            activity,
                                            resources.getQuantityString(
                                                R.plurals.toast_connection_failed,
                                                0,
                                                ssId
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }, CONNECTION_TIMEOUT_MILLIS)

                                progressBar.visibility = View.GONE

                                toastShown = true
                            }
                        }

                    }
                    CONNECTION_UNAVAILABLE_FILTER -> {
                        Log.i(
                            "PairingActivity",
                            "listenToConnection disconnected from $currentSsid"
                        )

//                        disconnected = true
                    }

                    CONNECTION_LOST_FILTER -> {
                        Log.i(
                            "PairingActivity",
                            "listenToConnection connection lost"
                        )

//                        disconnected = true
                    }

                    CONNECTION_TIMEOUT_FILTER -> {
                        Log.i(
                            "PairingActivity",
                            "listenToConnection connection timeout"
                        )

                        Toast.makeText(
                            context,
                            resources.getQuantityString(
                                R.plurals.toast_connection_timeout,
                                0,
                                ssId
                            ),
                            Toast.LENGTH_LONG
                        ).show()

//                        disconnected = true
                    }

                    else -> Log.e(
                        "PairingActivity", "Unexpected action ${
                        intent?.action
                        }"
                    )
                }


            }
        }

        Log.i(
            "PairingActivity",
            "listenToConnection will register receiver $receiver"
        )

        val successFilter = IntentFilter(CONNECTION_SUCCESS_FILTER)
        val lostFilter = IntentFilter(CONNECTION_LOST_FILTER)
        val unavailableFilter = IntentFilter(CONNECTION_UNAVAILABLE_FILTER)
        val timeoutFilter = IntentFilter(CONNECTION_TIMEOUT_FILTER)

        registerReceiver(receiver, successFilter)
        registerReceiver(receiver, lostFilter)
        registerReceiver(receiver, unavailableFilter)
        registerReceiver(receiver, timeoutFilter)

        connectivityManager.registerNetworkCallback(networkRequest, netCallback)
    }

    override fun getScanResults() {
        Log.i("PairingActivity", "getScanResults--------------")

        progressBar.visibility = View.VISIBLE

        val results = wifiManager.scanResults

        val dbDevices: List<DeviceModel> = deviceList.map { fromDb ->
            DeviceModel(
                fromDb.deviceNickname,
                fromDb.deviceSize,
                fromDb.deviceSsId,
                DeviceState.OFFLINE,
                DEFAULT_ADVANCED_KEY,
                fromDb.lastActiveTime
            )
        }

        viewModel.addListToDeviceList(dbDevices)

        viewModel.deviceList.forEach {
            Log.i(
                "PairingActivity",
                "getScanResults forEach $it ${ConnectionUtils.getIp(wifiManager)}"
            )
            when {
                it.deviceSsId == ConnectionUtils.getActiveSsId(wifiManager, this) -> {
                    Log.i("PairingActivity", "getScanResults forEach CONNECTED")
                    it.deviceState = DeviceState.CONNECTED
                }

                results.firstOrNull { result -> result.SSID == it.deviceSsId } != null -> {
                    Log.i("PairingActivity", "getScanResults forEach ONLINE")
                    it.deviceState = DeviceState.ONLINE
                }

                else -> Log.i("PairingActivity", "getScanResults forEach OFFLINE")
            }
        }

        adapter.notifyDataSetChanged()

        progressBar.visibility = View.GONE
    }

    private class DatabaseGetMinimalAsync(val parent: DeviceContainerActivity) :
        AsyncTask<Void, Void, Void?>() {

        override fun doInBackground(vararg p0: Void?): Void? {
            Log.i("PairingActivity", "DatabaseGetMinimal.doInBackground--------------")

//            parent.database.deleteAllByNick("")

//            parent.database.deleteAllBySsId("Vulkan-ESP")

            parent.deviceList = parent.database.databaseDao.getAllMinimal()

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            parent.getScanResults()
        }
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
        val hostResult = hostPreferences.getInt(HOST_SHARED_KEY, DEFAULT_MODBUS_HOST)


        val portPreferences = getSharedPreferences(
            PORT_SHARED_KEY,
            Context.MODE_PRIVATE
        )
        val portResult = portPreferences.getInt(PORT_SHARED_KEY, DEFAULT_MODBUS_PORT)

        val ssIdText = dialogView.findViewById<TextView>(R.id.device_ssid_text)
        val activeSsId = ConnectionUtils.getActiveSsId(wifiManager, this)
        ssIdText.text = activeSsId

        val hostEdit = dialogView.findViewById<EditText>(R.id.host_edit)
        hostEdit.setText(hostResult.toString())
        hostEdit.clearFocus()
//        hostEdit.requestFocus()

        val portEdit = dialogView.findViewById<EditText>(R.id.port_edit)
        portEdit.setText(portResult.toString())

        val langButton = dialogView.findViewById<ImageView>(R.id.langButton)

        langButton.setOnClickListener {
            inflateLangDialog()
        }

        val locale = Locale.getDefault()

        langButton.setImageResource(
            when (locale.language) {
                LANGUAGE_EN -> R.drawable.ic_gb
                LANGUAGE_ES -> R.drawable.ic_es
                LANGUAGE_PT -> R.drawable.ic_pt
                else -> R.drawable.ic_gb
            }
        )

        AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->

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

//        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }

    private fun goToTabs() {
        Log.i("PairingActivity", "goToTabs -----------------")

        val intent = Intent(this, TabActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        if (deviceName != null) {
//            driveManager.name = deviceName
//        }
        startActivity(intent)
    }

//    override fun onRestart() {
//        super.onRestart()
//
//        Log.i("PairingActivity", "onRestart called")
//
////        getScanResults()
////        DatabaseGetMinimalAsync(this).execute()
//    }

    override fun onResume() {
        super.onResume()

        Log.i("PairingActivity", "onResume called")
        AlertsFirebaseRepository.fetchAlertsAsync()
        DatabaseGetMinimalAsync(this).execute()
    }

    override fun onPause() {
        super.onPause()

        Log.i("PairingActivity", "onPause called")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i("PairingActivity", "onDestroy called")
    }

    private fun inflateOfflineDialog(ssId: String) {

        val titleString = getString(R.string.warning_title)
        val titleView = Util.titleForDialog(titleString, this)

        AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setMessage(resources.getQuantityString(R.plurals.dialog_offline_message, 0, ssId))
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                onlineManager.goOffline()
                goToTabs()
            }
            .setNegativeButton(R.string.dialog_no, null)
            .show()
    }

    override fun onDeviceClicked(pos: Int) {
        Log.i("PairingActivity", "onDeviceClicked $pos----------------------")

//        inflateLoginDialog(ssId)
        val item = viewModel.deviceList[pos]

        val ssId = item.deviceSsId

        val nick = item.deviceNickname

        Log.i("PairingActivity", "onDeviceClicked $nick")

        when (item.deviceState) {
            DeviceState.OFFLINE -> {
                inflateOfflineDialog(ssId)
            }
            DeviceState.ONLINE -> {
                driveManager.name = nick
                connectToAp(ssId)
            }
            DeviceState.CONNECTED -> {
                driveManager.name = nick
                goToTabs()
            }
        }
    }

    private fun connectToAp(ssId: String) {
        Log.i("PairingActivity", "connectToAp $ssId----------------")

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            val configuredList = wifiManager.configuredNetworks
                ?: listOf()

            val configured = configuredList.firstOrNull { it.SSID.contains(ssId) }

            if (configured != null) {

                wifiManager.enableNetwork(configured.networkId, true)

                Log.i("PairingActivity", "connectToAp $configured")

                networkSuggestionSuccess(ssId)
            } else {
                networkSuggestionFailed(ssId)
            }
        } else {
            if (wifiManager.addNetworkSuggestions(
                    listOf(WifiNetworkSuggestion.Builder().setSsid(ssId).build())
                ) == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
            ) {
                networkSuggestionSuccess(ssId)
            } else {
                networkSuggestionFailed(ssId)
            }
        }
    }

    private fun networkSuggestionFailed(ssId: String) {
        callSystemWifi(ssId)
    }

    private fun networkSuggestionSuccess(ssId: String) {
        progressBar.visibility = View.VISIBLE

        if (retryAttempts < 1) {
            retryAttempts++

            Toast.makeText(
                this,
                resources.getQuantityString(R.plurals.toast_connection_attempt, 0, ssId),
                Toast.LENGTH_SHORT
            ).show()

            listenToConnection(ssId, true)
        } else {
            retryAttempts = 0
            callSystemWifi(ssId)
        }

    }


}