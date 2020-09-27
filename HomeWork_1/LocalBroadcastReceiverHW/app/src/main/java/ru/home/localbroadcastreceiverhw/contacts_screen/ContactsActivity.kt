package ru.home.localbroadcastreceiverhw.contacts_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_contacts.*
import ru.home.localbroadcastreceiverhw.Contact
import ru.home.localbroadcastreceiverhw.R
import ru.home.localbroadcastreceiverhw.service.ExtractContactsService
import ru.home.localbroadcastreceiverhw.service.ServiceActivity

/**
 * Activity for displaying contacts.
 */
class ContactsActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 100
        private const val LOADING_FLAG = "loading_flag"
    }

    private val contactsViewModel by lazy {
        ViewModelProvider(this).get(ContactsViewModel::class.java)
    }

    private lateinit var adapter: ContactsAdapter

    private var isContactsLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        adapter = ContactsAdapter(contacts_recycler, empty_text_view)
        isContactsLoaded = savedInstanceState?.getBoolean(LOADING_FLAG) ?: false
        contactsViewModel.contactsList.observe(this, adapter::refreshContacts)
        if (isContactsLoaded.not()) checkPermissionAndStartServiceActivity()
    }

    /**
     * Explicitly ask user for the contacts permission in case of launching the app in
     * Android Marshmallow and higher APIs.
     */
    private fun checkPermissionAndStartServiceActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(
            arrayOf(Manifest.permission.READ_CONTACTS),
            PERMISSIONS_REQUEST_READ_CONTACTS
        ) else ServiceActivity.startForContacts(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ServiceActivity.startForContacts(this)
            } else showPermissionError()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showPermissionError() {
        empty_text_view.text = getString(R.string.no_contacts_permission_notification)
        adapter.refreshRecycler()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(LOADING_FLAG, isContactsLoaded)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val contacts: ArrayList<Contact> =
                data?.getParcelableArrayListExtra(ExtractContactsService.EXTRA_KEY_OUT)!!
            isContactsLoaded = true
            contactsViewModel.contactsList.value = contacts
        }
    }

    class ContactsViewModel : ViewModel() {
        val contactsList: MutableLiveData<List<Contact>> = MutableLiveData()
    }
}