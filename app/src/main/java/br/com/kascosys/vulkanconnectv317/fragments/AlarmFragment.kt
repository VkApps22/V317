package br.com.kascosys.vulkanconnectv317.fragments

import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.adapters.AlarmAdapter
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.database.AlertsFirebase
import br.com.kascosys.vulkanconnectv317.interfaces.AlarmListItem
import br.com.kascosys.vulkanconnectv317.interfaces.OnHeaderClick
import br.com.kascosys.vulkanconnectv317.managers.AlarmManager
import br.com.kascosys.vulkanconnectv317.managers.OnlineManager
import br.com.kascosys.vulkanconnectv317.models.AlarmHeader
import br.com.kascosys.vulkanconnectv317.models.AlarmItem
import br.com.kascosys.vulkanconnectv317.models.AlarmModel
import br.com.kascosys.vulkanconnectv317.utils.ConnectionUtils
import br.com.kascosys.vulkanconnectv317.utils.Util
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusUtils
import com.shuhart.stickyheader.StickyHeaderItemDecorator
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.fragment_alarms.view.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class AlarmFragment : Fragment(), OnHeaderClick {

    private lateinit var activeSsId: String
    private lateinit var onlineManager: OnlineManager

    private var configDialogOn: Boolean = false

    private val alarmList: MutableList<AlarmListItem> = mutableListOf()

    private lateinit var adapter: AlarmAdapter

    private val showInactiveAlarms = MutableLiveData<Boolean>(false)

    private lateinit var alarmManager: AlarmManager

    private lateinit var modBusUtils: ModBusUtils

    private lateinit var wifiManager: WifiManager

    private lateinit var alarmPollTimer: CountDownTimer

    private lateinit var progressBar: ProgressBar

    private var hardAlarm = -1

    var activeSection = 0
    var inactiveSection = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_alarms,
            container,
            false
        )

        Log.i("AlarmFragment","onCreateView locale ${Locale.getDefault()}")

        alarmManager = AlarmManager.getInstance(context!!)

        onlineManager = OnlineManager.getInstance(context!!)

        wifiManager =
            activity!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager


        runBlocking {
            launch {
                val data = AlertsFirebase().getData()
                Log.i("Firebase","Result list alerts: $data")
            }
        }

        val myAlarmsDataSet: MutableList<AlarmModel> =
            mutableListOf( //TODO: REMOVE HARDCODE
                AlarmModel(
                    getString(R.string.a001_label),
                    "A001",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a002_label),
                    "A002",
                    getString(R.string.a002_description)
                ),
                AlarmModel(
                    getString(R.string.a003_label),
                    "A003",
                    getString(R.string.a003_description)
                ),
                AlarmModel(
                    getString(R.string.a004_label),
                    "A004",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a005_label),
                    "A005",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a006_label),
                    "A006",
                    getString(R.string.a006_description)
                ),
                AlarmModel(
                    getString(R.string.a007_label),
                    "A007",
                    getString(R.string.a007_description)
                ),
                AlarmModel(
                    getString(R.string.a008_label),
                    "A008",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a009_label),
                    "A009",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a010_label),
                    "A010",
                    getString(R.string.alarm_generic_description)
                ), AlarmModel(
                    getString(R.string.a011_label),
                    "A011",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a012_label),
                    "A012",
                    getString(R.string.a012_description)
                ),
                AlarmModel(
                    getString(R.string.a013_label),
                    "A013",
                    getString(R.string.a013_description)
                ),
                AlarmModel(
                    getString(R.string.a014_label),
                    "A014",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a015_label),
                    "A015",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a016_label),
                    "A016",
                    getString(R.string.a016_description)
                ),
                AlarmModel(
                    getString(R.string.a017_label),
                    "A017",
                    getString(R.string.a017_description)
                ),
                AlarmModel(
                    getString(R.string.a018_label),
                    "A018",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a019_label),
                    "A019",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a020_label),
                    "A020",
                    getString(R.string.a020_description)
                ),
                AlarmModel(
                    getString(R.string.a021_label),
                    "A021",
                    getString(R.string.a021_description)
                ),
                AlarmModel(
                    getString(R.string.a022_label),
                    "A022",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a023_label),
                    "A023",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a024_label),
                    "A024",
                    getString(R.string.a024_description)
                ),
                AlarmModel(
                    getString(R.string.a025_label),
                    "A025",
                    getString(R.string.a025_description)
                ),
                AlarmModel(
                    getString(R.string.a026_label),
                    "A026",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a027_label),
                    "A027",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a028_label),
                    "A028",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a029_label),
                    "A029",
                    getString(R.string.a029_description)
                ),
                AlarmModel(
                    getString(R.string.a030_label),
                    "A030",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.a031_label),
                    "A031",
                    getString(R.string.a031_description)
                ),
                AlarmModel(
                    getString(R.string.a032_label),
                    "A032",
                    getString(R.string.a032_description)
                ),
                AlarmModel(
                    getString(R.string.a033_label),
                    "A033",
                    getString(R.string.a033_description)
                ),
                AlarmModel(
                    getString(R.string.w001_label),
                    "W001",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.w002_label),
                    "W002",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.w003_label),
                    "W003",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.w004_label),
                    "W004",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.w005_label),
                    "W005",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.w006_label),
                    "W006",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.w007_label),
                    "W007",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.w008_label),
                    "W008",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.w009_label),
                    "W009",
                    getString(R.string.alarm_generic_description)
                ),
                AlarmModel(
                    getString(R.string.wXXX_label),
                    "WXXX",
                    getString(R.string.alarm_generic_description)
                )
            )



        alarmList.add(
            AlarmHeader(
                getString(R.string.active_alarms_header),
                AlarmListItem.stateConstants.ACTIVE,
                activeSection
            )
        )
        alarmList.add(
            AlarmHeader(
                getString(R.string.inactive_alarms_header),
                AlarmListItem.stateConstants.INACTIVE,
                inactiveSection
            )
        )

        myAlarmsDataSet.forEachIndexed { i, item ->
            val itemPos = 2 + i
            alarmList.add(itemPos, AlarmItem(item, inactiveSection))
        }

        val recyclerView = view.alarm_recyclerView


        adapter = AlarmAdapter(alarmList, showInactiveAlarms, this)
        val decorator = StickyHeaderItemDecorator(adapter)
        decorator.attachToRecyclerView(recyclerView)

        recyclerView.adapter = adapter

        progressBar = view.alarm_progress_bar

        setHasOptionsMenu(true)

        tryToConnect()

        return view
    }

    override fun onResume() {
        super.onResume()

        Log.i("AlarmFragment", "onResume $alarmManager-----------")

        Lingver.getInstance().setLocale(context!!, Locale.getDefault())

        inactivateAlarm(alarmManager.oldAlarm)

        activateAlarm(alarmManager.alarm)

        alarmPolling()
    }

    override fun onPause() {
        super.onPause()

        Log.i("AlarmFragment", "onPause -----------")

        try {
            alarmPollTimer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        DisconnectAsync(this).execute()

        Lingver.getInstance().setLocale(context!!, Locale.getDefault())
    }

    private fun tryToConnect() {
        Log.i("AlarmFragment", "tryToConnect-------------------")

        val host = ConnectionUtils.getHostFromPreferences(activity!!.applicationContext)
        val port = ConnectionUtils.getPortFromPreferences(activity!!.applicationContext)
        AttemptConnectAsync(this, wifiManager, host, port).execute()
    }

    private fun alarmPolling() {
        val fragment = this

        alarmPollTimer = object : CountDownTimer(Long.MAX_VALUE, ALARM_POLLING_MILLIS) {
            override fun onFinish() {
                start()
            }

            override fun onTick(p0: Long) {
                GetAlarmAsync(fragment).execute()
            }
        }.start()
    }

    fun activateAlarm(id: String) {
        Log.i("AlarmFragment", "activateAlarm-----------id:$id")

        val alarm = alarmList.filter {
            (it.javaClass == AlarmItem::class.java) && ((it as AlarmItem).data.idNumber == id)
        }

        if (alarm.size == 1) {

            val alarmItem = alarm[0]
            val oldAlarmState = alarmItem.state

            alarmItem.state = AlarmListItem.stateConstants.ACTIVE
            alarmItem.section = activeSection

            // Reinsert alarm at correct position
            alarmList.remove(alarmItem)
            alarmList.add(activeSection + 1, alarmItem)

            if (oldAlarmState == AlarmListItem.stateConstants.INACTIVE) {

                // Reorder inactive header and alarms
                inactiveSection++
                alarmList.forEachIndexed { i, item ->
                    if (item.state == AlarmListItem.stateConstants.INACTIVE) {
                        alarmList[i].section = inactiveSection
                    }
                }
            }

            adapter.notifyDataSetChanged()

        } else if (alarm.isNotEmpty()) {
            Log.e("AlarmFragment", "activateAlarm Duplicate found")
        } else {
            Log.e("AlarmFragment", "activateAlarm Alarm not found")
        }

    }

    fun inactivateAlarm(id: String) {
        Log.i("AlarmFragment", "inactivateAlarm-----------id:$id")

        val alarm = alarmList.filter {
            (it.javaClass == AlarmItem::class.java) && ((it as AlarmItem).data.idNumber == id)
        }

        if (alarm.size == 1) {

            var alarmItem = alarm[0]
            val oldAlarmState = alarmItem.state

            if (oldAlarmState == AlarmListItem.stateConstants.ACTIVE) {
                alarmItem.state = AlarmListItem.stateConstants.INACTIVE
                inactiveSection--
                alarmItem.section = inactiveSection

                // Reinsert alarm at correct position
                alarmList.remove(alarmItem)

                // Reinsert inactivated alarm orderly
                run insertLoop@{
                    alarmList.forEachIndexed { i, item ->
                        if (item.state == AlarmListItem.stateConstants.INACTIVE) {
                            if (
                                item.listItemType == AlarmListItem.typeConstants.ITEM &&
                                (alarmItem as AlarmItem).data.idNumber < (item as AlarmItem).data.idNumber
                            ) {
                                alarmList.add(i, alarmItem)
                                return@insertLoop
                            }
                        }
                    }
                }

                // Reorder inactive header and items
                alarmList.forEachIndexed { i, item ->
                    if (item.state == AlarmListItem.stateConstants.INACTIVE) {
                        alarmList[i].section = inactiveSection
                    }
                }

                adapter.notifyDataSetChanged()
            } else {
                Log.e("AlarmFragment", "inactivateAlarm Alarm already inactive")
            }
        } else if (alarm.isNotEmpty()) {
            Log.e("AlarmFragment", "inactivateAlarm Duplicate found")
        } else {
            Log.e("AlarmFragment", "inactivateAlarm Alarm not found")
        }

    }

    override fun OnHeaderClicked() {
        Log.i("AlarmFragment", "OnHeaderClicked-----------")

        showInactiveAlarms.value = showInactiveAlarms.value?.not()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_alarm, menu)

        super.onCreateOptionsMenu(menu, inflater)

        val searchView = menu.findItem(R.id.app_bar_search)?.actionView as SearchView
        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
//                    adapter.filtering = true
                    adapter.filter.filter(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (newText.isNotEmpty()) {
//                        adapter.filtering = true
                        adapter.filter.filter(newText)
                    } else {
                        adapter.filter.filter("")
//                        adapter.filtering = false
//                        adapter.notifyDataSetChanged()
                    }
                }
                return false
            }
        })

        searchView.setOnCloseListener {
            adapter.filtering = false
            adapter.notifyDataSetChanged()
            false
        }
    }

    private class AttemptConnectAsync(
        val parent: AlarmFragment,
        val wifiManager: WifiManager,
        val host: Int,
        val port: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()
            parent.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            val prefix = ConnectionUtils.getIpPrefix(wifiManager)
            parent.modBusUtils = ModBusUtils(prefix, host, port)

            return parent.modBusUtils.isConnected()
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

//            parent.progressBar.visibility = View.GONE

            if (result) {
                Log.i("AttemptConnectAsync", "onPostExecute ModBus connection succeeded")


            } else {
                Log.e("AttemptConnectAsync", "onPostExecute ModBus connection failed!")

                CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
            }
        }

    }

    private class CheckConnectionAsync(
        val parent: AlarmFragment
    ) : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()

            Log.i(
                "CheckConnectionAsync",
                "onPreExecute--------------------"
            )

            parent.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Boolean {

            Log.i(
                "CheckConnectionAsync",
                "doInBackground--------------------"
            )

            if (!parent.onlineManager.onlineModeOn) {
                return false
            }

            if (!ConnectionUtils.isConnected(parent.wifiManager)) {
                return false
            }

            parent.modBusUtils.disconnect()

            parent.modBusUtils.connectMaster()

            return parent.modBusUtils.isConnected()
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result) {
                Log.i("CheckConnectionAsync", "onPostExecute ModBus connection succeeded")

                parent.onlineManager.resetTrials()

            } else {
                Log.e(
                    "CheckConnectionAsync",
                    "onPostExecute ModBus connection failed! ${parent.onlineManager.trials}"
                )
                if (!parent.onlineManager.onlineModeOn) {
                    return
                }

                parent.inflateConfigDialog()
            }
        }

    }

    private class GetAlarmAsync(
        val parent: AlarmFragment
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg p0: Void?): String? {
            val alarm = parent.modBusUtils.getAlarmNumber()

            if (alarm != "") {
                return alarm
            }

            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result != null) {
                Log.i(
                    "GetAlarmAsync", "onPostExecute ModBus read succeeded $result"
                )

                if (result != parent.alarmManager.alarm) {

                    parent.alarmManager.setAlarm(result)

                    parent.inactivateAlarm(parent.alarmManager.oldAlarm)

                    parent.activateAlarm(result)

                    val number =
                        if (result != DRIVE_OK) {


                            if (result.startsWith('A')) {
                                result.removePrefix("A").toInt()
                            } else {
                                if (result.removePrefix("W") != "XXX") {
                                    result.removePrefix("W").toInt() + MAX_ALARM_NUMBER
                                } else MAX_WARNING_NUMBER + 1
                            }
                        } else 0
                    parent.hardAlarm = number
                }

            } else {
                Log.e(
                    "GetAlarmAsync",
                    "onPostExecute ModBus read failed! ${parent.onlineManager.trials}"
                )

                if (!parent.onlineManager.onlineModeOn) {
                    return
                }

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    GetAlarmAsync(parent).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }

            }
        }

    }

    private class DisconnectAsync(val parent: AlarmFragment) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            Log.i(
                "DisconnectAsync", "doInBackground-------------"
            )

            try {
                parent.modBusUtils.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            Log.i(
                "DisconnectAsync", "onPostExecute-------------"
            )
        }

    }

    private fun inflateConfigDialog() {

        Log.i(
            "HomeFragment",
            "inflateConfigDialog $configDialogOn--------------"
        )

        if (configDialogOn) {
            return
        }

        if (!onlineManager.onlineModeOn) {
            return
        }

        configDialogOn = true

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_config, null)

        val titleView = Util.titleForDialog(
            getString(R.string.warning_title),
            activity!!
        )
        titleView.requestFocus()

        val inputManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager

        val hostPreferences = activity!!.getSharedPreferences(
            HOST_SHARED_KEY,
            Context.MODE_PRIVATE
        )
        val hostResult = hostPreferences.getInt(HOST_SHARED_KEY, DEFAULT_MODBUS_HOST)


        val portPreferences = activity!!.getSharedPreferences(
            PORT_SHARED_KEY,
            Context.MODE_PRIVATE
        )
        val portResult = portPreferences.getInt(PORT_SHARED_KEY, DEFAULT_MODBUS_PORT)

        val ssIdText = dialogView.findViewById<TextView>(R.id.device_ssid_text)
        activeSsId = ConnectionUtils.getActiveSsId(wifiManager, context!!)
        ssIdText.text = activeSsId

        val hostEdit = dialogView.findViewById<EditText>(R.id.host_edit)
        hostEdit.setText(hostResult.toString())
