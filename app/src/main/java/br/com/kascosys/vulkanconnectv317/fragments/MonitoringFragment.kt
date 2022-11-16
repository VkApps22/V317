package br.com.kascosys.vulkanconnectv317.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.activities.RtGraphActivity
import br.com.kascosys.vulkanconnectv317.utils.Util
import br.com.kascosys.vulkanconnectv317.adapters.MonitoringAdapter
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.database.DeviceDatabase
import br.com.kascosys.vulkanconnectv317.interfaces.GraphClickListener
import br.com.kascosys.vulkanconnectv317.interfaces.OnChildCheck
import br.com.kascosys.vulkanconnectv317.interfaces.OnRefreshClick
import br.com.kascosys.vulkanconnectv317.managers.*
import br.com.kascosys.vulkanconnectv317.models.MainsModel
import br.com.kascosys.vulkanconnectv317.models.MonitoringModel
import br.com.kascosys.vulkanconnectv317.utils.ConnectionUtils
import br.com.kascosys.vulkanconnectv317.utils.DataUtils
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusUtils
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusValues
import com.buildware.widget.indeterm.IndeterminateCheckBox
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.fragment_monitoring.view.*
import kotlinx.android.synthetic.main.model_card.view.*
import java.util.*
import kotlin.collections.ArrayList

class MonitoringFragment : Fragment(), OnChildCheck, OnRefreshClick, GraphClickListener {


    // For testing
//    private var newC2 = 0
//    private var newC3 = 0

    private lateinit var alarmBlinkTimer: CountDownTimer

    private lateinit var cardOfflineIcon: ImageView

    private lateinit var activeSsId: String

    private lateinit var onlineManager: OnlineManager

    private var configDialogOn: Boolean = false

    private lateinit var checkBox: IndeterminateCheckBox

    private lateinit var switch: Switch

    private lateinit var adapter: MonitoringAdapter

    lateinit var myDataset: MutableList<MonitoringModel>

    private var checkedSize: Int = 0

    private val autoMode: MutableLiveData<Boolean> = MutableLiveData(true)

    private lateinit var alarmManager: AlarmManager

    private lateinit var driveManager: DriveManager

    private lateinit var monitoringManager: MonitoringManager

    private lateinit var parameterManager: ParameterManager

    private lateinit var cardModelText: TextView

    private lateinit var cardNameTextView: TextView

    private lateinit var cardStatusText: TextView

    private lateinit var cardVoltageText: TextView

    private lateinit var cardFrequencyText: TextView

    private lateinit var progressBar: ProgressBar

    private lateinit var modBusUtils: ModBusUtils

    private lateinit var wifiManager: WifiManager

    private lateinit var database: DeviceDatabase

    private var isPolling = false

    private var monitoringPollTimer: CountDownTimer? = null

    private lateinit var alarmPollTimer: CountDownTimer

    private var hardAlarm = -1

    private val mainsModel = MainsModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_monitoring,
            container,
            false
        )

        Log.i("MonitoringFragment","onCreateView locale ${Locale.getDefault()}")

        driveManager = DriveManager.getInstance(context!!)
        alarmManager = AlarmManager.getInstance(context!!)
        monitoringManager = MonitoringManager.getInstance(context!!)
        parameterManager = ParameterManager.getInstance(context!!)
        onlineManager = OnlineManager.getInstance(context!!)
        database = DeviceDatabase.getInstance(context!!)

        cardModelText = view.card_serial_number
        cardModelText.text = driveManager.getFormattedModel()

        cardNameTextView = view.card_model_number
        cardNameTextView.text = driveManager.name

        cardStatusText = view.card_status_text
        cardStatusText.text = if (alarmManager.alarm != DRIVE_OK) alarmManager.alarm else "Running"

        var lastClick = SystemClock.elapsedRealtime()

        cardStatusText.setOnClickListener {
            if (lastClick + MINIMUM_TAB_RELOAD_MILLIS < SystemClock.elapsedRealtime()) {
                lastClick = SystemClock.elapsedRealtime()
                val action = MonitoringFragmentDirections
                    .actionMonitoringFragmentToAlarmFragment()
                view.findNavController().navigate(action)
            }
        }

        cardVoltageText = view.card_input_voltage_text
        cardFrequencyText = view.card_input_frequency_text
        cardOfflineIcon = view.card_wifi_off_image
        cardOfflineIcon.setOnClickListener {
            if (onlineManager.onlineModeOn) {
                Log.e("HomeFragment", "cardOfflineIcon.onClickListener icon should be gone!")
            } else {
//                Toast.makeText(activity!!, "Go onlineModeOn", Toast.LENGTH_SHORT).show()
                inflateOnlineShowcase()
            }
        }
        setOfflineIconByManager()

        val cardLockIcon = view.card_lock_image
        cardLockIcon.visibility = View.GONE

        progressBar = view.monitoring_progress_bar

        wifiManager =
            activity!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        myDataset = mutableListOf()
        monitoringManager.getAll().forEach {
            Log.i("MonitoringFragment", "onCreateView for each mon $it ${it.id}")
            myDataset.add(
                MonitoringModel(
                    getString(it.nameResId),
                    it.id!!,
                    it.modBusAddress!!,
                    it.checked,
                    it.ratio!!,
                    it.minRange!!,
                    it.maxRange!!,
                    it.value!!,
                    it.unit!!
                )
            )
        }

        checkedSize = myDataset.count { it.checked }

