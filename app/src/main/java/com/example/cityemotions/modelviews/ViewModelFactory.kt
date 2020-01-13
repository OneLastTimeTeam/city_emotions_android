package com.example.cityemotions.modelviews

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cityemotions.Injector


/**
 * Factory produces ViewModels objects
 */
class ViewModelFactory(private val context: Context): ViewModelProvider.Factory {
    companion object {
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            if (instance == null) {
                instance = ViewModelFactory(context)

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
        when (modelClass) {
            MapScreenViewModel::class.java -> {
                @Suppress("UNCHECKED_CAST")
                return MapScreenViewModel(
                    Injector.provideGetMarkers(context),
                    Injector.provideUseCaseHandler()
                ) as T
            }
            NewMarkerScreenViewModel::class.java -> {
                @Suppress("UNCHECKED_CAST")
                return NewMarkerScreenViewModel(
                    Injector.provideAddMarker(context),
                    Injector.provideUseCaseHandler()
                ) as T
            }
            UserEmotionsViewModel::class.java -> {
                @Suppress("UNCHECKED_CAST")
                return UserEmotionsViewModel(
                    Injector.provideRemoveMarker(context),
                    Injector.provideGetUsersMarker(),
                    Injector.provideUseCaseHandler()
                ) as T
            }
            ProfileViewModel::class.java -> {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(
                    Injector.provideGetUserStat(),
                    Injector.provideUseCaseHandler()
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown class $modelClass")
        }
    }
}