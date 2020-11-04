@file:Suppress("DEPRECATION")

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
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ramadan.notify.R
import com.ramadan.notify.ui.adapter.WhiteboardAdapter
import com.ramadan.notify.ui.viewModel.HomeViewModel
import com.ramadan.notify.ui.viewModel.NoteListener

class Whiteboards : Fragment(), NoteListener {
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }
    private var adapter: WhiteboardAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.recycle_view, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.dashboardRecycleView)
        viewModel.noteListener = this
        observeData()
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)
        recyclerView.adapter = adapter
        return view
    }

    fun showWhiteboard(bitmap: Bitmap, context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.whiteboard_dialog, null)
        dialogBuilder.setView(view)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
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

    override fun onStarted() {
        TODO("Not yet implemented")
    }

    override fun onSuccess() {
        TODO("Not yet implemented")
    }

    override fun onFailure(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

}