package com.example.cityemotions.usecases

abstract class UseCase<Q: UseCase.RequestValue, P: UseCase.ResponseValue> {
    var requestValue: Q? = null

    var useCaseCallback: UseCaseCallback<P>? = null

    fun run() {
        executeUseCase(requestValue)
    }

    abstract fun executeUseCase(requestValue: Q?)

    interface RequestValue
    interface ResponseValue

    interface UseCaseCallback<R> {
        fun onSuccess(response: R)
        fun onError(t: Throwable)
    }
}