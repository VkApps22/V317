package br.com.kascosys.vulkanconnectv317.database.firebase

import android.content.Context
import android.util.Log
import androidx.annotation.IntegerRes
import br.com.kascosys.vulkanconnectv317.models.AlertModel
import br.com.kascosys.vulkanconnectv317.models.AlertsFirebaseModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import java.io.IOException

class AlertsFirebaseRepository {
    private val database = Firebase.database
    private var myRef = database.getReference("alerts")
    private var resultList: MutableList<AlertsFirebaseModel> = mutableListOf()

    fun fetchAlertsAsync(){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach() { dataSnapshot ->
                    val alertObject = AlertsFirebaseModel()
                    alertObject.language = dataSnapshot.key

                    dataSnapshot.children.forEach() { doc ->
                        val alert = doc.getValue(AlertModel::class.java)!!.copy(id = doc.key!!)
                        alertObject.alerts.add(alert)
                    }
                    resultList.add(alertObject)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.v("firebase", "Failed to read value.", error.toException())
            }
        })
    }

    suspend fun fetchAlertsSync(): MutableList<AlertsFirebaseModel> {
        myRef.get().await().children.forEach() { dataSnapshot ->
            val alertObject = AlertsFirebaseModel()
            alertObject.language = dataSnapshot.key

            dataSnapshot.children.forEach() { doc ->
                val alert = doc.getValue(AlertModel::class.java)!!.copy(id = doc.key!!)
                alertObject.alerts.add(alert)
            }
            resultList.add(alertObject)
        }
        return resultList
    }

    fun getJsonDataFromAsset(context: Context, id: Int): List<AlertsFirebaseModel>? {
        val jsonString: String
        try {
            jsonString =
                context.resources.openRawResource(id).bufferedReader().use { it.readText() }
            val gson = Gson()
            val result = gson.fromJson(jsonString, Map::class.java)
            var resultList: MutableList<AlertsFirebaseModel> = mutableListOf()
            result.forEach() { dataSnapshot ->
                val alertObject = AlertsFirebaseModel()
                alertObject.language = dataSnapshot.key.toString()
                val children = dataSnapshot.value as Map<String, String>
                children.forEach() { doc ->
                    val element = doc.value as Map<String, String>
                    val id = doc.key
                    val values = doc.value as Map<String, String>
                    val label = values["label"]
                    val description = values["description"]
                    alertObject.alerts.add(AlertModel(id, description, label))

                }
                resultList.add(alertObject)
            }
            return resultList
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return mutableListOf()
        }
    }
}