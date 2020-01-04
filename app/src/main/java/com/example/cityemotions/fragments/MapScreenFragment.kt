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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.cityemotions.*
import com.example.cityemotions.R
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

        private const val markerSize = 96
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
    private var visibleMarkers = mutableListOf<Marker>()

    /** Saved placed marker */
    private var placedMarker: Marker? = null

    /** Saved selected marker */
    private var selectedMarker: Marker? = null

    private var isZoomed = false

    /** ViewModel class to work with map and sending requests to storage and etc. */
    private lateinit var mapScreenViewModel: MapScreenViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize google maps api and create necessary objects
        Places.initialize(activity as Context, getString(R.string.google_maps_key))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Context)
        geocoder = Geocoder(activity as Context)

        // Just update lastLocation
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                if (result != null) {
                    lastLocation = result.lastLocation
                }
            }
        }

        // Create a viewModel object
        val factory = Injector.provideViewModelFactory()
        mapScreenViewModel = factory.create(MapScreenViewModel::class.java)
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
        map.setOnMarkerClickListener(this)
        map.setOnMapLongClickListener(this)
        map.setOnCameraIdleListener(this)
        map.setOnMapClickListener(this)
        setupMapLocation()
        setupLocationUpdates()
        placedMarker = null
        fetchMarkers(map.projection.visibleRegion.latLngBounds)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null && placedMarker != null) {
            if (marker == placedMarker) {
                (activity as OnMarkerClicker).onMarkerClicked(marker.position)
            }
        }

        placedMarker?.remove()
        selectedMarker?.remove()
        selectedMarker = null
        placedMarker = null
        if (marker != null) {
            val selectedIndex = visibleMarkers.indexOfFirst { marker.tag == it.tag }
            if (selectedIndex != -1) {
                selectedMarker = visibleMarkers[selectedIndex]
                visibleMarkers.removeAt(selectedIndex)
            }
        }
        return false
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
            Log.e("Geocoding exception", geocodeEx.toString())
        }
    }

    override fun onError(status: Status) {
        status.statusMessage?.let {
            Log.e("PlaceSelection", it)
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
        selectedMarker?.let {
            visibleMarkers.add(it)
        }
        selectedMarker = null
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
        visibleMarkers.removeAll { it.remove(); true }
    }

    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            setupLocationUpdates()
        }
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
                    Log.e("LocationApi", sendEx.toString())
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
     * Displays MarkerModel on map
     *
     * @param marker marker model to set
     * @return Marker object from map
     */
    private fun setCustomMarkerOnMap(marker: MarkerModel): Marker {
        val location = LatLng(marker.latitude, marker.longtitude)
        val bitmap = BitmapFactory.decodeResource(resources, marker.emotion.resId)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, markerSize, markerSize, false)
        val options = MarkerOptions().position(location)
        options.title(getString(marker.emotion.titleId))
            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
        val mapMarker = map.addMarker(options)
        mapMarker.tag = marker.dbId
        return mapMarker
    }

    /**
     * Set simple marker on map to add emotion to its location
     *
     * @param latLng marker position
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
            if (bounds.contains(it.position)) {
                false
            } else {
                it.remove()
                true
            }
        }

        mapScreenViewModel.getMarkers(bounds, object : MarkerDataSource.LoadCallback {
            override fun onLoad(markers: List<MarkerModel>) {
                activity?.runOnUiThread {
                    markers.forEach {markerModel ->
                        if (markerModel.dbId != selectedMarker?.tag && isMarkerVisible(markerModel)) {
                            if (visibleMarkers.indexOfFirst { it.tag == markerModel.dbId } == -1) {
                                visibleMarkers.add(setCustomMarkerOnMap(markerModel))
                            }
                        }
                    }
                }
            }

            override fun onError(t: Throwable) {
                Log.e("LoadCallback", null, t)
            }
        })
    }
}