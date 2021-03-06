package com.example.cityemotions.usecases

import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.datasources.SQLiteDataSource
import com.google.android.gms.maps.model.LatLngBounds


/**
 * A class that allows you to get markers from the storage
 *
 * @property dataRepository instance of MarkerDataStorage
 * @constructor Creates new UseCase
 */
class GetMarkers(private val dataRepository: MarkerDataSource,
                 private val sqLiteDataSource: SQLiteDataSource):
    UseCase<GetMarkers.RequestValue, GetMarkers.ResponseValue>() {

    /**
     * Return markers from storage
     *
     * @param requestValue current position on map
     */
    override fun executeUseCase(requestValue: RequestValue?) {
        if (requestValue != null) {
            dataRepository.getMarkers(requestValue.bounds, object :
                MarkerDataSource.LoadCallback {
                override fun onLoad(markers: List<MarkerModel>) {
                    val responseValue =
                        ResponseValue(markers)
                    useCaseCallback?.onSuccess(responseValue)
                }

                override fun onError(t: Throwable) {
                    val localMarkers = sqLiteDataSource.getMarkers()
                    useCaseCallback?.onSuccess(ResponseValue(localMarkers))
                    useCaseCallback?.onError(t)
                }
            })
        }
    }

    class RequestValue(val bounds: LatLngBounds): UseCase.RequestValue
    class ResponseValue(val markers: List<MarkerModel>): UseCase.ResponseValue
}