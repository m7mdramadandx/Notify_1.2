@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.NativeAdOptions
import com.ramadan.notify.R
import com.ramadan.notify.ui.activity.RecordActivity
import com.ramadan.notify.ui.fragment.PlayRecordFragment
import com.ramadan.notify.utils.*
import kotlinx.android.synthetic.main.item_record.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class RecordAdapter(private val filepath: Array<String?>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val addRecord = 2
        private const val viewRecord = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) addRecord
        else viewRecord
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == addRecord) {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_add, parent, false)
            AddRecordViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
            return ViewRecordViewHolder(view)
        }
    }


    override fun getItemCount(): Int {
        filepath?.let {
            return if (it.isNotEmpty()) {
                it.size + 1
            } else 1
        } ?: return 1
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mContext = holder.itemView.context
        if (getItemViewType(position) == addRecord) {
            val addRecordViewHolder = AddRecordViewHolder(holder.itemView)
            addRecordViewHolder.addNote!!.setOnClickListener {
                mContext.startActivity(Intent(mContext, RecordActivity::class.java))
            }
        } else if (getItemViewType(position) == viewRecord) {
            filepath?.let {
                val file = File(it[position - 1])
                (holder as ViewRecordViewHolder).customView(file)
            }
        }
    }

    class ViewRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SimpleDateFormat")
        private val currentDate: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        private val mContext: Context = itemView.context
        fun customView(file: File) {
            val date = Date(file.lastModified())
            itemView.recordTitle.text = file.nameWithoutExtension
            itemView.recordLength.text = getRecordLength(getDuration(file)!!.toLong())
            itemView.recordDate.text = currentDate.format(date)
            itemView.setOnClickListener {
                try {
                    val playRecord = PlayRecordFragment().newInstance(file)
                    val transaction: FragmentTransaction = (mContext as FragmentActivity)
                        .supportFragmentManager
                        .beginTransaction()
                    playRecord.show(transaction, "dialog_playback")
                } catch (e: Exception) {
                    println(e)
                }
            }
            itemView.setOnLongClickListener {
                showOption(file)
                false
            }
        }

        private fun showOption(file: File) {
            val dialogBuilder = AlertDialog.Builder(mContext)
            val view = View.inflate(mContext, R.layout.dialog_option, null)
            dialogBuilder.setView(view)
            val alertDialog = dialogBuilder.create()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
            alertDialog.setCancelable(true)
            view.findViewById<TextView>(R.id.share).setOnClickListener {
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                    type = "audio/mp3"
                    mContext.startActivity(Intent.createChooser(this, "Send to"))
                }
                alertDialog.dismiss()
            }
            view.findViewById<TextView>(R.id.rename).setOnClickListener {
                renameRecord(file)
                alertDialog.dismiss()
            }
            view.findViewById<TextView>(R.id.delete).setOnClickListener {
                file.delete()
                it.snackBar("Deleted")
                alertDialog.dismiss()
                mContext.startHomeActivity()
            }
        }

        private fun renameRecord(file: File) {
            val dialogBuilder = AlertDialog.Builder(mContext)
            val view = View.inflate(mContext, R.layout.dialog_edit_text, null)
            dialogBuilder.setView(view)
            val alertDialog = dialogBuilder.create()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.SlideAnimation
            alertDialog.show()
            view.findViewById<TextView>(R.id.title).text = "Record name"
            val newName = view.findViewById<View>(R.id.input) as EditText
            view.findViewById<TextView>(R.id.confirm).setOnClickListener {
                try {
                    val value = newName.text.toString() + ".mp3"
                    file.renameTo(File(recordsDirPath + value))
                    it.snackBar("Renamed")
                    alertDialog.dismiss()
                    mContext.startHomeActivity()
                } catch (e: java.lang.Exception) {
                    mContext.startHomeActivity()
                }
            }
            view.findViewById<TextView>(R.id.cancel).setOnClickListener { alertDialog.dismiss() }
        }

        private fun getDuration(file: File): String? {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(mContext, Uri.fromFile(file))
            return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        }
    }

    class AddRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addNote: ImageButton? = itemView.findViewById(R.id.addItem)
    }

}
