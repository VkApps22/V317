package br.com.kascosys.vulkanconnectv317.database.firebase

import android.util.Log
import br.com.kascosys.vulkanconnectv317.models.AlertModel
import br.com.kascosys.vulkanconnectv317.models.AlertsFirebaseModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object AlertsFirebaseRepository {
    private val database = Firebase.database
    val resultList: MutableList<AlertsFirebaseModel> = mutableListOf()
    private const val validateFirebaseCache:Boolean = false
    init {
        if (!validateFirebaseCache){
            Firebase.database.setPersistenceEnabled(true)
        }
    }
    var myRef = database.getReference("alerts")
    fun fetchAlertsAsync(){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //este método é chamado na primeira vez e sempre que os dados são atualizados
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

        myRef.keepSynced(true)
    }
}