//        hostEdit.requestFocus()

        val portEdit = dialogView.findViewById<EditText>(R.id.port_edit)
        portEdit.setText(portResult.toString())

        val dialog = AlertDialog.Builder(activity!!)
            .setCustomTitle(titleView)
            .setMessage(R.string.reconfig_settings_message)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->

                val portString = portEdit.text.toString()
                val hostString = hostEdit.text.toString()

                val portInt = if (portString != "") portString.toInt() else DEFAULT_MODBUS_PORT
                val hostInt = if (hostString != "") hostString.toInt() else DEFAULT_MODBUS_HOST

                Log.i(
                    "HomeFragment",
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

                configDialogOn = false

                onlineManager.resetTrials()

                AttemptConnectAsync(
                    this,
                    wifiManager,
                    hostString.toInt(),
                    portString.toInt()
                ).execute()
            }
            .setNeutralButton(R.string.offline_mode_label) { _, _ ->
                enterOfflineMode()
                configDialogOn = false
            }
            .setOnDismissListener {
                inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                configDialogOn = false
            }
            .show()

        val connectButton = dialogView.findViewById<Button>(R.id.connectButton)
        connectButton.setOnClickListener {
            dialog.dismiss()
            callSystemWifi()
        }

        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }

    private fun callSystemWifi() {

        val currentSsid = ConnectionUtils.getActiveSsId(wifiManager, context!!)

        var disconnected = !wifiManager.isWifiEnabled

        progressBar.visibility = View.VISIBLE

        DisconnectAsync(this).execute()

        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))

        val connectivityManager =
            context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()


        val netCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                Log.i(
                    "AlarmFragment",
                    "connectToDevice netCallback onAvailable $network"
                )

                val intent = Intent(CONNECTION_SUCCESS_FILTER)
                context?.sendBroadcast(intent)

            }

            override fun onUnavailable() {
                super.onUnavailable()

                Log.i(
                    "AlarmFragment",
                    "connectToDevice netCallback onUnavailable"
                )

                val intent = Intent(CONNECTION_UNAVAILABLE_FILTER)
                context?.sendBroadcast(intent)

            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)

                Log.i(
                    "AlarmFragment",
                    "connectToDevice netCallback onLosing $network $maxMsToLive"
                )

            }

            override fun onLost(network: Network) {
                super.onLost(network)

                Log.i(
                    "AlarmFragment",
                    "connectToDevice netCallback onLosing $network"
                )

                val intent = Intent(CONNECTION_LOST_FILTER)
                context?.sendBroadcast(intent)

            }
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(
                    "AlarmFragment",
                    "connectToDevice receiver.onReceive $context $intent"
                )

                progressBar.visibility = View.GONE

                when (intent?.action) {
                    CONNECTION_SUCCESS_FILTER -> {
                        Log.i(
                            "AlarmFragment",
                            "connectToDevice receiver.onReceive ${intent.action}"
                        )

                        val connectedSsId =
                            ConnectionUtils.getActiveSsId(wifiManager, context!!)

                        if (connectedSsId.contains(AP_IDENTIFIER) &&
                            disconnected
                        ) {
                            reloadFragment()

                            context.unregisterReceiver(this)
                            connectivityManager.unregisterNetworkCallback(netCallback)
                        }

                    }
                    CONNECTION_UNAVAILABLE_FILTER -> {
                        Log.i(
                            "AlarmFragment",
                            "connectToDevice disconnected from $currentSsid"
                        )

                        disconnected = true
                    }

                    CONNECTION_LOST_FILTER -> {
                        Log.i(
                            "AlarmFragment",
                            "connectToDevice connection lost"
                        )

                        disconnected = true
                    }

                    else -> Log.e(
                        "AlarmFragment", "Unexpected action ${
                        intent?.action
                        }"
                    )
                }


            }
        }

        Log.i(
            "AlarmFragment",
            "connectToDevice register receiver $receiver"
        )

        val successFilter = IntentFilter(CONNECTION_SUCCESS_FILTER)
        val lostFilter = IntentFilter(CONNECTION_LOST_FILTER)
        val unavailableFilter = IntentFilter(CONNECTION_UNAVAILABLE_FILTER)

        context?.registerReceiver(receiver, successFilter)
        context?.registerReceiver(receiver, lostFilter)
        context?.registerReceiver(receiver, unavailableFilter)

        connectivityManager.registerNetworkCallback(networkRequest, netCallback)
    }

    private fun reloadFragment() {
        Log.i(
            "AlarmFragment",
            "reloadFragment-----------------------------------"
        )

        activity?.finish()
        startActivity(activity?.intent)

    }

    private fun enterOnlineMode() {
        onlineManager.goOnline()
        alarmPollTimer.start()
        inflateConfigDialog()
    }

    private fun enterOfflineMode() {
        onlineManager.goOffline()
        DisconnectAsync(this).execute()
        try {
            alarmPollTimer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Lingver.getInstance().setLocale(context!!, Locale.getDefault())
    }
}