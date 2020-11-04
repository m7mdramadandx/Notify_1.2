@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.ramadan.notify.R
import com.ramadan.notify.databinding.LoginBinding
import com.ramadan.notify.ui.viewModel.AuthListener
import com.ramadan.notify.ui.viewModel.AuthViewModel
import com.ramadan.notify.ui.viewModel.AuthViewModelFactory
import com.ramadan.notify.utils.startAppIntroActivity
import com.ramadan.notify.utils.startHomeActivity
import kotlinx.android.synthetic.main.login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class Login : AppCompatActivity(), AuthListener, KodeinAware {
    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
    }
    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: LoginBinding = DataBindingUtil.setContentView(this, R.layout.login)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.loginModel = viewModel
        binding.lifecycleOwner = this
        viewModel.authListener = this
        supportActionBar?.hide()
        configureGoogleSignIn()

        forgot.setOnClickListener {
            emailDialog()
        }

        google_button.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.loginWithGoogle(account!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun emailDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view: View = inflater.inflate(R.layout.edit_text_dialog, null)
        dialogBuilder.setView(view)
        dialogBuilder.setCancelable(false)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.SlideAnimation
        alertDialog.show()
        val title = view.findViewById<TextView>(R.id.title)
        title.text = "Enter Your Email"
        val email = view.findViewById<EditText>(R.id.input)
        val inputLayout = view.findViewById<TextInputLayout>(R.id.inputLayout)
        inputLayout.hint = "Email"
        val confirm = view.findViewById<TextView>(R.id.confirm)
        val cancel = view.findViewById<TextView>(R.id.cancel)
        confirm.setOnClickListener {
            if (viewModel.resetPassword(email.text.toString()))
                alertDialog.cancel()
        }
        cancel.setOnClickListener { alertDialog.cancel() }
    }


    override fun onStart() {
        super.onStart()
        viewModel.user?.let { startHomeActivity() }
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
