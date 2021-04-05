package com.ramadan.notify.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.ramadan.notify.R
import com.ramadan.notify.ui.viewModel.NoteListener
import com.ramadan.notify.ui.viewModel.WhiteboardViewModel
import com.ramadan.notify.utils.DrawView
import com.ramadan.notify.utils.TouchListener
import com.ramadan.notify.utils.menuItemColor
import com.ramadan.notify.utils.startHomeActivity
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment
import com.yalantis.contextmenu.lib.MenuObject
import com.yalantis.contextmenu.lib.MenuParams
import kotlinx.android.synthetic.main.activity_whiteboard.*


@Suppress("DEPRECATION")
class WhiteboardActivity : AppCompatActivity(), NoteListener {

    private val viewModel by lazy { ViewModelProvider(this).get(WhiteboardViewModel::class.java) }
    private lateinit var contextMenuDialogFragment: ContextMenuDialogFragment
    private var whiteboardName: String = "null"
    private var boardColor = Color.WHITE
    private lateinit var board: DrawView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whiteboard)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.noteListener = this
        checkPermission()
        board = findViewById(R.id.whiteboard)
        board.setBackgroundColor(boardColor)
        board.requestFocus()
        board.setOnTouchListener(TouchListener())
        initMenuFragment()

        penColorPicker.setListener { _, color ->
            board.setCurrentWidth(seekBar.progress)
            board.setCurrentColor(color)
            eraser.setBackgroundColor(resources.getColor(R.color.white))
        }

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                board.setCurrentWidth(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

    }

    override fun onBackPressed() {
        showAlertDialog()
    }

    private fun showAlertDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_alert, null)
        dialogBuilder.setView(view)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val saveChange = view.findViewById<TextView>(R.id.yes)
        val dismiss = view.findViewById<TextView>(R.id.discard)
        saveChange.setOnClickListener {
            setName()
            alertDialog.cancel()
        }
        dismiss.setOnClickListener { super.onBackPressed() }
    }

    @SuppressLint("SetTextI18n")
    private fun setName() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view: View = inflater.inflate(R.layout.dialog_edit_text, null)
        dialogBuilder.setView(view)
        dialogBuilder.setCancelable(false)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.SlideAnimation
        alertDialog.show()
        view.findViewById<TextView>(R.id.title).text = "Board"
        val fileName = view.findViewById<View>(R.id.input) as EditText
        val confirm = view.findViewById<TextView>(R.id.confirm)
        val cancel = view.findViewById<TextView>(R.id.cancel)
        confirm.setOnClickListener {
            board.isDrawingCacheEnabled = true
            whiteboardName = fileName.text.toString()
            if (fileName.text.isNullOrEmpty())
                onFailure("Please, Enter the board name")
            else {
                viewModel.saveImageToExternalStorage(board.drawingCache, whiteboardName)
                board.destroyDrawingCache()
                alertDialog.cancel()
            }
        }
        cancel.setOnClickListener { alertDialog.cancel() }
    }

    fun eraser() {
        if (boardColor == Color.WHITE) board.setCurrentColor(Color.WHITE)
        else board.setCurrentColor(Color.BLACK)
        board.setCurrentWidth(seekBar.progress * 8)
        eraser.setBackgroundColor(resources.getColor(R.color.colorAccent))
        penColorPicker.isLockMode = true
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
        item.let { if (it.itemId == R.id.context_menu) showContextMenuDialogFragment() }
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
            menuItemClickListener = { _, position ->
                when (position) {
                    0 -> {
                        boardColor = if (boardColor == Color.WHITE) {
                            board.setBackgroundColor(Color.BLACK)
                            Color.BLACK
                        } else {
                            board.setBackgroundColor(Color.WHITE)
                            Color.WHITE
                        }
                    }
                    1 -> board.clear()
                    2 -> setName()
                }
            }
        }
    }

    private fun getMenuObjects() = mutableListOf<MenuObject>().apply {
        MenuObject("Switch board color").apply {
            setResourceValue(R.drawable.whiteboard)
            setBgColorValue(menuItemColor)
            add(this)
        }
        MenuObject("Clear").apply {
            setResourceValue(R.drawable.clear_whiteboard)
            setBgColorValue(menuItemColor)
            add(this)
        }
        MenuObject("Save").apply {
            setResourceValue(R.drawable.save_note)
            setBgColorValue(menuItemColor)
            add(this)
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray,
    ) {
        when (requestCode) {
            101 -> if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) return
            else -> super.onBackPressed()
        }
    }

    private fun checkPermission() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ContextCompat.checkSelfPermission(this, permissions.toString())
            != PackageManager.PERMISSION_GRANTED
        ) ActivityCompat.requestPermissions(this, permissions, 101)
    }

    override fun onStarted() {}

    override fun onSuccess() {
        startHomeActivity()
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}