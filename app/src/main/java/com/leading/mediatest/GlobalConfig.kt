package com.leading.mediatest

import android.media.AudioFormat

/**
 * @package com.leading.mediatest
 * @fileName GlobalConfig
 * @date 2018/9/14 17:16
 * @author Zj
 * @describe TODO
 * @org Leading.com(北京理正软件)
 * @email 2856211755@qq.com
 * @computer Administrator
 */
class GlobalConfig {
    companion object {
        /**
         * 采样率，现在能够保证在所有设备上使用的采样率是44100Hz,
         * 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
         */
        const val SAMPLE_RATE_INHZ = 44100

        /**
         * 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO.
         * 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
         */
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO

        /**
         * 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
         */
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }
}