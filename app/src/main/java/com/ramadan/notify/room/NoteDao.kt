package com.ramadan.notify.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ramadan.notify.data.model.NoteTable


@Dao
interface NoteDao {
    // Data Access Object Class for mapping SQL quires

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noteTable: NoteTable)

    @Query("SELECT * FROM NOTE ")
    fun retrieveNotes(): LiveData<MutableList<NoteTable>>

    @Query("SELECT * FROM NOTE WHERE ID=:id")
    fun getNote(id: Int): LiveData<NoteTable>

    @Update
    suspend fun update(noteTable: NoteTable)

    @Query("DELETE FROM NOTE")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(noteTable: NoteTable)
}