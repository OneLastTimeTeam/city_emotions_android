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
    val id: Int,
    val emotionId: Int,
    val latitude: Double,
    val longtitude: Double,
    val description: String,
    val userId: String
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


data class UserIdRequest(
    val userId: String
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
        private val GET_USERS_MARKERS = "/users_markers"
        private val REMOVE_PATH = "/remove_marker"
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
    private fun makeRequest(path: String, body: String?): Request {
        val request = Request.Builder().url(URL + path)
        if (body != null) {
            val requestBody = body.toRequestBody(JSON)
            request.method("POST", requestBody)
        }

        return request.build()
    }

    /**
     * Make marker list from JSON response body
     *
     * @param response Response object
     *
     * @return MutableList of MarkerModels
     */
    private fun getMarkersFromBody(response: Response): MutableList<MarkerModel> {
        response.body?.let {
            val markers = mutableListOf<MarkerModel>()
            val stringBody = it.string()
            val jsonArray = JSONArray(stringBody)
            for (i in 0 until jsonArray.length()) {
                val markerJson = jsonArray.getJSONObject(i)
                markers.add(
                    MarkerModel(
                        dbId = markerJson.getInt("id"),
                        latitude = markerJson.getDouble("latitude"),
                        longtitude = markerJson.getDouble("longtitude"),
                        emotion = Emotion.values()[markerJson.getInt("emotionId")],
                        description = markerJson.getString("description"),
                        userId = markerJson.getString("userId")
                    )
                )
            }
            return markers
        }
        return mutableListOf()
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
                } else {
                    val markers = getMarkersFromBody(response)
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
     * @param userId user identificator string
     * @param callback user`s implementation of DataSource.LoadCallback interface
     */
    fun getUserMarkers(userId: String, callback: LoadCallback) {
        val userIdRequest = UserIdRequest(
            userId = userId
        )

        val body = Gson().toJson(userIdRequest)
        val request = makeRequest(GET_USERS_MARKERS, body)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    callback.onError(Throwable("Internal Server Error"))
                } else {
                    val markers = getMarkersFromBody(response)
                    callback.onLoad(markers)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    /**
     * Add marker to storage
     *
     * @param marker markerModel to add
     * @param callback user`s implementation of DataSource.AddCallback interface
     */
    fun addMarker(marker: MarkerModel, callback: AddCallback) {
        val markerRequest = MarkerRequest(
            id = marker.dbId,
            emotionId = marker.emotion.dbId,
            latitude = marker.latitude,
            longtitude = marker.longtitude,
            description = marker.description,
            userId = marker.userId
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
        val markerRequest = MarkerRequest(
            id = marker.dbId,
            emotionId = marker.emotion.dbId,
            latitude = marker.latitude,
            longtitude = marker.longtitude,
            description = marker.description,
            userId = marker.userId
        )

        val body = Gson().toJson(markerRequest)
        val request = makeRequest(REMOVE_PATH, body)
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.code != 200) {
                    callback.onError(Throwable("Internal Server Error"))
                } else {
                    callback.onRemove()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
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