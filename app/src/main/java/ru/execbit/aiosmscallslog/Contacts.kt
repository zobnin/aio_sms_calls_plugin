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
        val cursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null)

        val calls = ArrayList<Call>()

        cursor?.let {
            val numberIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE)

            while (cursor.moveToNext()) {
                val callType = cursor.getString(typeIdx)
                val dirCode = callType.toInt()

                val direction: String

                direction = when (dirCode) {
                    CallLog.Calls.OUTGOING_TYPE -> "outgoing"
                    CallLog.Calls.INCOMING_TYPE -> "incoming"
                    CallLog.Calls.MISSED_TYPE -> "missed"
                    else -> ""
                }

                val call = Call(
                        number = cursor.getString(numberIdx),
                        cachedName = cursor.getString(nameIdx),
                        date = cursor.getString(dateIdx).toLong(),
                        direction = direction
                )
                calls.add(call)
            }
            cursor.close()
        }

        // Some phones return calls in reversed order
        return calls.sortedBy { it.date }.reversed()
    }

    fun getContacts(context: Context): List<Contact> {
        val contacts = ArrayList<Contact>()

        val phones = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phones.moveToNext()) {
            val contactId = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            val contactName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val isPrimary = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY))

            // Meizu can have many duplicated numbers with commas
            if (contactNumber.contains(',')) {
                continue
            }

            val contact = Contact(contactName, contactId, contactNumber)

            if (isPrimary > 0) {
                contact.default = true
            }

            contacts.add(contact)
        }
        phones.close()

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

