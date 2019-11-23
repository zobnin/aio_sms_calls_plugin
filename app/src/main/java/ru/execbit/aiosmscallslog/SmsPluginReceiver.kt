package ru.execbit.aiosmscallslog

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.execbit.aiolauncher.models.*
import ru.execbit.aiolauncher.plugin.*

class SmsPluginReceiver : BroadcastReceiver() {
    companion object {
        const val REPLY_BUTTON_ID = -1000
        const val DIAL_BUTTON_ID = -1001

        const val MENU_BUTTON_PHONE = -0
        const val MENU_BUTTON_SMS = -1

        private var cn: ComponentName? = null
        private var initDone = false
        private var smses = emptyList<Sms>()
        private var openedSms: Sms? = null
    }

    override fun onReceive(context: Context, intent: Intent?) {
        cn = ComponentName(context.packageName, context.packageName + ".SmsPluginReceiver")

        CoroutineScope(Dispatchers.Default).launch {
            if (intent == null) return@launch

            when (intent.action) {
                PluginIntentActions.PLUGIN_GET_DATA -> {
                    if (checkUid(intent)) {
                        processGetData(context, intent)
                    }
                }

                PluginIntentActions.PLUGIN_SEND_ACTION -> {
                    if (checkUid(intent)) {
                        processAction(context, intent)
                    }
                }

                Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> processSmsReceived(context, intent)
            }

            Updater.checkForNewVersionAndShowNotify(context)
        }
    }

    private fun processAction(context: Context, intent: Intent) {
        intent.getParcelableExtra<PluginAction>("action")?.let { action ->
            try {
                when (action.context) {
                    "tap" -> processTapAction(context, action)
                    "longtap" -> processLongTapAction(context, action)
                    "menu" -> processMenuAction(context, action)
                    "dialog" -> processDialogAction(context, action)
                    else -> {
                        val result = PluginResult(
                            from = cn,
                            data = PluginError(4, context.getString(R.string.invalid_action))
                        )
                        context.sendPluginResult(result)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun processTapAction(context: Context, action: PluginAction) {
        try {
            val sms= getSmsById(action.selectedIds[0])
            if (sms == null) {
                generateAndSendResult(context)
                return
            }

            val dialogTitle = if (sms.name.isEmpty()) {
                sms.number
            } else {
                sms.name
            }

            val buttons = listOf(
                PluginButton(
                    text = context.getString(R.string.call),
                    id = DIAL_BUTTON_ID
                ),
                PluginButton(
                    text = context.getString(R.string.sms),
                    id = REPLY_BUTTON_ID
                )
            )

            val dialog = PluginDialog(
                title = dialogTitle,
                text = sms.body,
                bottomButtons = buttons
            )

            val result = PluginResult(
                from = cn,
                data = dialog
            )

            context.sendPluginResult(result)
            openedSms = sms
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processLongTapAction(context: Context, action: PluginAction) {
        try {
            val sms = getSmsById(action.selectedIds[0])
            if (sms == null) {
                generateAndSendResult(context)
                return
            }

            val menu = listOf(
                PluginButton(
                    text = "Phone",
                    icon = context.getDrawable(R.drawable.ic_phone).toBitmap(),
                    id = MENU_BUTTON_PHONE
                ),
                PluginButton(
                    text = "SMS",
                    icon = context.getDrawable(R.drawable.ic_sms).toBitmap(),
                    id = MENU_BUTTON_SMS
                )
            )

            val result = PluginResult(
                from = cn,
                data = PluginMenu(menu)
            )

            context.sendPluginResult(result)
            openedSms = sms

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processDialogAction(context: Context, action: PluginAction) {
        if (action.selectedIds.contains(REPLY_BUTTON_ID)) {
            context.openMessages(cn, openedSms?.number)
        } else if (action.selectedIds.contains(DIAL_BUTTON_ID)) {
            context.makeCall(cn, openedSms?.number)
        }
        openedSms = null
    }

    private fun processMenuAction(context: Context, action: PluginAction) {
        if (action.selectedIds.isNotEmpty()) {
            when (action.selectedIds[0]) {
                MENU_BUTTON_PHONE -> context.makeCall(cn, openedSms?.number)
                MENU_BUTTON_SMS -> context.openMessages(cn, openedSms?.number)
            }
            openedSms = null
        }
    }

    private fun processGetData(context: Context, intent: Intent) {
        intent.getStringExtra("event")?.let { event ->
            when (event) {
                // Called when plugin loaded
                "load" -> generateAndSendResult(context)
                // Called on force reload (by user)
                "force" -> generateAndSendResult(context)
                // Called on alarm (1 hour by default)
                //"alarm" -> {}
                // Called on launcher resume (user pressed home button and returned to desktop)
                "resume" -> {
                    // SMS loading is very expensive operation so we do not load it on resume
                    // But Android can unload plugin from memory in any moment
                    // We need to reinitialize it
                    if (Settings.smsUpdateOnResume || Settings.smsSettingsChanged || !initDone) {
                        generateAndSendResult(context)
                        if (Settings.smsSettingsChanged) {
                            Settings.smsSettingsChanged = false
                        }
                    }
                }
            }
        }
    }

    private fun generateAndSendResult(context: Context) {
        context.sendPluginResult(generateResult(context))
        initDone = true
    }

    private fun generateResult(context: Context): PluginResult {
        try {
            val lines = mutableListOf<PluginLine>()
            val ids = generateIds(Settings.smsNum.toInt())

            smses = SMS.getSms(context, limit = Settings.smsNum.toInt())
            smses.forEachIndexed { idx, sms ->
                val line = PluginLine(
                    body = sms.body.take(140),
                    from = if (sms.name.isNotEmpty()) sms.name else sms.number,
                    id = ids[idx]
                )
                lines.add(line)
            }

            val resultLines = if (lines.size < Settings.smsNum.toInt()) {
                lines
            } else {
                lines.subList(0, Settings.smsNum.toInt())
            }

            Settings.smsIds = idsToString(ids)

            return PluginResult(
                from = cn,
                data = PluginLines(resultLines, privateModeSupport = true)
            )

        } catch (e: Exception) {
            e.printStackTrace()

            return PluginResult(
                from = cn,
                data = PluginError(2, e.toString())
            )
        }
    }

    private fun getSmsById(id: Int): Sms? {
        try {
            val ids = stringToIds(Settings.smsIds)

            if (ids.isEmpty()) {
                return null
            }

            val smsIdx = ids.indexOf(id)
            return smses[smsIdx]

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private suspend fun processSmsReceived(context: Context, intent: Intent) {
        // Wait for DB update
        delay(10000)
        generateAndSendResult(context)
    }
}