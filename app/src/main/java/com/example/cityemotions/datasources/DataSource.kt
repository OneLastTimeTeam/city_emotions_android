package com.example.cityemotions.datasources

import androidx.lifecycle.MutableLiveData
import com.example.cityemotions.datamodels.MarkerModel
import com.google.android.gms.maps.model.LatLng


interface DataSource {
    interface LoadCallback {
        fun onLoad(markers: MutableLiveData<MarkerModel>)
        fun onError(t: Throwable)
    }

    interface AddCallback {
        fun onAdd()
        fun onError(t: Throwable)
    }

    fun getMarkers(position: LatLng, callback: LoadCallback)
    fun addMarker(marker: MarkerModel, callback: AddCallback)
}