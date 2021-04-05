@file:Suppress("DEPRECATION")

package com.ramadan.notify.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.ramadan.notify.MainActivity
import com.ramadan.notify.data.model.NoteTable
import com.ramadan.notify.ui.activity.AppIntroActivity
import com.ramadan.notify.ui.activity.NoteActivity

fun Context.startHomeActivity() =
    Intent(this, MainActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startAppIntroActivity() =
    Intent(this, AppIntroActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startNoteActivity(writtenNote: NoteTable) =
    Intent(this, NoteActivity::class.java).also {
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

fun Context.setFirstOpen() {
    val prefs = getDefaultSharedPreferences(this)
    prefs.edit().apply {
        putBoolean("FIRST_OPEN", false)
        apply()
    }
}

fun Context.getFirstOpen(): Boolean {
    val prefs = getDefaultSharedPreferences(this)
    return prefs.getBoolean("FIRST_OPEN", false)
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun View.snackBar(msg: String) {
    Snackbar.make(this, msg, Snackbar.LENGTH_LONG).apply {
        setTextColor(Color.WHITE)
    }
}


val menuItemColor = Color.rgb(238, 238, 238)
const val debug_tag = "TOTO"
const val STORAGE_PERMISSION = 1001
const val tryAgainMsg = "Sorry, try again later."






