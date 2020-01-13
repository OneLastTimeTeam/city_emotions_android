package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.usecases.AddMarker
import com.example.cityemotions.usecases.UseCase
import com.example.cityemotions.usecases.UseCaseHandler


/**
 * Implementation of ViewModel, operates with work on the map
 *
 * @property addMarker UseCase for adding markers
 * @property useCaseHandler handler for all UseCases
 */
class NewMarkerScreenViewModel(private val addMarker: AddMarker,
                               private val useCaseHandler: UseCaseHandler
): ViewModel() {
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
                callback.onAdd(response.marker)
            }

            override fun onError(t: Throwable) {
                callback.onError(t)
            }
        })
    }
}