//        Log.i("MonitoringFragment", "will call MonitoringAdapter autoMode $autoMode")
        adapter = MonitoringAdapter(this, myDataset, driveManager.size)
        view.monitoring_recyclerView.adapter = adapter

        checkBox = view.checkBox

//        updateParentCheckbox()
//        updateCheckboxVisibility()

        checkBox.setOnClickListener {
            if (checkBox.state == false) {
                myDataset.forEach { it.checked = false }
                checkedSize = 0
            } else {
                myDataset.forEach { it.checked = true }
                checkedSize = myDataset.size
            }
            adapter.notifyDataSetChanged()
            readChecked()
        }

        switch = view.switch1
        updateSwitchState()
        switch.setOnClickListener {
            Log.i(
                "MonitoringFragment",
                "switch.setOnClickListener Previous autoMode: ${autoMode.value}"
            )
            autoMode.value = autoMode.value?.not()
            updateSwitchState()
            updateCheckboxVisibility()
            adapter.notifyDataSetChanged()
            readChecked()
        }

//        setHasOptionsMenu(true)

        tryToConnect()

//        Log.i("MonitoringFragment","onCreateView test 3600 ${DataUtils.getTimeString(3600)}")
//        Log.i("MonitoringFragment","onCreateView test 43753947 ${DataUtils.getTimeString(43753947)}")
//        Log.i("MonitoringFragment","onCreateView test 567567 ${DataUtils.getTimeString(567567)}")
//        Log.i("MonitoringFragment","onCreateView test 23 ${DataUtils.getTimeString(23)}")
//        Log.i("MonitoringFragment","onCreateView test 234 ${DataUtils.getTimeString(234)}")
//        Log.i("MonitoringFragment","onCreateView test 234 ${DataUtils.getTimeString(100000)}")

        return view
    }

    override fun onResume() {
        super.onResume()

        Log.i("MonitoringFragment", "onResume----------------------")

        Lingver.getInstance().setLocale(context!!, Locale.getDefault())

        val monitoringList = myDataset.toList()

        ReadListAsync(this, monitoringList, false).execute()

        readChecked()

        cardInfoPolling()

        alarmBlinkPolling()
    }

    private fun cardInfoPolling() {
        val fragment = this

        alarmPollTimer = object : CountDownTimer(Long.MAX_VALUE, ALARM_POLLING_MILLIS) {
            override fun onFinish() {
                start()
            }

            override fun onTick(p0: Long) {
                GetAlarmAsync(fragment).execute()

                ReadMainsAsync(fragment).execute()
            }
        }.start()
    }

    private fun alarmBlinkPolling() {
        var flag = false

        val statusRunning = getString(R.string.status_running)

        alarmBlinkTimer = object : CountDownTimer(Long.MAX_VALUE, ALARM_BLINK_MILLIS) {
            override fun onFinish() {
                start()
            }

            override fun onTick(p0: Long) {
                if (!flag || hardAlarm == 0 || hardAlarm > MAX_ALARM_NUMBER) {
                    val id = ModBusValues.mapAlarmNumberToId(hardAlarm)

                    cardStatusText.text = when {
                        id == "A-01" -> getString(R.string.value_error)
                        id != DRIVE_OK -> id
                        else -> statusRunning
                    }
                } else {
                    cardStatusText.text = getString(R.string.card_blink_alarm_text)
                }

                flag = !flag
            }
        }.start()
    }

    private fun inflateOnlineShowcase() {
        val titleString = getString(R.string.warning_title)
        val context = context!!

        val titleView = Util.titleForDialog(titleString, context)

        AlertDialog.Builder(this.context!!)
            .setCustomTitle(titleView)
            .setMessage(R.string.online_mode_message)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                enterOnlineMode()
            }
            .setNegativeButton(R.string.dialog_no, null)
            .show()
    }


    private fun readChecked() {

        Log.i("MonitoringFragment", "readChecked----------------------")

        if (autoMode.value!!) {

            val autoList = myDataset.toList()

            if (autoList.isNotEmpty()) {
                val fragment = this

                monitoringPollTimer?.cancel()
                monitoringPollTimer =
                    object : CountDownTimer(Long.MAX_VALUE, MONITORING_POLLING_MILLIS) {
                        override fun onFinish() {
                            start()
                        }

                        override fun onTick(p0: Long) {
                            if (!isPolling) {
                                ReadListAsync(fragment, autoList).execute()
                            }
                        }
                    }.start()
            } else {
                monitoringPollTimer?.cancel()
            }

        } else {
            monitoringPollTimer?.cancel()
        }
    }

    override fun onPause() {
        super.onPause()

        try {
            monitoringPollTimer?.cancel()

            alarmPollTimer.cancel()

            alarmBlinkTimer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        DisconnectAsync(this).execute()

        Lingver.getInstance().setLocale(context!!, Locale.getDefault())
    }

    private fun tryToConnect() {
        Log.i("MonitoringFragment", "tryToConnect-------------------")

        val host = ConnectionUtils.getHostFromPreferences(activity!!.applicationContext)
        val port = ConnectionUtils.getPortFromPreferences(activity!!.applicationContext)
        AttemptConnectAsync(this, wifiManager, host, port).execute()
    }

    private class AttemptConnectAsync(
        val parent: MonitoringFragment,
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

            parent.progressBar.visibility = View.GONE

            if (result) {
                Log.i("AttemptConnectAsync", "onPostExecute ModBus connection succeeded")


            } else {
                Log.e("AttemptConnectAsync", "onPostExecute ModBus connection failed!")

                CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
            }
        }

    }

    private class CheckConnectionAsync(
        val parent: MonitoringFragment
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


//            if(parent.modBusUtils.isConnected()){
//                return parent.modBusUtils.isConnected()
//            }
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

//                parent.updateAll()

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

    private class ReadListAsync(
        val parent: MonitoringFragment,
        val monitoringList: List<MonitoringModel>,
        val pollMode: Boolean = true
    ) : AsyncTask<Void, Void, ArrayList<Number>>() {
        override fun onPreExecute() {
            super.onPreExecute()

            if (!pollMode) {
                parent.progressBar.visibility = View.VISIBLE
            }

            parent.isPolling = true
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<Number> {
//            parent.driveManager.size = parent.modBusUtils.getDriveSize()

//            parent.monitoringManager.updateM004ratio(parent.driveManager.size)

            return parent.modBusUtils.readMonitoringList(monitoringList)
        }

        override fun onPostExecute(result: ArrayList<Number>?) {
            super.onPostExecute(result)

            if (result != null) {
                if (monitoringList.isNotEmpty()) {
                    monitoringList.forEachIndexed { i, itemToRead ->
                        val listItem = parent.myDataset.first {
                            it.idNumber == itemToRead.idNumber
                        }
                        val readValue = result[i]

                        if (readValue.toFloat() < listItem.minRange.toFloat() ||
                            readValue.toFloat() > listItem.maxRange.toFloat()
                        ) {
                            if (readValue.toLong() < READ_ERROR) {
                                listItem.value = READ_ERROR
                            }

                            listItem.value = readValue
                        } else {
                            listItem.value = readValue

                            if (listItem.idNumber == M021) {
                                parent.updateC2C3(listItem.value.toInt())
                            }
                        }
                    }
                }
                parent.adapter.notifyDataSetChanged()

                parent.isPolling = false

                parent.progressBar.visibility = View.GONE
            } else {
                Log.e(
                    "ReadListAsync",
                    "onPostExecute ModBus reading failed! ${parent.onlineManager.trials}"
                )

                parent.isPolling = false

                parent.progressBar.visibility = View.GONE

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    ReadListAsync(parent, monitoringList, pollMode).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }
            }
        }

    }

    private fun updateC2C3(m021Val: Int) {
        val newC2 = DataUtils.getBitFromMask(m021Val, DIG_IN_ENABLE_MASK)
        val newC3 = DataUtils.getBitFromMask(m021Val, DIG_IN_MDI5_MASK)

        Log.i(
            "MonitoringFragment",
            "updateC2C3 $m021Val $newC2 $newC3 ${monitoringManager.c2} ${monitoringManager.c3}--------------"
        )

        Log.i(
            "MonitoringFragment",
            "updateC2C3 last times ${monitoringManager.openTime} ${monitoringManager.closeTime}--------------"
        )

        monitoringManager.c2 = newC2 == 1
        monitoringManager.c3 = newC3 == 1

        Log.i(
            "MonitoringFragment",
            "updateC2C3 new times ${monitoringManager.openTime} ${monitoringManager.closeTime}--------------"
        )

//        myDataset.firstOrNull { it.idNumber == OPEN_ID }?.value = monitoringManager.openTime
//        myDataset.firstOrNull { it.idNumber == CLOSE_ID }?.value = monitoringManager.closeTime
    }

    private class ReadSingleAsync(
        val parent: MonitoringFragment,
        val item: MonitoringModel,
        val pos: Int
    ) : AsyncTask<Void, Void, Number?>() {
        override fun onPreExecute() {
            super.onPreExecute()

            parent.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Number? {

            parent.monitoringManager.updateM004ratio(parent.driveManager.size)

            return parent.modBusUtils.readMonitoring(item)
        }

        override fun onPostExecute(result: Number?) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result != null) {
                Log.i(
                    "ReadSingleAsync", "onPostExecute ModBus reading succeeded ${
                    item.idNumber
                    } $result"
                )


                if (result.toFloat() < item.minRange.toFloat() ||
                    result.toFloat() > item.maxRange.toFloat()
                ) {
                    item.value = result
                    parent.adapter.notifyItemChanged(pos)

                } else {
                    item.value = result
                    parent.adapter.notifyItemChanged(pos)
                }


            } else {
                Log.e(
                    "ReadSingleAsync",
                    "onPostExecute ModBus reading failed! ${parent.onlineManager.trials}"
                )

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    ReadSingleAsync(parent, item, pos).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }
            }
        }

    }

    private class GetAlarmAsync(
        val parent: MonitoringFragment
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

//                parent.cardStatusText.text = if (result != DRIVE_OK) {
//                    result
//                } else {
//                    parent.getString(R.string.status_running)
//                }

                when {
                    result.startsWith('A') -> parent.cardStatusText.setTextColor(Color.RED)
                    result.startsWith('W') -> parent.cardStatusText.setTextColor(Color.YELLOW)
                    else -> parent.cardStatusText.setTextColor(Color.WHITE)
                }


                parent.alarmManager.setAlarm(result)

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

    private class ReadMainsAsync(
        val parent: MonitoringFragment
    ) : AsyncTask<Void, Void, MainsModel>() {
        override fun doInBackground(vararg p0: Void?): MainsModel {
            Log.i(
                "ReadMainsAsync", "doInBackground-----------------------"
            )

            return parent.modBusUtils.getMains()
        }

        override fun onPostExecute(result: MainsModel) {
            super.onPostExecute(result)

            if (result.voltage.toLong() < READ_ERROR) {
                Log.i(
                    "ReadMainsAsync", "onPostExecute voltage read successful"
                )

                parent.mainsModel.voltage = result.voltage
            } else {
                Log.e(
                    "ReadMainsAsync", "onPostExecute voltage read failed!"
                )
            }

            if (result.frequency.toLong() < READ_ERROR) {
                Log.i(
                    "ReadMainsAsync", "onPostExecute frequency read successful"
                )

                parent.mainsModel.frequency = result.frequency
            } else {
                Log.e(
                    "ReadMainsAsync",
                    "onPostExecute frequency read failed! ${parent.onlineManager.trials}"
                )

                if (!parent.onlineManager.onlineModeOn) {
                    return
                }

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    ReadMainsAsync(parent).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }
            }

//            parent.lineChart.description.text = parent.resources.getQuantityString(
//                R.plurals.mains_label,
//                0,
//                parent.mainsModel.voltage.toFloat(),
//                parent.mainsModel.frequency.toFloat()
//            )
            parent.cardFrequencyText.text = "${parent.mainsModel.frequency} Hz"
            parent.cardVoltageText.text = "${parent.mainsModel.voltage} V"
        }

    }

    private class DisconnectAsync(val parent: MonitoringFragment) : AsyncTask<Void, Void, Void>() {
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


//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_help, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.help_action) {
////            Toast.makeText(context, "Help clicked", Toast.LENGTH_SHORT).show()
//
//            inflateHelpShowcase()
//
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun inflateHelpShowcase() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_monitoring_help, null)

        val titleString = getString(R.string.dialog_help_title)
        val context = context!!

        val titleView = Util.titleForDialog(titleString, context)

        AlertDialog.Builder(context)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_ok, null)
            .show()
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

        startActivity(
            Intent(Settings.ACTION_WIFI_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        )

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
                    "ConnectionActivity",
                    "connectToDevice netCallback onAvailable $network"
                )

                val intent = Intent(CONNECTION_SUCCESS_FILTER)
                context?.sendBroadcast(intent)

