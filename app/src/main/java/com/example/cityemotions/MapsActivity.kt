package com.example.cityemotions

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import com.example.cityemotions.fragments.*
import com.google.android.gms.maps.model.LatLng


interface OnSelectProfile {
    fun onProfileSelected()
}

interface OnMarkerClicker {
    fun onMarkerClicked(position: LatLng)
}

interface  OnEmotionsClicker {
    fun onEmotionsClicked()
}

interface OnLoginClicker {
    fun onLoginClicked()
}


class MapsActivity : AppCompatActivity(), OnSelectProfile, OnMarkerClicker, OnEmotionsClicker,
    OnLoginClicker{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    override fun onProfileSelected() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfileFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onMarkerClicked(position: LatLng) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NewMarkerFragment.createFragment(position))
            .addToBackStack(null)
            .commit()
    }

    override fun onEmotionsClicked() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, UserEmotionsFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onLoginClicked() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MapScreenFragment())
            .addToBackStack(null)
            .commit()
    }
}
