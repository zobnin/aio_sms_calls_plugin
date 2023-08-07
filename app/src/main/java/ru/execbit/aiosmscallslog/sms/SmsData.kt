package ru.execbit.aiosmscallslog.sms

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import ru.execbit.aiolauncher.models.PluginError
import ru.execbit.aiolauncher.models.PluginHideCard
import ru.execbit.aiolauncher.models.PluginLine
import ru.execbit.aiolauncher.models.PluginLines
import ru.execbit.aiolauncher.models.PluginResult
import ru.execbit.aiolauncher.models.PluginShowCard
import ru.execbit.aiolauncher.models.SearchPluginLines
import ru.execbit.aiolauncher.models.Sms
import ru.execbit.aiolauncher.plugin.sendPluginResult
import ru.execbit.aiosmscallslog.INTERNAL_ERROR
import ru.execbit.aiosmscallslog.Settings
import ru.execbit.aiosmscallslog.sms.SmsPluginReceiver.Companion.MAX_LOADED_SMS
import ru.execbit.aiosmscallslog.sms.SmsPluginReceiver.Companion.MAX_SEARCH_RESULTS

class SmsData(
    private val context: Context,
    private val cn: ComponentName,
    private val smses: List<Sms>,
) {
    fun processGetData(intent: Intent) {
        intent.getStringExtra("event")?.let { event ->
            when (event) {
                "load" -> generateAndSendResult()
                "force" -> generateAndSendResult()
                "resume" -> generateAndSendResult()
                "search" -> generateAndSendSearchResult(intent)
                else -> return
            }
        }
    }

    fun generateAndSendResult() {
        try {
            val lines = generateList(Settings.smsNum.toInt())

            context.sendPluginResult(
                PluginResult(
                    from = cn,
                    data = PluginLines(
                        lines = lines,
                        privateModeSupport = true
                    )
                )
            )

            if (lines.isEmpty() && Settings.smsAutoHide) {
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
        if (!Settings.smsEnableSearch) return

        val data = intent.getStringExtra("data") ?: return
        val result = generateSearchResult(data) ?: return

        context.sendPluginResult(result)
    }

    private fun generateSearchResult(string: String): PluginResult? {
        if (string.isBlank()) return null

        return try {
            val lines = generateList(showRead = true).filter {
                it.from.contains(string, ignoreCase = true) ||
                        it.body.contains(string, ignoreCase = true)
            }

            if (lines.isEmpty()) return null

            PluginResult(
                from = cn,
                data = SearchPluginLines(lines.take(MAX_SEARCH_RESULTS))
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun generateList(
        limit: Int = MAX_LOADED_SMS,
        showRead: Boolean = Settings.smsShowRead
    ): List<PluginLine> {

        val lines = when {
            showRead -> smses.take(limit)
            else -> smses.filterNot { it.isRead }.take(limit)
        }

        return lines.map { sms ->
            PluginLine(
                body = truncateSms(sms.body),
                from = sms.name.ifEmpty { sms.number },
                id = smses.indexOf(sms)
            )
        }
    }

    private fun truncateSms(content: String): String {
        return if (content.length > 140) {
            content.take(140) + "…"
        } else {
            content
        }
    }
}