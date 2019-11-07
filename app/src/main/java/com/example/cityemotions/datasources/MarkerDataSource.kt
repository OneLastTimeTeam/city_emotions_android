package com.example.cityemotions.datasources

import com.example.cityemotions.datamodels.Emotion
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
    private var data: MutableList<MarkerModel> = mutableListOf()
    init {
        data.add(MarkerModel(10.0, 10.0, Emotion.HAPPY))
        data.add(MarkerModel(20.0, 20.0, Emotion.HAPPY))
        data.add(MarkerModel(30.0, 30.0, Emotion.HAPPY))
    }

    override fun getMarkers(position: LatLng, callback: DataSource.LoadCallback) {
        callback.onLoad(data)
    }

    override fun addMarker(marker: MarkerModel, callback: DataSource.AddCallback) {
        data.add(marker)
        callback.onAdd()
    }
}