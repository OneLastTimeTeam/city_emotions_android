package com.example.cityemotions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cityemotions.fragments.MapScreenFragment


class MapsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapScreenFragment.getInstance())
                .commit()
        }
    }
}
