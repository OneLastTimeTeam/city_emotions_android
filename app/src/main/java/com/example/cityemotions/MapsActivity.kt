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


interface OnSelectProfileListener {
    fun onProfileSelected()
}

interface OnSelectFilterListener {
    fun onFilterSelected()
}

interface OnMarkerClicker {
    fun onMarkerClicked(position: LatLng)
}

interface  OnEmotionsClicker {
    fun onEmotionsClicked()
}

interface OnSignInListener {
    fun onSignIn()
}

interface OnLogOutListener {
    fun onLogOut()
}


class MapsActivity : AppCompatActivity(), OnSelectProfileListener, OnSelectFilterListener, OnMarkerClicker,
    OnEmotionsClicker, OnSignInListener, OnLogOutListener {

    companion object {
        const val EMOTION_PREFERENCES = "emotion_preferences"
    }

    private val RC_SIGN_IN = 24
    private lateinit var client: GoogleSignInClient
    private lateinit var account: GoogleSignInAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
        val lastAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastAccount != null) {
            account = lastAccount
            logIn()
        }
    }

    override fun onProfileSelected() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfileFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onFilterSelected() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FilterFragment())
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

    /**
     * Get user`s MarkerModel from storage
     *
     * @param task sign-in Google API task
     */
    private fun handleSignIn(task: Task<GoogleSignInAccount>) {
        try {
            val userAccount = task.getResult(ApiException::class.java)
            if (userAccount != null) {
                account = userAccount
                logIn()
            }
        } catch (t: ApiException) {
            Log.e("AUTH", null, t)
        }
    }

    /**
     * Redirecting to map fragment
     */
    private fun logIn() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MapScreenFragment())
            .commit()
    }

    override fun onLogOut() {
        client.signOut()
            .addOnCompleteListener {
                val intent = Intent(this, MapsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
    }

    /**
     * User ID string getter
     *
     * @return user ID string
     */
    fun getUserId(): String {
        return account.id!!
    }

    /**
     * Get shared preferences tag
     *
     * @return shared preferences string
     */
    fun getSharedPreferencesTag(): String {
        return EMOTION_PREFERENCES + getUserId()
    }
}
