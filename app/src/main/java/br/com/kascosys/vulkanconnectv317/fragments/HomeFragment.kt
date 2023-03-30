package br.com.kascosys.vulkanconnectv317.fragments

import android.app.Activity
import android.content.*
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.activities.EditOnGraphActivity
import br.com.kascosys.vulkanconnectv317.adapters.ParametersAdapter
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.database.DeviceData
import br.com.kascosys.vulkanconnectv317.database.DeviceDatabase
import br.com.kascosys.vulkanconnectv317.enums.LockState
import br.com.kascosys.vulkanconnectv317.enums.ParameterAction
import br.com.kascosys.vulkanconnectv317.enums.ParameterState
import br.com.kascosys.vulkanconnectv317.enums.UserPermission
import br.com.kascosys.vulkanconnectv317.interfaces.GraphClickListener
import br.com.kascosys.vulkanconnectv317.interfaces.OnSlide
import br.com.kascosys.vulkanconnectv317.interfaces.ParameterListItem
import br.com.kascosys.vulkanconnectv317.managers.*
import br.com.kascosys.vulkanconnectv317.models.MainsModel
import br.com.kascosys.vulkanconnectv317.models.ParameterItem
import br.com.kascosys.vulkanconnectv317.utils.ConnectionUtils
import br.com.kascosys.vulkanconnectv317.utils.DataUtils
import br.com.kascosys.vulkanconnectv317.utils.Util
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusUtils
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusValues
import br.com.kascosys.vulkanconnectv317.viewModels.ParameterViewModel
import com.google.gson.GsonBuilder
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.util.*
import kotlin.random.Random


class HomeFragment : Fragment(), OnSlide, GraphClickListener {


    private var validDevice: Boolean = false

    private lateinit var viewModel: ParameterViewModel

    private lateinit var adapter: ParametersAdapter

    private lateinit var parameterList: MutableList<ParameterListItem>

    private lateinit var button: Button

    private lateinit var generalParameterState: LiveData<ParameterState>

    private lateinit var progressBar: ProgressBar

    private lateinit var wifiManager: WifiManager

    private lateinit var modBusUtils: ModBusUtils

    private var configDialogOn = false

    private lateinit var activeSsId: String

    private lateinit var cardStatusText: TextView

    private lateinit var alarmPollTimer: CountDownTimer

    private lateinit var alarmBlinkTimer: CountDownTimer

    private var hardAlarm = -1

    private lateinit var alarmManager: AlarmManager

    private lateinit var driveManager: DriveManager

    private lateinit var monitoringManager: MonitoringManager

    private lateinit var parameterManager: ParameterManager

    private lateinit var database: DeviceDatabase

    private lateinit var modelNameText: TextView

    private val mainsModel = MainsModel()

    private lateinit var cardVoltageText: TextView

    private lateinit var cardFrequencyText: TextView

    private lateinit var cardDeviceNameText: TextView

    private lateinit var cardOfflineIcon: ImageView

    private lateinit var permission: LiveData<Boolean>

    private lateinit var permissionManager: PermissionManager

    private lateinit var onlineManager: OnlineManager

    private lateinit var cardLockIcon: ImageView

//    private var modBusAttempts = CONNECTION_TRIALS

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("HomeFragment", "onCreateView---------------------")

        Log.i("HomeFragment", "onCreateView locale ${Locale.getDefault()}")

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        button = view.home_button
        progressBar = view.progressBar
        cardStatusText = view.card_container.findViewById(R.id.card_status_text)
        modelNameText = view.card_container.findViewById(R.id.card_serial_number)
        cardVoltageText = view.card_container.findViewById(R.id.card_input_voltage_text)
        cardFrequencyText = view.card_container.findViewById(R.id.card_input_frequency_text)
        cardDeviceNameText = view.card_container.findViewById(R.id.card_model_number)

        // Must be instantiated before cardOfflineIcon
        onlineManager = OnlineManager.getInstance(context!!)

        if (activity != null) {
            wifiManager =
                activity!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        }
        // Must be instantiated before viewModel

        parameterManager = ParameterManager.getInstance(this.context!!)

        alarmManager = AlarmManager.getInstance(context!!)
        driveManager = DriveManager.getInstance(context!!)
        monitoringManager = MonitoringManager.getInstance(context!!)

        database = DeviceDatabase.getInstance(context!!)

        cardOfflineIcon = view.card_container.findViewById(R.id.card_wifi_off_image)
        cardOfflineIcon.setOnClickListener {
            if (onlineManager.onlineModeOn) {
                Log.e("HomeFragment", "cardOfflineIcon.onClickListener icon should be gone!")
            } else {
//                Toast.makeText(activity!!, "Go onlineModeOn", Toast.LENGTH_SHORT).show()
                inflateOnlineShowcase()
            }
        }


        cardLockIcon = view.card_container.findViewById(R.id.card_lock_image)
        cardLockIcon.setOnClickListener {
            if (driveManager.lockState == LockState.UNLOCKED) {
                inflateSetPasswordDialog()
            } else {
                inflateUnlockDialog()
            }
        }
        setOfflineIconByManager()


