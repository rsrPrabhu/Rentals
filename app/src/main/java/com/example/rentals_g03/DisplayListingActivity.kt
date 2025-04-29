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
import com.example.rentals_g03.adapters.ListingAdapter
import com.example.rentals_g03.databinding.ActivityDisplayListingBinding
import com.example.rentals_g03.interfaces.ClickInterface
import com.example.rentals_g03.models.ListingModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DisplayListingActivity : AppCompatActivity(), ClickInterface {

    private lateinit var binding: ActivityDisplayListingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ListingAdapter
    private val dataToDisplay = mutableListOf<ListingModel>()

    // location data structure storage in firestore
    val TAG = "TESTING"
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDisplayListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setTitle("My Listing")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // initializing auth
        auth = Firebase.auth

        // adapter declartaion for listing
        adapter = ListingAdapter(dataToDisplay, this)
        binding.listingRV.adapter = adapter
        binding.listingRV.layoutManager = LinearLayoutManager(this)
        binding.listingRV.addItemDecoration( DividerItemDecoration( this, LinearLayoutManager.VERTICAL ))

        // loading the RV
        loadListing()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.owner_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_add_listing-> {
                val intent = Intent(this, AddListingActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.mi_log_out-> {
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            android.R.id.home-> {
                val intent = Intent(this, AddListingActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun loadListing() {
        val user = auth.currentUser
        if (user == null) {
            showSnackbar("User not logged in.")
            return
        }
        db.collection("listings")
            .whereEqualTo("ownerID", user.uid)
            .get()
            .addOnSuccessListener { results ->
                val listingFromDB = mutableListOf<ListingModel>()
                for (document in results) {
                    try {
                        val listing = document.toObject(ListingModel::class.java)
                        listingFromDB.add(listing)
                    } catch (e: Exception) {
                        Log.e("TESTING++", "Failed to parse document: ${document.id}", e)
                    }
                }
                dataToDisplay.clear()
                dataToDisplay.addAll(listingFromDB)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("TESTING++", "Firestore query FAILED", e)
                binding.result.text = "Firestore Error: ${e.message}"
            }
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
                showSnackbar("Listing ${docID} updated.")
                loadListing()
            }
            .addOnFailureListener { e ->
                Log.e("TESTING", "Failed to get from database.", e)
                binding.result.text = e.message
            }
    }
    override fun rowPressed(position: Int) {
    }
    private fun showSnackbar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackBar.show()
    }
}