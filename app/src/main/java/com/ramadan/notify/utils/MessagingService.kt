package com.ramadan.notify.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ramadan.notify.MainActivity
import com.ramadan.notify.R
import com.ramadan.notify.utils.ScheduledWorker.Companion.NOTIFICATION_MESSAGE
import com.ramadan.notify.utils.ScheduledWorker.Companion.NOTIFICATION_TITLE
import java.text.SimpleDateFormat
import java.util.*


class MessagingService : FirebaseMessagingService() {


    private val TAG = "FireBaseMessagingService"
    var NOTIFICATION_CHANNEL_ID = "midnight_notification"
    val NOTIFICATION_ID = 100

    fun onTokenRefresh() {
        // Get updated InstanceID token.
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "Refreshed token: $refreshedToken")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(refreshedToken)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.e(TAG, "Message is not empty ...");
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            val message = remoteMessage.data["message"]
            Log.d(TAG, "title => $title")
            Log.d(TAG, "body => $body")
            Log.d(TAG, "Message => $message")

            // Check that 'Automatic Date and Time' settings are turned ON.
            // If it's not turned on, Return
            if (!isTimeAutomatic(applicationContext)) {
                println("Automatic Date and Time` is not enabled")
            } else {
                println("Automatic Date and Time` is  enabled")

            }

            // Check whether notification is scheduled or not
            val isScheduled = remoteMessage.data["isScheduled"]
            if (isScheduled == "true") {
                val scheduledTime = remoteMessage.data["scheduledTime"]
                scheduleAlarm(scheduledTime, title, body)
            } else {
                showNotification(title!!, body!!)
            }

        } else {
            Log.e(TAG, "Message without data");
            val title = remoteMessage.notification!!.title
            val body = remoteMessage.notification!!.body

            // Check that 'Automatic Date and Time' settings are turned ON.
            // If it's not turned on, Return
            if (!isTimeAutomatic(applicationContext)) {
                println("Automatic Date and Time` is not enabled")
            } else {
                println("Automatic Date and Time` is  enabled")
            }
            showNotification(title!!, body!!)
        }

    }

    private fun scheduleAlarm(
        scheduledTimeString: String?,
        title: String?,
        message: String?,
    ) {
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent =
            Intent(applicationContext, NotificationBroadcastReceiver::class.java).let { intent ->
                intent.putExtra(NOTIFICATION_TITLE, title)
                intent.putExtra(NOTIFICATION_MESSAGE, message)
                PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
            }

        // Parse Schedule time
        val scheduledTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .parse(scheduledTimeString!!)

        scheduledTime?.let {
            // With set(), it'll set non repeating one time alarm.
            alarmMgr.set(
                AlarmManager.RTC_WAKEUP,
                it.time,
                alarmIntent
            )
        }
    }


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.e(TAG, "New Token")
    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        val handler = Handler(Looper.getMainLooper())

        handler.post(Runnable {
            Toast.makeText(baseContext,
                remoteMessage.messageType,
                Toast.LENGTH_LONG).show()
        }
        )
    }

    private fun checkGooglePlayServices(): Boolean {
        // 1
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        // 2
        return if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Error")
            // ask user to update google play services and manage the error.
            false
        } else {
            // 3
            Log.i(TAG, "Google play services updated")
            true
        }
    }


    fun showNotification(
        title: String?,
        message: String?,
    ) {
        val ii = Intent(this, MainActivity::class.java)
        ii.data = Uri.parse("custom://" + System.currentTimeMillis())
        ii.action = "actionstring" + System.currentTimeMillis()
        ii.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pi =
            PendingIntent.getActivity(this, 0, ii, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setVibrate(LongArray(1000))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setWhen(System.currentTimeMillis())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .build()
            val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                title,
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } else {
            notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setVibrate(LongArray(1000))
                .setAutoCancel(true)
                .setContentText(message)
                .setContentIntent(pi)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title).build()
            val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }


}
