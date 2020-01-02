package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.usecases.GetMarkers
import com.example.cityemotions.usecases.UseCase
import com.example.cityemotions.usecases.UseCaseHandler
import com.google.android.gms.maps.model.LatLngBounds


/**
 * Implementation of ViewModel, operates with work on the map
 *
 * @property getMarkers UseCase for getting markers
 * @property useCaseHandler handler for all UseCases
 */
class MapScreenViewModel(private val getMarkers: GetMarkers,
                         private val useCaseHandler: UseCaseHandler
): ViewModel() {
    /**
     * Return markers by position
     *
     * @param bounds bounds of visible piece of map
     * @param callback user`s callback implementation
     */
     fun getMarkers(bounds: LatLngBounds, callback: MarkerDataSource.LoadCallback) {
        val requestValue = GetMarkers.RequestValue(bounds)
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
}