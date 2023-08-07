package ru.execbit.aiosmscallslog.sms

import android.content.Context
import android.net.Uri
import ru.execbit.aiolauncher.models.Sms
import ru.execbit.aiosmscallslog.calls.Contacts

object SMS {
    fun getSms(context: Context, limit: Int = 10): MutableList<Sms> {
        val items = mutableListOf<Sms>()
        var num = 0

        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/inbox"), null, null, null, null
        )

        cursor?.use {
            if (cursor.moveToFirst()) { // must check the result to prevent exception
                do {
                    var sms = Sms()

                    for (idx in 0 until cursor.columnCount) {
                        when (cursor.getColumnName(idx)) {
                            "address" -> sms = sms.copy(number = cursor.getString(idx))
                            "date" -> sms = sms.copy(date = cursor.getString(idx).toLong())
                            "body" -> sms = sms.copy(body = cursor.getString(idx))
                            "read" -> sms = sms.copy(isRead = cursor.getInt(idx) == 1)
                        }
                    }

                    sms = sms.copy(name = Contacts.getContactName(context, sms.number))
                    items.add(sms)
                    num++

                } while (cursor.moveToNext() && num < limit)
            }
        }

        return items
    }
}
