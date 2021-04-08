package com.ramadan.notify.ui.activity

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.ramadan.notify.R
import com.ramadan.notify.data.model.NoteTable
import com.ramadan.notify.databinding.ActivityNoteBinding
import com.ramadan.notify.ui.viewModel.NoteListener
import com.ramadan.notify.ui.viewModel.NoteViewModel
import com.ramadan.notify.utils.menuItemColor
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment
import com.yalantis.contextmenu.lib.MenuObject
import com.yalantis.contextmenu.lib.MenuParams
import kotlinx.android.synthetic.main.activity_note.*


class NoteActivity : AppCompatActivity(), NoteListener {
    private val viewModel by lazy { ViewModelProvider(this).get(NoteViewModel::class.java) }
    private lateinit var binding: ActivityNoteBinding
    private lateinit var contextMenuDialogFragment: ContextMenuDialogFragment
    private var note: NoteTable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.noteModel = viewModel
        binding.lifecycleOwner = this
        viewModel.noteListener = this
        supportActionBar?.setBackgroundDrawable(ColorDrawable(viewModel.color!!))
        noteColorPicker.setListener { _, color ->
            noteLayout.setBackgroundColor(color)
            viewModel.color = color
            supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
        }
        initMenuFragment()
    }

    override fun onResume() {
        super.onResume()
        intent.getSerializableExtra("note")?.let {
            note = it as NoteTable
            observeDate(note!!.id)
        }
    }

    override fun onBackPressed() {
        if (noteContent.text?.isNotEmpty() == true) {
            showAlertDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showAlertDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val layoutView = layoutInflater.inflate(R.layout.dialog_alert, null)
        dialogBuilder.setView(layoutView)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val saveChange = layoutView.findViewById<TextView>(R.id.yes)
        val dismiss = layoutView.findViewById<TextView>(R.id.discard)
        saveChange.setOnClickListener {
            note?.let {
                viewModel.updateNote(applicationContext, it.id)
            } ?: viewModel.insertNote(applicationContext)
            alertDialog.dismiss()
        }
        dismiss.setOnClickListener {
            alertDialog.dismiss()
            super.onBackPressed()
        }
    }

    private fun observeDate(id: Int) {
        viewModel.getNote(applicationContext, id).observe(this, {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(it.color))
            noteColorPicker.selectColor(it.color)
            binding.noteModel = viewModel
            binding.lifecycleOwner = this
        })
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.run { if (itemId == R.id.context_menu) showContextMenuDialogFragment() }
        return super.onOptionsItemSelected(item)
    }

    private fun showContextMenuDialogFragment() {
        if (supportFragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
            contextMenuDialogFragment.show(supportFragmentManager, ContextMenuDialogFragment.TAG)
        }
    }


    private fun initMenuFragment() {
        val menuParams = MenuParams(
            actionBarSize = resources.getDimension(R.dimen.tool_bar_height).toInt(),
            menuObjects = getMenuObjects(),
            isClosableOutside = true
        )
        contextMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams).apply {
            menuItemClickListener = { view, position ->
                if (position == 0) {
                    note?.let {
                        viewModel.updateNote(view.context, it.id)
                    } ?: viewModel.insertNote(view.context)
                } else if (position == 1) {
                    note?.let { viewModel.delete(view.context, it) }
                }
            }
        }
    }

    private fun getMenuObjects() = mutableListOf<MenuObject>().apply {
        MenuObject("Save").apply {
            setResourceValue(R.drawable.save_note)
            setBgColorValue(menuItemColor)
            add(this)
        }
        MenuObject("Delete").apply {
            setResourceValue(R.drawable.delete)
            setBgColorValue(menuItemColor)
            add(this)
        }
    }

    override fun onStarted() {}

    override fun onSuccess() = super.onBackPressed()

    override fun onFailure(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

}