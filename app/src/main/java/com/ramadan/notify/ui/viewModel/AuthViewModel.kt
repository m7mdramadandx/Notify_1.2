package com.ramadan.notify.ui.viewModel

import android.view.View
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ramadan.notify.data.repository.UserRepository
import com.ramadan.notify.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    var email: String? = null
    var password: String? = null
    var confirmPassword: String? = null
    private val emailPattern = Pattern.compile(emailValidation)
    private val passwordPattern = Pattern.compile(passwordValidation)
    var authListener: AuthListener? = null
    private val disposables = CompositeDisposable()

    val user by lazy { repository.currentUser() }


    fun login() {
        authListener?.onStarted()
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            authListener?.onFailure("All fields are required!")
            return
        }
        val disposable = repository.login(email!!, password!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { authListener?.onSuccess() },
                {
                    var error = it.localizedMessage
                    when (error) {
                        "We have blocked all requests from this device due to unusual activity. Try again later. [ Too many unsuccessful login attempts. Please try again later. ]" -> error =
                            "Please, try again later"
                        "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> error == "Please, Check your network connection"
                        else -> error == "Sorry, Login failed"
                    }
                    authListener?.onFailure(error!!)
                })
        disposables.add(disposable)
    }

    fun loginWithGoogle(acct: GoogleSignInAccount) {
        val disposable = repository.loginWithGoogle(acct)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { authListener?.onSuccess() },
                {
                    var error = it.localizedMessage
                    when (error) {
                        "We have blocked all requests from this device due to unusual activity. Try again later. [ Too many unsuccessful login attempts. Please try again later. ]" -> error =
                            "Please, try again later"
                        "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> error == "Please, Check your network connection"
                        else -> error == "Sorry, Login failed"
                    }
                    authListener?.onFailure(error!!)
                })
        disposables.add(disposable)
    }

    fun signUp() {
        authListener?.onStarted()
        if (email.isNullOrEmpty() || password.isNullOrEmpty() || confirmPassword.isNullOrEmpty()) {
            authListener?.onFailure("All fields are required!")
            return
        }
        val emailMatcher = emailPattern.matcher(email!!)
        val passwordMatcher = passwordPattern.matcher(password!!)
        if (!emailMatcher.matches()) {
            authListener?.onFailure("Invalid email!")
            return
        } else if (!passwordMatcher.matches()) {
            authListener?.onFailure("Weak password!")
            return
        } else if (!password.equals(confirmPassword)) {
            authListener?.onFailure("Passwords are different")
            return
        }
        val disposable = repository.signUp(email!!, password!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { authListener?.onSuccess() },
                {
                    var error = it.localizedMessage
                    println(error)
                    when (error) {
                        "We have blocked all requests from this device due to unusual activity. Try again later. [ Too many unsuccessful login attempts. Please try again later. ]" -> error =
                            "Please, try again later"
                        "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> error == "Please, Check your network connection"
                        else -> error == "Sorry, Sign up failed"
                    }
                    authListener?.onFailure(error!!)
                })
        disposables.add(disposable)
    }

    fun resetPassword(email: String): Boolean {
        if (email.isEmpty()) {
            authListener?.onFailure("Please enter your email!")
            return false
        }
        val emailMatcher = emailPattern.matcher(email)
        if (!emailMatcher.matches()) {
            authListener?.onFailure("Invalid Email")
            return false
        }
        val disposable = repository.resetPassword(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ authListener?.onFailure("Check your inbox") },
                { authListener?.onFailure(it.localizedMessage!!) })
        disposables.add(disposable)
        return true
    }

    fun goToSignUp(view: View) {
        view.context.startSignUpActivity()
    }


    fun logout(view: View) {
        repository.logout()
        view.context.startLoginActivity()
    }


    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

}