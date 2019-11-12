package com.example.cityemotions.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cityemotions.Injector
import com.example.cityemotions.R
import com.example.cityemotions.datamodels.Emotion
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.modelviews.NewMarkerScreenViewModel
import com.google.android.gms.maps.model.LatLng

class NewMarkerFragment: Fragment() {
    companion object {
        const val SAVED_LONGTITUDE: String = "saved.longtitude"
        const val SAVED_LATITUDE: String = "saved.latitude"

        fun createFragment(latLng: LatLng): NewMarkerFragment {
            val fragment = NewMarkerFragment()

            fragment.apply { arguments = Bundle().apply {
                    putDouble(SAVED_LATITUDE, latLng.latitude)
                    putDouble(SAVED_LONGTITUDE, latLng.longitude)
                }
            }
            return fragment
        }
    }

    private lateinit var dataAdapter: EmotionAdapter
    private var longtitude: Double = 0.0
    private var latitude: Double = 0.0

    private lateinit var newMarkerScreenViewModel: NewMarkerScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataAdapter = EmotionAdapter()
        val factory = Injector.provideViewModelFactory()
        newMarkerScreenViewModel = factory.create(NewMarkerScreenViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.new_marker_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        longtitude = arguments?.getDouble(SAVED_LONGTITUDE) as Double
        latitude = arguments?.getDouble(SAVED_LATITUDE) as Double

        val recyclerView = view.findViewById<RecyclerView>(R.id.emotions_list)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = dataAdapter

        val addButton = view.findViewById<Button>(R.id.add_button)
        addButton.setOnClickListener {
            if (dataAdapter.checkedEmotion == null) {
                Toast.makeText(activity, "Choose emotion!", Toast.LENGTH_SHORT).show()
            } else {
                val emotionIdx = dataAdapter.checkedEmotion?.adapterPosition
                if (emotionIdx != null) {
                    val marker = MarkerModel(latitude, longtitude, Emotion.values()[emotionIdx])
                    newMarkerScreenViewModel.addMarker(marker, object : MarkerDataSource.AddCallback{
                        override fun onAdd() {
                            activity?.onBackPressed()
                        }

                        override fun onError(t: Throwable) {
                            Toast.makeText(activity, "Something went wrong...", Toast.LENGTH_LONG)
                                .show()
                        }
                    })
                }
            }
        }
    }

    class EmotionAdapter: RecyclerView.Adapter<EmotionViewHolder>() {
        var checkedEmotion: EmotionViewHolder? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmotionViewHolder {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.emotions_list_element, parent, false)
            return EmotionViewHolder(view)
        }

        override fun onBindViewHolder(holder: EmotionViewHolder, position: Int) {
            val emotion = Emotion.values()[position]
            holder.imageView.setImageResource(emotion.resId)
            holder.textView.text = emotion.title
            if (checkedEmotion != null && checkedEmotion == holder) {
                holder.checkButton.setImageResource(R.drawable.checked_checkbox)
            }

            holder.checkButton.setOnClickListener {
                checkedEmotion?.checkButton?.setImageResource(R.drawable.unchecked_checkbox)
                holder.checkButton.setImageResource(R.drawable.checked_checkbox)
                checkedEmotion = holder
            }
        }

        override fun getItemCount(): Int {
            return Emotion.values().size
        }
    }

    class EmotionViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.emotion_image)
        val textView: TextView = itemView.findViewById(R.id.emotion_title)
        val checkButton: ImageButton = itemView.findViewById(R.id.checkbox)
    }
}