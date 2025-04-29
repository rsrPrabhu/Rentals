package com.example.rentals_g03

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.rentals_g03.databinding.ActivityAddListingBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class AddListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddListingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var geocoder: Geocoder

    // location data structure storage in firestore
    val TAG = "TESTING"
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setTitle("Add Listing")

        // initializing auth
        auth = Firebase.auth

        // initialising geocoder
        geocoder = Geocoder(applicationContext, Locale.getDefault())

        binding.buttonSubmit.setOnClickListener {
            val ownerName: String = binding.ownerName.text.toString()
            val model: String = binding.model.text.toString()
            val price: Double = binding.price.text.toString().toDoubleOrNull() ?: 0.0
            val city: String = binding.city.text.toString()
            val address: String = binding.address.text.toString()
            val imageUrl: String = binding.imageUrl.text.toString()
            // validation
            if ( model.isEmpty() || city.isEmpty() || ownerName.isEmpty() ||
                address.isEmpty() || imageUrl.isEmpty()) {
                showSnackbar("ERROR: Cannot leave any filed empty.")
                return@setOnClickListener
            }
            // getting latitude and longtide based on address
            // we will use in listing for renters
            val searchResult: List<android.location.Address>? = geocoder.getFromLocationName(address, 1)
            if (searchResult == null || searchResult.isEmpty()) {
                showSnackbar("ERROR: Search address not found.")
                return@setOnClickListener
            }
            val data = hashMapOf(
                "ownerID" to auth.currentUser!!.uid,
                "ownerName" to ownerName,
                "model" to model,
                "price" to price,
                "city" to city,
                "address" to address,
                "latitude" to searchResult[0].latitude,
                "longitude" to searchResult[0].longitude,
                "imageUrl" to imageUrl,
                "bookedByID" to "",
            )
            db.collection("listings")
                .add(data)
                .addOnSuccessListener {
                        docRef: DocumentReference ->
                    showSnackbar("Listing created with id: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("TESTING", "Failed to get from database.", e)
                    binding.result.text = e.message
                }
        }
        binding.buttonCancel.setOnClickListener {
            binding.model.setText("")
            binding.price.setText("")
            binding.city.setText("")
            binding.address.setText("")
            binding.ownerName.setText("")
            binding.imageUrl.setText("")
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.owner_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_my_listing -> {
                val intent = Intent(this, DisplayListingActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.mi_log_out -> {
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showSnackbar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackBar.show()
    }
}