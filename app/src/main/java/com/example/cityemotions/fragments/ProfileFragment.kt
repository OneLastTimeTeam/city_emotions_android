package com.example.cityemotions.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cityemotions.Injector
import com.example.cityemotions.MapsActivity
import com.example.cityemotions.OnEmotionsClicker
import com.example.cityemotions.R
import com.example.cityemotions.datamodels.EmotionStat
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.modelviews.ProfileViewModel
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.PieChart
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
    companion object {
        const val TAG = "Profile"
    }

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.show()
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

        val factory = Injector.provideViewModelFactory(activity as Context)
        profileViewModel = factory.create(ProfileViewModel::class.java)
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

        profileViewModel.getUsersMarkers((activity as MapsActivity).getUserId(),
            object : MarkerDataSource.StatLoadCallback {
                override fun onLoad(stat: List<EmotionStat>) {
                    val dataColors = ArrayList<Int>()
                    val entries = ArrayList<PieEntry>()
                    val pieChart = view.findViewById<PieChart>(R.id.pieChart)

                    stat.forEach {
                        if (it.count > 0) {
                            entries.add(PieEntry(it.count.toFloat(), getString(it.emotion.titleId)))
                            dataColors.add(ContextCompat.getColor(context as Context,
                                it.emotion.colorId))

                        }
                    }
                    if (entries.count() > 0) {
                        val pieDataset = PieDataSet(entries, "Emotions")
                        pieChart.data = PieData(pieDataset)
                        pieDataset.sliceSpace = 2.0f
                        pieDataset.valueTextSize = 15.0f
                        pieDataset.valueTextColor = R.color.darkGrey
                        pieDataset.valueFormatter = PercentFormatter()
                        pieDataset.colors = dataColors
                    }

                    pieChart.legend.isEnabled = false
                    pieChart.description.isEnabled = false
                    pieChart.setUsePercentValues(true)
                    val paint = pieChart.getPaint(Chart.PAINT_INFO)
                    paint.textSize = 50.0f
                    paint.color = ContextCompat.getColor(context as Context, R.color.darkGrey)
                    pieChart.setNoDataText("You have not set emotions yet")
                    pieChart.invalidate()
                }

                override fun onError(t: Throwable) {
                    Log.e(TAG, null, t)
                }
            })
    }
}