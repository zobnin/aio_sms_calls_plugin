package ru.execbit.aiosmscallslog.calls

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.telephony.PhoneNumberUtils
import ru.execbit.aiolauncher.models.Call
import ru.execbit.aiolauncher.models.PluginAction
import ru.execbit.aiolauncher.models.PluginButton
import ru.execbit.aiolauncher.models.PluginCallDialog
import ru.execbit.aiolauncher.models.PluginError
import ru.execbit.aiolauncher.models.PluginMenu
import ru.execbit.aiolauncher.models.PluginResult
import ru.execbit.aiolauncher.plugin.sendPluginResult
import ru.execbit.aiosmscallslog.INVALID_ACTION_ERROR
import ru.execbit.aiosmscallslog.R
import ru.execbit.aiosmscallslog.getCompatDrawable
import ru.execbit.aiosmscallslog.makeCall
import ru.execbit.aiosmscallslog.openMessages
import ru.execbit.aiosmscallslog.showContact
import ru.execbit.aiosmscallslog.toBitmap

class CallsActions(
    private val context: Context,
    private val cn: ComponentName,
    private val data: CallsData,
    private val calls: List<Call>,
) {
    companion object {
        const val MENU_BUTTON_PHONE = -0
        const val MENU_BUTTON_SMS = -1
        const val MENU_BUTTON_INFO = -2
    }

    private var openedCall: Call? = null

    fun processAction(intent: Intent) {
        try {
            intent.getParcelableExtra<PluginAction>("action")?.let { action ->
                when (action.context) {
                    "tap" -> processTapAction(action)
                    "longtap" -> processLongTapAction(action)
                    "menu" -> processMenuAction(action)
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
            val contacts = Contacts.getContacts(context)

            val call = calls.getOrNull(action.selectedIds[0])
            if (call == null) {
                data.generateAndSendResult()
                return
            }

            val contact = contacts.find {
                PhoneNumberUtils.compare(it.phone, call.number)
            }

            context.sendPluginResult(
                PluginResult(
                    from = cn,
                    data = PluginCallDialog(
                        number = call.number,
                        contactId = contact?.id ?: 0
                    )
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processLongTapAction(action: PluginAction) {
        try {
            val call = calls.getOrNull(action.selectedIds[0])
            if (call == null) {
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
                ),
                PluginButton(
                    text = "Info",
                    icon = context.getCompatDrawable(R.drawable.ic_info)?.toBitmap(),
                    id = MENU_BUTTON_INFO
                )
            )

            val result = PluginResult(
                from = cn,
                data = PluginMenu(menu)
            )

            context.sendPluginResult(result)
            openedCall = call

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processMenuAction(action: PluginAction) {
        if (action.selectedIds.isNotEmpty()) {
            when (action.selectedIds[0]) {
                MENU_BUTTON_PHONE -> context.makeCall(cn, openedCall?.number)
                MENU_BUTTON_SMS -> context.openMessages(cn, openedCall?.number)
                MENU_BUTTON_INFO -> context.showContact(cn, openedCall?.number)
            }
            openedCall = null
        }
    }
}