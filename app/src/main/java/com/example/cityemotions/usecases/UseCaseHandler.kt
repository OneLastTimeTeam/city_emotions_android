package com.example.cityemotions.usecases


class UseCaseHandler {
    fun <T: UseCase.RequestValue, R: UseCase.ResponseValue> execute(
        useCase: UseCase<T, R>, values: T, callback: UseCase.UseCaseCallback<R>) {
        useCase.requestValue = values
        useCase.useCaseCallback = callback

        useCase.run()
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