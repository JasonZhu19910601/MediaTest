package com.leading.mediatest

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.media.AudioTrack
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_MUSIC
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.leading.mediatest.GlobalConfig.Companion.AUDIO_FORMAT
import com.leading.mediatest.GlobalConfig.Companion.CHANNEL_CONFIG
import com.leading.mediatest.GlobalConfig.Companion.SAMPLE_RATE_INHZ
import kotlinx.android.synthetic.main.activity_audio_record.*
import java.io.*
import kotlin.concurrent.thread


class AudioRecordActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_control -> if (btn_control.text.toString() == getString(R.string.start_record)) {
                btn_control.text = getString(R.string.stop_record)
                startRecord()
            } else {
                btn_control.text = getString(R.string.start_record)
                stopRecord()
            }

            R.id.btn_convert -> {
                val pcmToWavUtil = PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT)
                val pcmFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm")
                val wavFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.wav")
                if (!wavFile.mkdirs()) {
                    Log.e(TAG, "wavFile Directory not created")
                }
                if (wavFile.exists()) {
                    wavFile.delete()
                }
                pcmToWavUtil.pcmToWav(pcmFile.absolutePath, wavFile.absolutePath)
            }

            R.id.btn_play -> {
                val string = btn_play.text.toString()
                if (string == getString(R.string.start_play)) {
                    btn_play.text = getString(R.string.stop_play)
                    playInModeStream()
//                    playInModeStatic()
                } else {
                    btn_play.text = getString(R.string.start_play)
                    stopPlay()
                }
            }
        }
    }

    /**
     * 播放，使用static模式
     */
    private fun playInModeStatic() {
        // static模式，需要将音频数据一次性write到AudioTrack的内部缓冲区
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void): Void? {
                try {
                    val openRawResource = resources.openRawResource(R.raw.ding)
                    try {
                        val out = ByteArrayOutputStream()
                        var b = 0
                        while (b != -1) {
                            b = openRawResource.read()
                            out.write(b)
                        }
                        Log.d(TAG, "Got the data")
                        audioData = out.toByteArray()
                    } finally {
                        openRawResource.close()
                    }
                } catch (e: IOException) {
                    Log.wtf(TAG, "Failed to read", e)
                }
                return null
            }


            override fun onPostExecute(v: Void) {
                Log.i(TAG, "Creating track...audioData.length = " + audioData.size)

                // R.raw.ding铃声文件的相关属性为 22050Hz, 8-bit, Mono
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioTrack = AudioTrack(
                            AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build(),
                            AudioFormat.Builder().setSampleRate(22050)
                                    .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build(),
                            audioData.size,
                            AudioTrack.MODE_STATIC,
                            AudioManager.AUDIO_SESSION_ID_GENERATE)
                }
                Log.d(TAG, "Writing audio data...")
                audioTrack.write(audioData, 0, audioData.size)
                Log.d(TAG, "Starting playback")
                audioTrack.play()
                Log.d(TAG, "Playing")
            }
        }.execute()
    }

    /**
     * 停止播放
     */
    private fun stopPlay() {
        Log.e(TAG, "Stopping...")
        audioTrack.stop()
        Log.e(TAG, "Releasing...")
        audioTrack.release()
    }

    /**
     * 播放，使用stream模式
     */
    private fun playInModeStream() {
        /*
        * SAMPLE_RATE_INHZ 对应pcm音频的采样率
        * channelConfig 对应pcm音频的声道
        * AUDIO_FORMAT 对应pcm音频的格式
        * */
        try {
            val channelConfig = AudioFormat.CHANNEL_OUT_MONO
            val minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ, channelConfig, AUDIO_FORMAT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioTrack = AudioTrack(
                        AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build(),
                        AudioFormat.Builder()
                                .setEncoding(AUDIO_FORMAT)
                                .setChannelMask(channelConfig)
                                .build(),
                        minBufferSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE)
            }
            audioTrack.play()

            val file = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm")
            fileInputStream = FileInputStream(file)
            thread {
                Runnable {
                    val tempBuffer = ByteArray(minBufferSize)
                    while (fileInputStream.available() > 0) {
                        val read = fileInputStream.read(tempBuffer)
                        if (read == AudioTrack.ERROR_INVALID_OPERATION
                                || read == AudioTrack.ERROR_BAD_VALUE) {
                            continue
                        }
                        if (read != 0 && read != -1) {
                            audioTrack.write(tempBuffer, 0, read)
                        }
                    }
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }


    private fun stopRecord() {
        isRecording = false
        // 释放资源
        audioRecord.stop()
        audioRecord.release()
    }

    private fun startRecord() {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize)
        val data = ByteArray(minBufferSize)
        val file = File(DIRECTORY_MUSIC, "test.pcm")
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created")
        }
        if (file.exists()) {
            file.delete()
        }
        audioRecord.startRecording()
        isRecording = true

        // TODO: 2018/3/10 pcm数据无法直接播放，保存为WAV格式。
        thread {
            Runnable {
                var os: FileOutputStream? = null
                try {
                    os = FileOutputStream(file)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

                if (null != os) {
                    while (isRecording) {
                        val read = audioRecord.read(data, 0, minBufferSize)
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                os.write(data)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                    }
                    try {
                        Log.i(TAG, "run: close file output stream !")
                        os.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val permissionsDenied: ArrayList<String> = ArrayList()
            for (i in 0 until grantResults.size) {
                val grantResult = grantResults[i]
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    permissionsDenied.add(permissions[i])
                }
            }
            if (permissionsDenied.isNotEmpty()) {
                Toast.makeText(this, (permissionsDenied + " 权限被用户禁止！")
                        .toString(), Toast.LENGTH_LONG).show()
                Log.i(TAG, (permissionsDenied + " 权限被用户禁止！").toString())
                finish()
            }
        }
    }

    val PERMISSION_REQUEST_CODE = 666
    private val TAG = "AudioRecordActivity"
    /**
     * 需要申请的权限
     */
    val allPermissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    /**
     * 被用户拒绝的权限
     */
    lateinit var permissionsNeedToRequest: ArrayList<String>
    var isRecording: Boolean = false
    lateinit var audioRecord: AudioRecord
    lateinit var audioTrack: AudioTrack
    lateinit var audioData: ByteArray
    lateinit var fileInputStream: FileInputStream
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)
        btn_control.setOnClickListener(this)
        btn_convert.setOnClickListener(this)
        btn_play.setOnClickListener(this)
        checkPermissions()
    }

    private fun checkPermissions() {
        permissionsNeedToRequest = ArrayList()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            for (permission in allPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeedToRequest.add(permission)
                }
            }
            if (!permissionsNeedToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        permissionsNeedToRequest.toArray(arrayOfNulls(permissionsNeedToRequest.size))
                        , PERMISSION_REQUEST_CODE)
            }
        }
    }
}
