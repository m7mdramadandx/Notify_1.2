package com.ramadan.notify.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ramadan.notify.R
import com.ramadan.notify.ui.adapter.ToDoAdapter
import com.ramadan.notify.ui.viewModel.ToDoViewModel


class ToDos : Fragment() {
    private val viewModel by lazy { ViewModelProvider(this).get(ToDoViewModel::class.java) }
    private lateinit var adapter: ToDoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.recycle_view, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.dashboardRecycleView)
        adapter = ToDoAdapter(this)
        observeData()
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData() {
        viewModel.retrieveToDos(context!!)
            .observe(viewLifecycleOwner, Observer(adapter::setDataList))
    }

}
