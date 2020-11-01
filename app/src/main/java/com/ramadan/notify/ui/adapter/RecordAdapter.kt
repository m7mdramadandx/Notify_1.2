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
import com.ramadan.notify.R
import com.ramadan.notify.ui.activity.PlayRecord
import com.ramadan.notify.ui.activity.Record
import com.ramadan.notify.utils.getRecordLength
import com.ramadan.notify.utils.startHomeActivity
import kotlinx.android.synthetic.main.record_item.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class RecordAdapter(private val filepath: Array<String?>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val viewRecord = 0
    private val addRecord = 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) addRecord else viewRecord
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewRecord) {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.record_item, parent, false)
            return ViewRecordViewHolder(view)

        } else {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_item, parent, false)
            NoteAdapter.AddNoteViewHolder(view)

        }
    }

    override fun getItemCount(): Int {
        if (filepath == null)
            return 1
        if (filepath.isNotEmpty())
            return filepath.size + 1
        return 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == viewRecord) {
            val file = File(filepath!![position - 1]!!)
            (holder as ViewRecordViewHolder).customView(file)
        } else {
            val addRecordViewHolder = AddRecordViewHolder(holder.itemView)
            addRecordViewHolder.addNote!!.setOnClickListener {
                addRecordViewHolder.mContext.startActivity(
                    Intent(addRecordViewHolder.mContext, Record::class.java)
                )
            }
        }
    }

    class ViewRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SimpleDateFormat")
        private val currentDate: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        private val dirPath =
            Environment.getExternalStorageDirectory().path + "/Notify/Records/"
        private val mContext: Context = itemView.context
        fun customView(file: File) {
            val date = Date(file.lastModified())
            itemView.recordTitle.text = file.nameWithoutExtension
            itemView.recordLength.text = getRecordLength(getDuration(file)!!.toLong())
            itemView.recordDate.text = currentDate.format(date)
            itemView.setOnClickListener {
                try {
                    val playRecord = PlayRecord().newInstance(file)
                    val transaction: FragmentTransaction = (mContext as FragmentActivity)
                        .supportFragmentManager
                        .beginTransaction()
                    playRecord?.show(transaction, "dialog_playback")
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
            val view = View.inflate(mContext, R.layout.option_dialog, null)
            dialogBuilder.setView(view)
            val alertDialog = dialogBuilder.create()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
            alertDialog.setCancelable(true)
            val share = view.findViewById<TextView>(R.id.share)
            val rename = view.findViewById<TextView>(R.id.rename)
            val delete = view.findViewById<TextView>(R.id.delete)
            share.setOnClickListener {
                shareRecord(file)
                alertDialog.cancel()
            }
            rename.setOnClickListener {
                renameRecord(file)
                alertDialog.dismiss()
            }
            delete.setOnClickListener {
                file.delete()
                Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show()
                alertDialog.cancel()
                mContext.startHomeActivity()
            }
        }


        private fun shareRecord(file: File) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            shareIntent.type = "audio/mp3"
            mContext.startActivity(Intent.createChooser(shareIntent, "Send to"))
        }

        private fun renameRecord(file: File) {
            val dialogBuilder = AlertDialog.Builder(mContext)
            val view = View.inflate(mContext, R.layout.edit_text_dialog, null)
            dialogBuilder.setView(view)
            val alertDialog = dialogBuilder.create()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.SlideAnimation
            alertDialog.show()
            val title = view.findViewById<TextView>(R.id.title)
            title.text = "Record name"
            val newName = view.findViewById<View>(R.id.input) as EditText
            val confirm = view.findViewById<TextView>(R.id.confirm)
            val cancel = view.findViewById<TextView>(R.id.cancel)
            confirm.setOnClickListener {
                try {
                    val value = newName.text.toString() + ".mp3"
                    file.renameTo(File(dirPath + value))
                    Toast.makeText(mContext, "Renamed", Toast.LENGTH_SHORT).show()
                    alertDialog.cancel()
                    mContext.startHomeActivity()
                } catch (e: java.lang.Exception) {
                    mContext.startHomeActivity()
                }
            }
            cancel.setOnClickListener { alertDialog.cancel() }

        }

        private fun getDuration(file: File): String? {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(mContext, Uri.fromFile(file))
            val durationStr =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            println(durationStr)
            return durationStr
        }
    }

    class AddRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mContext: Context = itemView.context
        val addNote: ImageButton? = itemView.findViewById(R.id.addItem)
    }
}