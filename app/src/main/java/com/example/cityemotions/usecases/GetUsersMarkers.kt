package com.example.cityemotions.usecases

import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource


/**
 * A class that allows you to get user`s markers from the storage
 *
 * @property dataRepository instance of MarkerDataStorage
 * @constructor Creates new UseCase
 */
class GetUsersMarkers(private val dataRepository: MarkerDataSource):
    UseCase<GetUsersMarkers.RequestValue, GetUsersMarkers.ResponseValue>() {

    /**
     * Return user`s markers from storage
     */
    override fun executeUseCase(requestValue: RequestValue?) {
        if (requestValue != null) {
            dataRepository.getUserMarkers(object :
                MarkerDataSource.LoadCallback {
                override fun onLoad(markers: List<MarkerModel>) {
                    val responseValue =
                        ResponseValue(markers)
                    useCaseCallback?.onSuccess(responseValue)
                }

                override fun onError(t: Throwable) {
                    useCaseCallback?.onError(t)
                }
            })
        }
    }

    class RequestValue: UseCase.RequestValue
    class ResponseValue(val markers: List<MarkerModel>): UseCase.ResponseValue
}