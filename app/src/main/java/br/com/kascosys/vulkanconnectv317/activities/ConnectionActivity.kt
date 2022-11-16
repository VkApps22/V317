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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.databinding.ActivityConnectionBinding
import br.com.kascosys.vulkanconnectv317.enums.UserPermission
import br.com.kascosys.vulkanconnectv317.utils.ConnectionUtils
import br.com.kascosys.vulkanconnectv317.utils.Util

class ConnectionActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager

    private lateinit var connectivityManager: ConnectivityManager

    private lateinit var binding: ActivityConnectionBinding

    var loginDialogOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("ConnectionActivity", "onCreate------------------------")

        // Hide action bar
        supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_connection
        )

        binding.loginButton.setOnClickListener {
            inflateLoginDialog()
        }

        binding.configButton.setOnClickListener {
            inflateConfigDialog()
        }
    }

    override fun onResume() {
        super.onResume()

        Log.i("ConnectionActivity", "onResume------------------------")

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (!ConnectionUtils.isConnected(wifiManager)) {

            // Start Wifi system manager
            Log.i("ConnectionActivity", "onResume will intent system wifi")
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))

            // Listen to Wifi state change

            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()


            val netCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)

                    val connectedSsId = wifiManager
                        .connectionInfo.ssid.split("\"")[1]

                    Log.i(
                        "ConnectionActivity",
                        "connectToDevice netCallback onAvailable $network $connectedSsId"
                    )

                    val intent = Intent(CONNECTION_SUCCESS_FILTER)
                    sendBroadcast(intent)



//                    if (!wentToTabs) {
//                        wentToTabs = true
//
//                        inflateLoginDialog()
//                    }


                    connectivityManager.unregisterNetworkCallback(this)
                }

                override fun onUnavailable() {
                    super.onUnavailable()

                    Log.i(
                        "ConnectionActivity",
                        "connectToDevice netCallback onUnavailable"
                    )

                    val intent = Intent(CONNECTION_UNAVAILABLE_FILTER)
                    sendBroadcast(intent)

                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)

                    Log.i(
                        "ConnectionActivity",
                        "connectToDevice netCallback onLosing $network $maxMsToLive"
                    )

                }

                override fun onLost(network: Network) {
                    super.onLost(network)

                    Log.i(
                        "ConnectionActivity",
                        "connectToDevice netCallback onLosing $network"
                    )

                    val intent = Intent(CONNECTION_LOST_FILTER)
                    sendBroadcast(intent)

                }
            }

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.i(
                        "ConnectionActivity",
                        "connectToDevice receiver.onReceive $context $intent"
                    )

                    binding.pairingProgressBar.visibility = View.GONE

                    when (intent?.action) {
                        CONNECTION_SUCCESS_FILTER -> {
                            Log.i(
                                "ConnectionActivity",
                                "connectToDevice receiver.onReceive ${intent.action}"
                            )

                            goToTabs(UserPermission.BASIC)

                        }
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
                "ConnectionActivity",
                "connectToDevice register receiver $receiver"
            )

            val successFilter = IntentFilter(CONNECTION_SUCCESS_FILTER)
            val lostFilter = IntentFilter(CONNECTION_LOST_FILTER)
            val unavailableFilter = IntentFilter(CONNECTION_UNAVAILABLE_FILTER)

            registerReceiver(receiver, successFilter)
            registerReceiver(receiver, lostFilter)
            registerReceiver(receiver, unavailableFilter)

            connectivityManager.registerNetworkCallback(networkRequest, netCallback)

        } else {
//            inflateLoginDialog()

            goToTabs(UserPermission.BASIC)
        }

    }

    private fun inflateLoginDialog() {
        if (loginDialogOn) {
            return
        }
        loginDialogOn = true

        Log.i(
            "ConnectionActivity",
            "inflateLoginDialog-----------------------------------"
        )

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_login, null)

        val passwordEdit = dialogView.findViewById<EditText>(R.id.password_edit)
        passwordEdit.requestFocus()

        val titleString = "Login to continue"

        val titleView = Util.titleForDialog(titleString, this)
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val passwordString = passwordEdit.text.toString()
        AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setMessage("Device: ${ConnectionUtils.getActiveSsId(wifiManager,this)}")
            .setPositiveButton("Login") { _, _ ->
                loginDialogOn = false
                binding.pairingProgressBar.visibility = View.GONE
                goToTabs(getPermissionByPass(passwordString))
            }
            .setNegativeButton("Cancel") { _, _ ->
                loginDialogOn = false
                binding.pairingProgressBar.visibility = View.GONE
            }
            .setOnDismissListener {
                loginDialogOn = false
                binding.pairingProgressBar.visibility = View.GONE
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

        val ssIdText = dialogView.findViewById<TextView>(R.id.device_ssid_text)
        ssIdText.text = ConnectionUtils.getActiveSsId(wifiManager, this)


        val dialog = AlertDialog.Builder(this)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setPositiveButton("Ok") { _, _ ->

                val portString = portEdit.text.toString()
                val hostString = hostEdit.text.toString()

                val portInt = if (portString != "") portString.toInt() else DEFAULT_MODBUS_PORT
                val hostInt = if (hostString != "") hostString.toInt() else DEFAULT_MODBUS_HOST

                Log.i(
                    "ConnectionActivity",
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

        val connectButton = dialogView.findViewById<Button>(R.id.connectButton)
        connectButton.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }

    private fun goToTabs(permission: UserPermission) {
        Log.i(
            "ConnectionActivity",
            "goToTabs $permission-----------------------------------"
        )

        val intent = Intent(this, TabActivity::class.java)
        intent.putExtra(
            PERMISSION_EXTRA,
            permission.toBoolean()
        )
        startActivity(intent)

    }


    private fun getPermissionByPass(password: String): UserPermission {
        if (password.isNotBlank()) {
            return UserPermission.ADVANCED
        }

        return UserPermission.BASIC
    }
}
