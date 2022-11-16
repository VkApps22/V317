package br.com.kascosys.vulkanconnectv317.database

import android.content.Context
import android.util.Log
import androidx.room.*
import com.google.gson.GsonBuilder

@Database(entities = [DeviceData::class], version = 6, exportSchema = false)
@TypeConverters(DeviceDataConverter::class)
abstract class DeviceDatabase : RoomDatabase() {

    abstract val databaseDao: DeviceDataDao

    companion object {
        @Volatile
        private var INSTANCE: DeviceDatabase? = null

        fun getInstance(context: Context): DeviceDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DeviceDatabase::class.java,
                        "registered_devices_database"
                    ).fallbackToDestructiveMigration().build()
                }

                return instance
            }
        }
    }

    fun insertOrUpdate(deviceData: DeviceData): Boolean {
        var success = false

        runInTransaction {
            val dataList = databaseDao.getAllByNick(deviceData.deviceNickname)

            val gson = GsonBuilder().setPrettyPrinting().create()

            Log.i("DeviceDatabase", "insertOrUpdate ${gson.toJson(dataList)}")

            success = if (dataList.isEmpty()) {
                Log.i("DeviceDatabase", "insertOrUpdate will insert ${gson.toJson(deviceData)}")
                databaseDao.insert(deviceData)
                true
            } else {
                Log.i("DeviceDatabase", "insertOrUpdate will update ${gson.toJson(deviceData)}")
                deviceData.id = dataList[0].id
                databaseDao.update(deviceData) > -1
            }
        }

        Log.i("DeviceDatabase", "insertOrUpdate will return $success")

        return success
    }

    fun deleteAllBySsId(ssId: String) {
        runInTransaction {
            val dataList = databaseDao.getAllBySsId(ssId)

            dataList.forEach {
                databaseDao.delete(it)
            }
        }
    }

    fun deleteAllByNick(nick: String) {
        runInTransaction {
            val dataList = databaseDao.getAllByNick(nick)

            dataList.forEach {
                databaseDao.delete(it)
            }
        }
    }
}