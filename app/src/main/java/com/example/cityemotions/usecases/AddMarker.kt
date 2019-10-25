package com.example.cityemotions.usecases

import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.DataSource
import com.example.cityemotions.datasources.MarkerDataSource


class AddMarker (private val dataRepository: MarkerDataSource):
        UseCase<AddMarker.RequestValue, AddMarker.ResponseValue>() {
    companion object {
        private val TAG: String = "AddMarkers"
    }

    override fun executeUseCase(requestValue: RequestValue?) {
        if (requestValue != null) {
            dataRepository.addMarker(requestValue.marker, object :
                DataSource.AddCallback {
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