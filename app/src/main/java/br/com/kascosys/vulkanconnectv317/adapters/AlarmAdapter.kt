package br.com.kascosys.vulkanconnectv317.adapters

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.interfaces.AlarmListItem
import br.com.kascosys.vulkanconnectv317.interfaces.OnHeaderClick
import br.com.kascosys.vulkanconnectv317.models.AlarmHeader
import br.com.kascosys.vulkanconnectv317.models.AlarmItem
import com.shuhart.stickyheader.StickyAdapter


class AlarmAdapter(
    private val alarmDataSet: MutableList<AlarmListItem>,
    private val showInactive: MutableLiveData<Boolean>,
    private val inactiveHeaderListener: OnHeaderClick
) :
    StickyAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>(), Filterable {

    private val HEADER = AlarmListItem.typeConstants.HEADER
    private val ITEM = AlarmListItem.typeConstants.ITEM
    private val CUSTOM_HEADER = AlarmListItem.typeConstants.CUSTOM_HEADER

    var filtering = false
    private var filteredAlarms: MutableList<AlarmListItem> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return if (filtering){
            filteredAlarms[position].listItemType
        }else{
            alarmDataSet[position].listItemType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.i("AlarmAdapter", "onCreateViewHolder------viewType $viewType")
        when (viewType) {
            HEADER, CUSTOM_HEADER -> return HeaderViewHolder.from(parent)
            ITEM -> return ItemViewHolder.from(parent)
            else -> Log.e(
                "AlarmAdapter",
                "onCreateViewHolder Type out of bounds"
            )
        }

        return HeaderViewHolder.from(parent)
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        Log.i("AlarmAdapter", "onCreateHeaderViewHolder------")
        return createViewHolder(parent, HEADER)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.i("AlarmAdapter", "onBindViewHolder------pos $position")

        val item : AlarmListItem = if (filtering) {
            filteredAlarms[position]
        } else {
            alarmDataSet[position]
        }

        Log.e("onbindSection", item.section.toString())
        Log.e("onbindType", item.listItemType.toString())
        Log.e("onbindState", item.state.toString())


        when (item.listItemType) {
            HEADER, CUSTOM_HEADER -> (holder as HeaderViewHolder).bindOnList(
                item,
                showInactive.value!!,
                inactiveHeaderListener
            )
            ITEM -> (holder as ItemViewHolder).bind(item)
            else -> Log.e(
                "AlarmAdapter",
                "onBindViewHolder Type out of bounds"
            )
        }
    }

    override fun onBindHeaderViewHolder(viewHolder: RecyclerView.ViewHolder?, i: Int) {
        Log.i("AlarmAdapter", "onBindHeaderViewHolder------pos $i")

        val item = alarmDataSet[i]
        (viewHolder as HeaderViewHolder).bindOnHeader(item)

        if(filtering){
            viewHolder.itemView.visibility = View.INVISIBLE
        }

    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        Log.i("AlarmAdapter", "getHeaderPositionForItem------pos $itemPosition")

        return if (filtering){
            filteredAlarms[itemPosition].section
        }else{
            alarmDataSet[itemPosition].section
        }
    }

    override fun getItemCount(): Int {
        Log.i("AlarmAdapter", "getItemCount------size ${alarmDataSet.size}")

        if (filtering){
            return filteredAlarms.size
        }else if (!showInactive.value!!) {
            return alarmDataSet.filter {
                it.state == AlarmListItem.stateConstants.ACTIVE
            }.size + 1
        }

        return alarmDataSet.size
    }

    //FILTERABLE
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                if (charSequence != null) {
                    val queryString = charSequence.toString().toLowerCase()

                    if (queryString.isEmpty()) {
                        filtering = false
                        filteredAlarms = alarmDataSet
                    } else {
                        val alarms = alarmDataSet.filterIsInstance<AlarmItem>()
                        val headers = alarmDataSet.filterIsInstance<AlarmHeader>().toMutableList()

                        filtering = true
                        filteredAlarms = alarms.filter {
                            it.data.name!!.toLowerCase().contains(queryString) || it.data.idNumber.toLowerCase().contains(
                                queryString
                            )
                        }.toMutableList()

                        while (headers.isNotEmpty()) {
                            val header = headers.removeAt(0)
                            val index = filteredAlarms.indexOfFirst { it.section == header.section }
                            if (index > -1) {
                                filteredAlarms.add(index, header)
                            }
                        }
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredAlarms

                return filterResults
            }

            override fun publishResults(p0: CharSequence?, filterResults: FilterResults?) {
//                filteredAlarms = filterResults?.values as MutableList<AlarmListItem>
                notifyDataSetChanged()
            }
        }
    }

    class HeaderViewHolder private constructor(itemView: View, val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val INACTIVE = AlarmListItem.stateConstants.INACTIVE
        private val ACTIVE = AlarmListItem.stateConstants.ACTIVE

        private val headerContainer: View = itemView.findViewById(R.id.alarm_header_container)
        private val headerTitle: TextView = itemView.findViewById(R.id.alarm_header_title)
        private val headerIcon: ImageView = itemView.findViewById(R.id.alarm_header_icon)
//        private val headerShowHideText: TextView =
//            itemView.findViewById(R.id.alarm_header_showOrHide)
        private val headerShowHideIcon: ImageView = itemView.findViewById(R.id.alarm_header_show_icon)

        internal fun bindOnList(item: AlarmListItem, showInactive: Boolean, listener: OnHeaderClick) {
            Log.i("HeaderViewHolder", "bindOnList------state ${item.state}")

            val title = (item as AlarmHeader).headerTag
            val state = item.state

            this.headerTitle.text = title

            when (state) {
                INACTIVE -> {
                    headerShowHideIcon.visibility = View.VISIBLE
                    val resource = if (showInactive) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
                    headerShowHideIcon.setImageResource(resource)
                    headerContainer.setOnClickListener { listener.OnHeaderClicked() }
                }
                ACTIVE -> {
                    headerShowHideIcon.visibility = View.GONE
                }
                else -> Log.e(
                    "HeaderViewHolder",
                    "bindOnList Invalid alarm state"
                )
            }

            bind(item)
        }

        internal fun bindOnHeader(item: AlarmListItem) {
            Log.i("HeaderViewHolder", "bindOnHeader------state ${item.state}")

            headerShowHideIcon.visibility = View.GONE

            bind(item)
        }

        private fun bind(item: AlarmListItem) {
            Log.i("HeaderViewHolder", "bind------")

            val title = (item as AlarmHeader).headerTag
            val state = item.state

            this.headerTitle.text = title

            when (state) {
                INACTIVE -> {
                    val color = ContextCompat.getColor(context, R.color.colorAccent)
                    headerTitle.setTextColor(color)
                    headerIcon.visibility = View.GONE
                }
                ACTIVE -> {
                    headerTitle.setTextColor(Color.RED)
                    headerIcon.visibility = View.VISIBLE
                }
                else -> Log.e(
                    "HeaderViewHolder",
                    "bind Invalid alarm state"
                )
            }
        }

        companion object {

            internal fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val itemView = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.alarm_header, parent, false)

                return HeaderViewHolder(itemView, parent.context)
            }
        }

    }

    class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val alarmName: TextView = itemView.findViewById(R.id.alarm_name_text)
        private val alarmNumberName: TextView = itemView.findViewById(R.id.alarm_name_number_text)
        private val alarmDescription: TextView = itemView.findViewById(R.id.alarm_detail_text)
        private val showButton: ImageButton = itemView.findViewById(R.id.showAlarmDetailButton)

        private var isShowing: Boolean = false

        internal fun bind(item: AlarmListItem) {
            alarmName.text = (item as AlarmItem).data.name
            alarmNumberName.text = item.data.idNumber
            alarmDescription.text = item.data.alarmDescription

            alarmDescription.visibility = View.GONE

            if (item.state == AlarmListItem.stateConstants.ACTIVE) {
                alarmName.setTextColor(Color.RED)
            } else {
                alarmName.setTextColor(Color.BLACK)
            }

            itemView.setOnClickListener {
                changeShowState()
            }

            showButton.setOnClickListener {
                changeShowState()
            }



        }

        private fun changeShowState() {
            if (isShowing) {
                showButton.setImageResource(R.drawable.ic_arrow_down)
                alarmDescription.visibility = View.GONE
            } else {
                showButton.setImageResource(R.drawable.ic_arrow_up)
                alarmDescription.visibility = View.VISIBLE
            }
            isShowing = !isShowing
        }

        companion object {

            internal fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val itemView = LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.alarm_card, parent, false)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    itemView.findViewById<TextView>(R.id.alarm_detail_text).justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD

                return ItemViewHolder(itemView)
            }
        }
    }

}