package ru.execbit.aiosmscallslog

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.mohamadamin.kpreferences.base.KPreferenceManager

class App: Application() {
    companion object {
        const val REQUIRED_AIO_VERSION = "2.7.30-beta8"
        var PACKAGE_NAME: String? = null
    }

    override fun onCreate() {
        super.onCreate()
        PACKAGE_NAME = packageName

        createNotificationChannel()
        KPreferenceManager.initialize(this, name = "${packageName}_preferences")
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= 26) {
            val name = "AIO SMS & Calls Plugin"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("main", name, importance).apply {
                description = name
            }

            val notificationManager: NotificationManager? =
                getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }
}