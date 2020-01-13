package com.example.cityemotions

import android.content.Context
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.datasources.SQLiteDataSource
import com.example.cityemotions.modelviews.ViewModelFactory
import com.example.cityemotions.usecases.*


/**
 *  Dependency injector.
 *  Provides all necessary object instances
 */
object Injector {
    fun provideViewModelFactory(context: Context): ViewModelFactory
            = ViewModelFactory.getInstance(context)

    private fun provideMarkerDataSource(): MarkerDataSource = MarkerDataSource.getInstance()

    private fun provideSQLiteDataSource(context: Context): SQLiteDataSource
            = SQLiteDataSource.getInstance(context)

    fun provideUseCaseHandler(): UseCaseHandler = UseCaseHandler.getInstance()

    fun provideGetMarkers(context: Context): GetMarkers = GetMarkers(provideMarkerDataSource(),
        provideSQLiteDataSource(context))

    fun provideAddMarker(context: Context): AddMarker = AddMarker(provideMarkerDataSource(),
        provideSQLiteDataSource(context))

    fun provideRemoveMarker(context: Context): RemoveMarker = RemoveMarker(provideMarkerDataSource(),
        provideSQLiteDataSource(context))

    fun provideGetUsersMarker(): GetUsersMarkers = GetUsersMarkers(provideMarkerDataSource())

    fun provideGetUserStat(): GetUserStat = GetUserStat(provideMarkerDataSource())
}