package ru.execbit.aiosmscallslog.sms

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.execbit.aiolauncher.models.*
import ru.execbit.aiolauncher.plugin.*
import ru.execbit.aiosmscallslog.App
import ru.execbit.aiosmscallslog.getComponentName
import java.util.concurrent.CopyOnWriteArrayList

class SmsPluginReceiver : BroadcastReceiver() {
    companion object {
        const val MAX_SEARCH_RESULTS = 3
        const val MAX_LOADED_SMS = 200

        private val cn: ComponentName by lazy {
            getComponentName("sms.SmsPluginReceiver")
        }

        private var smses = CopyOnWriteArrayList<Sms>()
        private val data by lazy { SmsData(App.context, cn, smses) }
        private val actions by lazy { SmsActions(App.context, cn, data, smses) }

        private var observerRegistered = false
    }

    override fun onReceive(context: Context, intent: Intent?) {
        CoroutineScope(Dispatchers.Default).launch {
            if (intent == null) return@launch

            if (!checkUid(intent)) {
                return@launch
            }

            if (!checkAioVersion(context, App.REQUIRED_AIO_VERSION)) {
                context.sendInvalidAioVersionError(cn)
                return@launch
            }

            readSms(context)
            registerContentObserver()

            when (intent.action) {
                PluginIntentActions.PLUGIN_GET_DATA -> data.processGetData(intent)
                PluginIntentActions.PLUGIN_SEND_ACTION -> actions.processAction(intent)
            }

            Updater.checkForNewVersionAndShowNotify(context)
        }
    }

    private fun readSms(context: Context, force: Boolean = false) {
        if (force || smses.isEmpty()) {
            val newSmses = SMS.getSms(context, MAX_LOADED_SMS)
            smses.clear()
            smses.addAll(newSmses)
        }
    }

    private fun registerContentObserver() {
        if (observerRegistered) return

        try {
            App.context.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                createContentObserver()
            )
            observerRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createContentObserver() = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            readSms(App.context, force = true)
            data.generateAndSendResult()
        }
    }
}