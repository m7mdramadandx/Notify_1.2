@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ramadan.notify.R
import com.ramadan.notify.ui.adapter.ToDoAdapter
import com.ramadan.notify.ui.viewModel.ToDoViewModel


class ToDos : Fragment() {
    private val viewModel by lazy { ViewModelProviders.of(this).get(ToDoViewModel::class.java) }
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
        val staggeredGridLayoutManager =
            StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredGridLayoutManager
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
