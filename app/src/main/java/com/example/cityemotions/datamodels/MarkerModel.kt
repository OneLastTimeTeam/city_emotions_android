package com.example.cityemotions.datamodels

import com.example.cityemotions.R
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


/**
 * Enum class for emotion constants
 *
 * @property dbId emotion ID in database
 * @property resId resource ID
 */
enum class Emotion(val dbId: Int, val resId: Int, val titleId: Int) {
    HAPPY(0, R.drawable.emotion_happy, R.string.happy_emoji_title),
    SURPRISE(1, R.drawable.emotion_surprise, R.string.surprise_emoji_title),
    SADNESS(2, R.drawable.emotion_sadness, R.string.sadness_emoji_title),
    ANGER(3, R.drawable.emotion_anger, R.string.anger_emoji_title),
    DISGUST(4, R.drawable.emotion_disgust, R.string.disgust_emoji_title),
    CONTEMPT(5, R.drawable.emotion_contempt, R.string.contempt_emoji_title),
    FEAR(6, R.drawable.emotion_fear, R.string.fear_emoji_title)
}


/**
 * MarkerModel data class
 *
 * @property latitude marker`s latitude
 * @property longtitude marker`s longtitude
 * @property emotion marker`s emotion
 */
data class MarkerModel(val dbId: Int, val latitude: Double, val longtitude: Double,
                       val emotion: Emotion, val description: String, val userId: String): ClusterItem {

    override fun getPosition(): LatLng {
        return LatLng(latitude, longtitude)
    }

    override fun getSnippet(): String {
        return "Undefined emotion"
    }
    
    override fun getTitle(): String {
        return "Emotion"
    }
}

