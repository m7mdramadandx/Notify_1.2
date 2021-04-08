@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.viewModel

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import com.ramadan.notify.utils.whiteboardDirPath
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class WhiteboardViewModel : ViewModel() {
    var file: File? = null
    var noteListener: NoteListener? = null

    fun saveImageToExternalStorage(bitmap: Bitmap, fileName: String) {
        try {
            val dir = File(whiteboardDirPath)
            if (!dir.exists()) dir.mkdirs()
            val file = File("$whiteboardDirPath/$fileName.jpg")
            if (file.exists()) {
                noteListener?.onFailure("this name is already exist")
                return
            }
            file.createNewFile()
            val outStream: OutputStream?
            outStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
            noteListener?.onSuccess()
        } catch (e: Exception) {
            Log.e("saveToExternalStorage()", e.message!!)
            noteListener?.onFailure(e.message!!)
            return
        }
    }

}