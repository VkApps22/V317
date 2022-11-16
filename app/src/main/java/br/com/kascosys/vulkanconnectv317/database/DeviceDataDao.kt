package br.com.kascosys.vulkanconnectv317.database

import androidx.room.*

@Dao
interface DeviceDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(device: DeviceData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(device: DeviceData): Int

    @Query("SELECT * from device_data_table WHERE id = :key")
    fun get(key: Long): DeviceData?

    @Query("DELETE FROM device_data_table")
    fun clear()

    @Query("SELECT * from device_data_table ORDER BY last_active DESC")
    fun getAll(): List<DeviceData>

    @Query("SELECT device_ssid, device_nick, device_size, last_active from device_data_table ORDER BY last_active DESC")
    fun getAllMinimal(): MutableList<DeviceMinimalData>

    @Query("SELECT device_ssid, device_nick, device_size, last_active from device_data_table WHERE device_ssid = :ssId ORDER BY last_active DESC")
    fun getMinimalBySsId(ssId: String): MutableList<DeviceMinimalData>

    @Query("SELECT * from device_data_table WHERE device_ssid = :ssId ORDER BY last_active DESC")
    fun getAllBySsId(ssId: String): MutableList<DeviceData>

    @Query("SELECT * from device_data_table WHERE device_nick = :nick ORDER BY last_active DESC")
    fun getAllByNick(nick: String): MutableList<DeviceData>

    @Delete
    fun delete(device: DeviceData): Int
}