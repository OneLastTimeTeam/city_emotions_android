package com.example.cityemotions.fragments

import android.location.Geocoder
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
import com.example.cityemotions.R
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.modelviews.UserEmotionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class UserEmotionsFragment: Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private lateinit var dataAdapter: EmotionAdapter

    lateinit var userEmotionsViewModel: UserEmotionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataAdapter = EmotionAdapter(Geocoder(activity), this)
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
        launch {
            userEmotionsViewModel.getUsersMarkers(object : MarkerDataSource.LoadCallback {
                override fun onLoad(markers: MutableList<MarkerModel>) {
                    markers.forEach {
                        dataAdapter.emotionsList.add(it)
                        dataAdapter.notifyItemInserted(dataAdapter.emotionsList.size - 1)
                    }
                }

                override fun onError(t: Throwable) {
                    Log.e("LoadCallback", null, t)
                }
            })
        }
    }
}

class EmotionAdapter(private val geocoder: Geocoder,
                     private val userEmotionFragment: UserEmotionsFragment)
    : RecyclerView.Adapter<UserEmotionViewHolder>(), CoroutineScope {

    val emotionsList: MutableList<MarkerModel> = mutableListOf()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserEmotionViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.user_emotion, parent, false)
        return UserEmotionViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserEmotionViewHolder, position: Int) {
        val emotion = emotionsList[position]
        holder.imageView.setImageResource(emotion.emotion.resId)
        val longtitude = emotionsList[position].longtitude
        val latitude = emotionsList[position].latitude
        try {
            val addresses = geocoder.getFromLocation(latitude, longtitude, 1)
            if (addresses != null && addresses.size != 0) {
                val address = addresses[0]
                val addressFragments = with(address) {
                    (0..maxAddressLineIndex).map { getAddressLine(it) }
                }
                holder.textView.text = addressFragments.joinToString(separator = " ")
            } else {
                throw IOException()
            }
        } catch (_: IOException) {
            holder.textView.text = "${longtitude}, ${latitude}"
        }

        holder.deleteButton.setOnClickListener {
            launch {
                userEmotionFragment.userEmotionsViewModel.removeMarker(emotionsList[position],
                    object : MarkerDataSource.RemoveCallback {
                        override fun onRemove() {
                            emotionsList.removeAt(holder.adapterPosition)
                            notifyItemRemoved(holder.adapterPosition)
                        }

                        override fun onError(t: Throwable) {
                            Log.e("RemoveCallback", null, t)
                        }
                    })
            }

        }
    }

    override fun getItemCount(): Int {
        return emotionsList.size
    }
}

class UserEmotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.emotion_image)
    val textView: TextView = itemView.findViewById(R.id.emotion_address)
    val deleteButton: ImageButton = itemView.findViewById(R.id.delete)
}
