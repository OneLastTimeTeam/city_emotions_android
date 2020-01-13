package com.example.cityemotions.usecases

import com.example.cityemotions.datamodels.EmotionStat
import com.example.cityemotions.datasources.MarkerDataSource


class GetUserStat(private val dataRepository: MarkerDataSource):
    UseCase<GetUserStat.RequestValue, GetUserStat.ResponseValue>() {

    /**
     * Return user`s emotions stat from db
     */
    override fun executeUseCase(requestValue: RequestValue?) {
        if (requestValue != null) {
            dataRepository.getUserStat(requestValue.userId, object :
            MarkerDataSource.StatLoadCallback {
                override fun onLoad(stat: List<EmotionStat>) {
                    val responseValue = ResponseValue(stat)
                    useCaseCallback?.onSuccess(responseValue)
                }

                override fun onError(t: Throwable) {
                    useCaseCallback?.onError(t)
                }
            })
        }
    }

    class RequestValue(val userId: String): UseCase.RequestValue
    class ResponseValue(val stat: List<EmotionStat>): UseCase.ResponseValue
}