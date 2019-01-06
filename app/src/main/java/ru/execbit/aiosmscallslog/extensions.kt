package ru.execbit.aiosmscallslog

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import ru.execbit.aiolauncher.models.PluginIntentActions
import ru.execbit.aiolauncher.models.PluginResult

fun Context.openMessages(number: String?) {
    if (number == null) return

    try {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("sms:$number")
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.makeCall(number: String?) {
    if (number == null) return

    // Correctly handle # symbol
    val fixedNumber = if (number.contains("#")) {
        number.replace("#", "%23")
    } else {
        number
    }

    try {
        val i = Intent(Intent.ACTION_CALL, Uri.parse("tel:$fixedNumber"))
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.showContact(number: String?) {
    try {
        val i = Intent().apply {
            action = ContactsContract.Intents.SHOW_OR_CREATE_CONTACT
            data = Uri.fromParts("tel", number, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Drawable.toBitmap(): Bitmap? {
    if (this is BitmapDrawable) {
        if (this.bitmap != null) {
            return this.bitmap
        }
    }

    val bitmap: Bitmap? = Bitmap.createBitmap(
        intrinsicWidth,
        intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}
