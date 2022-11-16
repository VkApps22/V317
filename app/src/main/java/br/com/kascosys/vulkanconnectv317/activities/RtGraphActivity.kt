package br.com.kascosys.vulkanconnectv317.activities

import android.content.Context
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.comparators.MyEntryXComparator
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.databinding.ActivityRtGraphBinding
import br.com.kascosys.vulkanconnectv317.managers.DriveManager
import br.com.kascosys.vulkanconnectv317.managers.MonitoringManager
import br.com.kascosys.vulkanconnectv317.managers.OnlineManager
import br.com.kascosys.vulkanconnectv317.models.MainsModel
import br.com.kascosys.vulkanconnectv317.models.NewMonitoringModel
import br.com.kascosys.vulkanconnectv317.utils.ConnectionUtils
import br.com.kascosys.vulkanconnectv317.utils.modbus.ModBusUtils
import br.com.kascosys.vulkanconnectv317.viewModels.RtGraphViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.yariksoffice.lingver.Lingver
import java.io.File
import java.text.DecimalFormat
import java.util.*

class RtGraphActivity : AppCompatActivity(), OnChartGestureListener {

    private fun setXAxisTextSizeByLength() {
        when {
            xAxis.longestLabel.length < 7 -> {
                Log.i(
                    "RtGraphActivity",
                    "setXAxisTextSizeByLength length < 7------------------"
                )
                xAxis.textSize = 5f
            }
            xAxis.longestLabel.length < 5 -> {
                Log.i(
                    "RtGraphActivity",
                    "setXAxisTextSizeByLength length < 5------------------"
                )
                xAxis.textSize = 8f
            }
            xAxis.longestLabel.length < 3 -> {
                Log.i(
                    "RtGraphActivity",
                    "setXAxisTextSizeByLength length < 3------------------"
                )
                xAxis.textSize = 12f
            }
        }
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        Log.i(
            "RtGraphActivity",
            "onChartGestureEnd $lastPerformedGesture------------------"
        )
    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {
        Log.i(
            "RtGraphActivity",
            "onChartFling $velocityX $velocityY ${lineChart.highestVisibleX} ${data.xMax}------------------"
        )

//        boundReached = if (velocityX < 0) {
//            lineChart.highestVisibleX >= data.xMax * 0.95
//        } else {
//            false
//        }
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        Log.i("RtGraphActivity", "onChartSingleTapped------------------")
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        Log.i(
            "RtGraphActivity",
            "onChartGestureStart $lastPerformedGesture------------------"
        )


    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        Log.i("RtGraphActivity", "onChartScale $scaleX $scaleY------------------")
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        Log.i("RtGraphActivity", "onChartLongPressed------------------")

//        saveScreenshot()
    }

    private fun saveScreenshot() {
        val prefixSD = "/storage/emulated/0"

        val galleryPath = getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath
        val screenShotPath = "Vulkan"
        val folderPath = "$galleryPath/$screenShotPath"
        val folderFile = File(folderPath)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DATE)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val milli = calendar.get(Calendar.MILLISECOND)

        val fileName =
            "$year${"%02d".format(month)}${"%02d".format(day)}${"%02d".format(hour)}${"%02d".format(
                minute
            )}${"%02d".format(second)}${"%03d".format(milli)}_$id"

        Log.i("RtGraphActivity", "onChartLongPressed $folderPath $fileName")

        if (folderFile.exists() && folderFile.isDirectory) {
            Log.i("RtGraphActivity", "onChartLongPressed directory found. Saving on folder...")
        } else {
            Log.i(
                "RtGraphActivity",
                "onChartLongPressed directory not found. Trying to create folder..."
            )

            if (getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.exists()) {
                Log.i(
                    "RtGraphActivity",
                    "onChartLongPressed parent directory found. Creating folder..."
                )

                if (!folderFile.mkdir()) {
                    Log.e("RtGraphActivity", "onChartLongPressed making of directory failed!")

                    return
                }
            } else {
                Log.e("RtGraphActivity", "onChartLongPressed parent directory not found!")

                return
            }
        }

        lineChart.saveToPath(fileName, folderPath.removePrefix(prefixSD))

        Toast.makeText(this, "Screenshot saved to $folderPath", Toast.LENGTH_LONG).show()
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        Log.i("RtGraphActivity", "onChartDoubleTapped------------------")
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        Log.i("RtGraphActivity", "onChartTranslate $dX $dY------------------")
    }


    private lateinit var binding: ActivityRtGraphBinding

    private lateinit var lineChart: LineChart

    private lateinit var data: LineData

    private lateinit var viewModel: RtGraphViewModel

    private lateinit var monitoringManager: MonitoringManager

    private lateinit var onlineManager: OnlineManager

    private lateinit var driveManager: DriveManager

    private lateinit var wifiManager: WifiManager

    private lateinit var modBusUtils: ModBusUtils

    private var thread: Thread? = null

    private var readDone = true

    private var lastTime: Float = 0.0f

    private var id: String? = null

    private lateinit var progressBar: ProgressBar

    private var initialTime: Long = 0

    private lateinit var item: NewMonitoringModel

    private val mainsModel = MainsModel()

    private lateinit var mainsPollTimer: CountDownTimer

    private val entryQueue = mutableListOf<Entry>()

    private val handler = Handler()

    private lateinit var playPauseView: View

    private var lastXValue = -1.0f

    private var lastXIndex = -1

    private lateinit var xAxis: XAxis

    private var lastReadSuccessful = true

    private var mainsCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_rt_graph
        )

        progressBar = binding.progressBar2

        playPauseView = binding.rtGraphPauseView
        playPauseView.setOnClickListener {
            Log.i(
                "RtGraphActivity",
                "playPauseView.onClickListener ${onlineManager.onlineModeOn}-----------------"
            )

//            if (onlineManager.onlineModeOn) {
//                onlineManager.goOffline()
//                Toast.makeText(
//                    this,
//                    getString(R.string.rt_graph_pause_message),
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//            } else {
//                onlineManager.goOnline()
//                Toast.makeText(
//                    this,
//                    getString(R.string.rt_graph_play_message),
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//            }
            if (viewModel.graphPlayState) {
                viewModel.graphPlayState = false
                Toast.makeText(
                    this,
                    getString(R.string.rt_graph_pause_message),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                viewModel.graphPlayState = true
                readDone = true
                Toast.makeText(
                    this,
                    getString(R.string.rt_graph_play_message),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        // Hide action bar
        supportActionBar?.hide()

        id = intent.getStringExtra(RT_GRAPH_ID_EXTRA)

        Log.i("RtGraphActivity", "onCreate $id---------------------")

        driveManager = DriveManager.getInstance(this)
        monitoringManager = MonitoringManager.getInstance(this)
        onlineManager = OnlineManager.getInstance(this)

        wifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        viewModel = ViewModelProviders.of(this).get(RtGraphViewModel::class.java)

        item = monitoringManager.getBy(id!!)

//        Log.i("RtGraphActivity", "test ${0.0001002f} ${Utils.getDecimals(0.0001002f)}")

    }

    override fun onResume() {
        super.onResume()

        Log.i("RtGraphActivity", "onResume ${Process.myTid()}------------------")

        initializeChart()

        initializeData()

        tryToConnect()

        if (id != null) {
            feedMultiple(id!!)

            val context = this

            mainsPollTimer =
                object : CountDownTimer(Long.MAX_VALUE, ALARM_POLLING_MILLIS) {
                    override fun onFinish() {
                        start()
                    }

                    override fun onTick(p0: Long) {

                        if (onlineManager.onlineModeOn && viewModel.graphPlayState) {
                            ReadMainsAsync(context).execute()
                        }

                    }
                }.start()
        } else {
            Log.e("RtGraphActivity", "onResume id null!")
        }
    }

    override fun onPause() {
        super.onPause()

        Log.i("RtGraphActivity", "onPause------------------")

        thread?.interrupt()

        Log.i("RtGraphActivity", "onPause ${thread?.isInterrupted}")

        try {
            mainsPollTimer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        DisconnectAsync(this).execute()

        Lingver.getInstance().setLocale(this, Locale.getDefault())
    }

    override fun onStop() {
        super.onStop()

        thread?.interrupt()

        Log.i("RtGraphActivity", "onStop------------------")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i("RtGraphActivity", "onDestroy------------------")

        thread?.interrupt()
//        DisconnectAsync(this).execute()

        Lingver.getInstance().setLocale(this, Locale.getDefault())

    }

    private fun tryToConnect() {
        Log.i("RtGraphActivity", "tryToConnect-------------------")

        val host = ConnectionUtils.getHostFromPreferences(this)
        val port = ConnectionUtils.getPortFromPreferences(this)
        AttemptConnectAsync(this, wifiManager, host, port).execute()
    }

    private fun initializeChart() {

        Log.i(
            "RtGraphActivity",
            "initializeChart-----------------------"
        )

        lineChart = binding.monitoringLineChart

        val variable = item

        val gridNum = 20

        lineChart.description.isEnabled = true
        lineChart.description.text = "--- V --- Hz"

        lineChart.setBackgroundColor(Color.WHITE)


        val yAxis = lineChart.axisLeft

        val maxRange =
//            if (variable.id != M003) {
            variable.maxRange!!.toFloat()
//            } else {
//                variable.maxRange!!.toFloat() * driveManager.size
//            }

        val minRange =
//            if (variable.id != M003) {
            variable.minRange!!.toFloat()
//            } else {
//                variable.minRange!!.toFloat() * driveManager.size
//            }

        yAxis.axisMaximum = maxRange
        yAxis.axisMinimum = minRange
        yAxis.setDrawTopYLabelEntry(true)
        yAxis.setDrawGridLines(true)

        yAxis.labelCount = gridNum
        yAxis.granularity = 0.001f
        yAxis.valueFormatter = IAxisValueFormatter { value, axis ->
            //            val formatted =
//                DefaultAxisValueFormatter(Utils.getDecimals(value)).getFormattedValue(value, axis)

            val formatter = DecimalFormat("#.###")
            val formatted = formatter.format(value)

//            val unit = if (variable.id != M003) variable.unit else "A"

            val unit = variable.unit

            "$formatted $unit"
        }


        xAxis = lineChart.xAxis
//        val minBetweenGrid = 0.1f
//        val multiplier = 100    // 10 ^ (number of decimals of minBetweenGrid)

        xAxis.labelCount = gridNum
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 0.005f
        xAxis.valueFormatter = IAxisValueFormatter { value, axis ->

            val formatter = DecimalFormat("#.###")
            val formatted = formatter.format(value)

            "$formatted s"

//            return@IAxisValueFormatter gridNum.xAxisFormatter(value)

        }

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 8f

        lineChart.axisRight.isEnabled = false

        val legend = lineChart.legend
        legend.form = Legend.LegendForm.LINE
        legend.textColor = Color.BLACK

        lineChart.onChartGestureListener = this

    }

//    private fun Int.xAxisFormatter(value: Float): String {
//
//        val highestX = lineChart.highestVisibleX
//        val lowestX = lineChart.lowestVisibleX
//        val betweenGrids = (highestX - lowestX) / this
//
//        var finalString = ""
//
//        val formatter = DecimalFormat("#.####")
//        val formatted = formatter.format(value)
//
//        val indexFloat = (value - lowestX) / betweenGrids
//
//        if (value < lastXValue || (
//                    value > lastXValue + betweenGrids * 1.3)
//        ) {
//            lastXValue = value
//
//            finalString = "$formatted s"
//        }
//
//        Log.i(
//            "RtGraphActivity",
//            "xAxis.valueFormatter $value $betweenGrids $indexFloat $lastXIndex "
//        )
//
//        return finalString
//    }

    private fun initializeData() {

        Log.i(
            "RtGraphActivity",
            "initializeData-----------------------"
        )

        data = viewModel.data
        lineChart.data = data
    }

    private fun feedMultiple(id: String) {

        Log.i(
            "RtGraphActivity",
            "feedMultiple $id-----------------------"
        )

        if (thread != null) {
            thread?.interrupt()
        }

        initialTime = viewModel.initialTime

        thread = Thread(Runnable {
            while (true) {
                if (thread!!.isInterrupted) {
                    break
                }
                if (readDone) {
                    Log.i(
                        "RtGraphActivity",
                        "feedMultiple thread will post handler ${handler.looper.thread}"
                    )

                    readDone = false

                    if (onlineManager.onlineModeOn && viewModel.graphPlayState) {
                        ReadSingleAsync(this, id).execute()
                    }
                } else {
                    Log.e(
                        "RtGraphActivity",
                        "feedMultiple thread reading timeout $lastTime"
                    )

//                    addTimeoutEntry()
                }

                val posted = handler.post {
                    Log.i(
                        "RtGraphActivity",
                        "feedMultiple thread will add entry"
                    )
                    if (onlineManager.onlineModeOn && viewModel.graphPlayState) {
                        addEntry()
                    }
                }

                Log.i(
                    "RtGraphActivity",
                    "feedMultiple thread handler posted $posted main ${handler.looper.thread.isInterrupted}"
                )


                try {
                    Thread.sleep(RT_GRAPH_ENTRY_MILLIS)
                } catch (e: InterruptedException) {
                    e.printStackTrace()

                    break
                }

//                Log.i(
//                    "RtGraphActivity",
//                    "feedMultiple thread will read next. Last $lastReading $lastTime"
//                )
//                lastReading = (Random.nextInt(item.minRange!!.toInt(), item.maxRange!!.toInt()))
//
//                lastTime = (SystemClock.elapsedRealtime() - initialTime) / ONE_SECOND_IN_MILLIS

//                if(powerManager.isInteractive) {
//                handler.post {
//                    addEntry()
//                }
//                }
            }
        })
        thread?.start()
    }

//    private fun addTimeoutEntry() {
//        entryQueue.add(Entry(getInstant(), READ_ERROR.toFloat()))
//    }

    private fun addEntry() {

        val mainSetIndex = 0

        Log.i(
            "RtGraphActivity",
            "addEntry ----------------------------------"
        )

        if (entryQueue.isEmpty()) {
            return
        }

        val data = lineChart.data

        if (data != null) {

            var mainSet: ILineDataSet? = data.getDataSetByIndex(mainSetIndex)

            if (mainSet == null) {
                mainSet = createSet()
                data.addDataSet(mainSet)
            }
//            data.addEntry(Entry(set.entryCount.toFloat(), lastReading.toFloat()), 0)
//            if (lastReading.toLong() < READ_ERROR) {
//                data.addEntry(Entry(lastTime, lastReading.toFloat()), 0)
//            } else {
//                data.addEntry(Entry(lastTime, 0.0f), 0)
//            }

            val entries = entryQueue.sortedWith(MyEntryXComparator()).toMutableList()
//            entryQueue.sortBy { it.x }


            while (entries.isNotEmpty()) {
                val entry = entries.removeAt(0)


                val lastX =
                    if (mainSet.entryCount > 0) {
                        mainSet.getEntryForIndex(mainSet.entryCount - 1).x
                    } else -1.0f

                Log.i(
                    "RtGraphActivity",
                    "addEntry $entry last time $lastX"
                )

                when {
                    lastX > entry.x -> {
                        Log.e(
                            "RtGraphActivity",
                            "addEntry reading arrived late!"
                        )
                    }

                    entry.y < READ_ERROR -> {
                        data.addEntry(entry, mainSetIndex)

                        lastReadSuccessful = true
                    }

                    mainSet.entryCount > 0 -> {
                        val lastY = mainSet.getEntryForIndex(mainSet.entryCount - 1).y
                        data.addEntry(Entry(entry.x, lastY), mainSetIndex)

                        Log.e(
                            "RtGraphActivity",
                            "addEntry reading failed!"
                        )

                        if (lastReadSuccessful) {
                            Toast.makeText(
                                this,
                                resources.getQuantityString(
                                    R.plurals.toast_rt_graph_reading_failed,
                                    0,
                                    lastTime
                                ),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            lastReadSuccessful = false
                        }
                    }

                    else -> {
                        data.addEntry(Entry(entry.x, 0.0f), mainSetIndex)

                        Log.e(
                            "RtGraphActivity",
                            "addEntry first reading failed!"
                        )

                        if (lastReadSuccessful) {
                            Toast.makeText(
                                this,
                                resources.getQuantityString(
                                    R.plurals.toast_rt_graph_reading_failed,
                                    0,
                                    lastTime
                                ),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            lastReadSuccessful = false
                        }
                    }
                }
            }

            entryQueue.clear()

            data.notifyDataChanged()

            // let the chart know it's data has changed
            lineChart.notifyDataSetChanged()

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(DEFAULT_RTGRAPH_WINDOW)
//            lineChart.setVisibleXRange(DEFAULT_RTGRAPH_WINDOW, DEFAULT_RTGRAPH_WINDOW)
            // lineChart.setVisibleYRange(30, AxisDependency.LEFT);

//            val centerY = (item.maxRange!!.toFloat() + item.minRange!!.toFloat()) / 2

            val centerY = (data.yMax + data.yMin) / 2
            val centerX = (data.entryCount.toFloat() - DEFAULT_RTGRAPH_WINDOW / 2)

            // move to the latest entry
//            if (boundReached) {


            val dataAmplitude = data.yMax - data.yMin
            val chartAmplitude = lineChart.axisLeft.mAxisRange
            val scaleY = if (dataAmplitude != 0f) {
                chartAmplitude / dataAmplitude
            } else 1f

            val graphSize = 0.7f
            val percentage = 0.1f
            val tolerance = 0.1f

            val zoomY =
                when {
                    lineChart.scaleY > scaleY * (graphSize + tolerance) -> 1 - percentage
                    lineChart.scaleY < scaleY * (graphSize - tolerance) -> 1 + percentage
                    else -> 1f
                }

            Log.i(
                "RtGraphActivity",
                "addEntry zooming $zoomY $scaleY ${lineChart.scaleY}"
            )

            if (zoomY != 1.0f) {
                lineChart.zoom(
                    1f,
                    zoomY,
                    centerX,
                    centerY,
                    lineChart.axisLeft.axisDependency
                )
            } else {
                lineChart.centerViewTo(centerX, centerY, lineChart.axisLeft.axisDependency)
            }
//            lineChart.moveViewToX(data.entryCount.toFloat())
//            }

//            val transX = lineChart.highestVisibleX
//            Log.i("RtGraphActivity", "addEntry $transX")

            lineChart.onChartGestureListener = this
        }
    }

    private fun createSet(isErrorSet: Boolean = false): LineDataSet {

        Log.i(
            "RtGraphActivity",
            "createSet-------------------"
        )

        val variable = monitoringManager.getBy(id!!)
        val set = LineDataSet(null, getString(variable.nameResId))
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.lineWidth = 3.0f
        set.color = getLineColor(isErrorSet)
        set.isHighlightEnabled = false
        set.setDrawValues(false)
        set.setDrawCircles(false)
        set.mode = LineDataSet.Mode.LINEAR
//        set.mode = LineDataSet.Mode.CUBIC_BEZIER
//        set.cubicIntensity = 0.2f
        return set
    }

    private fun getLineColor(isError: Boolean): Int {
        return if (!isError) {
            ContextCompat.getColor(this, R.color.colorAccent)
        } else {
            Color.RED
        }
    }

    private class AttemptConnectAsync(
        val parent: RtGraphActivity,
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

                parent.readDone = true

            } else {
                Log.e("AttemptConnectAsync", "onPostExecute ModBus connection failed!")

                Toast.makeText(
                    parent,
                    parent.getString(R.string.toast_connection_failed),
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

    }

    @WorkerThread
    fun readSingle(): Number? {
        monitoringManager.updateM004ratio(driveManager.size)

        val item = monitoringManager.getBy(id!!)

        val instant =
            (SystemClock.elapsedRealtime() - initialTime) / ONE_SECOND_IN_MILLIS
        val reading = modBusUtils.readMonitoring(item)

        if (reading != null) {
            entryQueue.add(Entry(instant, reading.toFloat()))

            readDone = true
        } else {
            entryQueue.add(Entry(instant, READ_ERROR.toFloat()))
        }

//            lastTime = instant

        return reading
    }

    private class ReadSingleAsync(
        val parent: RtGraphActivity,
        val id: String
    ) : AsyncTask<Void, Void, Number?>() {
        override fun onPreExecute() {
            super.onPreExecute()

//            Log.i(
//                "ReadSingleAsync", "onPreExecute $id-----------------------"
//            )

//            parent.readDone = false

        }

        override fun doInBackground(vararg p0: Void?): Number? {

            Log.i(
                "ReadSingleAsync",
                "doInBackground-------------------"
            )

            parent.monitoringManager.updateM004ratio(parent.driveManager.size)

            val item = parent.monitoringManager.getBy(id)

            val instant =
                parent.getInstant()
            val reading = parent.modBusUtils.readMonitoring(item)

            if (reading != null) {
                val newReading = reading.toFloat()

                parent.entryQueue.add(Entry(instant, newReading))

                parent.readDone = true
            } else {
                Log.e("ReadSingleAsync", "doInBackground $id reading failed!")

                parent.entryQueue.add(Entry(instant, READ_ERROR.toFloat()))
            }

            parent.lastTime = instant

            Log.i(
                "ReadSingleAsync",
                "doInBackground ${parent.mainsCounter} ${ALARM_POLLING_MILLIS / RT_GRAPH_ENTRY_MILLIS}"
            )

            return reading
        }
    }

    @WorkerThread
    private fun readMains() {
        val mainsModel = modBusUtils.getMains()

        if (mainsModel.voltage.toLong() < READ_ERROR) {
            Log.i(
                "RtGraphActivity", "readMains voltage read successful"
            )

            mainsModel.voltage = mainsModel.voltage
        } else {
            Log.e(
                "RtGraphActivity", "readMains voltage read failed!"
            )
        }

        if (mainsModel.frequency.toLong() < READ_ERROR) {
            Log.i(
                "RtGraphActivity", "readMains frequency read successful"
            )

            mainsModel.frequency = mainsModel.frequency
        } else {
            Log.e(
                "RtGraphActivity", "readMains frequency read failed!"
            )
        }
    }

    private fun getInstant() =
        (SystemClock.elapsedRealtime() - initialTime) / ONE_SECOND_IN_MILLIS

    private class ReadMainsAsync(
        val parent: RtGraphActivity
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
                    "ReadMainsAsync", "onPostExecute frequency read failed!"
                )
            }

//            parent.lineChart.description.text =
//                "${"%.1f"
//                    .format(parent.mainsModel.voltage.toFloat())} V ${"%.1f"
//                    .format(parent.mainsModel.frequency.toFloat())} Hz"
            parent.lineChart.description.text = parent.resources.getQuantityString(
                R.plurals.mains_label,
                0,
                parent.mainsModel.voltage.toFloat(),
                parent.mainsModel.frequency.toFloat()
            )
        }

    }

    private class DisconnectAsync(val parent: RtGraphActivity) : AsyncTask<Void, Void, Void>() {
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

}
