package com.ramadan.notify.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class UserRepository(private val repository: Repository) {
    fun login(email: String, password: String) = repository.login(email, password)
    fun loginWithGoogle(acct: GoogleSignInAccount) = repository.loginWithGoogle(acct)

    fun signUp(email: String, password: String) = repository.signUp(email, password)
    fun resetPassword(email: String) = repository.resetPassword(email)

    fun currentUser() = repository.currentUser()

    fun logout() = repository.logout()

}