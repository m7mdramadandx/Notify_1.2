package com.ramadan.notify.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.ramadan.notify.MainActivity
import com.ramadan.notify.R
import kotlin.random.Random


class NotificationUtil(private val context: Context) {


    fun showNotification(title: String, message: String, imageUrl: Uri) {

        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUrl)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val notificationBuilder: NotificationCompat.Builder?
        notificationBuilder = NotificationCompat.Builder(context, "1001")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setTicker("Notify")
            .setLights(Color.CYAN, Color.MAGENTA, Color.RED)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVibrate(LongArray(10000))
            .setCategory(Notification.CATEGORY_STATUS)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setColor(context.resources.getColor(R.color.colorAccent))
            .setLargeIcon(bitmap)
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .bigLargeIcon(null))

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "1001",
                "default",
                NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.canShowBadge()
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }
}
