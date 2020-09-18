package ru.home.localbroadcastreceiverhw.service

import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.concurrent.TimeUnit

class ExtractContactsService : JobIntentService() {

    companion object {
        internal const val ACTION_CONTACTS_SERVICE =
            "ru.home.localbroadcastreceiverhw.service.RESPONSE"
        internal const val EXTRA_KEY_OUT = "EXTRA_OUT"

        internal const val WORK_TAG = "SERVICE_WORK"
    }

    override fun onHandleWork(intent: Intent) {
        val label = intent.getStringExtra(WORK_TAG)
        Log.d("ExtractContactsService", "onHandleWork started: working with $label")
        TimeUnit.SECONDS.sleep(5)
        Log.d("ExtractContactsService", "work emulation is done")

        val serviceOutput = "This is a message from the service"
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent()
                .setAction(ACTION_CONTACTS_SERVICE)
                .putExtra(EXTRA_KEY_OUT, serviceOutput)
        )
    }

}