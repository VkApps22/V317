package br.com.kascosys.vulkanconnectv317.database.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.kascosys.vulkanconnectv317.models.AlertsFirebaseModel

class AlertsFirebaseViewModel : ViewModel(){

    private val repository = AlertsFirebaseRepository()

    private val _alertsFirebaseLiveData = MutableLiveData<List<List<AlertsFirebaseModel>>>()

    val alertsFirebaseLiveData: LiveData<List<List<AlertsFirebaseModel>>> = _alertsFirebaseLiveData

    fun fetchAlertsFirebase(){
        repository.fetchAlerts(_alertsFirebaseLiveData)
    }


}