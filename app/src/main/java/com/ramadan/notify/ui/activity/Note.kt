@file:Suppress("CAST_NEVER_SUCCEEDS", "DEPRECATION")

package com.ramadan.notify.ui.activity

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ramadan.notify.R
import com.ramadan.notify.data.model.WrittenNote
import com.ramadan.notify.databinding.NoteBinding
import com.ramadan.notify.ui.viewModel.AuthViewModelFactory
import com.ramadan.notify.ui.viewModel.NoteListener
import com.ramadan.notify.ui.viewModel.NoteViewModel
import com.ramadan.notify.ui.viewModel.NoteViewModelFactory
import com.ramadan.notify.utils.isInternetAvailable
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment
import com.yalantis.contextmenu.lib.MenuObject
import com.yalantis.contextmenu.lib.MenuParams
import kotlinx.android.synthetic.main.note.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class Note : AppCompatActivity(), NoteListener, KodeinAware {
    override val kodein by kodein()
    private val factory: NoteViewModelFactory by instance()
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(NoteViewModel::class.java)
    }
    private lateinit var loadingDialog: AlertDialog
    private lateinit var binding: NoteBinding
    private lateinit var contextMenuDialogFragment: ContextMenuDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.note)
        binding.noteModel = viewModel
        binding.lifecycleOwner = this
        viewModel.noteListener = this
        supportActionBar?.title = "Text Note"
        titleColor = getColor(R.color.colorPrimary)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(viewModel.noteColor!!))
        initLoadingDialog()
        noteColorPicker.setListener { position, color ->
            noteLayout.setBackgroundColor(color)
            viewModel.noteColor = color
            supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
        }
        initMenuFragment()
    }

    override fun onResume() {
        super.onResume()
        if (intent.hasExtra("note")) {
            if (!isInternetAvailable(this))
                loadingDialog.show()
            val writtenNote: WrittenNote = intent.getSerializableExtra("note") as WrittenNote
            observeDate(writtenNote.ID)
        }
        if (!isInternetAvailable(this))
            Handler().postDelayed({
                loadingDialog.dismiss()
            }, 4000)

    }

    override fun onBackPressed() {
        if (!noteContent.text.isNullOrEmpty()) {
            showAlertDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showAlertDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val layoutView = layoutInflater.inflate(R.layout.alert_dialog, null)
        dialogBuilder.setView(layoutView)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val saveChange = layoutView.findViewById<TextView>(R.id.saveChange)
        val dismiss = layoutView.findViewById<TextView>(R.id.dismiss)
        saveChange.setOnClickListener {
            if (intent.hasExtra("note"))
                viewModel.updateNote()
            viewModel.insertNote()
            alertDialog.dismiss()
        }
        dismiss.setOnClickListener {
            alertDialog.dismiss()
            super.onBackPressed()
        }
    }

    private fun initLoadingDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val layoutView = layoutInflater.inflate(R.layout.loading_dialog, null)
        dialogBuilder.setView(layoutView)
        loadingDialog = dialogBuilder.create()
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun observeDate(ID: String) {
        viewModel.getNote(ID).observe(this, Observer {
            supportActionBar?.setBackgroundDrawable(ColorDrawable(it.noteColor))
            noteColorPicker.selectColor(it.noteColor)
            binding.noteModel = viewModel
            binding.lifecycleOwner = this
            viewModel.noteListener = this
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
        item.let {
            when (it.itemId) {
                R.id.context_menu -> {
                    showContextMenuDialogFragment()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initMenuFragment() {
        val menuParams = MenuParams(
            actionBarSize = resources.getDimension(R.dimen.tool_bar_height).toInt(),
            menuObjects = getMenuObjects(),
            isClosableOutside = true
        )
        contextMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams).apply {
            menuItemClickListener = { view, position ->
                when (position) {
                    0 -> {
                        if (intent.hasExtra("note"))
                            viewModel.updateNote()
                        viewModel.insertNote()
                    }
                    1 -> {
                        viewModel.deleteNote()
                    }
                }
            }
        }
    }

    private fun getMenuObjects() = mutableListOf<MenuObject>().apply {
        val save =
            MenuObject("Save").apply { setResourceValue(R.drawable.save_note) }
        save.setBgColorValue((Color.rgb(238, 238, 238)))
        val delete =
            MenuObject("Delete").apply { setResourceValue(R.drawable.delete) }
        delete.setBgColorValue((Color.WHITE))
        add(save)
        add(delete)
    }

    private fun showContextMenuDialogFragment() {
        if (supportFragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
            contextMenuDialogFragment.show(supportFragmentManager, ContextMenuDialogFragment.TAG)
        }
    }

    override fun onStarted() {
    }

    override fun onSuccess() {
        super.onBackPressed()
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}