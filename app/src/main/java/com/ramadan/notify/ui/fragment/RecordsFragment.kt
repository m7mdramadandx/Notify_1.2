package com.ramadan.notify.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ramadan.notify.R
import com.ramadan.notify.ui.adapter.RecordAdapter
import com.ramadan.notify.ui.viewModel.HomeViewModel
import com.ramadan.notify.ui.viewModel.NoteListener
import com.ramadan.notify.utils.STORAGE_PERMISSION

class RecordsFragment : Fragment(), NoteListener {
    private val viewModel by lazy { ViewModelProvider(this).get(HomeViewModel::class.java) }
    private lateinit var adapter: RecordAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.recycle_view, container, false)
        recyclerView = view.findViewById(R.id.dashboardRecycleView)
        viewModel.noteListener = this
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredGridLayoutManager
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ContextCompat.checkSelfPermission(requireContext(), permissions.toString())
            == PackageManager.PERMISSION_GRANTED
        ) {
            adapter = RecordAdapter(viewModel.retrieveRecords())
            recyclerView.adapter = adapter
        } else {
            ActivityCompat.requestPermissions(requireActivity(), permissions, STORAGE_PERMISSION)
        }
    }

    override fun onStarted() {}

    override fun onSuccess() {}

    override fun onFailure(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}