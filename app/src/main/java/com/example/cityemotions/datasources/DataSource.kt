package com.example.cityemotions.datasources

import com.example.cityemotions.datamodels.MarkerModel
import com.google.android.gms.maps.model.LatLng


/**
 * DataSource interface for working with markers
 */
interface DataSource {
    interface LoadCallback {
        fun onLoad(markers: MutableList<MarkerModel>)
        fun onError(t: Throwable)
    }

    interface AddCallback {
        fun onAdd()
        fun onError(t: Throwable)
    }

    fun getMarkers(position: LatLng, callback: LoadCallback)
    fun addMarker(marker: MarkerModel, callback: AddCallback)
}