package com.example.cityemotions

import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.modelviews.ViewModelFactory
import com.example.cityemotions.usecases.AddMarker
import com.example.cityemotions.usecases.GetMarkers
import com.example.cityemotions.usecases.UseCaseHandler


/**
 *  Dependency injector.
 *  Provides all necessary object instances
 */
object Injector {
    fun provideViewModelFactory(): ViewModelFactory = ViewModelFactory.getInstance()

    fun provideMarkerDataSource(): MarkerDataSource = MarkerDataSource.getInstance()

    fun provideUseCaseHandler(): UseCaseHandler = UseCaseHandler.getInstance()

    fun provideGetMarkers(): GetMarkers = GetMarkers(provideMarkerDataSource())

    fun provideAddMarker(): AddMarker = AddMarker(provideMarkerDataSource())
}