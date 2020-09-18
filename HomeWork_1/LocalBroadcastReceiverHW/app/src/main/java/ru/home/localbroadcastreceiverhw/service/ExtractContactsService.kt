package ru.home.localbroadcastreceiverhw.service

import android.content.Intent
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ru.home.localbroadcastreceiverhw.Contact


class ExtractContactsService : JobIntentService() {

    companion object {
        private const val TAG = "ExtractContactsService"

        internal const val ACTION_CONTACTS_SERVICE =
            "ru.home.localbroadcastreceiverhw.service.RESPONSE"
        internal const val EXTRA_KEY_OUT = "EXTRA_OUT"

        internal const val WORK_TAG = "SERVICE_WORK"
    }

    private fun Cursor.extractPhones(): MutableList<String> {
        val phones = mutableListOf<String>()
        while (moveToNext()) {
            phones.add(
                getString(
                    getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
            )
//            Log.d(TAG, "Contact: $name has phone: $phoneNumber")
        }
        close()
        return phones
    }

    private fun error(message: String): Nothing {
        Log.w(TAG, message)
        throw IllegalArgumentException(message)
    }

    private fun extractContacts(): ArrayList<Contact> {
        val contacts = ArrayList<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        ) ?: error("Error getting cursor from contentResolver, cursor is null")
        cursor.moveToFirst()
        do {
            val id = cursor.getString(
                cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID)
            )
            val name = cursor.getString(
                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            )
            val phones = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                null,
                null
            ).let { it?.extractPhones() }
            val contact = Contact(id, name, phones)
            Log.d(TAG, contact.toString())
            contacts.add(contact)
        } while (cursor.moveToNext())
        cursor.close()
        return contacts
    }

    override fun onHandleWork(intent: Intent) {
        val label = intent.getStringExtra(WORK_TAG)
        Log.d(TAG, "onHandleWork started: working with $label")
//        TimeUnit.SECONDS.sleep(5)
        val contacts = extractContacts()
        Log.d(TAG, "altogether ${contacts.size} contacts are extracted")
        if (contacts.isEmpty().not()) Log.d(TAG, "first contact is:\n${contacts[0]}")

//        val serviceOutput = "This is a message from the service"
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent()
                .setAction(ACTION_CONTACTS_SERVICE)
                .putParcelableArrayListExtra(EXTRA_KEY_OUT, contacts)
        )
    }

}