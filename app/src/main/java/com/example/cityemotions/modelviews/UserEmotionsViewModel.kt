package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.usecases.AddMarker
import com.example.cityemotions.usecases.RemoveMarker
import com.example.cityemotions.usecases.UseCase
import com.example.cityemotions.usecases.UseCaseHandler
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Implementation of ViewModel, operates with work on the map
 *
 * @property removeMarker UseCase for adding markers
 * @property useCaseHandler handler for all UseCases
 */
class UserEmotionsViewModel(private val removeMarker: RemoveMarker,
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
    suspend fun removeMarker(marker: MarkerModel, callback: MarkerDataSource.RemoveCallback) {
        coroutineContext.cancelChildren()
        launch {
            val requestValue = RemoveMarker.RequestValue(marker)

            useCaseHandler.execute(removeMarker, requestValue, object :
                UseCase.UseCaseCallback<RemoveMarker.ResponseValue> {
                override fun onSuccess(response: RemoveMarker.ResponseValue) {
                    callback.onRemove()
                }

                override fun onError(t: Throwable) {
                    callback.onError(t)
                }
            })
        }
    }
}