//                connectivityManager.unregisterNetworkCallback(this)
            }

            override fun onUnavailable() {
                super.onUnavailable()

                Log.i(
                    "ConnectionActivity",
                    "connectToDevice netCallback onUnavailable"
                )

                val intent = Intent(CONNECTION_UNAVAILABLE_FILTER)
                context?.sendBroadcast(intent)

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
                context?.sendBroadcast(intent)

            }
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(
                    "ConnectionActivity",
                    "connectToDevice receiver.onReceive $context $intent"
                )

                progressBar.visibility = View.GONE

                when (intent?.action) {
                    CONNECTION_SUCCESS_FILTER -> {
                        Log.i(
                            "ConnectionActivity",
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
                            "ConnectionActivity",
                            "connectToDevice disconnected from $currentSsid"
                        )

                        disconnected = true
                    }

                    CONNECTION_LOST_FILTER -> {
                        Log.i(
                            "ConnectionActivity",
                            "connectToDevice connection lost"
                        )

                        disconnected = true
                    }

                    else -> Log.e(
                        "ConnectionActivity", "Unexpected action ${
                        intent?.action
                        }"
                    )
                }


            }
        }

        Log.i(
            "ConnectionActivity",
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
            "MonitoringFragment",
            "reloadFragment-----------------------------------"
        )

        activity?.finish()
        startActivity(activity?.intent)

    }

    private fun enterOnlineMode() {
        onlineManager.goOnline()
        alarmPollTimer.start()
        setOfflineIconByManager()
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
        setOfflineIconByManager()
    }

    private fun setOfflineIconByManager() {
        cardOfflineIcon.visibility = if (!onlineManager.onlineModeOn) View.VISIBLE else View.GONE
    }

    override fun onAnyBoxChecked(pos: Int) {
        if (pos >= 0 && pos < myDataset.size) {
            if (!myDataset[pos].checked) {
                checkedSize++
            } else {
                checkedSize--
            }
            myDataset[pos].checked = !myDataset[pos].checked
            readChecked()
        }

        updateParentCheckbox()

        adapter.notifyItemChanged(pos)

//        Log.i("MonitoringFragment", "onAnyBoxChecked checked $checked")

    }

    override fun onRefreshClicked(pos: Int) {
        refreshItemValue(pos)
    }

    override fun onGraphButtonClick(pos: Int) {
        Log.i("MonitoringFragment", "onGraphButtonClick $pos")

        val id = myDataset[pos].idNumber

        Log.i("MonitoringFragment", "onGraphButtonClick will call graph for $id")

        val intent = Intent(this.activity, RtGraphActivity::class.java)

        val baudRateIndex = parameterManager.getBy(BAUD_RATE_ID).value?.toInt() ?: -1

        Log.i("MonitoringFragment", "onGraphButtonClick bps $baudRateIndex")

        if (baudRateIndex > -1) {
            val optimalRate = 57600
            if (DataUtils.mapToBitrate(baudRateIndex) < optimalRate) {
                Toast.makeText(
                    activity,
                    resources.getQuantityString(
                        R.plurals.monitoring_baud_rate_message,
                        0,
                        optimalRate
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        intent.putExtra(
            RT_GRAPH_ID_EXTRA,
            id
        )

        startActivity(intent)
    }

    private fun refreshItemValue(pos: Int) {
//        myDataset[pos].value =
//            Random.nextInt(myDataset[pos].minRange.toInt(), myDataset[pos].maxRange.toInt())

        ReadSingleAsync(this, myDataset[pos], pos).execute()

        adapter.notifyItemChanged(pos)
    }

    private fun updateParentCheckbox() {
        var checked: Boolean? = false
        if (checkedSize == myDataset.size) {
            checked = true
        } else if (checkedSize > 0) {
            checked = null
        }

        checkBox.state = checked
    }

    private fun updateSwitchState() {
        when {
            autoMode.value == true -> switch.isChecked = true
            autoMode.value == false -> switch.isChecked = false
            else -> Log.e("MonitoringFragment", "updateSwitchState Null autoMode value")
        }
    }

    private fun updateCheckboxVisibility() {
        when {
            autoMode.value == true ->
                checkBox.visibility = View.VISIBLE
            autoMode.value == false ->
                checkBox.visibility = View.INVISIBLE
            else ->
                Log.e("MonitoringFragment", "updateCheckboxVisibility Null autoMode value")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Lingver.getInstance().setLocale(context!!, Locale.getDefault())
    }
}