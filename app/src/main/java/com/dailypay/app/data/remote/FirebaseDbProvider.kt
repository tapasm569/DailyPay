package com.dailypay.app.data.remote

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseDbProvider {
    // Correct Singapore (asia-southeast1) default endpoint
    private const val RTDB_URL = "https://dailypay-63145-default-rtdb.asia-southeast1.firebasedatabase.app"

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance(RTDB_URL).apply {
            try {
                setPersistenceEnabled(true)
            } catch (_: Exception) {
            }
        }
    }

    val db: DatabaseReference
        get() = database.reference

    val lendersRef: DatabaseReference
        get() = database.getReference("lenders")

    val borrowersRef: DatabaseReference
        get() = database.getReference("borrowers")

    val loansRef: DatabaseReference
        get() = database.getReference("loans")

    val repaymentsRef: DatabaseReference
        get() = database.getReference("repayments")

    val storage: StorageReference
        get() = FirebaseStorage.getInstance().reference
}
