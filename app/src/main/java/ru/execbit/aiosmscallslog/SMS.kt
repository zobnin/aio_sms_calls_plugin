package ru.execbit.aiosmscallslog

import android.content.Context
import android.net.Uri
import ru.execbit.aiolauncher.models.Sms

object SMS {
    fun getSms(context: Context, limit: Int = 10): MutableList<Sms> {
        val items = mutableListOf<Sms>()
        var num = 0

        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            null,
            null,
            null,
            null
        )

        cursor?.use {
            if (cursor.moveToFirst()) { // must check the result to prevent exception
                do {
                    var address = ""
                    var date = ""
                    var body = ""

                    for (idx in 0 until cursor.columnCount) {
                        when (cursor.getColumnName(idx)) {
                            "address" -> address = cursor.getString(idx)
                            "date" -> date = cursor.getString(idx)
                            "body" -> body = cursor.getString(idx)
                        }
                    }

                    val sms = Sms(
                        number = address,
                        name = Contacts.getContactName(context, address),
                        date = date.toLong(),
                        body = body
                    )

                    items.add(sms)
                    num++

                } while (cursor.moveToNext() && num < limit)
            }
        }

        return items
    }
}
