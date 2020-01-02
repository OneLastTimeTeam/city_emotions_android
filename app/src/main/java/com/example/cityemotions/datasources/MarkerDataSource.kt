package com.example.cityemotions.datasources

import com.example.cityemotions.datamodels.Emotion
import com.example.cityemotions.datamodels.MarkerModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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

    /**
     * GetMarkers from storage and put them in callback
     *
     * @param bounds bounds of visible piece of map
     * @param callback user`s implementation of DataSource.LoadCallback interface
     */
    fun getMarkers(bounds: LatLngBounds, callback: LoadCallback) {
        val markersToSend = data.filter { bounds.contains(LatLng(it.latitude, it.longtitude)) }
        callback.onLoad(markersToSend)
    }

    /**
     * Get user`s markers from storage
     *
     * @param callback user`s implementation of DataSource.LoadCallback interface
     */
    fun getUserMarkers(callback: LoadCallback) {
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

    /**
     * Remove marker from storage
     *
     * @param marker markerModel to remove
     * @param callback user`s implementation of DataSource.RemoveCallback interface
     */
    fun removeMarker(marker: MarkerModel, callback: RemoveCallback) {
        data.remove(marker)
        callback.onRemove()
    }

    interface LoadCallback {
        fun onLoad(markers: List<MarkerModel>)
        fun onError(t: Throwable)
    }

    interface AddCallback {
        fun onAdd()
        fun onError(t: Throwable)
    }

    interface RemoveCallback {
        fun onRemove()
        fun onError(t: Throwable)
    }
}