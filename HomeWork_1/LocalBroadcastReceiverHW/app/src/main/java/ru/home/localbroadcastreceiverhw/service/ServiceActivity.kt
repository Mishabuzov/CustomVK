package ru.home.localbroadcastreceiverhw.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ServiceActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_CONTACTS = 1
        internal const val SERVICE_RESULT_KEY = "service result message"

        fun startForContacts(activity: AppCompatActivity) = activity.startActivityForResult(
            Intent(activity, ServiceActivity::class.java),
            REQUEST_CODE_CONTACTS
        )

        private fun startContactsService(activity: AppCompatActivity) = activity.startService(
            Intent(activity, ExtractContactsService::class.java)
                .putExtra(ExtractContactsService.WORK_TAG, "Android work")
        )
    }

    private val contactsBroadcastReceiver by lazy { ContactsBroadcastReceiver() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startContactsService(this)
    }

    override fun onStart() {
        super.onStart()
        registerContactsReceiver()
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

    inner class ContactsBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val serviceResult = intent?.getStringExtra(ExtractContactsService.EXTRA_KEY_OUT)
            setResult(RESULT_OK, Intent().putExtra(SERVICE_RESULT_KEY, serviceResult))
            finish()
        }
    }

}