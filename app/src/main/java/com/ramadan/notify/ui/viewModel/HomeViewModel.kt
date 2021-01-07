package com.ramadan.notify.ui.viewModel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import java.io.File


class HomeViewModel : ViewModel() {

    private var recordFilePath: Array<String?>? = null
    private var recordListFile: Array<File>? = null
    private var recordDirectory =
        File(Environment.getExternalStorageDirectory().path + "/Notify/Records")

    private var whiteboardFilePath: Array<String?>? = null
    private var whiteboardListFile: Array<File>? = null
    private var whiteboardDirectory =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + "/Notify")
    var noteListener: NoteListener? = null

    fun retrieveRecords(): Array<String?>? {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Log.e("Record", "Environment.MEDIA IS MOUNTED")
            noteListener?.onFailure("Error in load records")
            return recordFilePath
        }
        if (!recordDirectory.exists()) recordDirectory.mkdirs()
        if (recordDirectory.isDirectory) {
            recordListFile = recordDirectory.listFiles()
            recordListFile!!.sortByDescending { it.lastModified() }
            recordFilePath = arrayOfNulls(recordListFile!!.size)
            for (i in recordListFile!!.indices) recordFilePath!![i] = recordListFile!![i].path
        } else noteListener?.onFailure("No records yet")
        return recordFilePath
    }

    fun retrieveWhiteboards(): Array<String?>? {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Log.e("Records", "Environment.MEDIA IS MOUNTED")
            noteListener?.onFailure("Failed to load whiteboards")
            return whiteboardFilePath
        }
        if (!whiteboardDirectory.exists()) whiteboardDirectory.mkdirs()
        if (whiteboardDirectory.isDirectory) {
            whiteboardListFile = whiteboardDirectory.listFiles()
            whiteboardListFile!!.sortByDescending { it.lastModified() }
            whiteboardFilePath = arrayOfNulls(whiteboardListFile!!.size)
            for (i in whiteboardListFile!!.indices) {
                whiteboardFilePath!![i] = whiteboardListFile!![i].path
            }
        } else noteListener?.onFailure("No whiteboards yet")
        return whiteboardFilePath
    }

}