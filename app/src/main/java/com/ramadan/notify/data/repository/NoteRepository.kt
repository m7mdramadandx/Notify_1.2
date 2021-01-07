package com.ramadan.notify.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ramadan.notify.data.model.NoteTable
import com.ramadan.notify.room.NoteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteRepository {

    companion object {
        private var noteDatabase: NoteDatabase? = null

        fun insertNote(context: Context, note: NoteTable) {
            noteDatabase = NoteDatabase.getInstance(context)
            // Coroutines is used to perform asynchronous tasks
            CoroutineScope(Dispatchers.IO).launch { noteDatabase!!.noteDao().insert(note) }
        }

        fun updateNote(context: Context, note: NoteTable) {
            noteDatabase = NoteDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch { noteDatabase!!.noteDao().update(note) }
        }

        fun retrieveNotes(context: Context): LiveData<MutableList<NoteTable>> {
            noteDatabase = NoteDatabase.getInstance(context)
            return noteDatabase!!.noteDao().retrieveNotes()
        }

        fun getNote(context: Context, ID: Int): LiveData<NoteTable> {
            noteDatabase = NoteDatabase.getInstance(context)
            return noteDatabase!!.noteDao().getNote(ID)
        }

        fun deleteAll(context: Context) {
            noteDatabase = NoteDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch { noteDatabase!!.noteDao().deleteAll() }
        }

        fun deleteNote(context: Context, note: NoteTable) {
            noteDatabase = NoteDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch { noteDatabase!!.noteDao().delete(note) }
        }
    }

}