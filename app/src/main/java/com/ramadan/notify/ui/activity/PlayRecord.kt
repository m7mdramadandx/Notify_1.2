@file:Suppress("DEPRECATION")

package com.ramadan.notify.ui.activity

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
import com.google.android.gms.ads.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ramadan.notify.R
import com.ramadan.notify.utils.getRecordLength
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


class PlayRecord : DialogFragment(), MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

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


    fun newInstance(mFile: File?): PlayRecord? {
        val dialog: PlayRecord = PlayRecord()
        val args = Bundle().apply {
            mFile?.let { putString("record", it.path) }
        }
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
        val view: View = activity!!.layoutInflater.inflate(R.layout.playback, null)
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
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable)
                    mMediaPlayer!!.seekTo(seekBar.progress)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(
                        mMediaPlayer!!.currentPosition.toLong()
                    )
                    val seconds =
                        (TimeUnit.MILLISECONDS.toSeconds(
                            mMediaPlayer!!.currentPosition.toLong()
                        )
                                - TimeUnit.MINUTES.toSeconds(minutes))
                    currentProgress!!.text = String.format(
                        "%02d:%02d",
                        minutes,
                        seconds
                    )
                    updateSeekBar()
                }
            }
        })
        playPause?.setOnClickListener {
            onPlay(isPlaying)
            isPlaying = !isPlaying
        }
        MobileAds.initialize(view.context) {}
        mAdView = view.findViewById(R.id.adView2)
        val adRequest = AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build()
        mAdView!!.loadAd(adRequest)

        mAdView!!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {

            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

        }

        return alertDialog
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer != null)
            mMediaPlayer!!.pause()
    }

    override fun onResume() {
        super.onResume()
        if (mMediaPlayer != null)
            startPlaying()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mMediaPlayer != null)
            mMediaPlayer!!.stop()
    }

    override fun onStop() {
        super.onStop()
        if (mMediaPlayer != null)
            mMediaPlayer!!.stop()
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
        if (!isPlaying) {
            if (mMediaPlayer == null)
                startPlaying()
            else
                resumePlaying()
        } else {
            pausePlaying()
        }
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
        playPause?.setImageResource(R.drawable.play)
        mHandler.removeCallbacks(mRunnable)
        mMediaPlayer!!.pause()
    }

    private fun resumePlaying() {
        playPause?.setImageResource(R.drawable.pause)
        mHandler.removeCallbacks(mRunnable)
        mMediaPlayer!!.start()
        updateSeekBar()
    }

    private fun stopPlaying() {
        playPause?.setImageResource(R.drawable.play)
        mHandler.removeCallbacks(mRunnable)
        mMediaPlayer!!.stop()
        mMediaPlayer!!.reset()
        mMediaPlayer!!.release()
        mMediaPlayer = null

        isPlaying = !isPlaying
        seekBar!!.progress = 0
        currentProgress!!.text = "00:00"

        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private val mRunnable = Runnable {
        if (mMediaPlayer != null) {
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
        }
    }

    private fun updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mMediaPlayer?.start()
    }
}