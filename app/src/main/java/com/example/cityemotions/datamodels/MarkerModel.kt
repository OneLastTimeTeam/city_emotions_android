package com.example.cityemotions.datamodels

import com.example.cityemotions.R


enum class Emotion(val dbId: Int, val resId: Int) {
    HAPPY(0, R.drawable.emotion_happy)
}

class MarkerModel(val latitude: Double, val longtitude: Double, val emotion: Emotion)