package com.example.cityemotions.usecases

import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource


/**
 * A class that allows you to add new marker to the storage
 *
 * @property dataRepository instance of MarkerDataStorage
 * @constructor Creates new UseCase
 */
class AddMarker (private val dataRepository: MarkerDataSource):
        UseCase<AddMarker.RequestValue, AddMarker.ResponseValue>() {
    /**
     * Add new marker to the storage
     *
     * @param requestValue MarkerModel object to add
     */
    override fun executeUseCase(requestValue: RequestValue?) {
        if (requestValue != null) {
            dataRepository.addMarker(requestValue.marker, object :
                MarkerDataSource.AddCallback {
                override fun onAdd() {
                    useCaseCallback?.onSuccess(ResponseValue())
                }

                override fun onError(t: Throwable) {
                    useCaseCallback?.onError(t)
                }
            })
        }
    }

    class RequestValue(val marker: MarkerModel): UseCase.RequestValue
    class ResponseValue: UseCase.ResponseValue
}