        val intent = activity?.intent

//        Log.i("HomeFragment", "onCreateView will call Connection Manager")
//        ConnectionManager.getInstance(this.context!!)
//        val isConnected = ConnectionManager.getInstance()?.isConnected
//
//        if (isConnected == null || !isConnected) {
//            Toast.makeText(this.context, "Connection failed! Loading home...", Toast.LENGTH_LONG)
//                .show()
//        }


        viewModel = ViewModelProviders.of(this).get(ParameterViewModel::class.java)

        generalParameterState = viewModel.generalParameterState
        setButtonByGeneral()

        Log.i("HomeFragment", "onCreateView parameterManager $parameterManager")

        permissionManager = PermissionManager.getInstance(context!!)
        viewModel.setPermission(permissionManager.permission.toBoolean())



        Log.i(
            "HomeFragment",
            "onCreateView permissionManager $permissionManager ${viewModel.permission.value}"
        )

        permission = viewModel.permission

        viewModel.setPermission(
            intent!!.getBooleanExtra(
                PERMISSION_EXTRA,
                UserPermission.BASIC.toBoolean()
            )
        )

//            permission.value = true

//        Log.d("HomeFragment", "onCreateView permission $permission")

        parameterList = viewModel.parameterList

//        Log.d("HomeFragment", "-------${parameterList.size}")


//        view.button2.visibility = View.GONE

        view.setOnClickListener {
            hideKeyboard()
        }

        adapter = ParametersAdapter(
            parameterList,
            this,
            this,
            permissionManager,
            driveManager,
            onlineManager
        )

//        Log.d(
//            "HomeFragment", "-------${adapter.itemCount}"
//        )

        val recyclerView = view.parameters_recyclerView
        recyclerView.adapter = adapter

//        if (permissionManager.permission == UserPermission.ADVANCED) {
//            val decorator = StickyHeaderItemDecorator(adapter)
//            decorator.attachToRecyclerView(recyclerView)
//        }

        setHasOptionsMenu(true)

        button.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            Log.i(
                "HomeFragment",
                "button clickListener button pressed ${generalParameterState.value}"
            )

            Handler().post {
                if (onlineManager.onlineModeOn && driveManager.lockState == LockState.UNLOCKED) {
                    when (generalParameterState.value) {


                        ParameterState.UNEDITED -> Log.e(
                            "HomeFragment",
                            "button clickListener button should be hidden!"
                        )
                        ParameterState.EDITED -> onProgramClick()
                        ParameterState.UNSAVED -> onSaveClick()
                        else -> {}
                    }
                }

//                progressBar.visibility = View.GONE
            }


        }

        var lastClick = SystemClock.elapsedRealtime()

        cardStatusText.setOnClickListener {
            //            Log.i("HomeFragment", "cardStatusText.setOnClickListener before $hardAlarm")
//
//            ProgramAlarmAsync(this, hardAlarm).execute()
//
//            hardAlarm++
//
//            if (hardAlarm > MAX_WARNING_NUMBER) {
//                hardAlarm = 0
//            }
//
//            Log.i("HomeFragment", "cardStatusText.setOnClickListener after $hardAlarm")

            if (lastClick + MINIMUM_TAB_RELOAD_MILLIS < SystemClock.elapsedRealtime()) {
                lastClick = SystemClock.elapsedRealtime()

                val action = HomeFragmentDirections.actionHomeFragmentToAlarmFragment()

                Lingver.getInstance().setLocale(context!!, Locale.getDefault())

                view.findNavController().navigate(action)
            }

        }


