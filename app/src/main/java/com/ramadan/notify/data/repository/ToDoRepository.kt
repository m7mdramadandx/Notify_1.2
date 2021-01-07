package com.ramadan.notify.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ramadan.notify.data.model.ToDoTable
import com.ramadan.notify.room.ToDoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToDoRepository() {

    companion object {
        private var toDoDatabase: ToDoDatabase? = null

        fun insertToDo(context: Context, todo: ToDoTable) {
            toDoDatabase = ToDoDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch { toDoDatabase!!.toDoDao().insert(todo) }
        }

        fun updateToDo(context: Context, todo: ToDoTable) {
            toDoDatabase = ToDoDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch { toDoDatabase!!.toDoDao().update(todo) }
        }

        fun retrieveToDos(context: Context): LiveData<MutableList<ToDoTable>> {
            toDoDatabase = ToDoDatabase.getInstance(context)
            return toDoDatabase!!.toDoDao().retrieveToDos()
        }


        fun deleteAll(context: Context) {
            toDoDatabase = ToDoDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch { toDoDatabase!!.toDoDao().deleteAll() }
        }

        fun deleteToDo(context: Context, todo: ToDoTable) {
            toDoDatabase = ToDoDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch { toDoDatabase!!.toDoDao().delete(todo) }
        }
    }


}