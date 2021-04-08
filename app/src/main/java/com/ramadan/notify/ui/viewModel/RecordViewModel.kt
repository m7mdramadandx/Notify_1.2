@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.viewModel

import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import com.ramadan.notify.utils.recordsDirPath
import java.io.File


class RecordViewModel : ViewModel(), MediaRecorder.OnErrorListener {

    private var filePath = recordsDirPath + "/notify${System.currentTimeMillis()}.mp3"
    private var outputFile = File(filePath)
    var noteListener: NoteListener? = null

    fun startRecording() {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
        mRecorder.setAudioEncodingBitRate(48000)
        mRecorder.setAudioSamplingRate(16000)
        mRecorder.setOutputFile(outputFile.path)
        mRecorder.setOnErrorListener(this)
        mRecorder.prepare()
        mRecorder.start()
    }

    fun stopRecording(boolean: Boolean) {
        mRecorder.setOnErrorListener(this)
        mRecorder.stop()
        mRecorder.release()
        if (boolean) {
            Log.w("Record", "Deleted File ")
            outputFile.delete()
        }
    }

    fun saveRecordToExternalStorage(fileName: String) {
        val dir = File(recordsDirPath)

        if (!dir.exists()) dir.mkdirs()
        filePath = "$recordsDirPath/$fileName.mp3"
        outputFile = File(filePath)
        if (outputFile.exists()) {
            noteListener?.onFailure("this name is already exist")
            return
        }
    }

    override fun onError(mr: MediaRecorder?, what: Int, extra: Int) {
        Log.e("Record", mr.toString())
        noteListener?.onFailure("sorry, try again")
    }

    companion object {
        private var mRecorder = MediaRecorder()
    }

}