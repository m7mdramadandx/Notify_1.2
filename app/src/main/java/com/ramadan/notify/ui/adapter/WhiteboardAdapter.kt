@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ramadan.notify.R
import com.ramadan.notify.ui.activity.WhiteboardActivity
import com.ramadan.notify.ui.fragment.WhiteboardsFragment
import com.ramadan.notify.utils.startHomeActivity
import kotlinx.android.synthetic.main.item_whiteboard.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class WhiteboardAdapter(private val filepath: Array<String?>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @SuppressLint("SimpleDateFormat")
    private val currentDate: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
    private var bitmap: Bitmap? = null
    private val viewNote = 0
    private val addNote = 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) addNote else viewNote
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewNote) {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_whiteboard, parent, false)
            return ViewWhiteboardViewHolder(view)

        } else {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add, parent, false)
            AddWhiteboardViewHolder(view)

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
        if (getItemViewType(position) == viewNote) {
            val file = File(filepath!![position - 1]!!)
            val date = Date(file.lastModified())
            val bmpOptions = BitmapFactory.Options()
            bmpOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.path, bmpOptions)
            bmpOptions.inSampleSize = 2
            bmpOptions.inJustDecodeBounds = false
            bitmap = BitmapFactory.decodeFile(file.path, bmpOptions)
            (holder as ViewWhiteboardViewHolder)
            if (bitmap != null) {
                holder.customView(bitmap!!)
                holder.itemView.whiteboardTitle.text = file.nameWithoutExtension
                holder.itemView.whiteboardDate.text = currentDate.format(date)
                holder.itemView.setOnLongClickListener {
                    holder.showOption(file)
                    false
                }
            } else {
                holder.itemView.context.startHomeActivity()
            }

        } else {
            (holder as AddWhiteboardViewHolder).addNote!!.setOnClickListener {
                holder.mContext.startActivity(Intent(holder.mContext, WhiteboardActivity::class.java))
            }
        }
    }


    class ViewWhiteboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mContext: Context = itemView.context
        private val whiteboards = WhiteboardsFragment()
        private val dirPath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).path + "/Notify/"

        fun customView(bitmap: Bitmap) {
            itemView.whiteboardImg.setImageBitmap(bitmap)
            itemView.whiteboardImg.animation = AnimationUtils.loadAnimation(
                mContext,
                R.anim.zoom_in
            )
            itemView.setOnClickListener {
                whiteboards.showWhiteboard(bitmap, mContext)
            }
        }

        fun showOption(file: File) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            val view = View.inflate(mContext, R.layout.dialog_option, null)
            dialogBuilder.setView(view)
            val alertDialog = dialogBuilder.create()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
            alertDialog.setCancelable(true)
            val share = view.findViewById<TextView>(R.id.share)
            val rename = view.findViewById<TextView>(R.id.rename)
            val delete = view.findViewById<TextView>(R.id.delete)
            share.setOnClickListener {
                shareWhiteboard(file)
                alertDialog.dismiss()
            }
            rename.setOnClickListener {
                renameWhiteboard(file)
                alertDialog.dismiss()
            }
            delete.setOnClickListener {
                file.delete()
                Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show()
                alertDialog.cancel()
                mContext.startHomeActivity()
            }
        }

        private fun shareWhiteboard(file: File) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            shareIntent.type = "picture/jpg"
            mContext.startActivity(Intent.createChooser(shareIntent, "Send to"))
        }

        private fun renameWhiteboard(file: File) {
            val dialogBuilder = AlertDialog.Builder(mContext)
            val view = View.inflate(mContext, R.layout.dialog_edit_text, null)
            dialogBuilder.setView(view)
            val alertDialog = dialogBuilder.create()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.SlideAnimation
            alertDialog.show()
            view.findViewById<TextView>(R.id.title).text = "board name"
            val newName = view.findViewById<View>(R.id.input) as EditText
            val confirm = view.findViewById<TextView>(R.id.confirm)
            val cancel = view.findViewById<TextView>(R.id.cancel)
            confirm.setOnClickListener {
                try {
                    val value = newName.text.toString() + ".jpg"
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
    }

    class AddWhiteboardViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val mContext: Context = itemView.context
        val addNote: ImageButton? = itemView.findViewById(R.id.addItem)
    }
}