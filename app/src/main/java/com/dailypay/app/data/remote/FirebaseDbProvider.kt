package com.dailypay.app.data.remote

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseDbProvider {
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance().apply {
            try {
                setPersistenceEnabled(true)
            } catch (_: Exception) {
            }
        }
    }

    val db: DatabaseReference
        get() = database.reference

    val lendersRef: DatabaseReference
        get() = database.reference.child("lenders")

    val borrowersRef: DatabaseReference
        get() = database.reference.child("borrowers")

    val loansRef: DatabaseReference
        get() = database.reference.child("loans")

    val repaymentsRef: DatabaseReference
        get() = database.reference.child("repayments")

    val storage: StorageReference
        get() = FirebaseStorage.getInstance().reference
}
