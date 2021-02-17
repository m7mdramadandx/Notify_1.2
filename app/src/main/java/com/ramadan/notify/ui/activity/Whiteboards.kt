package com.ramadan.notify.ui.activity

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ramadan.notify.R
import com.ramadan.notify.ui.adapter.WhiteboardAdapter
import com.ramadan.notify.ui.viewModel.HomeViewModel
import com.ramadan.notify.ui.viewModel.NoteListener

class Whiteboards : Fragment(), NoteListener {
    private val viewModel by lazy { ViewModelProvider(this).get(HomeViewModel::class.java) }
    private var adapter: WhiteboardAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.recycle_view, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.dashboardRecycleView)
        viewModel.noteListener = this
        observeData()
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredGridLayoutManager
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    (activity as AppCompatActivity).supportActionBar?.hide()
                } else {
                    (activity as AppCompatActivity).supportActionBar?.show()
                }
            }
        })
        return view
    }

    fun showWhiteboard(bitmap: Bitmap, context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.whiteboard_dialog, null)
        dialogBuilder.setView(view)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.ShrinkAnimation
        alertDialog.setCancelable(true)
        val imageView = view.findViewById<ImageView>(R.id.img)
        imageView.setImageBitmap(bitmap)
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData() {
        adapter = WhiteboardAdapter(viewModel.retrieveWhiteboards())
    }

    override fun onStarted() {}

    override fun onSuccess() {}

    override fun onFailure(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

}