package br.com.kascosys.vulkanconnectv317.utils.modbus

import android.util.Log
import androidx.annotation.WorkerThread
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.interfaces.ParameterListItem
import br.com.kascosys.vulkanconnectv317.managers.ParameterManager
import br.com.kascosys.vulkanconnectv317.models.*
import com.intelligt.modbus.jlibmodbus.Modbus
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory
import com.intelligt.modbus.jlibmodbus.msg.request.ReadHoldingRegistersRequest
import com.intelligt.modbus.jlibmodbus.msg.request.WriteMultipleRegistersRequest
import com.intelligt.modbus.jlibmodbus.msg.response.ReadHoldingRegistersResponse
import com.intelligt.modbus.jlibmodbus.msg.response.WriteMultipleRegistersResponse
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters
import java.net.InetAddress

@WorkerThread
class ModBusUtils(baseIp: String, host: Int, port: Int) {

    private val tcpParameters: TcpParameters = TcpParameters()

    private val slaveId = 1

    private val quantity = 1

    private lateinit var master: ModbusMaster

    init {

        try {

            Log.i("ModBusUtils", "init---------------")

            tcpParameters.host = InetAddress.getByName("${baseIp}${host}")
            tcpParameters.isKeepAlive = true
            tcpParameters.port = port

            Log.i(
                "ModBusUtils", "init tcp ${
                tcpParameters.host
                } ${
                tcpParameters.port
                } ${
                tcpParameters.isKeepAlive
                }"
            )

            master = ModbusMasterFactory.createModbusMasterTCP(tcpParameters)
            Modbus.setAutoIncrementTransactionId(true)

            connectMaster()

        } catch (e: ModbusProtocolException) {
            e.printStackTrace()
        } catch (e: ModbusNumberException) {
            e.printStackTrace()
        } catch (e: ModbusIOException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun connectMaster() {
        Log.i("ModBusUtils", "connectMaster---------------")

        if (!master.isConnected) {
            Log.i("ModBusUtils", "connectMaster will connect master")
            try {
                master.connect()
            } catch (e: ModbusProtocolException) {
                e.printStackTrace()
            } catch (e: ModbusNumberException) {
                e.printStackTrace()
            } catch (e: ModbusIOException) {
                e.printStackTrace()
            } catch (e: RuntimeException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.i("ModBusUtils", "connectMaster master already connected")
        }
    }


    fun isConnected(): Boolean {
        return master.isConnected
    }

    fun disconnect() {
        Log.i("ModBusUtils", "disconnect---------------")

        try {
            master.disconnect()
        } catch (e: ModbusIOException) {
            e.printStackTrace()
        }
    }

    fun programParameter(id: String, value: Number, ratio: Number): Boolean {
        Log.i("ModBusUtils", "programParameter $id $value $ratio---------------")

//        when (id) {
//            P128 -> writeRegister(158, value.toInt())
//            P171 -> writeRegister(191, value.toInt())
//            P177 -> writeRegister(197, value.toInt())
//            C000 -> writeRegister(310, value.toInt())
//            else -> Log.e("ModBusUtils", "Invalid parameter ID")
//        }
        val item = ParameterManager.getInstance()?.getBy(id)
        val address = item?.modBusAddress!!
//        val address = ModBusValues.mapParIdToAddress(id)
        val converted = ModBusValues.getModBusVal(id, value, ratio)

        var success = false

        if (converted != null)
            success = writeRegister(address, converted, true)
        else {
            Log.e("ModBusUtils", "programParameter conversion failed!")
        }

        return success
    }

    fun changeModeKey(key: Int): Boolean {
        Log.i("ModBusUtils", "changeModeKey $key---------------")

        val address = ModBusValues.mapParIdToAddress(ADVANCED_KEY_ID)

        if (key <= DEFAULT_ADVANCED_KEY.toInt()) {
            return writeRegister(address, key, true)
        }

        return false
    }

    fun readParameter(id: String, ratio: Number): Number? {
        Log.i("ModBusUtils", "readParameter $id $ratio---------------")

        val item = ParameterManager.getInstance()?.getBy(id)
        val address = item?.modBusAddress!!

//        val address = ModBusValues.mapParIdToAddress(id)
        val modBusVal = this.readSingleRegister(address)

        if (modBusVal != null) {
            val converted = ModBusValues.getRealVal(id, modBusVal.toInt(), ratio)
            Log.i("ModBusUtils", "readParameter $id $modBusVal $converted")

            if (converted != null) {
                return converted
            }

            Log.e("ModBusUtils", "readParameter conversion failed!")

            return this.readSingleRegister(address)

        }

        Log.e("ModBusUtils", "readParameter read failed!")

        return this.readSingleRegister(address)
    }

    fun readMonitoring(item: MonitoringModel): Number? {
        Log.i("ModBusUtils", "readMonitoring ${item.idNumber} ${item.ratio}---------------")

        val address = item.address
        val modBusVal = this.readSingleRegister(address)
        val unit = item.unit
        val id = item.idNumber

        if (modBusVal != null) {
            val isSigned = !unit.contains("bit") && id != M027

            val converted =
                ModBusValues.getRealVal(item.idNumber, modBusVal.toInt(), item.ratio, isSigned)
            Log.i("ModBusUtils", "readMonitoring ${item.idNumber} $modBusVal $converted")

            if (converted != null) {
                return converted
            }

            Log.e("ModBusUtils", "readMonitoring conversion failed!")

            return this.readSingleRegister(address)

        }

        Log.e("ModBusUtils", "readParameter $id read failed!")

        return this.readSingleRegister(address)
    }

    fun readMonitoring(item: NewMonitoringModel): Number? {
        Log.i("ModBusUtils", "readParameter ${item.id} ${item.ratio}---------------")

        val address = item.modBusAddress!!
        val modBusVal = this.readSingleRegister(address)
        val unit = item.unit!!
        val id = item.id!!

        if (modBusVal != null) {
            val isSigned = !unit.contains("bit")

            val converted =
                ModBusValues.getRealVal(id, modBusVal.toInt(), item.ratio!!, isSigned)
            Log.i("ModBusUtils", "readParameter $id $modBusVal $converted")

            if (converted != null) {
                return converted
            }

            Log.e("ModBusUtils", "readParameter conversion failed!")

            return this.readSingleRegister(address)

        }

        Log.e("ModBusUtils", "readParameter $id read failed!")

        return this.readSingleRegister(address)
    }

    fun readMonitoringList(monitoringList: List<MonitoringModel>): ArrayList<Number> {
        Log.i("ModBusUtils", "readMonitoringList $monitoringList-------------------")

        val readList = ArrayList<Number>()
        for (i in monitoringList.indices) {
            val address = monitoringList[i].address
            val ratio = monitoringList[i].ratio
            val id = monitoringList[i].idNumber
            val unit = monitoringList[i].unit

            if (address < 0) {
                readList.add(READ_ERROR)
                continue
            }

            val modBusVal = readSingleRegister(address)

            Log.i("ModBusUtils", "readMonitoringList $i $id $address read:$modBusVal")

            if (modBusVal != null) {
                val isSigned = !unit.contains("bit") && id != M027

                Log.i("ModBusUtils", "readMonitoringList isSigned $isSigned")

                val converted = ModBusValues.getRealVal(
                    id,
                    modBusVal.toInt(),
                    ratio,
                    isSigned
                )

                if (converted != null) {
                    readList.add(converted)
                } else {
                    Log.e("ModBusUtils", "readMonitoringList $i $id conversion failed!")

                    readList.add(READ_ERROR)
                }

            } else {
                Log.e("ModBusUtils", "readMonitoringList $i $id read failed!")

                readList.add(READ_ERROR)
            }
        }

        Log.i("ModBusUtils", "readMonitoringList will return $readList")

        return readList
    }

    fun readParameterList(list: List<ParameterListItem>): ArrayList<Number> {
        Log.i("ModBusUtils", "readParameterList $list-------------------")

        val readList = ArrayList<Number>()
        list.forEachIndexed { i, item ->
            if (item is ParameterItem) {
                val address = item.data.modBusAddress!!
                val ratio = item.data.ratio!!
                val id = item.data.id!!
                val unit = item.data.unit!!

                val modBusVal = readSingleRegister(address)

                Log.i("ModBusUtils", "readParameterList $i $id $address read:$modBusVal")

                if (modBusVal != null) {
                    val isSigned = !unit.contains("bit")

                    val converted = ModBusValues.getRealVal(
                        id,
                        modBusVal.toInt(),
                        ratio,
                        isSigned
                    )

                    if (converted != null) {
                        readList.add(converted)
                    } else {
                        Log.e("ModBusUtils", "readParameterList $i $id conversion failed!")

                        readList.add(READ_ERROR)
                    }

                } else {
                    Log.e("ModBusUtils", "readParameterList $i $id read failed!")

                    readList.add(READ_ERROR)
                }
            }
        }

        Log.i("ModBusUtils", "readParameterList will return $readList")

        return readList
    }

    fun saveParameter(id: String): Boolean {
        Log.i("ModBusUtils", "saveParameter $id---------------")

        val registerAddress = ModBusValues.mapParIdToAddress(id)
        val modBusAddress = ModBusValues.mapParIdToAddress(SAVE_PARAMETER)

        return writeRegister(modBusAddress, registerAddress)

    }

    fun getAlarmNumber(): String {
        Log.i("ModBusUtils", "getAlarmNumber---------------")

        val modBusAddress = ModBusValues.mapParIdToAddress(ALARM_NUMBER)

        val readValue = readSingleRegister(modBusAddress)

        if (readValue != null) {
            Log.i(
                "ModBusUtils",
                "getAlarmNumber will map $readValue ${readValue.toInt()}"
            )

            val alarmString = ModBusValues.mapAlarmNumberToId(readValue.toInt())

            Log.i(
                "ModBusUtils",
                "getAlarmNumber will return $alarmString"
            )

            return alarmString
        }

        Log.e("ModBusUtils", "getAlarmNumber alarm reading failed!")

        return ""
    }

    fun getDriveSize(): Int {
        Log.i("ModBusUtils", "getDriveSize---------------")

        val modBusAddress = ModBusValues.mapParIdToAddress(DRIVE_SIZE)

        val readValue = readSingleRegister(modBusAddress)

        if (readValue != null) {
            Log.i("ModBusUtils", "getDriveSize will return $readValue")

            return readValue.toInt()
        }

        Log.e("ModBusUtils", "getDriveSize size reading failed!")

        return -1
    }

    fun getMains(): MainsModel {
        Log.i("ModBusUtils", "getMains---------------")

        val voltAddress = ModBusValues.mapParIdToAddress(MAINS_VOLTAGE)
        val freqAddress = ModBusValues.mapParIdToAddress(MAINS_FREQUENCY)

        val mains = MainsModel()

        val readMains = readMultipleRegisters(freqAddress)

        if (readMains.size == 2) {
            Log.i("ModBusUtils", "getMains $readMains")

            mains.frequency = readMains[0].toFloat() / 10
            mains.voltage = readMains[1]
        } else {
            Log.e("ModBusUtils", "getMains reading failed! $readMains")
        }

        return mains
    }

    fun programNewPassword(password: Int): Boolean {
        if (password in MIN_PASSWORD..MAX_PASSWORD) {
            writeRegister(ModBusValues.mapParIdToAddress(PASSWORD_EDIT_ID), password)

            return requestWritePermission(password)
        }

        return false
    }

    fun requestWritePermission(password: Int): Boolean {
        Log.i("ModBusUtils", "requestWritePermission $password---------------")

        if (password in MIN_PASSWORD..MAX_PASSWORD) {
            writeRegister(ModBusValues.mapParIdToAddress(PASSWORD_CHECK_ID), password)

            val readPassword =
                readSingleRegister(ModBusValues.mapParIdToAddress(PASSWORD_EDIT_ID))

            Log.i("ModBusUtils", "requestWritePermission current $readPassword")

            return readPassword == password
        }

        Log.e("ModBusUtils", "requestWritePermission password out of range!")

        return false
    }

    private fun readSingleRegister(registerNum: Int): Number? {
        Log.i("ModBusUtils", "readSingleRegister---------------")

        try {

            connectMaster()

            val request = ReadHoldingRegistersRequest()
            request.serverAddress = slaveId
            request.startAddress = registerNum
            request.quantity = quantity

            Log.i(
                "ModBusUtils",
                "request id ${request.transactionId} func ${
                request.function
                } qu ${request.quantity}"
            )

            val response = master.processRequest(request)
                    as ReadHoldingRegistersResponse

            var offset = registerNum
            for (value in response.holdingRegisters) {
//            Log.i("ModBusUtils", "Slave register address: ${
//            offset
//            }, Value: $value")
//            Log.i("ModBusUtils", "Slave register id: ${ModBusValues.mapParAddressToId(offset++)}")
                Log.i(
                    "ModBusUtils", "Slave register address: ${
                    ModBusValues.mapParAddressToId(offset++)
                    }, Value: $value"
                )
            }

            Log.i(
                "ModBusUtils", "readSingleRegister will return ${
                response.holdingRegisters[0]
                }"
            )
            return response.holdingRegisters[0]

        } catch (e: ModbusProtocolException) {
            e.printStackTrace()
        } catch (e: ModbusNumberException) {
            e.printStackTrace()
        } catch (e: ModbusIOException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun readMultipleRegisters(registerNum: Int, quantity: Int = 2): List<Number> {
        Log.i("ModBusUtils", "readMultipleRegisters---------------")

        try {

            connectMaster()

            val request = ReadHoldingRegistersRequest()
            request.serverAddress = slaveId
            request.startAddress = registerNum
            request.quantity = quantity

            Log.i(
                "ModBusUtils",
                "request id ${request.transactionId} func ${
                request.function
                } qu ${request.quantity}"
            )

            val response = master.processRequest(request)
                    as ReadHoldingRegistersResponse

            var offset = registerNum
            for (value in response.holdingRegisters) {
//            Log.i("ModBusUtils", "Slave register address: ${
//            offset
//            }, Value: $value")
//            Log.i("ModBusUtils", "Slave register id: ${ModBusValues.mapParAddressToId(offset++)}")
                Log.i(
                    "ModBusUtils", "Slave register address: ${
                    ModBusValues.mapParAddressToId(offset++)
                    }, Value: $value"
                )
            }

            Log.i(
                "ModBusUtils", "readMultipleRegisters will return ${
                response.holdingRegisters
                }"
            )
            return response.holdingRegisters.map { it }

        } catch (e: ModbusProtocolException) {
            e.printStackTrace()
        } catch (e: ModbusNumberException) {
            e.printStackTrace()
        } catch (e: ModbusIOException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listOf()
    }

    private fun writeRegister(
        registerNum: Int,
        value: Int,
        checkRegister: Boolean = false
    ): Boolean {
        Log.i("ModBusUtils", "writeRegister $registerNum $value---------------")

        try {

            connectMaster()

            val request = WriteMultipleRegistersRequest()
            request.serverAddress = slaveId
            request.startAddress = registerNum
            request.transactionId = master.transactionId
            request.quantity = quantity
            request.registers = arrayOf(value).toIntArray()

            Log.i(
                "ModBusUtils",
                "request id ${request.transactionId} func ${
                request.function
                } qu ${request.quantity} data ${
                request.registers
                }"
            )

            val response = master.processRequest(request)
                    as WriteMultipleRegistersResponse

            Log.i(
                "ModBusUtils",
                "Write response ${response.quantity}"
            )

//            if(checkRegister) {
//                return value == readSingleRegister(registerNum)
//            }

            return true

        } catch (e: ModbusProtocolException) {
            e.printStackTrace()
        } catch (e: ModbusNumberException) {
            e.printStackTrace()
        } catch (e: ModbusIOException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false

    }

}