package com.ramadan.notify.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.ramadan.notify.R
import com.ramadan.notify.data.model.ToDoTable
import com.ramadan.notify.data.repository.ToDoRepository
import com.ramadan.notify.databinding.TodoItemBinding
import com.ramadan.notify.ui.activity.ToDo
import com.ramadan.notify.ui.activity.ToDos
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size


class ToDoAdapter(val context: ToDos) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataList = mutableListOf<ToDoTable>()
    private val viewToDo = 0
    private val addToDo = 1

    fun setDataList(data: MutableList<ToDoTable>) {
        dataList = data
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) addToDo else viewToDo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == addToDo) {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_item, parent, false)
            AddToDoViewHolder(view)
        } else {
            val binding: TodoItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.todo_item, parent, false
            )
            ViewToDoViewHolder(binding)
        }
    }


    override fun getItemCount(): Int {
        return if (dataList.isNotEmpty()) {
            dataList.size + 1
        } else {
            1
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        if (getItemViewType(position) == addToDo) {
            (holder as AddToDoViewHolder).itemView.layoutParams.height =
                holder.mContext.resources.getDimension(R.dimen.add_item_height).toInt()
            holder.addToDo!!.layoutParams.height =
                holder.mContext.resources.getDimension(R.dimen.add_item_height).toInt()
            holder.addToDo.setOnClickListener {
                try {
                    val toDo = ToDo()
                    val transaction: FragmentTransaction = (holder.mContext as FragmentActivity)
                        .supportFragmentManager
                        .beginTransaction()
                    toDo.show(transaction, "addTodo")
                } catch (e: Exception) {
                    println(e)
                }
            }
        } else {
            val todo: ToDoTable = dataList[position - 1]
            (holder as ViewToDoViewHolder).bind(todo)
        }

    class ViewToDoViewHolder(private var binding: TodoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val mContext: Context = itemView.context
        fun bind(todo: ToDoTable) {
            binding.todoItem = todo
            binding.executePendingBindings()
            binding.checkBox.setOnCheckedChangeListener { button, b ->
                if (b) {
                    todo.isDone = true
                    ToDoRepository.updateToDo(mContext, todo)
                    binding.viewKonfetti.build()
                        .addColors(Color.YELLOW, Color.GREEN, Color.RED)
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.Square, Shape.Circle)
                        .addSizes(Size(12))
                        .setPosition(-50f, binding.viewKonfetti.width + 50f, -50f, -50f)
                        .streamFor(300, 5000L)
                }
            }
            binding.delete.setOnClickListener { ToDoRepository.deleteToDo(mContext, todo) }

        }
    }

    class AddToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mContext: Context = itemView.context
        val addToDo: ImageButton? = itemView.findViewById(R.id.addItem)
    }
}