package ru.home.localbroadcastreceiverhw.contacts_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_contacts.*
import ru.home.localbroadcastreceiverhw.Contact
import ru.home.localbroadcastreceiverhw.R
import ru.home.localbroadcastreceiverhw.service.ExtractContactsService
import ru.home.localbroadcastreceiverhw.service.ServiceActivity

class ContactsActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    }

    private lateinit var adapter: ContactsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        adapter = ContactsAdapter(contacts_recycler, empty_text_view)
        checkPermissionAndStartServiceActivity()
    }

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val contacts: ArrayList<Contact> =
                data?.getParcelableArrayListExtra(ExtractContactsService.EXTRA_KEY_OUT)!!
            adapter.refreshContacts(contacts)
        }
    }
}