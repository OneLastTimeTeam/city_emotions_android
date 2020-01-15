package com.example.cityemotions.datamodels


/**
 * Single emotion statistics class
 *
 * @property emotion Emotion object from enum
 * @property count amount of this emotion in database
 */
class EmotionStat(val emotion: Emotion, val count: Int)
