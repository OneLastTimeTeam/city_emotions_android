package com.example.cityemotions.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cityemotions.OnEmotionsClicker
import com.example.cityemotions.R
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.profile_screen.*
import kotlin.collections.ArrayList


/**
 * User`s profile fragment
 */
class ProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.show()
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user_emotions_button.setOnClickListener {
            (activity as OnEmotionsClicker).onEmotionsClicked()
        }
        val allColors = intArrayOf(ContextCompat.getColor(context!!, R.color.anger),
            ContextCompat.getColor(context!!, R.color.contempt),
            ContextCompat.getColor(context!!, R.color.disgust),
            ContextCompat.getColor(context!!, R.color.fear),
            ContextCompat.getColor(context!!, R.color.happy),
            ContextCompat.getColor(context!!, R.color.sadness),
            ContextCompat.getColor(context!!, R.color.surprise))
        val dataColors = ArrayList<Int>()
        val entries = ArrayList<PieEntry>()
        for (i in 0..6) {
            // TODO: insert data here!
            val value = (i+1).toFloat()
            if (value > 0) {
                entries.add(PieEntry(value, "emote"))
                dataColors.add(allColors[i])
            }
        }
        val pieDataset = PieDataSet(entries, "Emotions")
        pieChart.data = PieData(pieDataset)
        pieChart.legend.isEnabled = false
        pieDataset.sliceSpace = 2f
        pieDataset.valueTextSize = 15f
        pieDataset.valueTextColor = R.color.darkGrey
        pieDataset.valueFormatter = PercentFormatter()
        pieDataset.colors = dataColors
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
    }
}