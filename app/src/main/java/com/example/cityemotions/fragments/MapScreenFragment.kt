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
import com.example.cityemotions.Injector
import com.example.cityemotions.OnMarkerClicker
import com.example.cityemotions.OnSelectProfileListener
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
    PlaceSelectionListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener {
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

    private var placedMarker: Marker? = null

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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.setOnMapLongClickListener(this)
        map.setOnCameraIdleListener(this)
        setupMapLocation()
        setupLocationUpdates()
        placedMarker = null
        fetchMarkers(map.projection.visibleRegion.latLngBounds)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null && placedMarker != null) {
            if (marker == placedMarker) {
                (activity as OnMarkerClicker).onMarkerClicked(marker.position)
                return true
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
        // Restore placed marker
        placedMarker?.position?.let {
            placedMarker = map.addMarker(MarkerOptions().position(it))
        }
    }

    override fun onMapLongClick(pos: LatLng?) {
        pos?.let {
            placedMarker?.remove()
            setSimpleMarkerOnMap(it)
        }
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
    }

    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            setupLocationUpdates()
        }
    }

    /**
     * Check ACCESS_FINE_LOCATION permission and request if its not granted
     */
    private fun checkLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity as Activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

            return false
        }
        return true
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
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude), 18.0f))
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
     */
    private fun setCustomMarkerOnMap(marker: MarkerModel) {
        val location = LatLng(marker.latitude, marker.longtitude)
        val bitmap = BitmapFactory.decodeResource(resources, marker.emotion.resId)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, markerSize, markerSize, false)
        val options = MarkerOptions().position(location)
        options.title(getString(marker.emotion.titleId))
            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
        map.addMarker(options)
    }

    /**
     * Set simple marker on map to add emotion to its location
     */
    private fun setSimpleMarkerOnMap(latLng: LatLng) {
        placedMarker = map.addMarker(MarkerOptions().position(latLng))
    }

    /**
     * Updates the state of the map and the markers displayed on it
     */
     private fun fetchMarkers(bounds: LatLngBounds) {
        map.clear()

        mapScreenViewModel.getMarkers(bounds, object : MarkerDataSource.LoadCallback {
            override fun onLoad(markers: List<MarkerModel>) {
                activity?.runOnUiThread {
                    markers.forEach {
                        setCustomMarkerOnMap(it)
                    }
                }
            }

            override fun onError(t: Throwable) {
                Log.e("LoadCallback", null, t)
            }
        })
    }
}