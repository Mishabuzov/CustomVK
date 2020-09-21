package ru.home.localbroadcastreceiverhw.service

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ru.home.localbroadcastreceiverhw.Contact
import java.util.concurrent.TimeUnit

/**
 * Since IntentService is deprecated, I've implemented JobIntentService as recommended substitution.
 * This service calls ContentProvider for extracting user contacts (if any) and pass it to the
 * source activity (ServiceActivity) via LocalBroadcastReceiver.
 */
class ExtractContactsService : JobIntentService() {

    companion object {
        private const val TAG = "ExtractContactsService"
        private const val JOB_ID = 1

        internal const val ACTION_CONTACTS_SERVICE =
            "ru.home.localbroadcastreceiverhw.service.RESPONSE"
        internal const val EXTRA_KEY_OUT = "EXTRA_OUT"

        internal fun enqueueWork(context: Context) =
            enqueueWork(context, ExtractContactsService::class.java, JOB_ID, Intent())
    }

    private fun Cursor.extractPhones(): MutableList<String> {
        val phones = mutableListOf<String>()
        while (moveToNext())
            phones.add(getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))
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
        while (cursor.moveToNext()) {
            val id = cursor.getString(
                cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID)
            )
            val name = cursor.getString(
                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            )
            val phones = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + " = " + id,
                null,
                null
            )?.extractPhones()
            val contact = Contact(id, name, phones)
            log(contact.toString())
            contacts.add(contact)
        }
        cursor.close()
        return contacts
    }

    private fun log(message: String) = Log.d(TAG, message)

    override fun onHandleWork(intent: Intent) {
        TimeUnit.SECONDS.sleep(2)  // HardWork emulation.
        val contacts = extractContacts()
        log("altogether ${contacts.size} contacts are extracted")

        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent()
                .setAction(ACTION_CONTACTS_SERVICE)
                .putParcelableArrayListExtra(EXTRA_KEY_OUT, contacts)
        )
    }

}