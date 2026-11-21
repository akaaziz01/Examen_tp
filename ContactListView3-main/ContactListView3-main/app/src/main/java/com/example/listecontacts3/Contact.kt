package com.example.listecontacts3

import java.io.Serializable

data class Contact(
    val id: Long,
    var firstName: String,
    var lastName: String,
    var phoneNumber: String,
    var email: String,
    var photoUri: String?,
    var latitude: Double?,
    var longitude: Double?
) : Serializable {
    val fullName: String
        get() = "$firstName $lastName"
}
