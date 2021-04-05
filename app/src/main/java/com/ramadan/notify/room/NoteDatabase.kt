package com.ramadan.notify.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ramadan.notify.data.model.NoteTable

@Database(entities = [NoteTable::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase {
            INSTANCE?.let { return it } ?: synchronized(this) {
                INSTANCE = Room
                    .databaseBuilder(context, NoteDatabase::class.java, "NOTE_DB")
                    .fallbackToDestructiveMigration()
                    .build()
                return INSTANCE!!
            }
        }
    }

}