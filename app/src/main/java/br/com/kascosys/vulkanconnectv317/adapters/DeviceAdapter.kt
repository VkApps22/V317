package br.com.kascosys.vulkanconnectv317.adapters

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.com.kascosys.vulkanconnectv317.interfaces.OnDeviceClick
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.ONE_DAY_IN_MILLIS
import br.com.kascosys.vulkanconnectv317.database.DeviceData
import br.com.kascosys.vulkanconnectv317.enums.DeviceState
import br.com.kascosys.vulkanconnectv317.models.DeviceModel
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.*

class DeviceAdapter(
    private val deviceList: List<DeviceModel>,
    private val context: Context,
    private val listener: OnDeviceClick
) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

//    fun shuffleAndRefresh() {
//        deviceList.shuffle()
//        notifyDataSetChanged()
//    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceViewHolder {
        // create a new view
        return DeviceViewHolder.from(parent)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val item = deviceList[position]
        holder.bind(item, listener, context)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = deviceList.size

    class DeviceViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val deviceText: TextView = itemView.findViewById(R.id.device_item_nickname)
        private val deviceSsId: TextView = itemView.findViewById(R.id.device_item_ssid)
        private val modelName: TextView = itemView.findViewById(R.id.device_item_model)
        private val deviceStatus: TextView = itemView.findViewById(R.id.device_item_status)
        private val lastConnected: TextView = itemView.findViewById(R.id.device_item_lastConnected)

        companion object {
            fun from(parent: ViewGroup): DeviceViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.device_view, parent, false)

                return DeviceViewHolder(
                    itemView
                )
            }
        }

        fun bind(item: DeviceModel, clickListener: OnDeviceClick, context: Context) {
            deviceText.text = item.deviceNickname

            var colorId = -1
            when (item.deviceState) {
                DeviceState.OFFLINE -> {
                    colorId = context.resources.getColor(R.color.colorAccentLight)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        colorId = getColor(context,R.color.colorAccentLight)
                    }
                    deviceStatus.text = context.getString(R.string.offline_status)
                }
                DeviceState.ONLINE -> {
                    colorId = getColor(context,R.color.colorAccentMid)
                    deviceStatus.text = context.getString(R.string.online_status)
                }
                DeviceState.CONNECTED -> {
                    colorId = getColor(context,R.color.colorAccent)
                    deviceStatus.text = context.getString(R.string.connected_status)
                }
            }
            deviceStatus.setTextColor(colorId)

            deviceSsId.text = item.deviceSsId
            modelName.text = "V317-${item.modelSize}A"

            lastConnected.text =
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(Date(item.lastConnected))

            itemView.setOnClickListener { clickListener.onDeviceClicked(adapterPosition) }

        }

        private fun getColor(context: Context, colorFromResource: Int ): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context.getColor(colorFromResource)
            }

            @Suppress("DEPRECATION")
            return context.resources.getColor(colorFromResource)
        }
    }
}