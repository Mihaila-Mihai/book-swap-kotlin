package ro.example.bookswap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.distance_slider
import ro.example.bookswap.models.LocationModel
import ro.example.bookswap.models.User


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var longitude: String? = null
    private var latitude: String? = null
    private var currLongitude: String? = null
    private var currLatitude: String? = null
    private val currentUser = Firebase.auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        longitude =  intent.getStringExtra("LONGITUDE")
        latitude =  intent.getStringExtra("LATITUDE")

        var currentLocation: LocationModel

        Firebase.database.reference.child("users").child(currentUser!!).get().addOnSuccessListener {
            currentLocation = it.getValue<User>()?.location!!
            currLongitude = currentLocation.longitude
            currLatitude = currentLocation.latitude

            var currLatitudeD: Double = 0.0
            var currLongitudeD: Double = 0.0

            if (currLatitude != null) {
                currLatitudeD = currLatitude!!.toDouble()
            }

            if (currLongitude != null) {
                currLongitudeD = currLongitude!!.toDouble()
            }

            // Add a marker for current user location and move the camera
            val currLocation = LatLng(currLatitudeD, currLongitudeD)
            mMap.addMarker(
                MarkerOptions()
                    .position(currLocation)
                    .title("Your location")
            )
        }

        mMap = googleMap
        var latitudeD: Double = 0.0
        var longitudeD: Double = 0.0

        if (latitude != null) {
            latitudeD = latitude!!.toDouble()
        }

        if (longitude != null) {
            longitudeD = longitude!!.toDouble()
        }

        // Add a marker for user location and move the camera
        val location = LatLng(latitudeD, longitudeD)
        mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("User's location")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))


        mMap.uiSettings.isZoomControlsEnabled = true;
        mMap.uiSettings.isCompassEnabled = true;
        mMap.uiSettings.isRotateGesturesEnabled = true;
    }

}

      