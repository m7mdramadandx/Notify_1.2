package com.ramadan.notify.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ramadan.notify.R
import com.ramadan.notify.ui.adapter.ToDoAdapter
import com.ramadan.notify.ui.viewModel.ToDoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ToDosFragment : Fragment() {
    private val viewModel by lazy { ViewModelProvider(this).get(ToDoViewModel::class.java) }
    private lateinit var adapter: ToDoAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        adapter = ToDoAdapter()
        observeData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.recycle_view, container, false)
        recyclerView = view.findViewById(R.id.dashboardRecycleView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredGridLayoutManager
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData() {
        GlobalScope.launch(Dispatchers.Main) {
            viewModel.retrieveToDos(context!!)
                .observe(viewLifecycleOwner, { adapter.setDataList(it) })
        }
    }

}
