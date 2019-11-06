package com.example.cityemotions.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.cityemotions.Injector
import com.example.cityemotions.R
import com.example.cityemotions.datamodels.MarkerModel
import com.example.cityemotions.datasources.DataSource
import com.example.cityemotions.modelviews.MapScreenViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener


class MapScreenFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    companion object {
        fun getInstance(): MapScreenFragment {
            return MapScreenFragment()
        }

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    private lateinit var mapScreenViewModel: MapScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initialize(activity as Context, getString(R.string.google_maps_key))
        placesClient = Places.createClient(activity as Context)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Context)
        val factory = Injector.provideViewModelFactory()
        mapScreenViewModel = factory.create(MapScreenViewModel::class.java)

        locationCallback = object : LocationCallback() {
            // TODO: не стоит делать это тут(реально не стоит, это вызывается очень часто)
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                if (result != null) {
                    lastLocation = result.lastLocation
                    mapScreenViewModel.getMarkers(LatLng(lastLocation.latitude, lastLocation.longitude),
                        object : DataSource.LoadCallback {
                            override fun onLoad(markers: MutableList<MarkerModel>) {
                                // TODO: reimplement title, image etc.
                                markers.forEach { setMarkerOnMap(LatLng(it.latitude, it.longtitude),
                                    "Default Title") }
                            }

                            override fun onError(t: Throwable) {
                                Log.e("LoadCallback", null, t)
                            }
                        })
                }
            }
        }
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
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val searchBar = childFragmentManager.findFragmentById(R.id.search_bar)
                as AutocompleteSupportFragment
        searchBar.setPlaceFields(arrayListOf(Place.Field.ID, Place.Field.NAME))
        searchBar.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val geocoder = Geocoder(activity as Context)
                val locationList = geocoder.getFromLocationName(place.name, 1)

                if (locationList != null && locationList.size != 0) {
                    val location = locationList[0]
                    val latLng = LatLng(location.latitude, location.longitude)
                    map.addMarker(MarkerOptions().position(latLng).title(place.name))
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                }
            }

            override fun onError(status: Status) {
                if (status.statusMessage != null) {
                    Log.e("PlaceSelection", status.statusMessage as String)
                }
            }
        })
    }

    private fun setupMapLocation() {
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
            return
        }
        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(activity as Activity) { location ->
            if (location != null) {
                lastLocation = location
                val currentLocation = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
            }
        }
    }

    private fun setupLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                activity as Activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        setupMapLocation()
        setupLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(activity as Activity)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            setupLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(activity as Activity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {}
            }
        }
    }

    private fun setMarkerOnMap(location: LatLng, title: String) {
        val options = MarkerOptions().position(location)
        options.title(title)
        map.addMarker(options)
    }

    override fun onMarkerClick(marker: Marker?) = false
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
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
}