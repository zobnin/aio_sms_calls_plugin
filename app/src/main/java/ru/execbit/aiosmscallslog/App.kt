package ru.execbit.aiosmscallslog

import android.app.Application
import com.devs.acr.AutoErrorReporter
import com.mohamadamin.kpreferences.base.KPreferenceManager

class App: Application() {
    companion object {
        var PACKAGE_NAME: String? = null
    }

    override fun onCreate() {
        super.onCreate()
        PACKAGE_NAME = packageName
        KPreferenceManager.initialize(this, name = "${packageName}_preferences")

        AutoErrorReporter.get(this)
          .setEmailAddresses("zobnin@gmail.com")
          .setEmailSubject("AIO SMS & Calls crash report")
          .start()
    }
}