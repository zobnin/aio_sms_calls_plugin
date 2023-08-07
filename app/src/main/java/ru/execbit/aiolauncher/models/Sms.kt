package ru.execbit.aiolauncher.models

data class Sms(
    val date: Long = 0,
    val number: String = "",
    var name: String = "",
    val body: String = "",
    val isRead: Boolean = false,
)

