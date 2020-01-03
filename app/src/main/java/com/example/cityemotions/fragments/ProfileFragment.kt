package com.example.cityemotions.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.cityemotions.OnEmotionsClicker
import com.example.cityemotions.OnLogOutListener
import com.example.cityemotions.R


/**
 * User`s profile fragment
 */
class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.user_emotions_button).setOnClickListener {
            (activity as OnEmotionsClicker).onEmotionsClicked()
        }

        view.findViewById<Button>(R.id.logout_button).setOnClickListener {
            (activity as OnLogOutListener).onLogOut()
        }
    }
}