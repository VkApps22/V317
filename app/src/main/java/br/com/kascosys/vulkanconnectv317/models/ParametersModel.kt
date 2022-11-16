package br.com.kascosys.vulkanconnectv317.models

import android.content.Context
import android.util.JsonReader
import android.util.Log
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.enums.ParameterState
import br.com.kascosys.vulkanconnectv317.utils.Util
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.*


class ParametersModel(
    val name: String,
    val idParameter: String,
    val advancedParameter: Boolean,
    val minRange: Double,
    val maxRange: Double,
    var value: Double
)

class NewParameterModel {

    @SerializedName("id")
    var id: String? = null

    @SerializedName("shortName")
    var shortName: String? = null

    @SerializedName("nameResId")
    var nameResId: Int = 0

    @SerializedName("address")
    var modBusAddress: Int? = null

    @SerializedName("logicalBlocks")
    var logicalBlocks: Int? = null

    @SerializedName("description")
    var descriptionResId: Int = 0

    @SerializedName("minVal")
    var minRange: Number? = null

    @SerializedName("maxVal")
    var maxRange: Number? = null

    @SerializedName("minWrite")
    var minWrite: Number? = null

    @SerializedName("maxWrite")
    var maxWrite: Number? = null

    @SerializedName("minWriteAdvanced")
    var minAdvanced: Number? = null

    @SerializedName("maxWriteAdvanced")
    var maxAdvanced: Number? = null

    @SerializedName("unit")
    var unit: String? = null

    @SerializedName("ratio")
    var ratio: Number? = null

    @SerializedName("defaultVal40")
    var defaultVal40: Number? = null

    @SerializedName("defaultVal70")
    var defaultVal70: Number? = null

    @SerializedName("value")
    var value: Number? = null

    @SerializedName("lastVal")
    var lastVal: Number? = null

    @SerializedName("advancedParameter")
    var advancedParameter: Boolean? = null

    @SerializedName("state")
    var state = ParameterState.UNEDITED
}

//class ParameterList {
//
//    lateinit var parameterList: ArrayList<NewParameterModel>

//    init {
//        Log.i("NewParameterModel", File("").absolutePath)


//        Log.i("NewParameterModel", File("").absolutePath)
//        val filePath: String = "/home/kascobot/Desktop/Vulkan/vulkanconnectv317/app/src/main/res/raw"
//        val file = File(filePath)
//        val jsonReader = JsonReader(FileReader(file))
//        val stream = context.resources.openRawResource(R.raw.basic_parameter_table)

//        val fileContent = context::class.java.classLoader?.getResource(
//            "/raw/basic_parameter_table_table.json"
//        )?.readText()

//        val size = stream.available()
//
//        val buffer = ByteArray(size)
//
//        stream.read(buffer)
//
//        stream.close()
//
//        val fileContent = String(buffer)


//        val x = BufferedReader(InputStreamReader(stream)).readLine()
//        val jsonReader: JsonReader = JsonReader(InputStreamReader(stream))

//        Log.i("NewParameterModel", "init jsonString $jsonReader")


//        val parameterList= Gson().fromJson<NewParameterModel>(
//            x
//        )
//        val groupListType = object : TypeToken<ArrayList<NewParameterModel>>() {
//
//        }.type
//        parameterList = Gson().fromJson(Util.readBasicParameterJson(context), groupListType)
//
//        for(i in 0 until parameterList.size){
//            Log.i("NewParameterModel","init $i ${parameterList[i].id}")
//        }
//    }



//}