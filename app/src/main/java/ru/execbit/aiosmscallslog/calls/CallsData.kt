package ru.execbit.aiosmscallslog.calls

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.telephony.PhoneNumberUtils
import ru.execbit.aiolauncher.models.Call
import ru.execbit.aiolauncher.models.PluginButton
import ru.execbit.aiolauncher.models.PluginButtons
import ru.execbit.aiolauncher.models.PluginError
import ru.execbit.aiolauncher.models.PluginHideCard
import ru.execbit.aiolauncher.models.PluginResult
import ru.execbit.aiolauncher.models.PluginShowCard
import ru.execbit.aiolauncher.models.SearchPluginButtons
import ru.execbit.aiolauncher.plugin.sendPluginResult
import ru.execbit.aiosmscallslog.INTERNAL_ERROR
import ru.execbit.aiosmscallslog.Settings
import ru.execbit.aiosmscallslog.getTruncatedName
import java.util.Date

class CallsData(
    private val context: Context,
    private val cn: ComponentName,
    private val calls: List<Call>,
) {
    companion object {
        const val A_4_HOURS = 14400000L
        const val A_8_HOURS = 28800000L
        const val A_12_HOURS = 43200000L

        // From bright to lighter
        val red1 = Color.parseColor("#F44336")
        val red2 = Color.parseColor("#EF5350")
        val red3 = Color.parseColor("#E57373")
    }

    fun processGetData(intent: Intent) {
        intent.getStringExtra("event")?.let { event ->
            when (event) {
                "load" -> generateAndSendResult()
                "force" -> generateAndSendResult()
                "resume" -> generateAndSendResult()
                "search" -> generateAndSendSearchResult(intent)
                else -> {}
            }
        }
    }

    fun generateAndSendResult() {
        try {
            val buttons = generateButtons(context, Settings.callsShowOnlyMissed)

            context.sendPluginResult(
                PluginResult(
                    from = cn,
                    data = PluginButtons(
                        buttons,
                        maxLines = Settings.callsNum.toInt(),
                        privateModeSupport = true
                    )
                )
            )

            if (buttons.isEmpty() && Settings.callsAutoHide) {
                context.sendPluginResult(
                    PluginResult(cn, PluginHideCard())
                )
            } else {
                context.sendPluginResult(
                    PluginResult(cn, PluginShowCard())
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()

            context.sendPluginResult(
                PluginResult(
                    from = cn,
                    data = PluginError(INTERNAL_ERROR, e.toString())
                )
            )
        }
    }

    private fun generateAndSendSearchResult(intent: Intent) {
        if (!Settings.callsEnableSearch) return

        val data = intent.getStringExtra("data") ?: return
        val result = generateSearchResult(context, data) ?: return

        context.sendPluginResult(result)
    }

    private fun generateSearchResult(context: Context, string: String): PluginResult? {
        if (string.isBlank()) return null

        return try {
            val buttons = generateButtons(context).filter {
                it.text.contains(string, ignoreCase = true)
            }

            if (buttons.isEmpty()) return null

            PluginResult(
                from = cn,
                data = SearchPluginButtons(buttons)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun generateButtons(
        context: Context,
        onlyMissed: Boolean = false,
    ): MutableList<PluginButton> {

        val contacts = Contacts.getContacts(context)
        val buttons = mutableListOf<PluginButton>()
        val maxIdx = Settings.callsNum.toInt() * 10

        calls.forEach { call ->
            if (call.direction != "missed" && onlyMissed) {
                return@forEach
            }

            val contact = contacts.find { PhoneNumberUtils.compare(it.phone, call.number) }
            if (contact == null && !Settings.callsShowUnknown) {
                return@forEach
            }

            if (buttons.find { it.text == contact?.name } != null) {
                return@forEach
            }

            val text = when (contact?.name) {
                null -> call.number
                else -> contact.name.getTruncatedName()
            }

            val button = PluginButton(
                text = text,
                backgroundColor = getButtonColor(call),
                extra = call.number,
                id = calls.indexOf(call)
            )

            buttons.add(button)

            if (buttons.size >= maxIdx) {
                return@forEach
            }
        }

        return buttons
    }

    private fun getButtonColor(call: Call): Int {
        val date = Date().time

        return when {
            call.direction == "missed" && date - call.date < A_4_HOURS -> red1
            call.direction == "missed" && date - call.date < A_8_HOURS -> red2
            call.direction == "missed" && date - call.date < A_12_HOURS -> red3
            else -> 0
        }
    }
}