package ru.home.localbroadcastreceiverhw.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ru.home.localbroadcastreceiverhw.R

class ServiceActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_CONTACTS = 1

        fun startForContacts(activity: AppCompatActivity) = activity.startActivityForResult(
            Intent(activity, ServiceActivity::class.java),
            REQUEST_CODE_CONTACTS
        )

        private fun startContactsService(activity: AppCompatActivity) =
            activity.startService(Intent(activity, ExtractContactsService::class.java))
    }

    private val contactsBroadcastReceiver by lazy { ContactsBroadcastReceiver() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_view)
    }

    override fun onStart() {
        super.onStart()
        registerContactsReceiver()
        startContactsService(this)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactsBroadcastReceiver)
    }

    private fun registerContactsReceiver() =
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(
                contactsBroadcastReceiver,
                IntentFilter(ExtractContactsService.ACTION_CONTACTS_SERVICE)
            )

    override fun onBackPressed() {}

    inner class ContactsBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            setResult(RESULT_OK, intent)
            finish()
        }
    }

}