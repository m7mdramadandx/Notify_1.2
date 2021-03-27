@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ramadan.notify.data.model.ToDoTable
import com.ramadan.notify.data.repository.ToDoRepository


class ToDoViewModel : ViewModel() {

    fun insertToDo(context: Context, name: String) {
        val note = ToDoTable(name = name)
        ToDoRepository.insertToDo(context, note)
        return
    }

    fun retrieveToDos(context: Context): LiveData<MutableList<ToDoTable>> =
        ToDoRepository.retrieveToDos(context)

}