//Date: 2018-05-28
//Authour: ChuahLayYuan
//Title: Google Map API Tutorial, learn how to use the Google Maps Android API,
// the Google Location Services API and the Google Places API for Android to do the followwing:
//1. Show a user’s current location
//2. Display and customize markers on a map
//3. Retrieve the address of a location given the coordinates
//4. Listen for location updates
//5. Search for places

//Android Studio: V3.1.2
//Note: use Kotlin for app development.
//Reference: https://www.raywenderlich.com/183588/introduction-google-maps-api-android-2


//Step 1: Creating API Keys
//Open res/values/google_maps_api.xml. -> Copy link paste in browser -> enable API -> Create Project -> Continue -> Create API Key.
//Copy the API key and replace google_maps_key.

//For 5. Search for places
//Go back to the developer console and enable the Google Places API for Android.

package justcodeenterprise.com.cityguide

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {

    //2. Display and customize markers on a map
    override fun onMarkerClick(p0: Marker?) = false

    //Display map
    private lateinit var map: GoogleMap

    //set up
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //4.Receiving Location update
    // 4.1 create a location request.
    private lateinit var locationCallback: LocationCallback

    // 4.2
    private lateinit var locationRequest: LocationRequest

    //4.3
    private var locationUpdateState = false

    //1.1 Show a user’s current location
    private lateinit var lastLocation: Location

    companion object {

        //User Permission
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        // 4.5
        private const val REQUEST_CHECK_SETTINGS = 2

        //5 Play Search functionality. Use Google Play Service Place.
        //5.1
        private const val PLACE_PICKER_REQUEST = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //1.2
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        //4.12 - update lastLocation with the new location and update the map with the new location coordinates.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }


        //4.13 -  set to receive location updates
        createLocationRequest()

        //5 - Place search - When the search button pressed
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            loadPlacePicker()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


//        val myPlace = LatLng(40.73, -73.99)  // this is New York
//        map.addMarker(MarkerOptions().position(myPlace).title("My Favorite City"))
//       // map.moveCamera(CameraUpdateFactory.newLatLng(myPlace))
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 12.0f))

        //enable the zoom controls on the map
        map.getUiSettings().setZoomControlsEnabled(true)

        //2.1. Display and customize markers on a map
        //declare MapsActivity as the callback triggered when the user clicks a marker on this map.
        map.setOnMarkerClickListener(this)

        setUpMap()

    }

    private fun setUpMap() {

        //checks if the app has been granted the ACCESS_FINE_LOCATION permission. If it hasn’t, then request it from the user.
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Show a user’s current location
        //1.2 enables the my-location layer which draws a light blue dot on the user’s location
        map.isMyLocationEnabled = true

        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        //1.3 give the most recent location currently available.
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {

                lastLocation = location

                val currentLatLng = LatLng(location.latitude, location.longitude)

                //2.2
                placeMarkerOnMap(currentLatLng)

                //1.4 move the camera to the user’s current location.
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }
    }

    //2. Display and customize markers on a map
    private fun placeMarkerOnMap(location: LatLng) {

        val markerOptions = MarkerOptions().position(location)

        //2.1 To change marker, change image in resource with name ic_user_location_jb
       markerOptions.icon(BitmapDescriptorFactory.fromBitmap( BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location_jb)))

        //3.2 retrieve address
        val titleStr = getAddress(location)  // add these two line s
        markerOptions.title(titleStr)

        map.addMarker(markerOptions)
    }


    //3. Retrieve the address of a location given the coordinates
    private fun getAddress(latLng: LatLng): String {
        //3.1 Creates a Geocoder object to turn a latitude and longitude coordinate into an address and vice versa.
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {

                address = addresses[0]

                //TO CHECK AGAIN WHY NOT WORKING
//                for (i in 0 until address.maxAddressLineIndex) {
//
//                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
//                    Log.i("Address", "test")
//                }
                addressText = addresses[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            Log.e("Address", e.localizedMessage)
        }

        return addressText
    }


    //4.7 Retrieve location
    private fun startLocationUpdates() {
        //1 if the ACCESS_FINE_LOCATION permission has not been granted, request it now and return.
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2 If there is permission, request for location updates.
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    //4.8 Request location update
    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }


    // 4.9 - When this call?????
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }

        //5.3 Place search - retrieve details about the selected place
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng)
            }
        }
    }

    // 4.10
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 4.11
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    //Place search
    //5.2 - creates a new builder for an intent to start the Place Picker UI and then starts the PlacePicker intent.
    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(this@MapsActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }


}


