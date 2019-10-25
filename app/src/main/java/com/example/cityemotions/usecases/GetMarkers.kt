package com.example.cityemotions.usecases

import androidx.lifecycle.MutableLiveData
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.DataSource
import com.example.cityemotions.datasources.MarkerDataSource
import com.google.android.gms.maps.model.LatLng


class GetMarkers(private val dataRepository: MarkerDataSource):
    UseCase<GetMarkers.RequestValue, GetMarkers.ResponseValue>() {
    companion object {
        private val TAG: String = "GetMarkers"
    }

    override fun executeUseCase(requestValue: RequestValue?) {
        if (requestValue != null) {
            dataRepository.getMarkers(requestValue.position, object :
                DataSource.LoadCallback {
                override fun onLoad(markers: MutableLiveData<MarkerModel>) {
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
    class ResponseValue(val markers: MutableLiveData<MarkerModel>): UseCase.ResponseValue
}