package com.ramadan.notify.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.ramadan.notify.MainActivity
import com.ramadan.notify.data.model.NoteTable
import com.ramadan.notify.ui.activity.AppIntro
import com.ramadan.notify.ui.activity.Note

fun Context.startHomeActivity() =
    Intent(this, MainActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startAppIntroActivity() =
    Intent(this, AppIntro::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startNoteActivity(writtenNote: NoteTable) =
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

val menuItemColor = Color.rgb(238, 238, 238)
const val DEBUG_TAG = "TOTO"





