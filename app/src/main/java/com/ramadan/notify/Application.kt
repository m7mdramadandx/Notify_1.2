package com.ramadan.notify

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import com.ramadan.notify.utils.NotificationOpenedHandler

const val ONESIGNAL_APP_ID = "070ffbba-e43e-4748-afa9-cdab4c805d81"

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
//        OneSignal.OSRemoteNotificationReceivedHandler(NotificationReceived())
        OneSignal.setNotificationOpenedHandler(NotificationOpenedHandler(this))
        OneSignal.pauseInAppMessages(false)
        OneSignal.unsubscribeWhenNotificationsAreDisabled(true)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
        MobileAds.initialize(this, getString(R.string.ad_id))
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        FirebaseMessaging.getInstance().subscribeToTopic("allUsers")
    }
}