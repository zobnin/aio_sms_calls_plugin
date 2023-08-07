package ru.execbit.aiosmscallslog.calls

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.PhoneLookup
import ru.execbit.aiolauncher.models.Contact

@SuppressLint("MissingPermission")
object Contacts {
    fun getContacts(context: Context): List<Contact> {
        val contacts = ArrayList<Contact>()

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
        )

        cursor?.use {
            while (cursor.moveToNext()) {
                val contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val phoneType = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                val isPrimary = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY))

                if (contactName == null || contactNumber == null) continue
                if (contactNumber.contains(',')) continue

                val contact = Contact(contactName, contactId, contactNumber)

                if (isPrimary > 0 || phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MAIN.toString()) {
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

        var contactName = ""

        cursor?.use {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME))
            }
        }

        return contactName
    }
}

