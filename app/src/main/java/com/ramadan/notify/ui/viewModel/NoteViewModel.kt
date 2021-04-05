@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ramadan.notify.data.model.NoteTable
import com.ramadan.notify.data.repository.NoteRepository
import java.text.SimpleDateFormat
import java.util.*


class NoteViewModel : ViewModel() {
    @SuppressLint("SimpleDateFormat")
    private val currentDate = SimpleDateFormat("dd/MM/yyyy")
    private val todayDate = Date()
    var date: String? = currentDate.format(todayDate)
    var name: String? = null
    var content: String? = null
    var color: Int? = Color.parseColor("#ffffff")
    var noteListener: NoteListener? = null

    companion object {
        val noteRepository = NoteRepository()
    }

    fun insertNote(context: Context) {
        if (content.isNullOrEmpty()) {
            noteListener?.onFailure("No content to save")
            return
        }
        if (name.isNullOrEmpty()) name = ""
        val note = NoteTable(date = date!!, name = name!!, content = content!!, color = color!!)
        noteRepository.insertNote(context, note)
        noteListener?.onSuccess()
        return
    }

    fun updateNote(context: Context, ID: Int) {
        if (content.isNullOrEmpty()) {
            noteListener?.onFailure("No content to save")
            return
        }
        if (name.isNullOrEmpty()) name = ""
        val note = NoteTable(ID, date!!, name!!, content!!, color!!)
        noteRepository.updateNote(context, note)
        noteListener?.onSuccess()
        return
    }

     fun retrieveNotes(context: Context): LiveData<MutableList<NoteTable>> =
        noteRepository.retrieveNotes(context)

    fun getNote(context: Context, ID: Int): LiveData<NoteTable> {
        val liveData: LiveData<NoteTable> = noteRepository.getNote(context, ID)
        liveData.observeForever {
            date = it.date
            name = it.name
            content = it.content
            color = it.color
        }
        return liveData
    }

    fun delete(context: Context, note: NoteTable) {
        if (content.isNullOrEmpty()) {
            noteListener?.onFailure("No content to delete")
            return
        }
        noteRepository.deleteNote(context, note)
        return
    }

}