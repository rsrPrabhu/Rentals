package com.example.rentals_g03.models

import com.google.firebase.firestore.DocumentId

data class ListingModel(
    @DocumentId
    val docID: String = "",
    val ownerID: String = "",
    val ownerName: String = "",
    val model: String = "",
    val price: Double = 0.0,
    val city: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "https://upload.wikimedia.org/wikipedia/commons/f/fa/2016_Toyota_Prius_%28ZVW50L%29_Hybrid_liftback_%282016-04-02%29_01.jpg",

    // booking information
    val bookedByID: String = "",
)
