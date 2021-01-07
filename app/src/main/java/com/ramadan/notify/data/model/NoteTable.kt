package com.ramadan.notify.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "NOTE")
data class NoteTable(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    var id: Int = 0,

    @ColumnInfo(name = "DATE")
    var date: String,

    @ColumnInfo(name = "NAME")
    var name: String,

    @ColumnInfo(name = "CONTENT")
    var content: String,

    @ColumnInfo(name = "COLOR")
    var color: Int,

    ) : Serializable

