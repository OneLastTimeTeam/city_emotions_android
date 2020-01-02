package com.example.cityemotions.usecases

import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * A class providing a single interface for launching UseCases
 */
class UseCaseHandler {
    private val executor: Executor = Executors.newSingleThreadExecutor()

    /**
     * Function, which starts any UseCase with request and response values and callbacks
     *
     * @param useCase an object that is a descendant of an abstract class UseCase
     * @param values requestValue for this UseCase
     * @param callback callback, which works with response
     */
    fun <T: UseCase.RequestValue, R: UseCase.ResponseValue> execute(
        useCase: UseCase<T, R>, values: T, callback: UseCase.UseCaseCallback<R>) {
        useCase.requestValue = values
        useCase.useCaseCallback = callback
        executor.execute {
            useCase.run()
        }
    }

    companion object {
        private var instance: UseCaseHandler? = null

        fun getInstance(): UseCaseHandler {
            if (instance == null) {
                instance = UseCaseHandler()
            }
            return instance!!
        }
    }
}