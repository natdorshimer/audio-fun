package audio

import javax.sound.sampled.AudioFormat

data class AudioSettings(
    val sampleRate: Double = 44100.0,
    val bitsPerSample: Int = 16,
    val bufferSize: Int = 1024,
) {
    val bytesPerSample: Int
        get() = bitsPerSample / 8

    val numOfSamplesInBuffer: Int
        get() = bufferSize / bytesPerSample
}

fun createAudioFormat(audioSettings: AudioSettings): AudioFormat {
    val bigEndian = false
    val signed = true
    val channels = 1
    return AudioFormat(
        audioSettings.sampleRate.toFloat(),
        audioSettings.bitsPerSample,
        channels,
        signed,
        bigEndian
    )
}