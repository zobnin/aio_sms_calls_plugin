package ru.execbit.aiosmscallslog.calls

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.provider.CallLog
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.execbit.aiolauncher.models.*
import ru.execbit.aiolauncher.plugin.*
import ru.execbit.aiosmscallslog.App
import ru.execbit.aiosmscallslog.getComponentName
import java.util.concurrent.CopyOnWriteArrayList

class CallsPluginReceiver : BroadcastReceiver() {
    companion object {
        private val cn: ComponentName by lazy {
            getComponentName("calls.CallsPluginReceiver")
        }

        private val calls = CopyOnWriteArrayList<Call>()
        private val data by lazy { CallsData(App.context, cn, calls) }
        private val actions by lazy { CallsActions(App.context, cn, data, calls) }

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

            readCalls(context)
            registerContentObserver()

            when (intent.action) {
                PluginIntentActions.PLUGIN_GET_DATA -> data.processGetData(intent)
                PluginIntentActions.PLUGIN_SEND_ACTION -> actions.processAction(intent)
            }

            Updater.checkForNewVersionAndShowNotify(context)
        }
    }

    private fun readCalls(context: Context, force: Boolean = false) {
        if (calls.isEmpty() || force) {
            val newCalls = Calls.getCalls(context)
                .filterNot { it.number.isEmpty() }
                .distinctBy { it.number }

            calls.clear()
            calls.addAll(newCalls)
        }
    }

    private fun registerContentObserver() {
        if (observerRegistered) return

        try {
            App.context.contentResolver.registerContentObserver(
                CallLog.Calls.CONTENT_URI,
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
            readCalls(App.context, force = true)
            data.generateAndSendResult()
        }
    }
}