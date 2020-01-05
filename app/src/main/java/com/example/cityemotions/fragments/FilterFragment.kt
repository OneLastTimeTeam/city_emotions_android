package com.example.cityemotions.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cityemotions.*
import com.example.cityemotions.datamodels.Emotion

/**
 * Filters fragment
 */
class FilterFragment: Fragment() {
    /** RecycleView adapter */
    private lateinit var dataAdapter: FilterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.show()
        super.onCreate(savedInstanceState)
        dataAdapter = FilterAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.filter_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.filter_list)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = dataAdapter
    }
}


/**
 * DataAdapter class implementation for user`s emotions list
 */
class FilterAdapter : RecyclerView.Adapter<FilterHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.filter, parent, false)
        return FilterHolder(view)
    }

    override fun onBindViewHolder(holder: FilterHolder, position: Int) {
        val emotion = Emotion.values()[position]
        holder.imageView.setImageResource(emotion.resId)
        val context = holder.itemView.context
        val emotionTag = context.resources.getString(emotion.titleId)

        holder.textView.text = emotionTag
        holder.switch.tag = emotionTag

        val pref = context.getSharedPreferences((context as MapsActivity)
            .getSharedPreferencesTag(), 0)
        holder.switch.isChecked = pref.getBoolean(emotionTag, true)

        holder.switch.setOnCheckedChangeListener { buttonView, isChecked ->
            val switchContext = buttonView.context
            val switchPref = switchContext.getSharedPreferences((switchContext as MapsActivity)
                .getSharedPreferencesTag(), 0)
            val editor = switchPref.edit()

            editor.putBoolean(buttonView.tag as String, isChecked)
            editor.apply()
        }
    }

    override fun getItemCount(): Int {
        return Emotion.values().size
    }
}


/**
 * ViewHolder class implementation for filters
 */
class FilterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.emotion_image)
    val textView: TextView = itemView.findViewById(R.id.emotion_title)
    val switch: Switch = itemView.findViewById(R.id.filter_switch)
}
