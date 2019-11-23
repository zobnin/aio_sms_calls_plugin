package ru.execbit.aiosmscallslog

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import ru.execbit.aiolauncher.models.PluginResult
import ru.execbit.aiolauncher.plugin.sendPluginResult

// We can't start activity from background on Android 10
// Instead we send pending intent to the launcher

fun Context.openMessages(cn: ComponentName?, number: String?) {
    if (number == null) return

    try {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("sms:$number")
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        sendPendingIntentResult(cn, i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.makeCall(cn: ComponentName?, number: String?) {
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
        sendPendingIntentResult(cn, i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.showContact(cn: ComponentName?, number: String?) {
    try {
        val i = Intent().apply {
            action = ContactsContract.Intents.SHOW_OR_CREATE_CONTACT
            data = Uri.fromParts("tel", number, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        sendPendingIntentResult(cn, i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun Context.sendPendingIntentResult(cn: ComponentName?, i: Intent) {
    val pi = PendingIntent.getActivity(this, 1, i, PendingIntent.FLAG_UPDATE_CURRENT)
    val result = PluginResult(
        from = cn,
        data = pi
    )
    sendPluginResult(result)
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
