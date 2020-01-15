package com.example.cityemotions.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.cityemotions.*
import com.example.cityemotions.R
import com.example.cityemotions.datamodels.Emotion
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.MarkerDataSource
import com.example.cityemotions.modelviews.MapScreenViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import java.io.IOException


/**
 * Fragment with map-screen. Implements the logic of working with map
 */
class MapScreenFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    PlaceSelectionListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener,
    GoogleMap.OnMapClickListener {
    companion object {
        /** Permission access constants */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2

        const val markerSize = 96
        const val TAG = "MapScreen"
    }

    /** GoogleMap object */
    private lateinit var map: GoogleMap

    /** Client which controls location updates */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /** Last sent location  */
    private lateinit var lastLocation: Location

    /** Geocoder to get location by name */
    private lateinit var geocoder: Geocoder

    /** LocationUpdates callback */
    private lateinit var locationCallback: LocationCallback

    /** Location requests settings */
    private lateinit var locationRequest: LocationRequest

    /** Are location updates enabled? */
    private var locationUpdateState = false

    /** Visible markers list */
    private var visibleMarkers = mutableListOf<MarkerModel>()

    /** Saved placed marker */
    private var placedMarker: Marker? = null

    private var isZoomed = false

    /** ViewModel class to work with map and sending requests to storage and etc. */
    private lateinit var mapScreenViewModel: MapScreenViewModel

    /** Marker Clustering manager */
    private lateinit var clusterManager: ClusterManager<MarkerModel>

    /** Custom listeners */
    private lateinit var cameraIdleListener: CompositeOnCameraIdleListener
    private lateinit var markerClickListener: CompositeOnMarkerClickListener


    override fun onCreate(savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.hide()
        super.onCreate(savedInstanceState)

        Places.initialize(activity as Context, getString(R.string.google_maps_key))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Context)
        geocoder = Geocoder(activity as Context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                if (result != null) {
                    lastLocation = result.lastLocation
                }
            }
        }

        val factory = Injector.provideViewModelFactory(activity as Context)
        mapScreenViewModel = factory.create(MapScreenViewModel::class.java)

        cameraIdleListener = CompositeOnCameraIdleListener()
        markerClickListener = CompositeOnMarkerClickListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        startLocationUpdates()
        return inflater.inflate(R.layout.map_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.hide()
        super.onViewCreated(view, savedInstanceState)
        // Setup google api fragments
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val searchBar = childFragmentManager.findFragmentById(R.id.search_bar)
                as AutocompleteSupportFragment
        searchBar.setPlaceFields(arrayListOf(Place.Field.ID, Place.Field.NAME))
        searchBar.setOnPlaceSelectedListener(this)

        // Setup buttons
        view.findViewById<ImageButton>(R.id.profile_button).setOnClickListener {
            (activity as OnSelectProfileListener).onProfileSelected()
        }
        view.findViewById<ImageButton>(R.id.filter_button).setOnClickListener {
            (activity as OnSelectFilterListener).onFilterSelected()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        cameraIdleListener.addListener(this)
        markerClickListener.addListener(this)

        setupMapLocation()
        setupLocationUpdates()
        setupClusterManager()

        fetchMarkers(map.projection.visibleRegion.latLngBounds)

        map.setOnCameraIdleListener(cameraIdleListener)
        map.setOnMarkerClickListener(markerClickListener)
        map.setOnMapClickListener(this)
        map.setOnMapLongClickListener(this)
    }

    /** Setup cluster manager and add listeners */
    private fun setupClusterManager() {
        clusterManager = ClusterManager(context, map)
        clusterManager.renderer = MarkerClusterRenderer(context as Context, map, clusterManager)
        cameraIdleListener.addListener(clusterManager)
        markerClickListener.addListener(clusterManager)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null && placedMarker != null) {
            if (marker == placedMarker) {
                (activity as OnMarkerClicker).onMarkerClicked(marker.position)
            }
        }

        placedMarker?.remove()
        placedMarker = null
        return true
    }

    override fun onPlaceSelected(place: Place) {
        try {
            val locationList = geocoder.getFromLocationName(place.name, 1)
            if (locationList != null && locationList.size != 0) {
                val location = locationList[0]
                val latLng = LatLng(location.latitude, location.longitude)
                setSimpleMarkerOnMap(latLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f))
            }
        } catch (geocodeEx: IOException) {
            Log.e(TAG, geocodeEx.toString())
        }
    }

    override fun onError(status: Status) {
        status.statusMessage?.let {
            Log.e(TAG, it)
        }
    }

    override fun onCameraIdle() {
        val bounds = map.projection.visibleRegion.latLngBounds
        fetchMarkers(bounds)
    }

    override fun onMapLongClick(pos: LatLng?) {
        pos?.let {
            setSimpleMarkerOnMap(it)
        }
    }

    override fun onMapClick(p0: LatLng?) {
        placedMarker?.remove()
        placedMarker = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                setupLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        visibleMarkers.removeAll { clusterManager.removeItem(it); true }
    }

    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            setupLocationUpdates()
            return
        }
        fetchMarkers(map.projection.visibleRegion.latLngBounds)
    }

    /**
     * Check ACCESS_FINE_LOCATION permission and request if its not granted
     *
     * @return is permissions granted
     */
    private fun checkLocationPermissions(): Boolean {
        activity?.let {
            if (ActivityCompat.checkSelfPermission(
                    activity as Activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )

                return false
            }
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.count() > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMapLocation()
                setupLocationUpdates()
            }
        }
    }

    /**
     * Get location tracking permission and initial setup
     */
    private fun setupMapLocation() {
        if (!checkLocationPermissions()) {
            return
        }
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(activity as Activity) { location ->
            location?.let {
                if (!isZoomed) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude), 18.0f))
                    isZoomed = true
                }
                lastLocation = it
            }
        }
    }

    /**
     * Setup locationRequest object
     */
    private fun setupLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Add location requests that app will be using
     */
    private fun startLocationUpdates() {
        setupLocationRequest()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val client = LocationServices.getSettingsClient(activity as Activity)
        val task = client.checkLocationSettings(builder)

        task.addOnSuccessListener {
            setupLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(activity as Activity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e(TAG, sendEx.toString())
                }
            }
        }
    }

    /**
     * Get location tracking permission and setup location updates handlers
     */
    private fun setupLocationUpdates() {
        if (!checkLocationPermissions()) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        locationUpdateState = true
    }

    /**
     * Check if the marker is disabled in the filter
     *
     * @param marker marker model to check
     */
    private fun isMarkerVisible(marker: MarkerModel): Boolean {
        val emotionTag = getString(marker.emotion.titleId)
        context?.let {
            val pref = it.getSharedPreferences((activity as MapsActivity)
                .getSharedPreferencesTag(), 0)
            return pref.getBoolean(emotionTag, true)
        }
        return true
    }

    /**
     * Set simple marker on map to add emotion to its location
     *
     * @param latLng marker position
     */
    private fun setSimpleMarkerOnMap(latLng: LatLng) {
        placedMarker?.remove()
        placedMarker = map.addMarker(MarkerOptions().position(latLng))
    }

    /**
     * Updates the state of the map and the markers displayed on it
     *
     * @param bounds map`s bounds
     */
     private fun fetchMarkers(bounds: LatLngBounds) {
        visibleMarkers.removeAll {
            if (bounds.contains(it.position) && isMarkerVisible(it)) {
                false
            } else {
                clusterManager.removeItem(it)
                true
            }
        }
        clusterManager.cluster()

        mapScreenViewModel.getMarkers(bounds, object : MarkerDataSource.LoadCallback {
            override fun onLoad(markers: List<MarkerModel>) {
                activity?.runOnUiThread {
                    markers.forEach {markerModel ->
                        if (isMarkerVisible(markerModel)) {
                            if (visibleMarkers.indexOfFirst { it.dbId == markerModel.dbId } == -1) {
                                clusterManager.addItem(markerModel)
                                visibleMarkers.add(markerModel)
                            }
                        }
                    }
                    clusterManager.cluster()
                }
            }

            override fun onError(t: Throwable) {
                Log.e(TAG, null, t)
            }
        })
    }
}


