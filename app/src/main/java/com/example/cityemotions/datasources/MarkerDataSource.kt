package com.example.cityemotions.datasources

import com.example.cityemotions.datamodels.Emotion
import com.example.cityemotions.datamodels.MarkerModel
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.io.IOException

/**
 * Marker JSON implementation
 */
data class MarkerRequest(
    val emotionId: Int,
    val latitude: Double,
    val longtitude: Double,
    val description: String
)


/**
 * LatLng JSON implementation
 */
data class LatLngRequest(
    val latitude: Double,
    val longtitude: Double
)


/**
 * Bounds JSON implementation
 */
data class BoundsRequest(
    val southwest: LatLngRequest,
    val northeast: LatLngRequest
)


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

        private val URL = "http://79.143.31.234:80"
        private val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

        private val ADD_PATH = "/add_marker"
        private val GET_ALL_PATH = "/all_markers_from_coords"
    }

    // Temporary solution
    private var data: MutableList<MarkerModel> = mutableListOf()

    /**
     * GetMarkers from storage and put them in callback
     *
     * @param path server path
     * @param body JSON request body
     *
     * @return Request objects
     */
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
        val boundsRequest = BoundsRequest(
            southwest = LatLngRequest(
                latitude = bounds.southwest.latitude,
                longtitude = bounds.southwest.longitude
            ),
            northeast = LatLngRequest(
                latitude = bounds.northeast.latitude,
                longtitude = bounds.northeast.longitude
            )
        )
        val body = Gson().toJson(boundsRequest)

        val request = makeRequest(GET_ALL_PATH, body)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    callback.onError(Throwable("Internal Server Error"))
                    return
                }
                response.body?.let {
                    val markers = mutableListOf<MarkerModel>()
                    val stringBody = it.string()
                    val jsonArray = JSONArray(stringBody)
                    for (i in 0 until jsonArray.length()) {
                        val markerJson = jsonArray.getJSONObject(i)
                        markers.add(MarkerModel(
                            dbId = markerJson.getInt("id"),
                            latitude = markerJson.getDouble("latitude"),
                            longtitude = markerJson.getDouble("longtitude"),
                            emotion = Emotion.values()[markerJson.getInt("emotionId")],
                            description = markerJson.getString("description")
                        ))
                    }
                    callback.onLoad(markers)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
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
        val markerRequest = MarkerRequest(
            emotionId = marker.emotion.dbId,
            latitude = marker.latitude,
            longtitude = marker.longtitude,
            description = marker.description
        )

        val body = Gson().toJson(markerRequest)
        val request = makeRequest(ADD_PATH, body)

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    callback.onError(Throwable("Internal Server Error"))
                } else {
                    callback.onAdd()
                }
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