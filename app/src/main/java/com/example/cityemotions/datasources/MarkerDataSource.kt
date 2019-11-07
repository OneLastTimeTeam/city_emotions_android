package com.example.cityemotions.datasources

import com.example.cityemotions.datamodels.Emotion
import com.example.cityemotions.datamodels.MarkerModel
import com.google.android.gms.maps.model.LatLng


/**
 *  DataSource implementation
 */
class MarkerDataSource : DataSource {
    companion object {
        private var instance: MarkerDataSource? = null

        fun getInstance(): MarkerDataSource {
            if (instance == null) {
                instance = MarkerDataSource()
            }
            return instance!!
        }
    }

    // Temporary solution
    private var data: MutableList<MarkerModel> = mutableListOf()
    init {
        data.add(MarkerModel(10.0, 10.0, Emotion.HAPPY))
        data.add(MarkerModel(20.0, 20.0, Emotion.HAPPY))
        data.add(MarkerModel(30.0, 30.0, Emotion.HAPPY))
    }

    /**
     * GetMarkers from storage and put them in callback
     *
     * @param position current position on map
     * @param callback user`s implementation of DataSource.LoadCallback interface
     */
    override fun getMarkers(position: LatLng, callback: DataSource.LoadCallback) {
        callback.onLoad(data)
    }

    /**
     * Add marker to storage
     *
     * @param marker markerModel to add
     * @param callback user`s implementation of DataSource.AddCallback interface
     */
    override fun addMarker(marker: MarkerModel, callback: DataSource.AddCallback) {
        data.add(marker)
        callback.onAdd()
    }
}