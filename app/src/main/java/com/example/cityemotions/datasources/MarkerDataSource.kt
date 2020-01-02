package com.example.cityemotions.datasources

import com.example.cityemotions.datamodels.MarkerModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


/**
 *  DataSource implementation
 */
class MarkerDataSource {
    companion object {
        private var instance: MarkerDataSource? = null
        private val client = OkHttpClient()

        fun getInstance(): MarkerDataSource {
            if (instance == null) {
                instance = MarkerDataSource()
            }
            return instance!!
        }

        private val URL = "http://134.209.18.0:7777"
        private val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

        private val ADD_PATH = "/add_marker"
    }

    // Temporary solution
    private var data: MutableList<MarkerModel> = mutableListOf()

    fun makeRequest(path: String, body: String?): Request {
        val request = Request.Builder().url(URL + path)
        if (body != null) {
            val requestBody = body.toRequestBody(JSON)
            request.method("POST", requestBody)
        }

        return request.build()
    }

    /**
     * GetMarkers from storage and put them in callback
     *
     * @param bounds bounds of visible piece of map
     * @param callback user`s implementation of DataSource.LoadCallback interface
     */
    fun getMarkers(bounds: LatLngBounds, callback: LoadCallback) {
        val markersToSend = data.filter { bounds.contains(LatLng(it.latitude, it.longtitude)) }
        callback.onLoad(markersToSend)
    }

    /**
     * Get user`s markers from storage
     *
     * @param callback user`s implementation of DataSource.LoadCallback interface
     */
    fun getUserMarkers(callback: LoadCallback) {
        callback.onLoad(data)
    }

    /**
     * Add marker to storage
     *
     * @param marker markerModel to add
     * @param callback user`s implementation of DataSource.AddCallback interface
     */
    fun addMarker(marker: MarkerModel, callback: AddCallback) {
        data.add(marker)

        val body = """{
            "emotionId":${marker.emotion.dbId},
            "latitude":${marker.latitude},
            "longtitude":${marker.longtitude},
            "description":"${marker.description}"
        }
        """.trimIndent()

        val request = makeRequest(ADD_PATH, body)

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                callback.onAdd()
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    /**
     * Remove marker from storage
     *
     * @param marker markerModel to remove
     * @param callback user`s implementation of DataSource.RemoveCallback interface
     */
    fun removeMarker(marker: MarkerModel, callback: RemoveCallback) {
        data.remove(marker)
        callback.onRemove()
    }

    interface LoadCallback {
        fun onLoad(markers: List<MarkerModel>)
        fun onError(t: Throwable)
    }

    interface AddCallback {
        fun onAdd()
        fun onError(t: Throwable)
    }

    interface RemoveCallback {
        fun onRemove()
        fun onError(t: Throwable)
    }
}