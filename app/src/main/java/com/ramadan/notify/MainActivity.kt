@file:Suppress("DEPRECATION")

package com.ramadan.notify

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.*
import com.google.android.material.tabs.TabLayout
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.ramadan.notify.ui.activity.*
import com.ramadan.notify.ui.adapter.ViewPagerAdapter
import com.ramadan.notify.ui.viewModel.AuthViewModel
import com.ramadan.notify.ui.viewModel.AuthViewModelFactory
import com.ramadan.notify.utils.MyNotificationPublisher
import com.ramadan.notify.utils.NotificationService
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment
import com.yalantis.contextmenu.lib.MenuObject
import com.yalantis.contextmenu.lib.MenuParams
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class MainActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
    }
    private val notes: Notes = Notes()
    private val records: Records = Records()
    private val whiteboards: Whiteboards = Whiteboards()
    private lateinit var contextMenuDialogFragment: ContextMenuDialogFragment
    private lateinit var mAdView: AdView
    private lateinit var mInterstitialAd: InterstitialAd
    val TAG = "Adv"
    val NOTIFICATION_CHANNEL_ID = "10001"
    private val default_notification_channel_id = "default"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tabs)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewPagerAdapter.addFragment(notes)
        viewPagerAdapter.addFragment(records)
        viewPagerAdapter.addFragment(whiteboards)
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)!!.setIcon(R.drawable.note).contentDescription = "Text notes"
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.record).contentDescription = "Voice notes"
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.whiteboard).contentDescription = "Drawing notes"
        initMenuFragment()

        MobileAds.initialize(this, getString(R.string.ad_id))
//        val testDeviceIds = listOf("33BE2250B43518CCDA7DE426D04EE231")
//        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//        MobileAds.setRequestConfiguration(configuration)

        mAdView = findViewById(R.id.adView)
        mAdView.loadAd(AdRequest.Builder().build())

        mInterstitialAd = InterstitialAd(this)
//        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.adUnitId = getString(R.string.Interstitial_ad_unit_id)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                mInterstitialAd.show()
            }
        }
        Log.e("token ", "" + FirebaseInstanceId.getInstance().token);
        println(FirebaseInstanceId.getInstance().token)
        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        FirebaseMessaging.getInstance().subscribeToTopic("App")

        //        scheduleNotification(getNotification("5 second delay"), 5000)

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun scheduleNotification(notification: Notification?, delay: Int) {
        val notificationIntent = Intent(this, MyNotificationPublisher::class.java)
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION_ID, 1)
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification)
        val pendingIntent = PendingIntent.getBroadcast(this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        val futureInMillis = SystemClock.elapsedRealtime() + delay
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis] = pendingIntent
    }

    private fun getNotification(content: String): Notification? {
        val builder = NotificationCompat.Builder(this, default_notification_channel_id)
            .setContentTitle("Check your notes before shutting your eyes! \uD83D\uDC40")
//        builder.setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .setVibrate(LongArray(1000))
            .setTicker("Notification Listener Service Example");


        return builder.build()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onStop() {
        super.onStop()
//        startService(Intent(this, NotificationService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
//        startService(Intent(this, NotificationService::class.java))
        println("Destroy")
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                R.id.context_menu -> {
                    showContextMenuDialogFragment()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initMenuFragment() {
        val menuParams = MenuParams(
            actionBarSize = resources.getDimension(R.dimen.tool_bar_height).toInt(),
            menuObjects = getMenuObjects(),
            isClosableOutside = true
        )

        contextMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams).apply {
            menuItemClickListener = { view, position ->
                when (position) {
                    0 -> {
                        Intent(view.context, Note::class.java).also {
                            startActivity(it)
                        }
                    }
                    1 -> {
                        Intent(view.context, Record::class.java).also {
                            startActivity(it)
                        }
                    }
                    2 -> {
                        Intent(view.context, Whiteboard::class.java).also {
                            startActivity(it)
                        }
                    }
                    3 -> {
                        viewModel.logout(view)
                    }
                }
            }
        }
    }

    private fun getMenuObjects() = mutableListOf<MenuObject>().apply {
        val note =
            MenuObject("New text note").apply { setResourceValue(R.drawable.note) }
        note.setBgColorValue(Color.rgb(238, 238, 238))
        val record =
            MenuObject("New voice note").apply { setResourceValue(R.drawable.record) }
        record.setBgColorValue(Color.WHITE)
        val whiteboard =
            MenuObject("New whiteboard").apply { setResourceValue(R.drawable.whiteboard) }
        whiteboard.setBgColorValue(Color.rgb(238, 238, 238))
        val logOut =
            MenuObject("Logout").apply { setResourceValue(R.drawable.logout) }
        logOut.setBgColorValue(Color.WHITE)
        add(note)
        add(record)
        add(whiteboard)
        add(logOut)
    }

    private fun showContextMenuDialogFragment() {
        if (supportFragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
            contextMenuDialogFragment.show(supportFragmentManager, ContextMenuDialogFragment.TAG)
        }
    }

}