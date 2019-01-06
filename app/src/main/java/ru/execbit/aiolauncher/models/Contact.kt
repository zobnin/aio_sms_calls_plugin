package ru.execbit.aiolauncher.models

data class Contact(
    val name: String,
    val id: Int,
    val phone: String,
    var default: Boolean = false
)