//        if (onlineManager.onlineModeOn) {
//            tryToConnect()
//        }

        Log.i("HomeFragment", "onCreateView will return $view")

        return view
    }


    private fun setOfflineIconByManager() {
        if (!onlineManager.onlineModeOn) {
            cardOfflineIcon.visibility = View.VISIBLE

            cardLockIcon.visibility = View.GONE
        } else {
            cardOfflineIcon.visibility = View.GONE

            cardLockIcon.visibility = View.VISIBLE
            setLockIconByManager()
        }
    }

    private fun setLockIconByManager() {
        Log.i("HomeFragment", "setLockIconByManager ${driveManager.lockState}-----------")

        cardLockIcon.setImageResource(
            if (driveManager.lockState == LockState.UNLOCKED) {
                R.drawable.ic_lock_open
            } else {
                R.drawable.ic_lock
            }
        )
    }

    override fun onResume() {
        Log.i("HomeFragment", "onResume---------------------------")

        super.onResume()

        Lingver.getInstance().setLocale(context!!, Locale.getDefault())

        if (onlineManager.onlineModeOn) {
            tryToConnect()
        }

        configDialogOn = false

//        Handler().postDelayed({
//            tryToConnect()
//            progressBar.visibility = View.GONE
//        }, 100)

        var flag = false
        val fragment = this

        Log.d(
            "HomeFragment",
            "onResume ${onlineManager.onlineModeOn} ${fragment.driveManager.lockState}"
        )


        GetDriveSizeAsync(this).execute()

        val statusRunning = getString(R.string.status_running)

        // if app hasn't been unlocked yet in online mode, ask for unlocking

        if (fragment.onlineManager.onlineModeOn
            && fragment.driveManager.lockState == LockState.FIRST_LOCKED
        ) {
            inflateUnlockDialog()
        }

        alarmPollTimer = object : CountDownTimer(Long.MAX_VALUE, ALARM_POLLING_MILLIS) {
            override fun onFinish() {
                start()
            }

            override fun onTick(p0: Long) {
                Log.i("HomeFragment", "alarmPollTimer.onTick ${onlineManager.onlineModeOn}-----")

                if (fragment.onlineManager.onlineModeOn) {

                    GetAlarmAsync(fragment).execute()

                    ReadMainsAsync(fragment).execute()

                    ReadListAsync(fragment, parameterList, false).execute()

                    if (driveManager.lockState != LockState.FIRST_LOCKED) {
                        CheckPasswordAsync(fragment).execute()
                    }
                }
            }
        }.start()

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

    override fun onPause() {
        Log.i("HomeFragment", "onPause---------------------------")

        super.onPause()

        configDialogOn = true

        try {
            alarmPollTimer.cancel()

            alarmBlinkTimer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        DisconnectAsync(this).execute()

        Lingver.getInstance().setLocale(context!!, Locale.getDefault())
    }

    private class AttemptConnectAsync(
        val parent: HomeFragment,
        val wifiManager: WifiManager,
        val host: Int,
        val port: Int,
        val postAction: ParameterAction
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

                parent.onlineManager.resetTrials()

                when (postAction) {
                    ParameterAction.READ_ALL -> parent.updateAll()
                    ParameterAction.TRY -> Log.i(
                        "AttemptConnectAsync",
                        "onPostExecute try succeeded"
                    )
                    else -> Log.i(
                        "AttemptConnectAsync",
                        "onPostExecute else"
                    )
                }

                if (parent.driveManager.lockState == LockState.FIRST_LOCKED) {
                    parent.inflateUnlockDialog()
                }

            } else {
                Log.e(
                    "AttemptConnectAsync",
                    "onPostExecute ModBus connection failed! ${parent.onlineManager.trials}"
                )

                CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
            }
        }


    }

    private fun addDeviceToDatabase() {
        Log.i(
            "HomeFragment",
            "addDeviceToDatabase---------------------"
        )

        validDevice = true

        DatabaseAddDeviceAsync(this).execute()
    }

    private class DatabaseAddDeviceAsync(val parent: HomeFragment) :
        AsyncTask<Void, Void, Boolean>() {

        var ssId = ""

        private var deviceName = ""

        override fun doInBackground(vararg p0: Void?): Boolean {

            Log.i(
                "DatabaseAddDeviceAsync",
                "doInBackground---------------------"
            )

            ssId = ConnectionUtils.getActiveSsId(parent.wifiManager, parent.context!!)

            deviceName = parent.driveManager.name

            val newDeviceData = DeviceData(
                Random.nextLong(),
                ssId,
                parent.driveManager.size,
                deviceName,
                Date().time,
                mutableListOf(),
                mutableListOf(),
                parent.alarmManager.alarm,
                mutableListOf(),
                M003
            )

            parent.database.insertOrUpdate(newDeviceData)


            val devices = parent.database.databaseDao.getAllMinimal()

            val gson = GsonBuilder().setPrettyPrinting().create()

            Log.i(
                "DatabaseAddDeviceAsync",
                "doInBackground will return ${gson.toJson(devices)}"
            )

            return (devices.indexOfFirst { it.deviceSsId == newDeviceData.deviceSsId } > -1)
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)

            Log.i(
                "DatabaseAddDeviceAsync",
                "doInBackground---------------------"
            )

            parent.driveManager.name = deviceName

            parent.cardDeviceNameText.text = parent.driveManager.name
        }

    }

    private class CheckConnectionAsync(
        val parent: HomeFragment
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

            try {
                parent.modBusUtils.disconnect()

                parent.modBusUtils.connectMaster()
            } catch (e: Exception) {
                e.printStackTrace()
            }


            return parent.modBusUtils.isConnected()
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result) {
                Log.i(
                    "CheckConnectionAsync",
                    "onPostExecute ModBus connection succeeded ${
                    ConnectionUtils.getIp(parent.wifiManager)
                    }"
                )

                parent.onlineManager.resetTrials()

            } else {
                Log.e(
                    "CheckConnectionAsync",
                    "onPostExecute ModBus connection failed! ${parent.onlineManager.trials}"
                )
                if (!parent.onlineManager.onlineModeOn) {
                    return
                }

                parent.inflateConfigDialog(ParameterAction.READ_ALL)
            }
        }

    }

    private class ReadAsync(
        val parent: HomeFragment,
        val item: ParameterItem,
        val pos: Int
    ) : AsyncTask<Void, Void, Number?>() {
        override fun onPreExecute() {
            super.onPreExecute()

            parent.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Number? {
            val id = item.data.id
            val ratio = item.data.ratio

            if (id != null && ratio != null) {
                return parent.modBusUtils.readParameter(id, ratio)
            }

            return null
        }

        override fun onPostExecute(result: Number?) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result != null) {
                Log.i(
                    "ReadAsync", "onPostExecute ModBus reading succeeded ${
                    item.data.id
                    } $result"
                )

                parent.onlineManager.resetTrials()

                if (result != item.data.value) {
                    Log.i(
                        "ReadAsync", "onPostExecute ModBus reading new value $result ${
                        item.data.value
                        }"
                    )

                    item.data.value = result
                    item.data.lastVal = item.data.value
                    item.data.state = ParameterState.UNEDITED

                    parent.adapter.notifyItemChanged(pos)

                    parent.setButtonByParameters()
                } else {
                    Log.i(
                        "ReadAsync", "onPostExecute ModBus reading old value $result ${
                        item.data.value
                        }"
                    )

                    if (item.data.state != ParameterState.UNSAVED) {
                        item.data.state = ParameterState.UNEDITED
                    }

                    parent.adapter.notifyItemChanged(pos)

                    parent.setButtonByParameters()
                }


            } else {
                Log.e(
                    "ReadAsync",
                    "onPostExecute ModBus reading failed! ${parent.onlineManager.trials}"
                )

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    ReadAsync(parent, item, pos).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }
            }
        }

    }

    private class ReadListAsync(
        val parent: HomeFragment,
        val parameterList: List<ParameterListItem>,
        val firstRefresh: Boolean = true
    ) : AsyncTask<Void, Void, ArrayList<Number>>() {
        override fun onPreExecute() {
            super.onPreExecute()


            parent.progressBar.visibility = View.VISIBLE

        }

        override fun doInBackground(vararg p0: Void?): ArrayList<Number> {
            return try {
                parent.modBusUtils.readParameterList(parameterList)
            } catch (e: Exception) {
                e.printStackTrace()
                arrayListOf()
            }
        }

        override fun onPostExecute(result: ArrayList<Number>?) {
            super.onPostExecute(result)

            if (result != null && result.isNotEmpty()) {
                parameterList.forEachIndexed { i, listItem ->
                    if (listItem is ParameterItem) {
                        val readValue = result.removeAt(0)

                        if (readValue != listItem.data.value
                        ) {
                            Log.i(
                                "ReadAsync",
                                "onPostExecute ModBus reading new value $result ${
                                listItem.data.value
                                }"
                            )

                            if (listItem.data.state == ParameterState.UNEDITED || firstRefresh) {
                                listItem.data.value = readValue
                                listItem.data.lastVal = listItem.data.value
                                listItem.data.state = ParameterState.UNEDITED

                                parent.adapter.notifyItemChanged(i)

                                parent.setButtonByParameters()
                            }
                        }
                    }
                }

            } else {
                Log.e("ReadListAsync", "onPostExecute ModBus reading failed!")

                Log.e(
                    "ReadListAsync",
                    "onPostExecute ModBus reading failed! ${parent.onlineManager.trials}"
                )

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    ReadListAsync(parent, parameterList).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }
            }

            parent.progressBar.visibility = View.GONE
        }

    }

    private class CheckPasswordAsync(
        val parent: HomeFragment
    ) : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()

            parent.progressBar.visibility = View.VISIBLE

        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            val preferences = parent.activity?.getSharedPreferences(
                PASSWORD_SHARED_KEY,
                Context.MODE_PRIVATE
            )

            val password =
                preferences?.getInt(PASSWORD_SHARED_KEY, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD

            return try {
                parent.modBusUtils.requestWritePermission(password)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            if (result) {
                parent.driveManager.setUnlocked()

            } else {
                parent.driveManager.setLocked()
            }

            parent.progressBar.visibility = View.GONE

            parent.setLockIconByManager()
        }

    }

    private class ChangePasswordAsync(
        val parent: HomeFragment
    ) : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()

            parent.progressBar.visibility = View.VISIBLE

        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            val preferences = parent.activity?.getSharedPreferences(
                PASSWORD_SHARED_KEY,
                Context.MODE_PRIVATE
            )

            val password =
                preferences?.getInt(PASSWORD_SHARED_KEY, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD

            return try {
                parent.modBusUtils.programNewPassword(password)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            if (result) {
                parent.driveManager.setUnlocked()

            } else {
                parent.driveManager.setLocked()
            }

            parent.progressBar.visibility = View.GONE

            parent.setLockIconByManager()
        }

    }

    private class ProgramAsync(
        val parent: HomeFragment,
        val item: ParameterItem,
        val pos: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()

            Log.i(
                "ProgramAsync", "onPreExecute will change progress ${
                item.data.id
                } ${parent.progressBar.visibility}"
            )

            parent.progressBar.visibility = View.VISIBLE

            Log.i(
                "ProgramAsync", "onPreExecute will return ${
                item.data.id
                } ${parent.progressBar.visibility}"
            )
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            val id = item.data.id
            val ratio = item.data.ratio
            val value = item.data.value

            if (!parent.onlineManager.onlineModeOn) {
                return false
            }

            if (id != null && value != null && ratio != null) {
                return parent.modBusUtils.programParameter(id, value, ratio)
            }

            return false
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result) {
                Log.i(
                    "ProgramAsync", "onPostExecute ModBus test succeeded ${
                    item.data.id
                    } $result"
                )

                parent.onlineManager.resetTrials()

                item.data.state = ParameterState.UNSAVED

                parent.adapter.notifyItemChanged(pos, item)

                parent.setButtonByParameters()

            } else {
                Log.e(
                    "ProgramAsync",
                    "onPostExecute ModBus test failed! ${parent.onlineManager.trials}"
                )

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    ProgramAsync(parent, item, pos).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }
            }
        }

    }

    private class ModeAsync(
        val parent: HomeFragment,
        val advanced: UserPermission = UserPermission.ADVANCED
    ) : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()

            Log.i(
                "ModeAsync",
                "onPreExecute will change mode $ADVANCED_KEY_ID $advanced"
            )

            parent.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            val id = ADVANCED_KEY_ID
            val ratio = 1
            val value = if (advanced == UserPermission.ADVANCED) {
                DEFAULT_ADVANCED_KEY.toInt()
            } else 0

            return parent.modBusUtils.changeModeKey(value)
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result) {
                Log.i(
                    "ModeAsync",
                    "onPostExecute ModBus test succeeded $ADVANCED_KEY_ID $result"
                )

                parent.onlineManager.resetTrials()

            } else {
                Log.e(
                    "ModeAsync",
                    "onPostExecute ModBus test failed! ${parent.onlineManager.trials}"
                )

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    ModeAsync(parent, advanced).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }
            }
        }

    }

    private class SaveAsync(
        val parent: HomeFragment,
        val item: ParameterItem,
        val pos: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()

            Log.i(
                "SaveAsync", "onPreExecute ${
                item.data.id
                }---------------"
            )

            parent.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            val id = item.data.id

            if (!parent.onlineManager.onlineModeOn) {
                return false
            }

            if (id != null) {
                return parent.modBusUtils.saveParameter(id)
            }

            return false
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result) {
                Log.i(
                    "SaveAsync", "onPostExecute ModBus save succeeded ${
                    item.data.id
                    } $result"
                )

                parent.onlineManager.resetTrials()

                item.data.state = ParameterState.UNEDITED
                parent.adapter.notifyItemChanged(pos, item)

                parent.setButtonByParameters()

            } else {
                Log.e(
                    "SaveAsync",
                    "onPostExecute ModBus save failed! ${parent.onlineManager.trials}"
                )

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    SaveAsync(parent, item, pos).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }
            }
        }

    }

    private class GetAlarmAsync(
        val parent: HomeFragment
    ) : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg p0: Void?): String? {


            try {
                val alarm = parent.modBusUtils.getAlarmNumber()

                if (alarm != "") {
                    return alarm
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

                parent.onlineManager.resetTrials()

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

    private class GetDriveSizeAsync(
        val parent: HomeFragment
    ) : AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg p0: Void?): Int {
            return try {
                val size = parent.modBusUtils.getDriveSize()

                parent.parameterManager.getBy(BAUD_RATE_ID).value =
                    parent.modBusUtils.readParameter(
                        BAUD_RATE_ID, 1
                    )

                size
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result > -1) {
                Log.i(
                    "GetDriveSizeAsync", "onPostExecute ModBus read succeeded $result"
                )

                parent.onlineManager.resetTrials()

                parent.driveManager.size = result

                parent.monitoringManager.updateM004ratio(result)

                parent.modelNameText.text = parent.driveManager.getFormattedModel()

                parent.addDeviceToDatabase()

            } else {
                Log.e(
                    "GetDriveSizeAsync",
                    "onPostExecute ModBus read failed! ${parent.onlineManager.trials}"
                )

                if (parent.onlineManager.trials < CONNECTION_TRIALS) {
                    parent.onlineManager.trials++

                    GetDriveSizeAsync(parent).execute()
                } else {
                    CheckConnectionAsync(parent).executeOnExecutor(THREAD_POOL_EXECUTOR)
                }
            }
        }

    }

    private class ReadMainsAsync(
        val parent: HomeFragment
    ) : AsyncTask<Void, Void, MainsModel>() {
        override fun doInBackground(vararg p0: Void?): MainsModel {
            Log.i(
                "ReadMainsAsync", "doInBackground-----------------------"
            )

            return try {
                parent.modBusUtils.getMains()
            } catch (e: Exception) {
                e.printStackTrace()
                MainsModel()
            }
        }

        override fun onPostExecute(result: MainsModel) {
            super.onPostExecute(result)

            var failed = false


            if (result.voltage.toLong() < READ_ERROR) {


                if (result.voltage.toLong() > 0) {
                    Log.i(
                        "ReadMainsAsync", "onPostExecute voltage read successful"
                    )

                    parent.mainsModel.voltage = result.voltage
                } else {
                    Log.e(
                        "ReadMainsAsync", "onPostExecute voltage read failed!"
                    )

                    failed = true
                }
            } else {
                Log.e(
                    "ReadMainsAsync", "onPostExecute voltage read failed!"
                )

                failed = true
            }

            if (result.frequency.toLong() < READ_ERROR) {
                if (result.frequency.toLong() > 0) {
                    Log.i(
                        "ReadMainsAsync", "onPostExecute frequency read successful"
                    )

                    parent.mainsModel.frequency = result.frequency
                } else {
                    Log.e(
                        "ReadMainsAsync", "onPostExecute frequency read failed!"
                    )

                    failed = true
                }
            } else {
                Log.e(
                    "ReadMainsAsync", "onPostExecute frequency read failed!"
                )

                failed = true
            }

            if (!failed) {
                parent.cardFrequencyText.text = "${parent.mainsModel.frequency} Hz"

                parent.cardVoltageText.text = "${parent.mainsModel.voltage} V"

                parent.onlineManager.resetTrials()

            } else {
                Log.e(
                    "ReadMainsAsync",
                    "onPostExecute read failed! ${parent.onlineManager.trials}"
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
        }

    }

    private class DisconnectAsync(val parent: HomeFragment) : AsyncTask<Void, Void, Void>() {
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

    private fun tryToConnect() {

        val host = ConnectionUtils.getHostFromPreferences(activity!!.applicationContext)
        val port = ConnectionUtils.getPortFromPreferences(activity!!.applicationContext)
        AttemptConnectAsync(this, wifiManager, host, port, ParameterAction.READ_ALL).execute()

        setButtonByParameters()
    }

    private fun updateAll() {
        ReadListAsync(this, parameterList).execute()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help_action ->
                inflateHelpShowcase()
            R.id.edit_graph_action ->
                launchEditGraph()
            R.id.advanced_action ->
                if (permissionManager.permission == UserPermission.BASIC) {
                    inflateAdvancedShowcase()
                } else {
                    changeToBasic()
                }
            R.id.edit_name_action ->
                inflateEditNameDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun inflateEditNameDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_name, null)

        val titleString = getString(R.string.dialog_edit_name_title)
        val context = context!!

        val editText: EditText = dialogView.findViewById(R.id.name_edit)
        editText.setText(driveManager.name)

        val titleView = Util.titleForDialog(titleString, context)

        AlertDialog.Builder(this.context!!)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_ok) { _: DialogInterface, _: Int ->
                saveNameToDatabase(editText.text.toString())
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun inflateUnlockDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_login, null)

        val titleString = getString(R.string.dialog_unlock_title)
        val context = context!!

        val editText: EditText = dialogView.findViewById(R.id.password_edit)

        val titleView = Util.titleForDialog(titleString, context)

        val preferences = activity!!.getSharedPreferences(
            PASSWORD_SHARED_KEY,
            Context.MODE_PRIVATE
        )

        val password = preferences.getInt(PASSWORD_SHARED_KEY, DEFAULT_PASSWORD)

        AlertDialog.Builder(this.context!!)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_ok) { _: DialogInterface, _: Int ->
                tryToUnlock(
                    editText.text.toString().toIntOrNull() ?: password
                )
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun tryToUnlock(password: Int) {
        Log.i("HomeFragment", "tryToUnlock $password--------------")

        val preferences = activity!!.getSharedPreferences(
            PASSWORD_SHARED_KEY,
            Context.MODE_PRIVATE
        )

        with(preferences.edit()) {
            putInt(PASSWORD_SHARED_KEY, password)
            commit()
        }

        CheckPasswordAsync(this).execute()
    }

    private fun inflateSetPasswordDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_change_password, null)

        val titleString = getString(R.string.warning_title)
        val context = context!!

        val password = getSavedPassword()

        var shownPassword = password

        val message = resources.getQuantityString(
            R.plurals.dialog_password_message,
            0,
            password
        )

        var increment = 0

        val titleView = Util.titleForDialog(titleString, context)

        val passwordText: TextView = dialogView.findViewById(R.id.dialog_change_password_text)
        passwordText.text = shownPassword.toString()

        val plusButton: ImageButton = dialogView.findViewById(R.id.dialog_change_password_plus)

        val minusButton: ImageButton = dialogView.findViewById(R.id.dialog_change_password_minus)

        fun setButtonsByIncrement() {
            var plusVisibility = View.VISIBLE
            var minusVisibility = View.VISIBLE

            if (shownPassword >= MAX_PASSWORD || increment > 0) {
                plusVisibility = View.INVISIBLE
            }

            if (shownPassword <= MIN_PASSWORD || increment < 0) {
                minusVisibility = View.INVISIBLE
            }

            plusButton.visibility = plusVisibility
            minusButton.visibility = minusVisibility
        }

        setButtonsByIncrement()

        plusButton.setOnClickListener {
            if (increment < 1 && shownPassword < MAX_PASSWORD) {
                increment++
            }

            shownPassword = password + increment
            setButtonsByIncrement()
            passwordText.text = shownPassword.toString()
        }


        minusButton.setOnClickListener {
            if (increment > -1 && shownPassword > MIN_PASSWORD) {
                increment--
            }

            shownPassword = password + increment
            setButtonsByIncrement()
            passwordText.text = shownPassword.toString()
        }

        AlertDialog.Builder(this.context!!)
            .setView(dialogView)
            .setCustomTitle(titleView)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_ok) { _: DialogInterface, _: Int ->
                when (increment) {
                    -1 -> changePassword(false)
                    1 -> changePassword(true)
                    0 -> Log.i(
                        "HomeFragment",
                        "inflateSetPasswordDialog.positiveButton wont change password"
                    )
                    else -> Log.e(
                        "HomeFragment",
                        "inflateSetPasswordDialog.positiveButton invalid value $increment"
                    )
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun getSavedPassword(): Int {
        Log.i("HomeFragment", "getSavedPassword---------------")

        val preferences = activity!!.getSharedPreferences(
            PASSWORD_SHARED_KEY,
            Context.MODE_PRIVATE
        )

        return preferences.getInt(PASSWORD_SHARED_KEY, DEFAULT_PASSWORD)
    }

    private fun changePassword(increment: Boolean) {
        Log.i("HomeFragment", "changePassword $increment---------------")

        val password = getSavedPassword()

        val newPassword = if (increment) {
            password + 1
        } else {
            password - 1
        }

        Log.i("HomeFragment", "changePassword new password $newPassword")

        val preferences = activity!!.getSharedPreferences(
            PASSWORD_SHARED_KEY,
            Context.MODE_PRIVATE
        )

        with(preferences.edit()) {
            putInt(PASSWORD_SHARED_KEY, newPassword)
            commit()
        }

        ChangePasswordAsync(this).execute()

        // Hardcode for tests
//        driveManager.setLocked()
    }

    private fun saveNameToDatabase(name: String) {
        Log.i("HomeFragment", "saveNameToDatabase $name---------------")

        SaveNameToDatabaseAsync(this, name).execute()
    }

    private class SaveNameToDatabaseAsync(val parent: HomeFragment, val name: String) :
        AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg p0: Void?): Boolean {
            Log.i("SaveNameToDatabaseAsync", "doInBackground $name---------------")

            val listByNick = parent.database.databaseDao.getAllByNick(
                parent.driveManager.name
            )

            Log.i("SaveNameToDatabaseAsync", "doInBackground $listByNick")

            if (listByNick.isNotEmpty()) {
                val item = listByNick[0]

                item.deviceNickname = name
                return parent.database.databaseDao.update(item) > -1
            }

            return false
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)

            Log.i("SaveNameToDatabaseAsync", "doInBackground $result---------------")

            if (result == true) {
                parent.driveManager.name = name
                parent.cardDeviceNameText.text = parent.driveManager.name
            }
        }

    }

    private fun changeToBasic() {
        ModeAsync(this, UserPermission.BASIC).execute()
        notifyPermissionChanged()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val advancedAction = menu.findItem(R.id.advanced_action)

        val editNameAction = menu.findItem(R.id.edit_name_action)

        val advancedTitleId = R.string.advanced_action_title
        val basicTitleId = R.string.basic_action_title

        try {
            val permission = permissionManager.permission

            if (permission == UserPermission.BASIC) {
                advancedAction.title = getString(advancedTitleId)
            } else {
                advancedAction.title = getString(basicTitleId)
            }
        } catch (e: Exception) {
            e.printStackTrace()

            advancedAction.title = getString(advancedTitleId)
        }

        try {
            editNameAction.isVisible = onlineManager.onlineModeOn && validDevice
        } catch (e: Exception) {
            editNameAction.isVisible = false
        }
    }

    private fun launchEditGraph(id: String? = null) {
        val intent = Intent(this.activity, EditOnGraphActivity::class.java)
        if (id != null) {
            intent.putExtra(EDIT_GRAPH_ID_EXTRA, id)
        }

        startActivityForResult(intent, RETURN_GRAPH_REQUEST)
    }

    private fun hideKeyboard() {
        val focus = activity?.currentFocus?.windowToken
        val inputMethodManager: InputMethodManager =
            activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(focus, 0)
    }

    private fun inflateHelpShowcase() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_home_help, null)

        val titleString = getString(R.string.dialog_help_title)
        val context = context!!

        val titleView = Util.titleForDialog(titleString, context)

        AlertDialog.Builder(this.context!!)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_ok, null)
            .show()
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

    private fun inflateAdvancedShowcase() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_advanced, null)

        val titleString = getString(R.string.dialog_advanced_title)
        val context = context!!

        val editText: EditText = dialogView.findViewById(R.id.password_edit)

        val titleView = Util.titleForDialog(titleString, context)

        AlertDialog.Builder(this.context!!)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_ok) { _: DialogInterface, _: Int ->
                if (editText.text.toString() == DEFAULT_ADVANCED_KEY) {
                    notifyPermissionChanged()
                    if (onlineManager.onlineModeOn) {
                        ModeAsync(this).execute()
                    }
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun notifyPermissionChanged() {
        Log.i("HomeFragment", "notifyPermissionChanged--------------")

        if (permissionManager.permission == UserPermission.BASIC) {
            permissionManager.permission = UserPermission.ADVANCED
            Log.i("HomeFragment", "notifyPermissionChanged ${permission.value}")
            viewModel.notifyPermissionChanged(permissionManager.permission)
            adapter.notifyDataSetChanged()
            updateAll()
        } else {
            permissionManager.permission = UserPermission.BASIC
            Log.i("HomeFragment", "notifyPermissionChanged ${permission.value}")
            viewModel.notifyPermissionChanged(permissionManager.permission)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setButtonByParameters() {
        Log.i("HomeFragment", "setButtonByParameters--------------")

        var uneditedCount = 0

        run visibilityLoop@{
            parameterList.forEach {
                if (it is ParameterItem) {
                    val id = it.data.id
                    val state = it.data.state

                    Log.i("HomeFragment", "setButtonByParameters forEach $id $state")

                    when (state) {
                        ParameterState.UNEDITED -> uneditedCount++
                        ParameterState.EDITED -> {
                            viewModel.setGeneralState(ParameterState.EDITED)
                            return@visibilityLoop
                        }
                        ParameterState.UNSAVED ->
                            viewModel.setGeneralState(ParameterState.UNSAVED)
                    }

                } else {
                    uneditedCount++
                }
            }
        }

        if (parameterList.size == uneditedCount) {
            viewModel.setGeneralState(ParameterState.UNEDITED)
        }

        Log.i(
            "HomeFragment",
            "setButtonByParameters general $uneditedCount ${generalParameterState.value}"
        )

        setButtonByGeneral()

        Log.i("HomeFragment", "setButtonByParameters end")


    }

    private fun setButtonByGeneral() {
        when (generalParameterState.value) {
            ParameterState.UNEDITED -> button.visibility = View.GONE
            ParameterState.EDITED -> {
                button.visibility = View.VISIBLE
                button.setText(R.string.program_button)
            }
            ParameterState.UNSAVED -> {
                button.visibility = View.VISIBLE
                button.setText(R.string.save_button)
            }
            else -> {}
        }
    }

    private fun onProgramClick() {
        Log.i("HomeFragment", "onProgramClick---------------------")

        run programLoop@{
            parameterList.forEachIndexed { pos, it ->
                if (it is ParameterItem) {

                    val id = it.data.id
                    val value = it.data.value

                    if (it.data.state == ParameterState.EDITED) {
                        Log.i("HomeFragment", "onProgramClick edited $id $value")

                        progressBar.visibility = View.VISIBLE
                        ProgramAsync(this, it, pos).execute()

                    }
                }
            }
        }
    }


    private fun onSaveClick() {

        Log.i("HomeFragment", "onSaveClick list ${String.format(parameterList.toString())}")

        val connectionManager = ConnectionManager.getInstance(this.context!!)

        run saveLoop@{
            parameterList.forEachIndexed { pos, it ->
                if (it is ParameterItem) {

                    val id = it.data.id

                    Log.i("HomeFragment", "onSaveClick item $id ${it.data.state}")

                    if (it.data.state == ParameterState.UNSAVED) {

                        Log.i("HomeFragment", "onSaveClick unsaved $id")

                        SaveAsync(this, it, pos).execute()

                    }
                }
            }
        }
    }

    override fun slideListener(pos: Int, value: Number) {
        Log.i("HomeFragment", "slideListener pos $pos value $value")

        val item = viewModel.parameterList[pos]
        if (item is ParameterItem) {
            val parameter = item.data
            parameter.value = value

            Log.i(
                "HomeFragment",
                "slideListener value ${parameter.value} equip ${parameter.lastVal}"
            )

            if (parameter.value?.toFloat() != parameter.lastVal?.toFloat()) {
                Log.i(
                    "HomeFragment",
                    "slideListener value is different from equip"
                )
                parameter.state = ParameterState.EDITED
                parameter.lastVal = parameter.value
            } else {
                Log.i(
                    "HomeFragment",
                    "slideListener value is equal to equip"
                )
            }

            setButtonByParameters()

            adapter.notifyItemChanged(pos, item)

        } else {
            Log.e("HomeFragment", "slideListener item is header")
        }
    }

    private fun callSystemWifi(postAction: ParameterAction) {

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

        val fragment = this
        val host = ConnectionUtils.getHostFromPreferences(activity!!.applicationContext)
        val port = ConnectionUtils.getPortFromPreferences(activity!!.applicationContext)
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
                            reloadHome()

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

    private fun inflateConfigDialog(postAction: ParameterAction) {

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

        val portEdit = dialogView.findViewById<EditText>(R.id.port_edit)
        portEdit.setText(portResult.toString())

        Log.i(
            "HomeFragment",
            "inflateConfigDialog will inflate dialog"
        )

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
                    portString.toInt(),
                    postAction
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

        Log.i(
            "HomeFragment",
            "inflateConfigDialog after inflate dialog"
        )


        val connectButton = dialogView.findViewById<Button>(R.id.connectButton)
        connectButton.setOnClickListener {
            dialog.dismiss()
            callSystemWifi(postAction)
        }

        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    }

    private fun enterOnlineMode() {
        tryToConnect()
        onlineManager.goOnline()
        alarmPollTimer.start()
        setOfflineIconByManager()
        inflateConfigDialog(ParameterAction.TRY)
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

    private fun reloadHome() {
        Log.i(
            "HomeFragment",
            "reloadHome-----------------------------------"
        )

        activity?.finish()
        startActivity(activity?.intent)

    }

    private fun notifyChangedOnGraph() {
        viewModel.notifyGraphParametersChanged()
        adapter.notifyDataSetChanged()
        setButtonByParameters()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(
            "HomeFragment",
            "onActivityResult $resultCode-----------------------------------"
        )

        if (requestCode == RETURN_GRAPH_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                notifyChangedOnGraph()
            }
        }
    }

    override fun onGraphButtonClick(pos: Int) {
        val item = parameterList[pos]

        if (item is ParameterItem) {
            val id = item.data.id!!

            if (DataUtils.isGraphParameter(id)) {
                launchEditGraph(id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Lingver.getInstance().setLocale(context!!, Locale.getDefault())
    }
}