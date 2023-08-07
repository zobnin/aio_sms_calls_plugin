package ru.execbit.aiolauncher.models

data class Call(
    val number: String = "",
    val cachedName: String?,
    val date: Long = 0,
    val direction: String = "",
    val duration: Long = 0,
)

