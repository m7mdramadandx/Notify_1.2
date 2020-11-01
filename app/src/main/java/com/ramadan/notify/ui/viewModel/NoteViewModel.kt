@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.viewModel

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ramadan.notify.data.model.WrittenNote
import com.ramadan.notify.data.repository.NoteRepository
import java.text.SimpleDateFormat
import java.util.*


class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    @SuppressLint("SimpleDateFormat")
    private val currentDate = SimpleDateFormat("dd/MM/yyyy")
    private val todayDate = Date()
    private var ID: String? = System.currentTimeMillis().toString()
    var date: String? = currentDate.format(todayDate)
    var name: String? = null
    var content: String? = null
    var noteColor: Int? = Color.parseColor("#ffffff")
    var noteListener: NoteListener? = null

    fun retrieveNotes(): LiveData<MutableList<WrittenNote>> {
        val mutableData = MutableLiveData<MutableList<WrittenNote>>()
        repository.fetchNotes().observeForever { mutableData.value = it }
        return mutableData
    }

    fun getNote(ID: String): MutableLiveData<WrittenNote> {
        val mutableData = MutableLiveData<WrittenNote>()
        repository.fetchNote(ID).observeForever {
            mutableData.value = it
            this.ID = it.ID
            name = it.name
            date = it.date
            content = it.content
            noteColor = it.noteColor
        }
        return mutableData
    }

    fun insertNote() {
        if (content.isNullOrEmpty()) {
            noteListener?.onFailure("No content to save")
            return
        }
        if (name.isNullOrEmpty())
            name = ""
        val note: HashMap<String, Any?> = hashMapOf(
            "noteID" to ID,
            "noteDate" to date,
            "noteName" to name,
            "noteContent" to content,
            "noteColor" to noteColor
        )
        repository.insertNote(note)
        noteListener?.onSuccess()
        return
    }

    fun updateNote() {
        if (name.isNullOrEmpty())
            name = ""
        val note: HashMap<String, Any?> = hashMapOf(
            "noteID" to ID,
            "noteDate" to currentDate.format(todayDate),
            "noteName" to name,
            "noteContent" to content,
            "noteColor" to noteColor
        )
        repository.updateNote(note)
        noteListener?.onSuccess()
        return
    }

    fun deleteNote() {
        if (content.isNullOrEmpty()) {
            noteListener?.onFailure("No content to delete")
            return
        }
        repository.deleteNote(ID!!)
        noteListener?.onSuccess()
        return
    }

}