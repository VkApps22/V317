package br.com.kascosys.vulkanconnectv317.database.firebase

import android.util.Log
import br.com.kascosys.vulkanconnectv317.models.AlertModel
import br.com.kascosys.vulkanconnectv317.models.AlertsFirebaseModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AlertsFirebaseRepository  {
    private val database = Firebase.database
    private var myRef = database.getReference("alerts")
    private var resultList: MutableList<AlertsFirebaseModel> = mutableListOf()
    fun fetchAlertsAsync(){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //este método é chamado na primeira vez e sempre que os dados são atualizados

                snapshot.children.forEach() { dataSnapshot ->
                    val alertObject = AlertsFirebaseModel()
                    alertObject.language = dataSnapshot.key
                    val alertList: MutableList<AlertModel> = mutableListOf()

                    dataSnapshot.children.forEach() { doc ->
                        val alert = doc.getValue(AlertModel::class.java)!!.copy(id = doc.key!!)
                        alertList.add(alert)
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
}