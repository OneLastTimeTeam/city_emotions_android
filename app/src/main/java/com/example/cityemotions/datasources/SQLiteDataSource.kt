package com.example.cityemotions.datasources

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.cityemotions.MapsActivity
import com.example.cityemotions.datamodels.Emotion
import com.example.cityemotions.datamodels.MarkerModel


class SQLiteDataSource(context: Context):
    SQLiteOpenHelper(context, DATABASE_NAME + (context as MapsActivity).getUserId(),
        null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "user_emotions"
        const val EMOTIONS_TABLE_NAME = "emotions"
        const val EMOTIONS_COLUMN_ID = "id"
        const val EMOTIONS_COLUMN_EMOTION_ID = "emotion_id"
        const val EMOTIONS_COLUMN_LATITUDE = "latitude"
        const val EMOTIONS_COLUMN_LONGTITUDE = "longtitude"
        const val EMOTIONS_COLUMN_DESCRIPTION = "description"
        const val EMOTIONS_COLUMN_USER_ID = "user_id"

        private var instance: SQLiteDataSource? = null
        fun getInstance(context: Context): SQLiteDataSource {
            if (instance == null) {
                instance = SQLiteDataSource(context)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE " + EMOTIONS_TABLE_NAME + " (" +
                        EMOTIONS_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        EMOTIONS_COLUMN_EMOTION_ID + " INT UNSIGNED, " +
                        EMOTIONS_COLUMN_LATITUDE + " REAL, " +
                        EMOTIONS_COLUMN_LONGTITUDE + " REAL, " +
                        EMOTIONS_COLUMN_DESCRIPTION + " TEXT, " +
                        EMOTIONS_COLUMN_USER_ID + " TEXT" + ")")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${EMOTIONS_TABLE_NAME}")
        onCreate(db)
    }

    fun addMarker(marker: MarkerModel) {
        val db = this.writableDatabase
        val content = ContentValues()
        content.put(EMOTIONS_COLUMN_ID, marker.dbId.toString())
        content.put(EMOTIONS_COLUMN_EMOTION_ID, marker.emotion.dbId.toString())
        content.put(EMOTIONS_COLUMN_LATITUDE, marker.latitude.toString())
        content.put(EMOTIONS_COLUMN_LONGTITUDE, marker.longtitude.toString())
        content.put(EMOTIONS_COLUMN_DESCRIPTION, marker.description)
        content.put(EMOTIONS_COLUMN_USER_ID, marker.userId)
        db.insert(EMOTIONS_TABLE_NAME, null, content)
    }

    fun getMarkers(): MutableList<MarkerModel> {
        val db = this.readableDatabase
        val projection = arrayOf(
            EMOTIONS_COLUMN_ID,
            EMOTIONS_COLUMN_EMOTION_ID,
            EMOTIONS_COLUMN_LATITUDE,
            EMOTIONS_COLUMN_LONGTITUDE,
            EMOTIONS_COLUMN_DESCRIPTION,
            EMOTIONS_COLUMN_USER_ID
        )

        val cursor = db.query(
            EMOTIONS_TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        val markers = mutableListOf<MarkerModel>()

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                markers.add(
                    MarkerModel(
                        dbId = cursor.getInt(cursor.getColumnIndex(EMOTIONS_COLUMN_ID)),
                        latitude = cursor.getDouble(cursor.getColumnIndex(
                            EMOTIONS_COLUMN_LATITUDE)),
                        longtitude = cursor.getDouble(cursor.getColumnIndex(
                            EMOTIONS_COLUMN_LONGTITUDE)),
                        emotion = Emotion.values()[cursor.getInt(cursor.getColumnIndex(
                            EMOTIONS_COLUMN_EMOTION_ID))],
                        description = cursor.getString(cursor.getColumnIndex(
                            EMOTIONS_COLUMN_DESCRIPTION)),
                        userId = cursor.getString(cursor.getColumnIndex(EMOTIONS_COLUMN_USER_ID))
                ))

                cursor.moveToNext()
            }
        }
        return markers
    }

    fun removeMarker(marker: MarkerModel) {
        val db = this.writableDatabase
        db.delete(EMOTIONS_TABLE_NAME,
            EMOTIONS_COLUMN_ID + "=" + marker.dbId.toString(), null)
    }
}