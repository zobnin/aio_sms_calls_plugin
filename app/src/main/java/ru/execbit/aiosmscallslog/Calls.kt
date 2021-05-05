package ru.execbit.aiosmscallslog

import android.content.Context
import android.provider.CallLog
import ru.execbit.aiolauncher.models.Call

object Calls {
    fun getCalls(context: Context): List<Call> {
        val calls = ArrayList<Call>()

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (cursor.moveToNext()) {
                val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                val name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                val type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
                val date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))

                if (number == null) continue
                if (type == CallLog.Calls.BLOCKED_TYPE) continue

                val direction = when (type) {
                    CallLog.Calls.OUTGOING_TYPE -> "outgoing"
                    CallLog.Calls.INCOMING_TYPE -> "incoming"
                    CallLog.Calls.MISSED_TYPE -> "missed"
                    else -> ""
                }

                val call = Call(
                    number = number,
                    cachedName = name ?: "",
                    date = date,
                    direction = direction
                )
                calls.add(call)
            }
        }

        // Some phones return calls in reversed order
        return calls.sortedBy { it.date }.reversed()
    }
}