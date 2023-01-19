package br.com.kascosys.vulkanconnectv317.database

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AlertsFirebase  {
    var result: Map<String, Any> = mutableMapOf<String, Any>()
    val database = Firebase.database
    private val myRef = database.getReference("alerts")

    constructor(){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //este método é chamado na primeira vez e sempre que os dados são atualizados
                result = dataSnapshot.getValue() as Map<String, Any>
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.v("firebase", "Failed to read value.", error.toException())
            }
        })
    }

    suspend fun getData() : Map<String, Any> {
        val value = myRef.get().await().value
        if (value != null){
            @Suppress("UNCHECKED_CAST")
            result = value as Map<String, Any>
        }
        return result
    }
}