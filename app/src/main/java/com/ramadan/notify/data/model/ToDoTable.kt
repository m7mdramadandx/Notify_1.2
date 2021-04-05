package com.ramadan.notify.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
@Keep
@Entity(tableName = "TODO")
data class ToDoTable(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    var id: Int = 0,

    @ColumnInfo(name = "NAME")
    var name: String,

    @ColumnInfo(name = "IS_DONE")
    var isDone: Boolean = false,

    ) : Serializable

