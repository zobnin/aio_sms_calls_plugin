package ru.execbit.aiosmscallslog

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.telephony.PhoneNumberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.execbit.aiolauncher.models.*
import ru.execbit.aiolauncher.plugin.*

class CallsPluginReceiver : BroadcastReceiver() {
    companion object {
        const val DIAL_BUTTON_ID = -1000
        const val SMS_BUTTON_ID = -1001
        const val DONT_SHOW_AGAIN_ID = -1002

        const val MENU_BUTTON_PHONE = -0
        const val MENU_BUTTON_SMS = -1
        const val MENU_BUTTON_INFO = -2

        val redColor = Color.parseColor("#F44336")

        private var cn: ComponentName? = null
        private var calls = emptyList<Call>()
        private var openedCall: Call? = null
        private var openedPersonContacts: List<Contact>? = null
    }

    override fun onReceive(context: Context, intent: Intent?) {
        cn = ComponentName(context.packageName, context.packageName + ".CallsPluginReceiver")

        CoroutineScope(Dispatchers.Default).launch {
            if (intent == null) return@launch
            if (!checkUid(intent)) return@launch
            if (!checkAioVersion(context, App.REQUIRED_AIO_VERSION)) {
                context.sendInvalidAioVersionError(cn!!)
                return@launch
            }

            when (intent.action) {
                PluginIntentActions.PLUGIN_GET_DATA -> processGetData(context, intent)
                PluginIntentActions.PLUGIN_SEND_ACTION -> processAction(context, intent)
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
            val contacts = Contacts.getContacts(context)

            val call = getCallById(action.selectedIds[0])
            if (call == null) {
                generateAndSendResult(context)
                return
            }

            /*
            if (!Settings.callsConfirmation) {
                context.makeCall(cn, call.number)
                return
            }
            */

            val contact = contacts.find { PhoneNumberUtils.compare(it.phone, call.number) }

            context.sendPluginResult(
                PluginResult(
                    from = cn,
                    data = PluginCallDialog(
                        number = call.number,
                        contactId = contact?.id ?: 0
                    )
                )
            )

            /*
            val radioButtons = if (contact != null) {

                val radioButtons = mutableListOf<PluginRadioButton>()

                openedPersonContacts = contacts
                    .filter { it.id == contact.id }
                    .distinctBy { it.phone.replace("[\\D]".toRegex(), "") }

                openedPersonContacts?.apply {
                    var haveDefaultButton = false

                    forEachIndexed { idx, it ->
                        val isChecked = this.size == 1 || it.default
                        haveDefaultButton = isChecked == true

                        radioButtons.add(
                            PluginRadioButton(
                                text = it.phone,
                                checked = isChecked,
                                id = idx
                            )
                        )
                    }

                    // Not all phones mark default numbers
                    if (!haveDefaultButton && size > 0) {
                        radioButtons[0].checked = true
                    }
                }

                radioButtons
            } else {
                // Only one checkbox for unknown numbers
                listOf(PluginRadioButton(text = call.number, id = 0, checked = true))
            }

            val checkBoxes = listOf(
                PluginCheckBox(
                    text = context.getString(R.string.dont_ask_again),
                    id = DONT_SHOW_AGAIN_ID
                )
            )

            val buttons = listOf(
                PluginButton(
                    text = context.getString(R.string.call),
                    id = DIAL_BUTTON_ID
                ),
                PluginButton(
                    text = context.getString(R.string.sms),
                    id = SMS_BUTTON_ID
                )
            )

            val dialog = PluginDialog(
                title = contact?.name ?: call.number,
                radioButtons = radioButtons,
                checkBoxes = checkBoxes,
                bottomButtons = buttons
            )

            val result = PluginResult(
                from = cn,
                data = dialog
            )

            context.sendPluginResult(result)
            openedCall = call
             */
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processLongTapAction(context: Context, action: PluginAction) {
        try {
            val call = getCallById(action.selectedIds[0])
            if (call == null) {
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
                ),
                PluginButton(
                    text = "Info",
                    icon = context.getDrawable(R.drawable.ic_info).toBitmap(),
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

    private fun processMenuAction(context: Context, action: PluginAction) {
        if (action.selectedIds.isNotEmpty()) {
            when (action.selectedIds[0]) {
                MENU_BUTTON_PHONE -> context.makeCall(cn, openedCall?.number)
                MENU_BUTTON_SMS -> context.openMessages(cn, openedCall?.number)
                MENU_BUTTON_INFO -> context.showContact(cn, openedCall?.number)
            }
            openedCall = null
        }
    }

    private fun processDialogAction(context: Context, action: PluginAction) {
        var smsPressed = false
        var callPressed = false
        var dontShowChecked = false
        var checkBoxSelected = -1

        action.selectedIds.forEach {
            when (it) {
                SMS_BUTTON_ID -> smsPressed = true
                DIAL_BUTTON_ID -> callPressed = true
                DONT_SHOW_AGAIN_ID -> dontShowChecked = true
                else -> checkBoxSelected = it
            }
        }

        if (dontShowChecked) {
            Settings.callsConfirmation = false
        }

        if (checkBoxSelected >= 0) {
            val number = if (openedPersonContacts != null) {
                openedPersonContacts!![checkBoxSelected].phone
            } else {
                openedCall?.number
            }

            number?.let {
                if (smsPressed) {
                    context.openMessages(cn, number)
                } else if (callPressed) {
                    context.makeCall(cn, number)
                }
            }
        }

        openedCall = null
        openedPersonContacts = null
    }

    private fun processGetData(context: Context, intent: Intent) {
        intent.getStringExtra("event")?.let { event ->
            when (event) {
                "load" -> generateAndSendResult(context)
                "force" -> generateAndSendResult(context)
                //"alarm" -> {}
                "resume" -> generateAndSendResult(context)
            }
        }
    }

    private fun generateAndSendResult(context: Context) {
        context.sendPluginResult(generateResult(context))
    }

    private fun generateResult(context: Context): PluginResult {
        try {
            val allCalls = Contacts.getCalls(context)
            val filteredCalls = mutableListOf<Call>()
            val contacts = Contacts.getContacts(context)
            val buttons = mutableListOf<PluginButton>()

            val shownNames = mutableListOf<String>()

            var idx = 0
            val nonRepeatingCalls = allCalls
                .filterNot { it.number.isEmpty() }.distinctBy { it.number }
            val ids = generateIds(nonRepeatingCalls.size)

            nonRepeatingCalls.forEach { call ->
                val contact = contacts.find { PhoneNumberUtils.compare(it.phone, call.number) }

                if (contact == null && !Settings.callsShowUnknown) {
                    return@forEach
                }

                var canTruncate = true

                // We don't want to truncate call numbers and show already showed names
                val name = if (contact?.name == null) {
                    canTruncate = false
                    call.number
                } else {
                    if (shownNames.contains(contact.name)) {
                        return@forEach
                    }
                    shownNames.add(contact.name)
                    contact.name
                }

                val text = if (canTruncate) {
                    getTruncatedName(name)
                } else {
                    name
                }

                val buttonColor = if (call.direction == "missed") {
                    redColor
                } else {
                    0
                }

                val button = PluginButton(
                    text = text,
                    backgroundColor = buttonColor,
                    extra = call.number,
                    id = ids[idx]
                )

                buttons.add(button)
                filteredCalls.add(call)

                idx++
            }

            calls = filteredCalls
            Settings.callsIds = idsToString(ids)

            return PluginResult(
                from = cn,
                data = PluginButtons(
                    buttons,
                    maxLines = Settings.callsNum.toInt(),
                    privateModeSupport = true
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()

            return PluginResult(
                from = cn,
                data = PluginError(2, e.toString())
            )
        }
    }

    private fun getTruncatedName(fullName: String): String {
        return when (Settings.callsTruncateMethod) {
            "by_symbols" -> {
                if (fullName.length > 10) {
                    fullName.take(9) + '.'
                } else {
                    fullName
                }
            }

            "by_last_name" -> {
                if (fullName.trim().contains(' ')) {
                    val nameList = fullName.split(' ')
                    val firstName = nameList[0]
                    val lastName = if (nameList[1].length > 1) {
                        nameList[1].take(1) + '.'
                    } else {
                        nameList[1]
                    }
                    "$firstName $lastName"
                } else {
                    if (fullName.length > 10) {
                        fullName.take(9) + '.'
                    } else {
                        fullName
                    }
                }
            }

            else -> fullName
        }

    }

    private fun getCallById(id: Int): Call? {
        try {
            val ids = stringToIds(Settings.callsIds)

            if (ids.isEmpty()) {
                return null
            }

            val smsIdx = ids.indexOf(id)
            return calls[smsIdx]

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

}