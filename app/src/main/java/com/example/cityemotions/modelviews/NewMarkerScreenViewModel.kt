package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.usecases.AddMarker
import com.example.cityemotions.usecases.UseCase
import com.example.cityemotions.usecases.UseCaseHandler
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


/**
 * Implementation of ViewModel, operates with work on the map
 *
 * @property addMarker UseCase for adding markers
 * @property useCaseHandler handler for all UseCases
 */
class NewMarkerScreenViewModel(private val addMarker: AddMarker,
                               private val useCaseHandler: UseCaseHandler
): ViewModel(), CoroutineScope {
    private val job: Job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    /**
     * Add MarkerModel to storage
     *
     * @param marker marker model to add
     * @param callback user`s callback implementation
     */
    suspend fun addMarker(marker: MarkerModel, callback: MarkerDataSource.AddCallback) {
        coroutineContext.cancelChildren()
        launch {
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
}