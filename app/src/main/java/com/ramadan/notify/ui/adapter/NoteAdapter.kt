@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.NativeAdOptions
import com.ramadan.notify.MainActivity
import com.ramadan.notify.MainActivity.Companion.isConnected
import com.ramadan.notify.R
import com.ramadan.notify.data.model.NoteTable
import com.ramadan.notify.data.repository.NoteRepository
import com.ramadan.notify.databinding.ItemNoteBinding
import com.ramadan.notify.ui.activity.NoteActivity
import com.ramadan.notify.utils.debug_tag
import com.ramadan.notify.utils.startNoteActivity


class NoteAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataList = mutableListOf<NoteTable>()

    companion object {
        private const val addNote = 2
        private const val viewNote = 1
        private const val nativeAd = 3
    }

    fun setDataList(data: MutableList<NoteTable>) {
        dataList = data
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> addNote
            position % 4 == 0 && isConnected -> nativeAd
            else -> viewNote
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            nativeAd -> {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_native_ad_templete, parent, false)
                return AdViewHolder(view)
            }
            addNote -> {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add, parent, false)
                AddNoteViewHolder(view)
            }
            else -> {
                val binding: ItemNoteBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_note, parent, false
                )
                ViewNoteViewHolder(binding)
            }
        }
    }


    override fun getItemCount(): Int {
        return if (dataList.isNotEmpty()) {
            if (dataList.size % 4 == 0) {
                (dataList.size / 4) + 1 + dataList.size
            } else
                dataList.size + 1
        } else 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mContext = holder.itemView.context
        when (getItemViewType(position)) {
            addNote -> {
                (holder as AddNoteViewHolder).addNote!!.setOnClickListener {
                    it.context.startActivity(Intent(it.context, NoteActivity::class.java))
                }
            }
            viewNote -> {
                var p0 = if (dataList.size % 4 == 0) {
                    position - (dataList.size / 4) - 1
                } else position - 1
                if (p0 < 0) p0 = 0
                val writtenNote: NoteTable = dataList[p0]
                (holder as ViewNoteViewHolder).bind(writtenNote)
            }
            nativeAd -> {
                val adLoader = AdLoader.Builder(mContext, mContext.getString(R.string.native_ad))
                    .forUnifiedNativeAd {
                        val styles = NativeTemplateStyle.Builder().build()
                        val template: TemplateView = (holder as AdViewHolder).adTemplate
                        template.setStyles(styles)
                        template.setNativeAd(it)
                    }
                    .withNativeAdOptions(NativeAdOptions.Builder().build())
                    .build()
                adLoader.loadAd(AdRequest.Builder().build())

            }
        }
    }

    class ViewNoteViewHolder(private var binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val mContext: Context = itemView.context
        fun bind(note: NoteTable) {
            binding.noteItem = note
            binding.note.setCardBackgroundColor(note.color)
            binding.executePendingBindings()
            itemView.setOnClickListener { it.context.startNoteActivity(note) }
            itemView.setOnLongClickListener {
                showOption(note)
                false
            }
        }

        private fun showOption(note: NoteTable) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            val view = View.inflate(mContext, R.layout.dialog_option, null)
            dialogBuilder.setView(view)
            val alertDialog = dialogBuilder.create()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
            alertDialog.setCancelable(true)
            view.findViewById<TextView>(R.id.rename).visibility = GONE
            view.findViewById<TextView>(R.id.share).setOnClickListener {
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, note.content)
                    type = "text/plain"
                    mContext.startActivity(Intent.createChooser(this, "Send to"))
                }
                alertDialog.cancel()
            }
            view.findViewById<TextView>(R.id.delete).setOnClickListener {
                NoteRepository().deleteNote(mContext, note)
                Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show()
                alertDialog.cancel()
            }
        }
    }

    class AddNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addNote: ImageButton? = itemView.findViewById(R.id.addItem)
    }

    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adTemplate: TemplateView = itemView.findViewById(R.id.nativeTemplateView)
    }

}