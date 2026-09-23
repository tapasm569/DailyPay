package com.dailypay.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dailypay.app.data.model.Borrower

@Entity(tableName = "borrowers")
data class BorrowerEntity(
    @PrimaryKey
    val id: String,
    val lenderId: String,
    val name: String,
    val mobileNumber: String,
    val villageCity: String?,
    val postOffice: String?,
    val policeStation: String?,
    val dist: String?,
    val profilePicUrl: String?,
    val aadhaarCardUrl: String?,
    val panCardUrl: String?
) {
    fun toBorrower() = Borrower(
        id = id,
        lenderId = lenderId,
        name = name,
        mobileNumber = mobileNumber,
        villageCity = villageCity,
        postOffice = postOffice,
        policeStation = policeStation,
        dist = dist,
        profilePicUrl = profilePicUrl,
        aadhaarCardUrl = aadhaarCardUrl,
        panCardUrl = panCardUrl
    )

    companion object {
        fun fromModel(borrower: Borrower) = BorrowerEntity(
            id = borrower.id ?: "",
            lenderId = borrower.lenderId,
            name = borrower.name,
            mobileNumber = borrower.mobileNumber,
            villageCity = borrower.villageCity,
            postOffice = borrower.postOffice,
            policeStation = borrower.policeStation,
            dist = borrower.dist,
            profilePicUrl = borrower.profilePicUrl,
            aadhaarCardUrl = borrower.aadhaarCardUrl,
            panCardUrl = borrower.panCardUrl
        )
    }
}
