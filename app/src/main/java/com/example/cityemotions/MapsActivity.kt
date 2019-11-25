package com.example.cityemotions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cityemotions.fragments.MapScreenFragment
import com.example.cityemotions.fragments.NewMarkerFragment
import com.example.cityemotions.fragments.ProfileFragment
import com.example.cityemotions.fragments.UserEmotionsFragment
import com.example.cityemotions.modelviews.UserEmotionsViewModel
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


class MapsActivity : AppCompatActivity(), OnSelectProfile, OnMarkerClicker, OnEmotionsClicker {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapScreenFragment())
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
}
