package com.example.rentals_g03

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.rentals_g03.databinding.ActivityMainBinding
import com.example.rentals_g03.models.UserProfile
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    // location data structure storage in firestore
    val TAG = "TESTING"
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setTitle("Login")

        // initializing auth
        auth = Firebase.auth

        // check if already logged in or not
        checkIfUserLoggedIn()

        binding.buttonLogin.setOnClickListener {
            val email = binding.emailFromUI.text.toString()
            val password = binding.passwordFromUI.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showSnackbar("ERROR: Canot leave fields empty.")
                return@setOnClickListener
            }
            logIn(email, password)
        }
        binding.buttonSignup.setOnClickListener {
            val email = binding.emailFromUI.text.toString()
            val password = binding.passwordFromUI.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showSnackbar("ERROR: Canot leave fields empty.")
                return@setOnClickListener
            }
            signUp(email, password)
        }
    }
    private fun logIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "signInWithEmail:success with ${auth.currentUser!!.uid}")
                db.collection("userProfile")
                    .document(auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener {
                        document: DocumentSnapshot ->
                        Log.d(TAG, "Data before parsing ${document.data}.")
                        val user: UserProfile? = document.toObject(UserProfile::class.java)
                        Log.d(TAG, "Data received after login success ${user}.")
                        if ( user!!.isOwner == true) {
                            val intent = Intent(this, DisplayListingActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this, MyBookingActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener { ex ->
                        Log.e("TESTING", "Exception occurred while adding a document : $ex", )
                    }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "signInWithEmail:failure", error)
                binding.result.text = error.message
            }
    }
    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "createUserWithEmail:success")
                binding.result.setText("SUCCESS: User created: ${auth.currentUser?.uid}")
                binding.buttonLogin.isEnabled = false

                // connect to a firestore collection and create user profile
                var isOwner: Boolean = false
                if ( binding.isOwner.isChecked) {
                    isOwner = true
                }
                var data = hashMapOf(
                    "isOwner" to isOwner
                )
                db.collection("userProfile")
                    .document(auth.currentUser!!.uid)
                    .set(data)
                    .addOnSuccessListener {
                            docRef ->
                        binding.result.text = "User Profile created!"
                        if ( isOwner == true) {
                            val intent = Intent(this, DisplayListingActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this, MyBookingActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener { ex ->
                        Log.e("TESTING", "Exception occurred while adding a document : $ex", )
                    }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "createUserWithEmail:failure", error)
                binding.result.text = error.message
            }
    }
    private fun checkIfUserLoggedIn() {
       if (auth.currentUser == null) {
            binding.result.setText("NO USER LOGGED IN!")
        } else {
           db.collection("userProfile")
               .document(auth.currentUser!!.uid)
               .get()
               .addOnSuccessListener {
                       document: DocumentSnapshot ->
                   Log.d(TAG, "Data before parsing ${document.data}.")
                   val user: UserProfile? = document.toObject(UserProfile::class.java)
                   Log.d(TAG, "Data received after login success ${user}.")
                   if ( user!!.isOwner == true) {
                       val intent = Intent(this, DisplayListingActivity::class.java)
                       startActivity(intent)
                   } else {
                       val intent = Intent(this, MyBookingActivity::class.java)
                       startActivity(intent)
                   }
               }
        }
    }
    private fun showSnackbar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackBar.show()
    }
}