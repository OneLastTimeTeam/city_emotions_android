package com.example.cityemotions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cityemotions.fragments.MapScreenFragment
import com.example.cityemotions.fragments.ProfileFragment


interface OnSelectProfile {
    fun onProfileSelected()
}


class MapsActivity : AppCompatActivity(), OnSelectProfile {

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
}
