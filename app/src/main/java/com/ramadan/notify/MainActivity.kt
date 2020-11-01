@file:Suppress("DEPRECATION")

package com.ramadan.notify

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.ramadan.notify.ui.activity.*
import com.ramadan.notify.ui.adapter.ViewPagerAdapter
import com.ramadan.notify.ui.viewModel.AuthViewModel
import com.ramadan.notify.ui.viewModel.AuthViewModelFactory
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
    lateinit var mAdView: AdView
    private lateinit var mInterstitialAd: InterstitialAd
    val TAG = "Adv"

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

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        mAdView.loadAd(AdRequest.Builder().build())

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                mInterstitialAd.show()
            }
        }

//        val channel = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel("App", "Notify", NotificationManager.IMPORTANCE_DEFAULT)
//            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
//
//        } else {
//            TODO("VERSION.SDK_INT < O")
//        }


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener
        { task ->
            if (!task.isSuccessful) {
                Log.w("Adv", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.w("Adv", token)
            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        })

    }

    fun onNewToken(token: String) {
        Log.d("Adv", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d("Adv", "sendRegistrationTokenToServer($token)")
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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