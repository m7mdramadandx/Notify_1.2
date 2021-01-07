package com.ramadan.notify.ui.activity

import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.ramadan.notify.R
import com.ramadan.notify.utils.startHomeActivity
import kotlinx.android.synthetic.main.splash_screen.*


class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.navigationBarColor = getColor(R.color.colorAccent)
        supportActionBar?.hide()
        setContentView(R.layout.splash_screen)
        val animation = AnimationUtils.loadAnimation(
            this,
            R.anim.animate_in_out_enter
        )
        animation.duration = 500
        notifyLogo.animation = animation
        Handler().postDelayed(Runnable { startHomeActivity() }, 800)
    }
}