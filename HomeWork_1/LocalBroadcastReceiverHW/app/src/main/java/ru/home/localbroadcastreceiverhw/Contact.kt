package ru.home.localbroadcastreceiverhw

data class Contact(
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumbers: List<String>
)