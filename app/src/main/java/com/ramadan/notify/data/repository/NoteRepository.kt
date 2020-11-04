package com.ramadan.notify.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ramadan.notify.data.model.WrittenNote

class NoteRepository(private val repository: Repository) {

    fun insertNote(note: HashMap<String, Any?>) = repository.insertNote(note)
    fun updateNote(note: HashMap<String, Any?>) = repository.updateNote(note)
    fun deleteNote(ID: String) = repository.deleteNote(ID)

    fun fetchNotes(): LiveData<MutableList<WrittenNote>> {
        val mutableData = MutableLiveData<MutableList<WrittenNote>>()
        repository.fetchNotes().observeForever { mutableData.value = it }
        return mutableData
    }

    fun fetchNote(ID: String): MutableLiveData<WrittenNote> {
        val mutableData = MutableLiveData<WrittenNote>()
        repository.fetchNote(ID).observeForever { mutableData.value = it }
        return mutableData
    }

}