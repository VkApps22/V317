package br.com.kascosys.vulkanconnectv317.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.databinding.ActivityEditOnGraphBinding
import br.com.kascosys.vulkanconnectv317.enums.DataType
import br.com.kascosys.vulkanconnectv317.enums.LockState
import br.com.kascosys.vulkanconnectv317.enums.ParameterState
import br.com.kascosys.vulkanconnectv317.managers.DriveManager
import br.com.kascosys.vulkanconnectv317.managers.OnlineManager
import br.com.kascosys.vulkanconnectv317.managers.ParameterManager
import br.com.kascosys.vulkanconnectv317.models.NewParameterModel
import br.com.kascosys.vulkanconnectv317.utils.ConnectionUtils
import br.com.kascosys.vulkanconnectv317.utils.DataUtils
import br.com.kascosys.vulkanconnectv317.utils.DecimalUtils
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusUtils
import br.com.kascosys.vulkanconnectv317.viewModels.EditOnGraphViewModel
import com.google.android.material.textfield.TextInputLayout
import com.yariksoffice.lingver.Lingver
import java.text.DecimalFormat
import java.util.*

class EditOnGraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditOnGraphBinding

    private lateinit var viewModel: EditOnGraphViewModel

    private lateinit var wifiManager: WifiManager

    private lateinit var modBusUtils: ModBusUtils

    private lateinit var progressBar: ProgressBar

    private lateinit var parameterManager: ParameterManager

    private lateinit var driveManager: DriveManager

    private lateinit var onlineManager: OnlineManager

    private lateinit var generalState: ParameterState

    private val returnIntent = Intent()

    private lateinit var editC000: EditText

    private lateinit var editP128: EditText

    private lateinit var editP171: EditText

    private lateinit var c000layout: TextInputLayout

    private lateinit var p128layout: TextInputLayout

    private lateinit var p171layout: TextInputLayout

    private lateinit var graphImage: ImageView

    private lateinit var applyButton: Button

    private lateinit var mainLayout: ConstraintLayout

    private var focusId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_on_graph)

        parameterManager = ParameterManager.getInstance(this)
        driveManager = DriveManager.getInstance(this)
        onlineManager = OnlineManager.getInstance(this)

        viewModel = ViewModelProviders.of(this).get(EditOnGraphViewModel::class.java)
        generalState = viewModel.generalState

        val manager = ParameterManager.getInstance(this)

        Log.i("EditOnGraphActivity", "onCreateView parameterManager $manager")

        wifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        progressBar = binding.progressBar

        c000layout = binding.editGraphC000InputLayout
        p128layout = binding.editGraphP128InputLayout
        p171layout = binding.editGraphP171InputLayout

        editC000 = binding.editGraphC000EditText
        editP128 = binding.editGraphP128EditText
        editP171 = binding.editGraphP171EditText

        graphImage = binding.editGraphImage
        graphImage.setOnClickListener {
            setFocusToId(null)
        }

        mainLayout = binding.editGraphMainLayout
        mainLayout.setOnClickListener {
            setFocusToId(null)
        }

        supportActionBar?.hide()

        val ids = listOf(C000, P128, P171)
        ids.forEach { id ->
            val unit = viewModel.getUnit(id)
            val dataType = DataUtils.getExhibitType(unit) ?: DataType.NUMBER_DATA
            val initialValue = getValueByDataType(
                parameterManager.getBy(id).value ?: READ_ERROR,
                dataType,
                driveManager.size
            )
            val unitString =
                " ${DataUtils.getFormattedUnit(
                    unit,
                    initialValue.toInt(),
                    this
                )}"
            val editText = getEditText(id)

            if (initialValue.toFloat() < READ_ERROR.toFloat()) {
                editText?.setText(
                    "%.2f".format(initialValue.toFloat()) + unitString
                )
            } else {
                editText?.setText(
                    R.string.value_error
                )
            }

            editText?.setOnEditorActionListener { textView, actionId, _ ->
                Log.i(
                    "EditOnGraphActivity",
                    "editText.setOnEditorActionListener $id editor action $actionId"
                )

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    val inputManager: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                    inputManager.hideSoftInputFromWindow(textView.windowToken, 0)

                    onConfirmValue(id, editText)

                    setFocusToId(null)

                    true
                } else {
                    false
                }
            }
            editText?.setOnFocusChangeListener { _, hasFocus ->
                onEditFocusChange(hasFocus, id, editText, unitString)
            }
            setFieldColor(id)
        }

        setFocusToId(intent.getStringExtra(EDIT_GRAPH_ID_EXTRA))


        applyButton = binding.editGraphApplyButton
        applyButton.setOnClickListener {
            if (onlineManager.onlineModeOn && driveManager.lockState == LockState.UNLOCKED) {
                when (generalState) {
                    ParameterState.EDITED -> {
                        onProgramClick()
                    }
                    ParameterState.UNSAVED -> {
                        onSaveClick()
                    }
                    else -> Log.e(
                        "EditOnGraphActivity",
                        "editGraphApplyButton button should be gone!"
                    )
                }
            }
        }

        setResult(Activity.RESULT_CANCELED, returnIntent)

        setButtonByParameters()

        tryToConnect()
    }

    private fun onEditFocusChange(
        hasFocus: Boolean,
        id: String,
        editText: EditText,
        unit: String
    ) {
        // Disable editing if screen is locked
        if (onlineManager.onlineModeOn
            && driveManager.lockState != LockState.UNLOCKED
        ) {
            editText.isCursorVisible = false
            editText.clearFocus()
            return
        }

        if (hasFocus) {
            Log.i(
                "EditOnGraphActivity",
                "onEditFocusChange $id has focus $hasFocus ${editText.text}"
            )
            focusId = id
            editText.setText(editText.text.split(unit)[0])
        } else {
            editText.setText(editText.text.toString() + unit)
            focusId = null
        }
    }

    private fun setFocusToId(newId: String?) {
        Log.i(
            "EditOnGraphActivity",
            "setFocusToId $newId $focusId-------------------------"
        )

        if (newId != null) {
            getEditText(newId)?.requestFocus()
        } else if (focusId != null) {

            val unit = " ${viewModel.getUnit(focusId!!)}"

            val editText = getEditText(focusId!!)
            editText?.clearFocus()

            val inputManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(graphImage.windowToken, 0)

        }
    }

    override fun onPause() {
        super.onPause()

        Log.i("EditOnGraphActivity", "onPause-------------------")

        DisconnectAsync(this).execute()

        Lingver.getInstance().setLocale(this, Locale.getDefault())
    }

    private fun onConfirmValue(id: String, editText: EditText) {

        Log.i(
            "EditOnGraphActivity",
            "onConfirmValue $id ${editText.text}-------------------"
        )

        val item = parameterManager.getBy(id)
        val unitString = item.unit!!

        val driveSize = driveManager.size

        val maxValue = item.maxWrite!!
        val minValue = item.minWrite!!

        val dataType = DataUtils.getExhibitType(unitString) ?: DataType.NUMBER_DATA

        val valueString =
            editText.text.toString()

        val lastVal = item.lastVal ?: READ_ERROR

        Log.i(
            "EditOnGraphActivity",
            "onConfirmValue last value $lastVal"
        )


        var newValue = getPercentageByDataType(
            DecimalUtils.getNumberFromInputString(
                valueString, unitString
            ) ?: getValueByDataType(
                lastVal, dataType, driveSize
            ),
            dataType,
            driveSize
        )


        if (newValue.toFloat() > maxValue.toFloat()) {
            newValue = maxValue
        } else if (newValue.toFloat() < minValue.toFloat()) {
            newValue = minValue
        }

        if (item.value?.toFloat() == newValue.toFloat()) {
            Log.i(
                "EditOnGraphActivity",
                "onConfirmValue value unchanged ${item.value}"
            )
        } else {
            item.state = ParameterState.EDITED
            setFieldColor(id)
            setButtonByParameters()
        }

        item.value = newValue
        item.lastVal = item.value

        setResult(Activity.RESULT_OK, returnIntent)


        if (newValue.toFloat() < READ_ERROR.toFloat()) {
            editText.setText(
                DecimalFormat("0.00").format(
                    getValueByDataType(
                        newValue,
                        dataType,
                        driveSize
                    )
                )
            )
        } else {
            editText.setText(R.string.value_error)
        }

    }

    private fun getValueByDataType(percentage: Number, dataType: DataType, driveSize: Int): Number {
        if (percentage.toLong() >= READ_ERROR) {
            return percentage
        }

        return when (dataType) {
            DataType.CURRENT_DATA -> DataUtils.getValueFromPercentage(percentage, driveSize)
            else -> percentage
        }
    }

    private fun getPercentageByDataType(value: Number, dataType: DataType, driveSize: Int): Number {
        if (value.toLong() >= READ_ERROR) {
            return value
        }

        return when (dataType) {
            DataType.CURRENT_DATA -> DataUtils.getPercentageFromValue(value, driveSize)
            else -> value
        }
    }

    private fun tryToConnect() {
        Log.i("EditOnGraphActivity", "tryToConnect-------------------")

        val host = ConnectionUtils.getHostFromPreferences(applicationContext)
        val port = ConnectionUtils.getPortFromPreferences(applicationContext)
        AttemptConnectAsync(this, wifiManager, host, port).execute()
    }

    private class AttemptConnectAsync(
        val parent: EditOnGraphActivity,
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
            }
        }

    }

    private class ProgramAsync(
        val parent: EditOnGraphActivity,
        val id: String
    ) : AsyncTask<Void, Void, Boolean>() {
        private lateinit var item: NewParameterModel

        override fun onPreExecute() {
            super.onPreExecute()

            Log.i(
                "ProgramAsync",
                "onPreExecute will change progress $id ${parent.progressBar.visibility}"
            )

            parent.progressBar.visibility = View.VISIBLE

        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            item = parent.parameterManager.getBy(id)
            val ratio = item.ratio
            val value = item.value

            if (value != null && ratio != null) {
                return parent.modBusUtils.programParameter(id, value, ratio)
            }

            return false
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result) {
                Log.i(
                    "ProgramAsync",
                    "onPostExecute ModBus test succeeded $id $result"
                )

                item.state = ParameterState.UNSAVED
                parent.setFieldColor(id)
                parent.setButtonByParameters()

            } else {
                Log.e("ProgramAsync", "onPostExecute ModBus test failed!")

                Toast.makeText(parent, R.string.program_failed, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private class SaveAsync(
        val parent: EditOnGraphActivity,
        val id: String
    ) : AsyncTask<Void, Void, Boolean>() {

        override fun onPreExecute() {
            super.onPreExecute()

            parent.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            return parent.modBusUtils.saveParameter(id)
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            parent.progressBar.visibility = View.GONE

            if (result) {
                Log.i(
                    "SaveAsync", "onPostExecute ModBus save succeeded $id $result"
                )

                val item = parent.parameterManager.getBy(id)

                item.state = ParameterState.UNEDITED
                parent.setFieldColor(id)

                parent.setButtonByParameters()

            } else {
                Log.e("SaveAsync", "onPostExecute ModBus save failed!")

                Toast.makeText(parent, R.string.save_failed, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private class DisconnectAsync(val parent: EditOnGraphActivity) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            Log.i(
                "DisconnectAsync", "doInBackground-------------"
            )

            parent.modBusUtils.disconnect()

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            Log.i(
                "DisconnectAsync", "onPostExecute-------------"
            )
        }

    }

    private fun setFieldColor(id: String) {
        val item = parameterManager.getBy(id)
        val inputLayout = getInputLayout(id)

        val uneditedColorId = R.color.colorWhite
        val editedColorId = R.color.colorAccentMid
        val unsavedColorId = R.color.colorAccentLight

        if (inputLayout != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                inputLayout.boxBackgroundColor = resources.getColor(
                    when (item.state) {
                        ParameterState.UNEDITED -> uneditedColorId
                        ParameterState.EDITED -> editedColorId
                        ParameterState.UNSAVED -> unsavedColorId
                    }, resources.newTheme()
                )
            } else {
                @Suppress("DEPRECATION")
                inputLayout.boxBackgroundColor =
                    resources.getColor(
                        when (item.state) {
                            ParameterState.UNEDITED -> uneditedColorId
                            ParameterState.EDITED -> editedColorId
                            ParameterState.UNSAVED -> unsavedColorId
                        }
                    )
            }
        } else {
            Log.e("EditOnGraphActivity", "setFieldColor $id not found!")
        }

    }

    private fun getEditText(id: String): EditText? {
        return when (id) {
            C000 -> editC000
            P128 -> editP128
            P171 -> editP171
            else -> return null
        }
    }

    private fun getInputLayout(id: String): TextInputLayout? {
        return when (id) {
            C000 -> c000layout
            P128 -> p128layout
            P171 -> p171layout
            else -> return null
        }
    }

    private fun setButtonByParameters() {
        val c000item = parameterManager.getBy(C000)
        val p128item = parameterManager.getBy(P128)
        val p171item = parameterManager.getBy(P171)
        val states = listOf(c000item.state, p128item.state, p171item.state)

        var uneditedCount = 0
        run statesLoop@{
            states.forEach {
                when (it) {
                    ParameterState.EDITED -> {
                        generalState = ParameterState.EDITED
                        return@statesLoop
                    }
                    ParameterState.UNEDITED -> uneditedCount++
                    else -> generalState = ParameterState.UNSAVED
                }
            }
        }

        if (uneditedCount == states.size) {
            generalState = ParameterState.UNEDITED
        }

        setButtonByState()
    }

    private fun onProgramClick() {
        val c000item = parameterManager.getBy(C000)
        val p128item = parameterManager.getBy(P128)
        val p171item = parameterManager.getBy(P171)
        val items = listOf(c000item, p128item, p171item)

        items.forEach { item ->
            if (item.state == ParameterState.EDITED) {
                ProgramAsync(this, item.id!!).execute()
            }
        }

        setResult(Activity.RESULT_OK)

        setButtonByParameters()
    }

    private fun onSaveClick() {
        val c000item = parameterManager.getBy(C000)
        val p128item = parameterManager.getBy(P128)
        val p171item = parameterManager.getBy(P171)
        val items = listOf(c000item, p128item, p171item)

        items.forEach { item ->
            if (item.state == ParameterState.UNSAVED) {
                SaveAsync(this, item.id!!).execute()
            }
        }

        setResult(Activity.RESULT_OK)

        setButtonByParameters()
    }

    private fun setButtonByState() {
        when (generalState) {
            ParameterState.UNEDITED -> applyButton.visibility = View.GONE
            ParameterState.EDITED -> {
                applyButton.visibility = View.VISIBLE
                applyButton.setText(R.string.program_button)
            }
            ParameterState.UNSAVED -> {
                applyButton.visibility = View.VISIBLE
                applyButton.setText(R.string.save_button)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Lingver.getInstance().setLocale(this, Locale.getDefault())
    }

}
