package com.example.cityemotions.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.cityemotions.OnEmotionsClicker
import com.example.cityemotions.OnLogOutListener
import com.example.cityemotions.R


/**
 * User`s profile fragment
 */
class ProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_screen, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
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