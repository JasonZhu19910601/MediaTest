package com.leading.mediatest

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import android.os.Environment
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class MediaExtractorAndMediaMuxerActivity : BaseActivity() {
    companion object {
        val SDCARD_PATH = Environment.getExternalStorageDirectory().path
    }

    private lateinit var mediaExtractor: MediaExtractor
    private lateinit var mediaMuxer: MediaMuxer

    override fun allPermissionsGranted() {
        thread { Runnable { process() } }.start()
    }

    private fun process(): Boolean {
        mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(SDCARD_PATH + File.separator + "ss.mp4")
        var videoTrackIndex = -1
        var frameRate = 0
        for (i in 0 until mediaExtractor.trackCount) {
            val trackFormat = mediaExtractor.getTrackFormat(i)
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (!mime.startsWith("video/")) {
                continue
            }
            frameRate = trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
            mediaExtractor.selectTrack(i)

            mediaMuxer = MediaMuxer(SDCARD_PATH + File.separator + "output.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            videoTrackIndex = mediaMuxer.addTrack(trackFormat)
            mediaMuxer.start()
        }

        val info: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
        info.presentationTimeUs = 0
        val buffer: ByteBuffer = ByteBuffer.allocate(500 * 1024)
        var sampleSize = 1
        while (sampleSize > 0) {
            sampleSize = mediaExtractor.readSampleData(buffer, 0)
            info.offset = 0
            info.size = sampleSize
            info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
            info.presentationTimeUs += 1000 * 1000 / frameRate
            mediaMuxer.writeSampleData(videoTrackIndex, buffer, info);
            mediaExtractor.advance()
        }

        mediaExtractor.release()

        mediaMuxer.stop()
        mediaMuxer.release()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_extractor_and_media_muxer)
    }
}
