package br.com.kascosys.vulkanconnectv317.database.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
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
    private var resultList: List<List<AlertsFirebaseModel>> = mutableListOf()
    fun fetchAlerts(liveData: MutableLiveData<List<List<AlertsFirebaseModel>>>){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //este método é chamado na primeira vez e sempre que os dados são atualizados

                resultList = snapshot.children.map { dataSnapshot ->
                    dataSnapshot.children.map { doc ->
                        doc.getValue(AlertsFirebaseModel::class.java)!!.copy(language = dataSnapshot.key!!, id = doc.key!!)
                    }
                }

                liveData.postValue(resultList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.v("firebase", "Failed to read value.", error.toException())
            }
        })
    }

    suspend fun getData() : List<List<AlertsFirebaseModel>> {
        val value = myRef.get().await().children.map { dataSnapshot ->
            dataSnapshot.children.map { doc ->
                doc.getValue(AlertsFirebaseModel::class.java)!!.copy(language = dataSnapshot.key!!, id = doc.key!!)
            }
        }

        if(value != null){
            resultList = value
        }
        return resultList
    }
}