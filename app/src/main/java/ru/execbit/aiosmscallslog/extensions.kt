package ru.execbit.aiosmscallslog

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import ru.execbit.aiolauncher.models.PluginActivity
import ru.execbit.aiolauncher.models.PluginResult
import ru.execbit.aiolauncher.plugin.sendPluginResult

// UPD 1: We can't start activity from background on Android 10
// Instead we send pending intent to the launcher

// UPD 2: Chinese crap phones can block pending intents
// So we need to use hack: custom "pending intent" (PluginActivity)
// Requires AIO version: 2.7.30

fun Context.openMessages(cn: ComponentName?, number: String?) {
    if (number == null) return

    val result = PluginResult(
        from = cn,
        data = PluginActivity(
            action = Intent.ACTION_VIEW,
            data = Uri.parse("sms:$number")
        )
    )
    sendPluginResult(result)
}

fun Context.makeCall(cn: ComponentName?, number: String?) {
    if (number == null) return

    // Correctly handle # symbol
    val fixedNumber = if (number.contains("#")) {
        number.replace("#", "%23")
    } else {
        number
    }

    val result = PluginResult(
        from = cn,
        data = PluginActivity(
            action = Intent.ACTION_CALL,
            data = Uri.parse("tel:$fixedNumber")
        )
    )
    sendPluginResult(result)
}

fun Context.showContact(cn: ComponentName?, number: String?) {
    val result = PluginResult(
        from = cn,
        data = PluginActivity(
            action = ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
            data = Uri.fromParts("tel", number, null)
        )
    )
    sendPluginResult(result)
}

fun Drawable.toBitmap(): Bitmap? {
    if (this is BitmapDrawable) {
        return this.bitmap
    }

    val bitmap = Bitmap.createBitmap(
        intrinsicWidth,
        intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    bitmap?.let {
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
    }

    return bitmap
}

fun String.getTruncatedName(): String {
    return when (Settings.callsTruncateMethod) {
        "by_symbols" -> truncateBySymbols()
        "by_last_name" -> truncateByLastName()
        else -> this
    }
}

private fun String.truncateByLastName(): String {
    return if (this.trim().contains(' ')) {
        val nameList = this.split(' ')
        val firstName = nameList[0]

        val lastName = if (nameList[1].length > 1) {
            nameList[1].take(1) + '.'
        } else {
            nameList[1]
        }

        "$firstName $lastName"
    } else {
        truncateBySymbols()
    }
}

private fun String.truncateBySymbols(): String {
    return if (length > 10) {
        take(9) + '.'
    } else {
        this
    }
}

