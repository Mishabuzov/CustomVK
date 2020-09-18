package ru.home.localbroadcastreceiverhw.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_contacts.*
import ru.home.localbroadcastreceiverhw.R
import ru.home.localbroadcastreceiverhw.service.ServiceActivity

class ContactsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        ServiceActivity.startForContacts(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val serviceMessage = data?.getStringExtra(ServiceActivity.SERVICE_RESULT_KEY)
            message_text_view.text = serviceMessage
        }
    }
}