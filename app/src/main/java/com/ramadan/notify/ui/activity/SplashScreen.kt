package com.ramadan.notify.ui.activity

import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.ramadan.notify.R
import com.ramadan.notify.ui.viewModel.AuthViewModel
import com.ramadan.notify.ui.viewModel.AuthViewModelFactory
import com.ramadan.notify.utils.startHomeActivity
import com.ramadan.notify.utils.startLoginActivity
import kotlinx.android.synthetic.main.splash_screen.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class SplashScreen : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
    }

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
        Handler().postDelayed(Runnable {
            if (viewModel.user?.isAnonymous != null)
                startHomeActivity()
            else
                startLoginActivity()
        }, 800)
    }
}