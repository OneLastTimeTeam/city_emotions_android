package com.example.cityemotions

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.cityemotions.fragments.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task


interface OnSelectProfile {
    fun onProfileSelected()
}

interface OnMarkerClicker {
    fun onMarkerClicked(position: LatLng)
}

interface  OnEmotionsClicker {
    fun onEmotionsClicked()
}

interface OnSignInClicker {
    fun onSignIn()
}


class MapsActivity : AppCompatActivity(), OnSelectProfile, OnMarkerClicker, OnEmotionsClicker,
    OnSignInClicker {

    private val RC_SIGN_IN = 24
    private lateinit var client: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        client = GoogleSignIn.getClient(this, gso)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            logIn()
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

    override fun onSignIn() {
        val intent = client.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignIn(task)
        }
    }

    private fun handleSignIn(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                logIn()
            }
        } catch (t: ApiException) {
            Log.e("AUTH", null, t)
        }
    }

    private fun logIn() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MapScreenFragment())
            .commit()
    }
}
