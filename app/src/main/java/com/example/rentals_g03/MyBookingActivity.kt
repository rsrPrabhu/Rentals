package com.example.rentals_g03

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.rentals_g03.adapters.ListingAdapter
import com.example.rentals_g03.databinding.ActivityMyBookingBinding
import com.example.rentals_g03.interfaces.ClickInterface
import com.example.rentals_g03.models.ListingModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyBookingActivity : AppCompatActivity(), ClickInterface {

    private lateinit var binding: ActivityMyBookingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ListingAdapter

    // location data structure storage in firestore
    val TAG = "TESTING"
    val db = Firebase.firestore

    val dataToDisplay = mutableListOf<ListingModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setTitle("My Booking")

        // initializing auth
        auth = Firebase.auth

        // initializing adapter
        adapter = ListingAdapter(dataToDisplay, this)
        binding.listingRV.adapter = adapter
        binding.listingRV.layoutManager = LinearLayoutManager(this)
        binding.listingRV.addItemDecoration( DividerItemDecoration( this, LinearLayoutManager.VERTICAL ))

        loadMyBookings()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.renter_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_search_listing-> {
                val intent = Intent(this, SearchListingActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.mi_log_out-> {
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun loadMyBookings() {
        val listingFromDB = mutableListOf<ListingModel>()
        db.collection("listings")
            .whereEqualTo("bookedByID", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                results: QuerySnapshot->
                for(document: QueryDocumentSnapshot in results) {
                    val listing = document.toObject(ListingModel::class.java)
                    listingFromDB.add(listing)
                }
                dataToDisplay.clear()
                dataToDisplay.addAll(listingFromDB)
                adapter.notifyDataSetChanged()

                if ( dataToDisplay.isEmpty()) {
                    showSnackbar("No data to display.")
                }
            }
            .addOnFailureListener { e ->
                Log.w("", "Error adding document", e)
            }
    }
    override fun rowPressed(position: Int) {
        // image with Glide
        Glide.with(this)
            .load(dataToDisplay[position].imageUrl)
            .into(binding.vehicleImage)

        binding.detail.text = """
            Booking ID:         ${dataToDisplay[position].docID}
            Model:              ${dataToDisplay[position].model}
            Price:              ${dataToDisplay[position].price}
            City:               ${dataToDisplay[position].city}
            Pickup location:    ${dataToDisplay[position].address}
            Owner name:         ${dataToDisplay[position].ownerName}
            Booked By:          ${auth.currentUser!!.email}
        """.trimIndent()
    }
    override fun cancelPressed(position: Int) {
        val docID = dataToDisplay[position].docID
        Log.d(TAG, "Data to delete/cancel ${docID}")

        val data = hashMapOf(
            "bookedByID" to ""
        )
        db.collection("listings")
            .document(docID)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                showSnackbar("${dataToDisplay[position].docID} booking is cancelled.")
                loadMyBookings()
            }
            .addOnFailureListener { e ->
                Log.e("TESTING", "Failed to get from database.", e)
            }
    }
    private fun showSnackbar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackBar.show()
    }
}