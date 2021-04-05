package com.ramadan.notify.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.devlomi.record_view.OnRecordListener
import com.ramadan.notify.R
import com.ramadan.notify.ui.viewModel.NoteListener
import com.ramadan.notify.ui.viewModel.RecordViewModel
import com.ramadan.notify.utils.startHomeActivity
import kotlinx.android.synthetic.main.activity_record.*

class RecordActivity : AppCompatActivity(), NoteListener {
    private val viewModel by lazy { ViewModelProvider(this).get(RecordViewModel::class.java) }
    var recordName = "notify${System.currentTimeMillis()}.mp3"

    override fun onStart() {
        super.onStart()
        setName()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.noteListener = this
        checkPermission()
        recordButton.isListenForRecord = true
        recordButton.setRecordView(recordView)
        recordView.setSoundEnabled(false)
        recordView.setCounterTimeColor(Color.parseColor("#21bf73"))
        recordView.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0)
        recordView.setSlideToCancelTextColor(Color.parseColor("#21bf73"))
        recordView.setSlideToCancelArrowColor(Color.parseColor("#313131"))
        recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                viewModel.startRecording()
                startTimer()
            }

            override fun onCancel() {
                viewModel.stopRecording(true)
                stopTimer()
                Toast.makeText(this@RecordActivity, "Canceled", Toast.LENGTH_SHORT).show()
            }

            override fun onFinish(recordTime: Long) {
                stopTimer()
                viewModel.stopRecording(false)
                Toast.makeText(this@RecordActivity, "Saved", Toast.LENGTH_SHORT).show()
                onSuccess()
            }

            override fun onLessThanSecond() = stopTimer()
        })
        recordView.setOnBasketAnimationEndListener {
            stopTimer()
            Toast.makeText(this@RecordActivity, "Canceled", Toast.LENGTH_SHORT).show()
        }
    }

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
        val title = view.findViewById<TextView>(R.id.title)
        title.text = "Voice note"
        val fileName = view.findViewById<View>(R.id.input) as EditText
        val confirm = view.findViewById<TextView>(R.id.confirm)
        val cancel = view.findViewById<TextView>(R.id.cancel)
        confirm.setOnClickListener {
            this.recordName = fileName.text.toString()
            viewModel.saveRecordToExternalStorage(recordName)
            alertDialog.cancel()
        }
        cancel.setOnClickListener { alertDialog.cancel() }
    }

    fun startTimer() {
        chronometer.start()
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.setTextColor(Color.parseColor("#21bf73"))
        recordProgressBar.isIndeterminate = true
    }

    fun stopTimer() {
        chronometer.stop()
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.setTextColor(Color.parseColor("#525252"))
        recordProgressBar.isIndeterminate = false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray,
    ) {
        when (requestCode) {
            77 -> if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) return
            else -> super.onBackPressed()
        }
    }

    private fun checkPermission() {
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (ContextCompat.checkSelfPermission(
                this,
                permissions.toString()
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, permissions, 77
            )
        }
    }


    override fun onStarted() {}

    override fun onSuccess() {
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        startHomeActivity()
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}