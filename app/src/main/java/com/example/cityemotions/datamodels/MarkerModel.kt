package com.example.cityemotions.datamodels

import com.example.cityemotions.R


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


// TODO(Xenobyte): добавить юзера и текстовое описание геолокации
/**
 * MarkerModel data class
 *
 * @property latitude marker`s latitude
 * @property longtitude marker`s longtitude
 * @property emotion marker`s emotion
 */
data class MarkerModel(val dbId: Int, val latitude: Double, val longtitude: Double,
                       val emotion: Emotion, val description: String)

/**

 CREATE TABLE emotions (
    id bigserial primary key,
    emotion_id integer NOT NULL,
    latitude real NOT NULL,
    longtitude real NOT NULL,
    description text NOT NULL
 );

 */
