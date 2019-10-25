package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.usecases.AddMarker
import com.example.cityemotions.datasources.DataSource
import com.example.cityemotions.usecases.GetMarkers
import com.example.cityemotions.usecases.UseCase
import com.example.cityemotions.usecases.UseCaseHandler
import com.google.android.gms.maps.model.LatLng


class MapScreenViewModel(private val getMarkers: GetMarkers,
                         private val addMarker: AddMarker,
                         private val useCaseHandler: UseCaseHandler
): ViewModel() {
    fun getMarkers(position: LatLng, callback: DataSource.LoadCallback) {
        val requestValue = GetMarkers.RequestValue(position)
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

    fun addMarker(marker: MarkerModel, callback: DataSource.AddCallback) {
        val requestValue = AddMarker.RequestValue(marker)

        useCaseHandler.execute(addMarker, requestValue, object :
        UseCase.UseCaseCallback<AddMarker.ResponseValue> {
            override fun onSuccess(response: AddMarker.ResponseValue) {
                callback.onAdd()
            }

            override fun onError(t: Throwable) {
                callback.onError(t)
            }
        })
    }
}