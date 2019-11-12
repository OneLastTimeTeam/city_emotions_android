package com.example.cityemotions.datasources

import com.example.cityemotions.datamodels.Emotion
import com.example.cityemotions.datamodels.MarkerModel
import com.google.android.gms.maps.model.LatLng
import kotlin.random.Random


/**
 *  DataSource implementation
 */
class MarkerDataSource {
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
//    init {
//        val emotionsArray = Emotion.values()
//        for (x in 0..10) {
//            val longtitude = Random.nextFloat() * 360.0 - 180.0
//            val latitude = Random.nextFloat() * 180.0 - 90.0
//            val emotion = emotionsArray[emotionsArray.indices.random()]
//            data.add(MarkerModel(latitude, longtitude, emotion))
//        }
//    }

    /**
     * GetMarkers from storage and put them in callback
     *
     * @param position current position on map
     * @param callback user`s implementation of DataSource.LoadCallback interface
     */
    fun getMarkers(position: LatLng, callback: LoadCallback) {
        callback.onLoad(data)
    }

    /**
     * Add marker to storage
     *
     * @param marker markerModel to add
     * @param callback user`s implementation of DataSource.AddCallback interface
     */
    fun addMarker(marker: MarkerModel, callback: AddCallback) {
        data.add(marker)
        callback.onAdd()
    }

    interface LoadCallback {
        fun onLoad(markers: MutableList<MarkerModel>)
        fun onError(t: Throwable)
    }

    interface AddCallback {
        fun onAdd()
        fun onError(t: Throwable)
    }
}