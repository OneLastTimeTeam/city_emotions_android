package com.example.cityemotions.usecases

import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.google.android.gms.maps.model.LatLng


/**
 * A class that allows you to get markers from the storage
 *
 * @property dataRepository instance of MarkerDataStorage
 * @constructor Creates new UseCase
 */
class GetMarkers(private val dataRepository: MarkerDataSource):
    UseCase<GetMarkers.RequestValue, GetMarkers.ResponseValue>() {

    /**
     * Return markers from storage
     *
     * @param requestValue current position on map
     */
    override fun executeUseCase(requestValue: RequestValue?) {
        if (requestValue != null) {
            dataRepository.getMarkers(requestValue.position, object :
                MarkerDataSource.LoadCallback {
                override fun onLoad(markers: MutableList<MarkerModel>) {
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

    class RequestValue(val position: LatLng): UseCase.RequestValue
    class ResponseValue(val markers: MutableList<MarkerModel>): UseCase.ResponseValue
}