package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.usecases.GetUserStat
import com.example.cityemotions.usecases.UseCase
import com.example.cityemotions.usecases.UseCaseHandler


class ProfileViewModel(private val getUserStat: GetUserStat,
                       private val useCaseHandler: UseCaseHandler): ViewModel() {
    /**
     * Get user`s emotions stat from db
     *
     * @param userId user identificator string
     * @param callback user`s callback implementation
     */
    fun getUsersMarkers(userId: String, callback: MarkerDataSource.StatLoadCallback) {
        val requestValue = GetUserStat.RequestValue(userId)

        useCaseHandler.execute(getUserStat, requestValue, object :
            UseCase.UseCaseCallback<GetUserStat.ResponseValue> {
            override fun onSuccess(response: GetUserStat.ResponseValue) {
                callback.onLoad(response.stat)
            }

            override fun onError(t: Throwable) {
                callback.onError(t)
            }
        })
    }
}