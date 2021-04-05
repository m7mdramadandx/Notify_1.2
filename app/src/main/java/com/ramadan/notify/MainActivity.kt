package com.ramadan.notify

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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
import com.ramadan.notify.utils.menuItemColor
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment
import com.yalantis.contextmenu.lib.MenuObject
import com.yalantis.contextmenu.lib.MenuParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private val notesFragment: NotesFragment = NotesFragment()
    private val toDos: ToDosFragment = ToDosFragment()
    private val recordsFragment: RecordsFragment = RecordsFragment()
    private val whiteboardsFragment: WhiteboardsFragment = WhiteboardsFragment()
    private lateinit var contextMenuDialogFragment: ContextMenuDialogFragment
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tabs)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewPagerAdapter.apply {
            addFragment(whiteboardsFragment)
            addFragment(notesFragment)
            addFragment(toDos)
            addFragment(recordsFragment)
            notifyDataSetChanged()
        }
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)!!.setIcon(R.drawable.note).contentDescription = "Text notes"
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.todo).contentDescription = "ToDos"
        tabLayout.getTabAt(3)!!.setIcon(R.drawable.record).contentDescription = "Voice notes"
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.whiteboard).contentDescription = "Drawing notes"
        initMenuFragment()
        adView = findViewById(R.id.adView)
//        loadAd()
    }

    private fun loadAd() {
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { adView.loadAd(AdRequest.Builder().build()) }
        }
    }


    override fun onResume() {
        super.onResume()
        loadAd()
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
                    0 -> NoteRepository.deleteAll(view.context)
                    1 -> ToDoRepository.deleteAll(view.context)
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
        MenuObject("Delete all voice notes").apply {
            setResourceValue(R.drawable.record)
            setBgColorValue(menuItemColor)
            add(this)
        }
        MenuObject("Delete all boards").apply {
            setResourceValue(R.drawable.whiteboard)
            setBgColorValue(color)
            add(this)
        }
    }
}