package com.ramadan.notify

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_T
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import com.ramadan.notify.utils.NotificationOpenedHandler
import java.util.*

const val ONESIGNAL_APP_ID = "070ffbba-e43e-4748-afa9-cdab4c805d81"

@Suppress("DEPRECATION")
class Application : Application() {

    override fun onCreate() {
        super.onCreate()
//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
//        OneSignal.setNotificationOpenedHandler(NotificationOpenedHandler(this))
//        OneSignal.pauseInAppMessages(false)
//        OneSignal.unsubscribeWhenNotificationsAreDisabled(true)
//        OneSignal.initWithContext(this)
//        OneSignal.setAppId(ONESIGNAL_APP_ID)
        FirebaseMessaging.getInstance().subscribeToTopic("allUsers")
        val conf = RequestConfiguration.Builder()
//            .setTestDeviceIds(listOf("95B73043073F86AAE72BC71577E335B0"))
            .setMaxAdContentRating(MAX_AD_CONTENT_RATING_T)
            .build()
        MobileAds.setRequestConfiguration(conf)
        MobileAds.initialize(this, getString(R.string.ad_id))
    }
}