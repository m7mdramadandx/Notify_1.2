package com.ramadan.notify.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal

class NotificationOpenedHandler(base: Context?) : OneSignal.OSNotificationOpenedHandler,
    ContextWrapper(base) {

    override fun notificationOpened(result: OSNotificationOpenedResult?) {
        val data = result!!.notification.additionalData

//        when (data["intentName"]) {
//            "topic" -> Intent(applicationContext, Topic::class.java)
//            "video" -> Intent(applicationContext, VideosList::class.java)
//            "story" -> Intent(applicationContext, Story::class.java)
//            "ramadan" -> Intent(applicationContext, Quote::class.java)
//            "hadiths" -> Intent(applicationContext, Hadiths::class.java)
//            else -> Intent(applicationContext, Dashboard::class.java)
//        }.also {
//            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            it.putExtra("documentID", data["documentID"].toString())
//            it.putExtra("collectionID", data["collectionID"].toString())
//            startActivity(it)
//        }
    }
}