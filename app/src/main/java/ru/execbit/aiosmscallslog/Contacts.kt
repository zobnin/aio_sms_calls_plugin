package ru.execbit.aiosmscallslog

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.ContactsContract.PhoneLookup
import ru.execbit.aiolauncher.models.Call
import ru.execbit.aiolauncher.models.Contact

@SuppressLint("MissingPermission")
object Contacts {
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

    fun getContacts(context: Context): List<Contact> {
        val contacts = ArrayList<Contact>()

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (cursor.moveToNext()) {
                val contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val isPrimary = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY))

                if (contactName == null || contactNumber == null) continue
                // Meizu can have many duplicated numbers with commas
                if (contactNumber.contains(',')) continue

                val contact = Contact(contactName, contactId, contactNumber)

                if (isPrimary > 0) {
                    contact.default = true
                }

                contacts.add(contact)
            }
        }

        return contacts
    }

    fun getContactName(context: Context, phoneNumber: String): String {
        val cr = context.contentResolver
        val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val cursor = cr.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null)
            ?: return ""
        var contactName = ""

        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME))
        }

        if (!cursor.isClosed) {
            cursor.close()
        }

        return contactName
    }
}

