package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cityemotions.Injector


class ViewModelFactory : ViewModelProvider.Factory {
    companion object {
        private var instance: ViewModelFactory? = null

        fun getInstance(): ViewModelFactory {
            if (instance == null) {
                instance = ViewModelFactory()
            }
            return instance!!
        }
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == MapScreenViewModel::class.java) {
            return MapScreenViewModel(Injector.provideGetMarkers(),
                Injector.provideAddMarker(),
                Injector.provideUseCaseHandler()) as T
        }
        throw IllegalArgumentException("Unknown class $modelClass")
    }
}