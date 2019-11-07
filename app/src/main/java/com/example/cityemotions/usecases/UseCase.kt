package com.example.cityemotions.usecases


/**
 * Abstract UseCase class. Simulates a scenario using the interaction of architecture components,
 * like "getSomethingFromStorage, addNewElements, otherRequests"
 *
 * @param Q request value type
 * @param P response value type
 */
abstract class UseCase<Q: UseCase.RequestValue, P: UseCase.ResponseValue> {
    var requestValue: Q? = null

    /** Custom user`s callback */
    var useCaseCallback: UseCaseCallback<P>? = null

    /**
     * Function that runs any UseCase with response and request values and callbacks
     * Provides a single startup script for each UseCase
     */
    fun run() {
        executeUseCase(requestValue)
    }

    /**
     * Function with custom usage UseCase logic
     *
     * @param requestValue value to process
     */
    abstract fun executeUseCase(requestValue: Q?)

    /** Interfaces for request and response values */
    interface RequestValue
    interface ResponseValue

    /** Interface for user`s callbacks implementations */
    interface UseCaseCallback<R> {
        fun onSuccess(response: R)
        fun onError(t: Throwable)
    }
}