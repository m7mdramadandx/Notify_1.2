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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.ramadan.notify.MainActivity
import com.ramadan.notify.MainActivity.Companion.isConnected
import com.ramadan.notify.R
import com.ramadan.notify.ui.activity.WhiteboardActivity
import com.ramadan.notify.ui.fragment.WhiteboardsFragment
import com.ramadan.notify.utils.debug_tag
import com.ramadan.notify.utils.startHomeActivity
import com.ramadan.notify.utils.whiteboardDirPath
import kotlinx.android.synthetic.main.item_whiteboard.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class WhiteboardAdapter(private val filepath: Array<String?>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @SuppressLint("SimpleDateFormat")
    private val currentDate: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
    private var bitmap: Bitmap? = null

    companion object {
        private const val viewNote = 1
        private const val addNote = 2
        private const val adView = 3
    }


    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> addNote
            position % 3 == 0 && isConnected -> adView
            else -> viewNote
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            viewNote -> {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_whiteboard, parent, false)
                return ViewWhiteboardViewHolder(view)
            }
            adView -> {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_native_ad_templete, parent, false)
                return AdViewHolder(view)
            }
            else -> {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add, parent, false)
                AddWhiteboardViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        filepath?.let {
            return if (it.isNotEmpty()) {
                if (it.size % 3 == 0) {
                    (it.size / 3) + 1 + it.size
                } else
                    it.size + 1
            } else 1
        } ?: return 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mContext = holder.itemView.context
        when (getItemViewType(position)) {
            addNote -> {
                (holder as AddWhiteboardViewHolder).addNote!!.setOnClickListener {
                    mContext.startActivity(Intent(mContext, WhiteboardActivity::class.java))
                }
            }
            viewNote -> {
                filepath?.let {
                    var p0 = if (filepath.size % 3 == 0) {
                        position - (filepath.size / 3) - 1
                    } else position - 1
                    if (p0 < 0) p0 = 0
                    val file = File(filepath[p0])
                    val date = Date(file.lastModified())
                    BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        BitmapFactory.decodeFile(file.path, this)
                        inSampleSize = 2
                        inJustDecodeBounds = false
                        bitmap = BitmapFactory.decodeFile(file.path, this)
                    }
                    (holder as ViewWhiteboardViewHolder)
                    bitmap?.let {
                        holder.customView(it)
                        holder.itemView.whiteboardTitle.text = file.nameWithoutExtension
                        holder.itemView.whiteboardDate.text = currentDate.format(date)
                        holder.itemView.setOnLongClickListener {
                            holder.showOption(file)
                            false
                        }
                    } ?: mContext.startHomeActivity()
                }
            }
            adView -> {
                val adLoader = AdLoader.Builder(holder.itemView.context,
                    mContext.getString(R.string.native_ad))
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


    class ViewWhiteboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mContext: Context = itemView.context
        private val whiteboards = WhiteboardsFragment()

        fun customView(bitmap: Bitmap) {
            itemView.whiteboardImg.setImageBitmap(bitmap)
            itemView.whiteboardImg.animation = AnimationUtils.loadAnimation(
                mContext,
                R.anim.zoom_in
            )
            itemView.setOnClickListener { whiteboards.showWhiteboard(bitmap, mContext) }
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
                    file.renameTo(File(whiteboardDirPath + value))
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

    class AddWhiteboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addNote: ImageButton? = itemView.findViewById(R.id.addItem)
    }

    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adTemplate: TemplateView = itemView.findViewById(R.id.nativeTemplateView)
    }
}