/**
 * Custom composite camera idle listener implementation with many listeners
 */
class CompositeOnCameraIdleListener: GoogleMap.OnCameraIdleListener {
    private var mListeners = mutableListOf<GoogleMap.OnCameraIdleListener>()

    fun addListener(listener: GoogleMap.OnCameraIdleListener) {
        mListeners.add(listener)
    }

    override fun onCameraIdle() {
        for (listener in mListeners) {
            listener.onCameraIdle()
        }
    }
}


/**
 * Custom composite on marker click listener implementation with many listeners
 */
class CompositeOnMarkerClickListener: GoogleMap.OnMarkerClickListener {
    private var mListeners = mutableListOf<GoogleMap.OnMarkerClickListener>()

    fun addListener(listener: GoogleMap.OnMarkerClickListener) {
        mListeners.add(listener)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        for (listener in mListeners) {
            listener.onMarkerClick(marker)
        }
        return true
    }
}


/**
 * Custom cluster renderer. Overrides markers rendering logic
 */
class MarkerClusterRenderer(private val context: Context, private val map: GoogleMap,
                            private val clusterManager: ClusterManager<MarkerModel>):
    DefaultClusterRenderer<MarkerModel>(context, map, clusterManager),
    ClusterManager.OnClusterItemClickListener<MarkerModel> {
    companion object {
        const val MINIMUM_CLUSTER_SIZE = 5
    }

    init {
        clusterManager.setOnClusterItemClickListener(this)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<MarkerModel>?): Boolean {
        cluster?.let {
            return cluster.size >= MINIMUM_CLUSTER_SIZE
        }
        return false
    }

    override fun onBeforeClusterItemRendered(item: MarkerModel?, markerOptions: MarkerOptions?) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        item?.let {
            val bitmap = BitmapFactory.decodeResource(context.resources, it.emotion.resId)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap,
                MapScreenFragment.markerSize,
                MapScreenFragment.markerSize, false)
            markerOptions?.title(context.getString(item.emotion.titleId))
                ?.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                ?.snippet(null)
        }
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<MarkerModel>?,
        markerOptions: MarkerOptions?
    ) {
        super.onBeforeClusterRendered(cluster, markerOptions)
        val emotionsStat = HashMap<Emotion, Int>()
        cluster?.let {
            cluster.items.forEach {
                val count = emotionsStat[it.emotion]
                if (count == null) {
                    emotionsStat[it.emotion] = 1
                } else {
                    emotionsStat[it.emotion] = count + 1
                }
            }

            val entry = emotionsStat.maxBy { it.value }
            entry?.let {
                val bitmap = BitmapFactory.decodeResource(context.resources, it.key.resId)
                val emotionsCount = emotionsStat.map { it.value }.sum()
                val markerSize = getBucket(cluster) + MapScreenFragment.markerSize
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap,
                    markerSize, markerSize, false)
                markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                    ?.title("Total emotions: $emotionsCount")
            }
        }
    }

    override fun onClusterItemRendered(clusterItem: MarkerModel?, marker: Marker?) {
        super.onClusterItemRendered(clusterItem, marker)
        clusterItem?.let {
            marker?.tag = it.dbId
        }
    }

    override fun onClusterItemClick(markerModel: MarkerModel?): Boolean {
        markerModel?.let {
            for (marker in clusterManager.markerCollection.markers) {
                if (marker.tag == it.dbId) {
                    marker.showInfoWindow()
                }
            }
        }
        return false
    }
}
