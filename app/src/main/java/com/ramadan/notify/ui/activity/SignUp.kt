@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.ramadan.notify.R
import com.ramadan.notify.databinding.SignUpBinding
import com.ramadan.notify.ui.viewModel.AuthListener
import com.ramadan.notify.ui.viewModel.AuthViewModel
import com.ramadan.notify.ui.viewModel.AuthViewModelFactory
import com.ramadan.notify.utils.startAppIntroActivity
import com.ramadan.notify.utils.startHomeActivity
import kotlinx.android.synthetic.main.sign_up.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SignUp : AppCompatActivity(), AuthListener, KodeinAware {
    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var binding: SignUpBinding
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)
        binding = DataBindingUtil.setContentView(this, R.layout.sign_up)
        binding.signUpModel = viewModel
        binding.lifecycleOwner = this
        viewModel.authListener = this
        supportActionBar?.hide()
        btn_back.setOnClickListener { super.onBackPressed() }
    }

    override fun onStarted() {
        progressBar.visibility = View.VISIBLE
    }

    override fun onSuccess() {
        progressBar.visibility = View.GONE
        startAppIntroActivity()
    }

    override fun onFailure(message: String) {
        progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


}
