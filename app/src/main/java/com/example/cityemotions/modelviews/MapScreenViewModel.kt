package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.usecases.AddMarker
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.usecases.GetMarkers
import com.example.cityemotions.usecases.UseCase
import com.example.cityemotions.usecases.UseCaseHandler
import com.google.android.gms.maps.model.LatLng


/**
 * Implementation of ViewModel, operates with work on the map
 *
 * @property getMarkers UseCase for getting markers
 * @property addMarker UseCase for adding markers
 * @property useCaseHandler handler for all UseCases
 */
class MapScreenViewModel(private val getMarkers: GetMarkers,
                         private val addMarker: AddMarker,
                         private val useCaseHandler: UseCaseHandler
): ViewModel() {
    /**
     * Return markers by position
     *
     * @param position current position on Map
     * @param callback user`s callback implementation
     */
    fun getMarkers(position: LatLng, callback: MarkerDataSource.LoadCallback) {
        val requestValue = GetMarkers.RequestValue(position)
        useCaseHandler.execute(getMarkers, requestValue, object :
            UseCase.UseCaseCallback<GetMarkers.ResponseValue> {
            override fun onSuccess(response: GetMarkers.ResponseValue) {
                callback.onLoad(response.markers)
            }

            override fun onError(t: Throwable) {
                callback.onError(t)
            }
        })
    }

    /**
     * Add MarkerModel to storage
     *
     * @param marker marker model to add
     * @param callback user`s callback implementation
     */
    fun addMarker(marker: MarkerModel, callback: MarkerDataSource.AddCallback) {
        val requestValue = AddMarker.RequestValue(marker)

        useCaseHandler.execute(addMarker, requestValue, object :
        UseCase.UseCaseCallback<AddMarker.ResponseValue> {
            override fun onSuccess(response: AddMarker.ResponseValue) {
                callback.onAdd()
            }

            override fun onError(t: Throwable) {
                callback.onError(t)
            }
        })
    }
}