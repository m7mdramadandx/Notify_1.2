@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ramadan.notify.R
import com.ramadan.notify.utils.getRecordLength
import com.ramadan.notify.utils.showToast
import com.ramadan.notify.utils.tryAgainMsg
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


class PlayRecordFragment : DialogFragment(), MediaPlayer.OnErrorListener,
    MediaPlayer.OnPreparedListener {

    private var file: File? = null
    private val mHandler = Handler()
    private var mMediaPlayer: MediaPlayer? = null
    private var seekBar: SeekBar? = null
    private var playPause: FloatingActionButton? = null
    private var recordName: TextView? = null
    private var recordDuration: TextView? = null
    private var currentProgress: TextView? = null
    private var mAdView: AdView? = null

    private var isPlaying = false
    var minutes: Long = 0
    var seconds: Long = 0


    fun newInstance(mFile: File?): PlayRecordFragment {
        val dialog = PlayRecordFragment()
        val args = Bundle().apply { mFile?.let { putString("record", it.path) } }
        dialog.arguments = args
        println(mFile?.extension)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = arguments!!.getString("record")
        file = File(path!!)
        val mp = MediaPlayer.create(context, Uri.fromFile(file))
        val itemDuration: Long = mp.duration.toLong()
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration)
        seconds = (TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes))
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
        val view: View = activity!!.layoutInflater.inflate(R.layout.dialog_playback, null)
        dialogBuilder.setView(view)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.ShrinkAnimation
        seekBar = view.findViewById<View>(R.id.seek_bar) as SeekBar
        recordName = view.findViewById<View>(R.id.recordName) as TextView
        recordDuration = view.findViewById<View>(R.id.fileDuration) as TextView
        currentProgress = view.findViewById<View>(R.id.currentProgress) as TextView
        playPause = view.findViewById(R.id.action_button) as FloatingActionButton
        recordName?.text = file!!.nameWithoutExtension
        recordDuration!!.text = String.format("%02d:%02d", minutes, seconds)
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mMediaPlayer != null && fromUser) {
                    mMediaPlayer!!.seekTo(progress)
                    mHandler.removeCallbacks(mRunnable)
                    currentProgress!!.text =
                        getRecordLength(mMediaPlayer!!.currentPosition.toLong())
                    updateSeekBar()
                } else if (mMediaPlayer == null && fromUser) {
                    prepareMediaPlayerFromPoint(progress)
                    updateSeekBar()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mMediaPlayer?.let { mHandler.removeCallbacks(mRunnable) }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable)
                    mMediaPlayer!!.seekTo(seekBar.progress)
                    val minutes =
                        TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer!!.currentPosition.toLong())
                    val seconds =
                        (TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer!!.currentPosition.toLong())
                                - TimeUnit.MINUTES.toSeconds(minutes))
                    currentProgress!!.text = String.format("%02d:%02d", minutes, seconds)
                    updateSeekBar()
                }
            }
        })
        playPause?.setOnClickListener {
            onPlay(isPlaying)
            isPlaying = !isPlaying
        }
        mAdView = view.findViewById(R.id.adView2)
        mAdView!!.loadAd(AdRequest.Builder().build())
        return alertDialog
    }

    override fun onPause() {
        super.onPause()
        mMediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        mMediaPlayer?.let { startPlaying() }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.stop()
    }

    override fun onStop() {
        super.onStop()
        mMediaPlayer?.stop()
    }

    private fun prepareMediaPlayerFromPoint(progress: Int) {
        mMediaPlayer = MediaPlayer.create(context, Uri.fromFile(file))
        try {
            mMediaPlayer?.setOnErrorListener(this)
            mMediaPlayer?.setOnPreparedListener(this)
            seekBar!!.max = mMediaPlayer!!.duration
            mMediaPlayer!!.seekTo(progress)
            mMediaPlayer!!.setOnCompletionListener { stopPlaying() }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun onPlay(isPlaying: Boolean) {
        if (!isPlaying) mMediaPlayer?.let { startPlaying() } ?: resumePlaying() else pausePlaying()
    }

    private fun startPlaying() {
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        playPause?.setImageResource(R.drawable.pause)
        mMediaPlayer = MediaPlayer.create(context, Uri.fromFile(file))
        try {
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer!!.setOnPreparedListener(this)
            mMediaPlayer!!.setOnErrorListener(this)
            seekBar!!.max = mMediaPlayer!!.duration
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mMediaPlayer!!.setOnCompletionListener { stopPlaying() }
        updateSeekBar()
    }

    private fun pausePlaying() {
        playPause?.let {
            it.setImageResource(R.drawable.play)
            mHandler.removeCallbacks(mRunnable)
            mMediaPlayer!!.pause()
        } ?: requireContext().showToast(tryAgainMsg)
    }

    private fun resumePlaying() {
        playPause?.let {
            it.setImageResource(R.drawable.pause)
            mHandler.removeCallbacks(mRunnable)
            mMediaPlayer?.start()
            updateSeekBar()
        } ?: requireContext().showToast(tryAgainMsg)
    }

    private fun stopPlaying() {
        playPause?.let {
            it.setImageResource(R.drawable.play)
            mHandler.removeCallbacks(mRunnable)
            mMediaPlayer?.let { it1 ->
                it1.stop()
                it1.reset()
                it1.release()
                mMediaPlayer = null
            }
            isPlaying = !isPlaying
            seekBar!!.progress = 0
            currentProgress!!.text = "00:00"
            activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } ?: requireContext().showToast(tryAgainMsg)
    }

    private val mRunnable = Runnable {
        mMediaPlayer?.let {
            val mCurrentPosition = mMediaPlayer!!.currentPosition
            seekBar?.progress = mCurrentPosition
            val minutes =
                TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition.toLong())
            val seconds =
                (TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition.toLong())
                        - TimeUnit.MINUTES.toSeconds(minutes))
            currentProgress!!.text = String.format(
                "%02d:%02d",
                minutes,
                seconds
            )
            updateSeekBar()
        } ?: requireContext().showToast(tryAgainMsg)
    }

    private fun updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean = false

    override fun onPrepared(mp: MediaPlayer?) {
        mMediaPlayer?.start()
    }
}