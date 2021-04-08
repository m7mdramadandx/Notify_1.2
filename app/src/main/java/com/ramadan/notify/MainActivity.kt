package com.ramadan.notify

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.tabs.TabLayout
import com.ramadan.notify.data.repository.NoteRepository
import com.ramadan.notify.data.repository.ToDoRepository
import com.ramadan.notify.ui.adapter.ViewPagerAdapter
import com.ramadan.notify.ui.fragment.NotesFragment
import com.ramadan.notify.ui.fragment.RecordsFragment
import com.ramadan.notify.ui.fragment.ToDosFragment
import com.ramadan.notify.ui.fragment.WhiteboardsFragment
import com.ramadan.notify.utils.*
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment
import com.yalantis.contextmenu.lib.MenuObject
import com.yalantis.contextmenu.lib.MenuParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MainActivity : AppCompatActivity() {
    private val notesFragment: NotesFragment = NotesFragment()
    private val toDos: ToDosFragment = ToDosFragment()
    private val recordsFragment: RecordsFragment = RecordsFragment()
    private val whiteboardsFragment: WhiteboardsFragment = WhiteboardsFragment()
    private lateinit var contextMenuDialogFragment: ContextMenuDialogFragment
    private lateinit var adView: AdView

    companion object {
        var isConnected: Boolean = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tabs)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewPagerAdapter.apply {
            addFragment(notesFragment)
            addFragment(toDos)
            addFragment(whiteboardsFragment)
            addFragment(recordsFragment)
            notifyDataSetChanged()
        }
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)!!.setIcon(R.drawable.note).contentDescription = "Text notes"
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.todo).contentDescription = "ToDos"
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.whiteboard).contentDescription = "Drawing notes"
        tabLayout.getTabAt(3)!!.setIcon(R.drawable.record).contentDescription = "Voice notes"
        initMenuFragment()
        adView = findViewById(R.id.adView)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyPermission()) requestForSpecificPermission()
        }
        isConnected = this.isNetworkConnected()
    }

    private fun requestForSpecificPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), STORAGE_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            STORAGE_PERMISSION -> if (grantResults[0] != PackageManager.PERMISSION_GRANTED) finish()
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun checkIfAlreadyPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }


    private fun loadAd() {
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { adView.loadAd(AdRequest.Builder().build()) }
        }
    }


    override fun onResume() {
        super.onResume()
        isConnected = this.isNetworkConnected()
        loadAd()
    }

    override fun onPause() {
        super.onPause()
        isConnected = this.isNetworkConnected()
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
            menuItemClickListener = { view, position ->
                when (position) {
                    0 -> showAlertDialog("note")
                    1 -> showAlertDialog("todo")
                    2 -> showAlertDialog("whiteboard")
                    3 -> showAlertDialog("record")
                }
            }
        }
    }

    private fun getMenuObjects() = mutableListOf<MenuObject>().apply {
        MenuObject("Delete all text notes").apply {
            setResourceValue(R.drawable.note)
            setBgColorValue(menuItemColor)
            add(this)
        }
        MenuObject("Delete all todos").apply {
            setResourceValue(R.drawable.todo)
            setBgColorValue(menuItemColor)
            add(this)
        }
        MenuObject("Delete all boards").apply {
            setResourceValue(R.drawable.whiteboard)
            setBgColorValue(color)
            add(this)
        }
        MenuObject("Delete all voice notes").apply {
            setResourceValue(R.drawable.record)
            setBgColorValue(menuItemColor)
            add(this)
        }
    }

    private fun showAlertDialog(data: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val layoutView = layoutInflater.inflate(R.layout.dialog_alert, null)
        dialogBuilder.setView(layoutView)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        layoutView.findViewById<TextView>(R.id.message).text =
            getString(R.string.deletion_confirmation)
        layoutView.findViewById<TextView>(R.id.discard).apply {
            text = context.getString(R.string.back)
            setOnClickListener { alertDialog.dismiss() }
        }
        layoutView.findViewById<TextView>(R.id.yes).apply {
            text = context.getString(R.string.confirm)
            setOnClickListener {
                when (data) {
                    "note" -> NoteRepository.deleteAll(alertDialog.context)
                    "todo" -> ToDoRepository.deleteAll(alertDialog.context)
                    "whiteboard" -> {
                        val directory = File(whiteboardDirPath)
                        if (directory.isDirectory) {
                            for (fileName in directory.list()) File(directory, fileName).delete()
                        }
                    }
                    "record" -> {
                        val directory = File(recordsDirPath)
                        if (directory.isDirectory) {
                            for (fileName in directory.list()) File(directory, fileName).delete()
                        }
                    }
                }
                it.context.showToast("Deleted")
                alertDialog.dismiss()
            }
        }
    }

}