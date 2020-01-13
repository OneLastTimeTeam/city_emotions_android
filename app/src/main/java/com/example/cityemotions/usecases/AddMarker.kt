package com.example.cityemotions.usecases

import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.datasources.SQLiteDataSource


/**
 * A class that allows you to add new marker to the storage
 *
 * @property dataRepository instance of MarkerDataStorage
 * @constructor Creates new UseCase
 */
class AddMarker (private val dataRepository: MarkerDataSource,
                 private val sqLiteRepository: SQLiteDataSource):
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
                override fun onAdd(marker: MarkerModel) {
                    sqLiteRepository.addMarker(marker)
                    useCaseCallback?.onSuccess(ResponseValue(marker))
                }

                override fun onError(t: Throwable) {
                    useCaseCallback?.onError(t)
                }
            })
        }
    }

    class RequestValue(val marker: MarkerModel): UseCase.RequestValue
    class ResponseValue(val marker: MarkerModel): UseCase.ResponseValue
}