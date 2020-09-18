package ru.home.localbroadcastreceiverhw.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_service.*
import ru.home.localbroadcastreceiverhw.R

class ServiceActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_CONTACTS = 1
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 100
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
        setContentView(R.layout.activity_service)
        checkContactsPermissionAndStartService()
    }

    private fun checkContactsPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else startContactsService(this)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startContactsService(this)
            } else {
                notification_text_view.text =
                    getString(R.string.no_contacts_permission_notification)
                progress_loading.visibility = GONE
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    inner class ContactsBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
//            val serviceResult = intent?.getStringExtra(ExtractContactsService.EXTRA_KEY_OUT)
//            setResult(RESULT_OK, Intent().putExtra(SERVICE_RESULT_KEY, serviceResult))
            setResult(RESULT_OK, intent)
            finish()
        }
    }

}