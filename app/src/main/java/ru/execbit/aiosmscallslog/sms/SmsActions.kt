package ru.execbit.aiosmscallslog.sms

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import ru.execbit.aiolauncher.models.PluginAction
import ru.execbit.aiolauncher.models.PluginButton
import ru.execbit.aiolauncher.models.PluginDialog
import ru.execbit.aiolauncher.models.PluginError
import ru.execbit.aiolauncher.models.PluginMenu
import ru.execbit.aiolauncher.models.PluginResult
import ru.execbit.aiolauncher.models.Sms
import ru.execbit.aiolauncher.plugin.sendPluginResult
import ru.execbit.aiosmscallslog.INVALID_ACTION_ERROR
import ru.execbit.aiosmscallslog.R
import ru.execbit.aiosmscallslog.getCompatDrawable
import ru.execbit.aiosmscallslog.makeCall
import ru.execbit.aiosmscallslog.openMessages
import ru.execbit.aiosmscallslog.toBitmap
import ru.execbit.aiosmscallslog.toDateTimeString
import java.text.SimpleDateFormat

class SmsActions(
    private val context: Context,
    private val cn: ComponentName,
    private val data: SmsData,
    private val smses: List<Sms>
) {
    companion object {
        const val REPLY_BUTTON_ID = -1000
        const val DIAL_BUTTON_ID = -1001

        const val MENU_BUTTON_PHONE = -0
        const val MENU_BUTTON_SMS = -1
    }

    private var openedSms: Sms? = null

    fun processAction(intent: Intent) {
        try {
            intent.getParcelableExtra<PluginAction>("action")?.let { action ->
                when (action.context) {
                    "tap" -> processTapAction(action)
                    "longtap" -> processLongTapAction(action)
                    "menu" -> processMenuAction(action)
                    "dialog" -> processDialogAction(action)
                    else -> sendError()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendError() {
        val result = PluginResult(
            from = cn,
            data = PluginError(INVALID_ACTION_ERROR, context.getString(R.string.invalid_action))
        )
        context.sendPluginResult(result)
    }

    private fun processTapAction(action: PluginAction) {
        try {
            val sms = smses.getOrNull((action.selectedIds[0]))
            if (sms == null) {
                data.generateAndSendResult()
                return
            }

            val dialogTitle = when {
                sms.name.isEmpty() -> sms.number
                else -> sms.name
            }

            val dialogSubTitle = sms.date.toDateTimeString()

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
                title = "$dialogTitle\n$dialogSubTitle",
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

    private fun processLongTapAction(action: PluginAction) {
        try {
            val sms = smses.getOrNull(action.selectedIds[0])
            if (sms == null) {
                data.generateAndSendResult()
                return
            }

            val menu = listOf(
                PluginButton(
                    text = "Phone",
                    icon = context.getCompatDrawable(R.drawable.ic_phone)?.toBitmap(),
                    id = MENU_BUTTON_PHONE
                ),
                PluginButton(
                    text = "SMS",
                    icon = context.getCompatDrawable(R.drawable.ic_sms)?.toBitmap(),
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

    private fun processDialogAction(action: PluginAction) {
        when {
            action.selectedIds.contains(REPLY_BUTTON_ID) -> {
                context.openMessages(cn, openedSms?.number)
            }

            action.selectedIds.contains(DIAL_BUTTON_ID) -> {
                context.makeCall(cn, openedSms?.number)
            }
        }
        openedSms = null
    }

    private fun processMenuAction(action: PluginAction) {
        if (action.selectedIds.isNotEmpty()) {
            when (action.selectedIds[0]) {
                MENU_BUTTON_PHONE -> context.makeCall(cn, openedSms?.number)
                MENU_BUTTON_SMS -> context.openMessages(cn, openedSms?.number)
            }
            openedSms = null
        }
    }

}