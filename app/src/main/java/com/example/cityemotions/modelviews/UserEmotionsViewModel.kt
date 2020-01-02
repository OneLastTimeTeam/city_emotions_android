package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.usecases.*


/**
 * Implementation of ViewModel, operates with work on the map
 *
 * @property removeMarker UseCase for adding markers
 * @property useCaseHandler handler for all UseCases
 */
class UserEmotionsViewModel(private val removeMarker: RemoveMarker,
                            private val getUsersMarkers: GetUsersMarkers,
                            private val useCaseHandler: UseCaseHandler
): ViewModel() {
    /**
     * Remove MarkerModel from storage
     *
     * @param marker marker model to remove
     * @param callback user`s callback implementation
     */
     fun removeMarker(marker: MarkerModel, callback: MarkerDataSource.RemoveCallback) {
        val requestValue = RemoveMarker.RequestValue(marker)

        useCaseHandler.execute(removeMarker, requestValue, object :
            UseCase.UseCaseCallback<RemoveMarker.ResponseValue> {
            override fun onSuccess(response: RemoveMarker.ResponseValue) {
                callback.onRemove()
            }

            override fun onError(t: Throwable) {
                callback.onError(t)
            }
        })
    }

    /**
     * Get user`s MarkerModel from storage
     *
     * @param marker marker model to get
     * @param callback user`s callback implementation
     */
     fun getUsersMarkers(callback: MarkerDataSource.LoadCallback) {
        val requestValue = GetUsersMarkers.RequestValue()

        useCaseHandler.execute(getUsersMarkers, requestValue, object :
            UseCase.UseCaseCallback<GetUsersMarkers.ResponseValue> {
            override fun onSuccess(response: GetUsersMarkers.ResponseValue) {
                callback.onLoad(response.markers)
            }

            override fun onError(t: Throwable) {
                callback.onError(t)
            }
        })
    }
}