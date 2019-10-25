package com.example.cityemotions.datasources

import androidx.lifecycle.MutableLiveData
import com.example.cityemotions.datamodels.MarkerModel
import com.google.android.gms.maps.model.LatLng

class MarkerDataSource : DataSource {
    companion object {
        private val TAG: String = "MarkerDataSource"
        private var instance: MarkerDataSource? = null

        fun getInstance(): MarkerDataSource {
            if (instance == null) {
                instance = MarkerDataSource()
            }
            return instance!!
        }
    }
    private var data: MutableLiveData<MarkerModel> = MutableLiveData()

    override fun getMarkers(position: LatLng, callback: DataSource.LoadCallback) {
        // TODO: тут достаются нужные маркеры в data из базы
    }

    override fun addMarker(marker: MarkerModel, callback: DataSource.AddCallback) {
        // TODO: тут должна закидываться новая метка в базу
        data.postValue(marker)
    }
}