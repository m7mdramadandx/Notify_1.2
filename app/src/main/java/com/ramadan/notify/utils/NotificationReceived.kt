package com.ramadan.notify.utils

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal
import com.ramadan.notify.R


internal class NotificationReceived : OneSignal.OSRemoteNotificationReceivedHandler {

    override fun remoteNotificationReceived(
        context: Context?,
        notificationReceivedEvent: OSNotificationReceivedEvent?,
    ) {
        val notification = notificationReceivedEvent!!.notification
        val data = notification.additionalData
        Log.i("TOTO", "Received Notification Data: $data")
        val mutableNotification = notification.mutableCopy()
        mutableNotification.setExtender { builder: NotificationCompat.Builder ->
            builder.setColor(context!!.resources.getColor(R.color.colorPrimary))
                .setNumber(5)
                .setChannelId(notification.notificationId)
        }
        notificationReceivedEvent.complete(mutableNotification)
    }
}

