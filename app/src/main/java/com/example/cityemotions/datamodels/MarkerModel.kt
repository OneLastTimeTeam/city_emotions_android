package com.example.cityemotions.datamodels

import com.example.cityemotions.R


/**
 * Enum class for emotion constants
 *
 * @property dbId emotion ID in database
 * @property resId resource ID
 */
enum class Emotion(val dbId: Int, val resId: Int) {
    HAPPY(0, R.drawable.emotion_happy),
    SURPRISE(1, R.drawable.emotion_surprise),
    SADNESS(2, R.drawable.emotion_sadness),
    ANGER(3, R.drawable.emotion_anger),
    DISGUST(4, R.drawable.emotion_disgust),
    CONTEMPT(5, R.drawable.emotion_contempt),
    FEAR(6, R.drawable.emotion_fear)
}


/**
 * MarkerModel data class
 *
 * @property latitude marker`s latitude
 * @property longtitude marker`s longtitude
 * @property emotion marker`s emotion
 */
data class MarkerModel(val latitude: Double, val longtitude: Double, val emotion: Emotion)