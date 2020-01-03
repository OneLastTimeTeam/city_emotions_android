package com.example.cityemotions.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cityemotions.Injector
import com.example.cityemotions.MapsActivity
import com.example.cityemotions.R
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.modelviews.UserEmotionsViewModel


/**
 * Fragment for manipulating user emotions: viewing, deleting
 */
class UserEmotionsFragment: Fragment() {
    /** RecycleView adapter */
    private lateinit var dataAdapter: EmotionAdapter

    /** ViewModel class to work with markers storage */
    lateinit var userEmotionsViewModel: UserEmotionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataAdapter = EmotionAdapter(this)
        val factory = Injector.provideViewModelFactory()
        userEmotionsViewModel = factory.create(UserEmotionsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.emotions_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.user_emotions_list)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = dataAdapter
        val userId = (activity as MapsActivity).getUserId()
        userEmotionsViewModel.getUsersMarkers(userId, object : MarkerDataSource.LoadCallback {
            override fun onLoad(markers: List<MarkerModel>) {
                activity?.runOnUiThread {
                    markers.forEach {
                        dataAdapter.markersList.add(it)
                        dataAdapter.notifyItemInserted(dataAdapter.markersList.size - 1)
                    }
                }
            }

            override fun onError(t: Throwable) {
                Log.e("LoadCallback", null, t)
            }
        })
    }
}


/**
 * DataAdapter class implementation for user`s emotions list
 */
class EmotionAdapter(private val userEmotionFragment: UserEmotionsFragment)
    : RecyclerView.Adapter<UserEmotionViewHolder>() {

    val markersList: MutableList<MarkerModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserEmotionViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.user_emotion, parent, false)
        return UserEmotionViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserEmotionViewHolder, position: Int) {
        val marker = markersList[position]
        holder.imageView.setImageResource(marker.emotion.resId)
        holder.textView.text = marker.description
        holder.deleteButton.tag = marker.dbId
        holder.deleteButton.setOnClickListener {
            val holderPosition = markersList.indexOfFirst { markerModel -> markerModel.dbId == it.tag }
            userEmotionFragment.userEmotionsViewModel.removeMarker(markersList[holderPosition],
                object : MarkerDataSource.RemoveCallback {
                    override fun onRemove() {
                        userEmotionFragment.activity?.runOnUiThread {
                            markersList.removeAt(holderPosition)
                            notifyItemRemoved(holderPosition)
                        }
                    }

                    override fun onError(t: Throwable) {
                        Log.e("RemoveCallback", null, t)
                    }
                })
        }
    }

    override fun getItemCount(): Int {
        return markersList.size
    }
}


/**
 * ViewHolder class implementation for each user`s emotion
 */
class UserEmotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.emotion_image)
    val textView: TextView = itemView.findViewById(R.id.emotion_address)
    val deleteButton: ImageButton = itemView.findViewById(R.id.delete)
}
