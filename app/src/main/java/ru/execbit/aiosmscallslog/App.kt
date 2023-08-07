package ru.execbit.aiosmscallslog

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.provider.Telephony
import com.mohamadamin.kpreferences.base.KPreferenceManager
import ru.execbit.aiosmscallslog.calls.CallsPluginReceiver
import ru.execbit.aiosmscallslog.sms.SmsPluginReceiver

class App: Application() {
    companion object {
        const val REQUIRED_AIO_VERSION = "2.7.30"
        const val AIO_API_VERSION = 2

        // Holding app context is not a memory leak
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

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

            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.apply {
                createNotificationChannel(channel)
            }
        }
    }
}