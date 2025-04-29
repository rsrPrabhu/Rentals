package com.example.rentals_g03.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class UserProfile(
    @DocumentId
    val docID: String = "",
    @get:PropertyName("isOwner")
    @set:PropertyName("isOwner")
    var isOwner: Boolean = false
)
