package ru.home.localbroadcastreceiverhw.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_contacts.*
import ru.home.localbroadcastreceiverhw.Contact
import ru.home.localbroadcastreceiverhw.R
import ru.home.localbroadcastreceiverhw.service.ExtractContactsService
import ru.home.localbroadcastreceiverhw.service.ServiceActivity

class ContactsActivity : AppCompatActivity() {

    private val adapter by lazy { ContactsAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        initAdapter()
        ServiceActivity.startForContacts(this)
    }

    private fun initAdapter() {
        contacts_recycler.layoutManager = LinearLayoutManager(this)
        contacts_recycler.adapter = this.adapter
//        contacts_recycler
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