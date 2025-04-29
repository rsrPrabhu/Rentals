package com.example.rentals_g03

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.rentals_g03.databinding.ActivitySearchListingBinding
import com.example.rentals_g03.models.ListingModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class SearchListingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySearchListingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var gMap: GoogleMap
    private lateinit var geocoder: Geocoder

    // location data structure storage in firestore
    val TAG = "TESTING"
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setTitle("Search Listing")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // initializing firebase auth
        auth = Firebase.auth

        // initiliazing gecoder
        geocoder = Geocoder(applicationContext, Locale.getDefault())

        // initializing google map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.buttonBookNow.setOnClickListener {
            val listingID = binding.listingID.text.toString()
            val data = hashMapOf(
                "bookedByID" to auth.currentUser!!.uid
            )
            db.collection("listings")
                .document(listingID)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    showSnackbar("Booking is completed..")
                    val intent = Intent(this, MyBookingActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { ex ->
                    Log.e("TESTING", "Exception occurred while adding a document : $ex", )
                }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.renter_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_log_out-> {
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            android.R.id.home-> {
                val intent = Intent(this, MyBookingActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "I am in onMapReady()")
        gMap = googleMap
        gMap.uiSettings.isZoomControlsEnabled = true
        loadListingOnMap()
    }
    private fun loadListingOnMap() {
        val markerMap = mutableMapOf<String, ListingModel>()
        db.collection("listings")
            .get()
            .addOnSuccessListener {
                results: QuerySnapshot->
                for(document: DocumentSnapshot in results) {
                    // iterating each document add call add marker fun
                    // passing lat and lng
                    val listing: ListingModel? = document.toObject(ListingModel::class.java)
                    val spot = LatLng(listing!!.latitude, listing!!.longitude)
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spot, 12f))
                    val marker = gMap.addMarker(
                        MarkerOptions()
                            .position(spot)
                            .icon(createTextMarker("$${listing.price}"))
                            .title(listing.docID)
                    )
                    if (marker != null) {
                        markerMap[marker.id] = listing
                    }
                }
                gMap.setOnMarkerClickListener { marker ->
                    val data = markerMap[marker.id]
                    binding.listingID.text = data?.docID
                    if ( data != null) {
                        binding.model.text = data?.model
                         var result = ""
                          result += "Owner: ${data?.ownerName}\n"
                          result += "$${data!!.price}\n"
                          binding.result.text = result

                                // image with Glide
                          Glide.with(this)
                            .load(data.imageUrl)
                            .into(binding.vehicleImage)

                        if ( data.bookedByID.isNotEmpty()) {
                            binding.buttonBookNow.visibility = View.GONE
                            binding.result.text = "Already booked."
                        } else {
                            binding.buttonBookNow.visibility = View.VISIBLE
                        }
                    }
                    true
                }
            }
    }
    fun createTextMarker(text: String): BitmapDescriptor {
        val paint = Paint().apply {
            textSize = 30f
            color = Color.BLACK  // Text Color
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        val padding = 20  // padding around text
        val textWidth = paint.measureText(text).toInt()
        val textHeight = (paint.textSize).toInt()

        val width = textWidth + padding * 2
        val height = textHeight + padding * 2

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw White Background with Padding
        val backgroundPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Draw Text
        canvas.drawText(text, padding.toFloat(), height - padding.toFloat(), paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    private fun showSnackbar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackBar.show()
    }
}