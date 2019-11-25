package com.example.cityemotions.modelviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cityemotions.Injector


/**
 * Factory produces ViewModels objects
 */
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

    /**
     * Function which creates ViewModel class object by class name
     *
     * @param T ViewModel class type
     * @param modelClass java class name of ViewModel class
     * @return ViewModel object
     */
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == MapScreenViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return MapScreenViewModel(Injector.provideGetMarkers(),
                Injector.provideUseCaseHandler()) as T
        } else if (modelClass == NewMarkerScreenViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return NewMarkerScreenViewModel(Injector.provideAddMarker(),
                Injector.provideUseCaseHandler()) as T
        } else if (modelClass == UserEmotionsViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return UserEmotionsViewModel(Injector.provideRemoveMarker(),
                Injector.provideGetUsersMarker(),
                Injector.provideUseCaseHandler()) as T
        }
        throw IllegalArgumentException("Unknown class $modelClass")
    }
}