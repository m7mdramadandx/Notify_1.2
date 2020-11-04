package com.ramadan.notify.utils

import android.R.attr.password
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import com.ramadan.notify.MainActivity
import com.ramadan.notify.data.model.WrittenNote
import com.ramadan.notify.ui.activity.AppIntro
import com.ramadan.notify.ui.activity.Login
import com.ramadan.notify.ui.activity.Note
import com.ramadan.notify.ui.activity.SignUp
import android.content.Context
import android.provider.Settings

fun Context.startHomeActivity() =
    Intent(this, MainActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startLoginActivity() =
    Intent(this, Login::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
fun Context.startSignUpActivity() =
    Intent(this, SignUp::class.java).also {
        startActivity(it)
    }


fun Context.startAppIntroActivity() =
    Intent(this, AppIntro::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startNoteActivity(writtenNote: WrittenNote) =
    Intent(this, Note::class.java).also {
        it.putExtra("note", writtenNote)
        startActivity(it)
    }

fun getRecordLength(milliseconds: Long): String {
    return String.format(
        "%02d:%02d",
        java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(milliseconds),
        java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                java.util.concurrent.TimeUnit.MINUTES.toSeconds(
                    java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(
                        milliseconds
                    )
                )
    )
}

fun isInternetAvailable(activity: AppCompatActivity): Boolean {
    val connectivityManager =
        activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

const val emailValidation = "^(.+)@(.+).(.*[a-z])$"
const val passwordValidation = "(?=.*[0-9])(?=.*[a-z]).{8,}"

fun isTimeAutomatic(context: Context): Boolean {
    return Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.AUTO_TIME,
        0
    ) == 1;
}



