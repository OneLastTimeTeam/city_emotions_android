package com.example.cityemotions.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cityemotions.Injector
import com.example.cityemotions.OnEmotionsClicker
import com.example.cityemotions.R
import com.example.cityemotions.datamodels.Emotion
import com.example.cityemotions.modelviews.UserEmotionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class UserEmotionsFragment: Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private lateinit var dataAdapter: EmotionAdapter

    private lateinit var userEmotionsViewModel: UserEmotionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataAdapter = EmotionAdapter()
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
    }

    class EmotionAdapter : RecyclerView.Adapter<UserEmotionViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserEmotionViewHolder {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.user_emotion, parent, false)
            return UserEmotionViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserEmotionViewHolder, position: Int) {
            val emotion = Emotion.values()[position]
            holder.imageView.setImageResource(emotion.resId)
            holder.textView.text = emotion.title
            //if (checkedEmotion != null && checkedEmotion == holder) {
            //    holder.deleteButton.setImageResource(R.drawable.checked_checkbox)
            //}

            holder.deleteButton.setOnClickListener {
                //checkedEmotion?.checkButton?.setImageResource(R.drawable.unchecked_checkbox)
                //holder.checkButton.setImageResource(R.drawable.checked_checkbox)
                //checkedEmotion = holder
            }
        }

        override fun getItemCount(): Int {
            return Emotion.values().size
        }
    }

    class UserEmotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.emotion_image)
        val textView: TextView = itemView.findViewById(R.id.emotion_address)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete)
    }
}
