package com.ramadan.notify.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    private val TAG = "FireBaseMessagingService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification!!.title
        val body = remoteMessage.notification!!.body
        val imageUrl = remoteMessage.notification!!.imageUrl
        NotificationUtil(applicationContext).showNotification(title!!, body!!, imageUrl!!)
    }


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.e(TAG, "New Token")
    }

}
