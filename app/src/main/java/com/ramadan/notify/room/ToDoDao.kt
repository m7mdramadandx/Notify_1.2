package com.ramadan.notify.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ramadan.notify.data.model.ToDoTable


@Dao
interface ToDoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(toDoTable: ToDoTable)

    @Query("SELECT * FROM ToDo ")
    fun retrieveToDos(): LiveData<MutableList<ToDoTable>>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(toDoTable: ToDoTable)

    @Query("DELETE FROM ToDo")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(toDoTable: ToDoTable)
}