import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.math.pow

class AudioStreamingOutput(
    private val audioFormat: AudioFormat
) {
    private val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
    private val sourceDataLine = AudioSystem.getLine(info) as SourceDataLine

    fun start() {
        sourceDataLine.open(audioFormat)
        sourceDataLine.start()
        println("Started")
        println("Is active: ${sourceDataLine.isActive}")
    }

    fun close() {
        sourceDataLine.flush()
        sourceDataLine.close()
        println("Source line Closed")
    }

    fun write(samples: FloatArray) {
        val buffer = transformSamplesToByteBuffer(samples, audioFormat.sampleSizeInBits)
        sourceDataLine.write(buffer, 0, buffer.size.coerceAtMost(sourceDataLine.available()))
    }
}



private fun transformSamplesToByteBuffer(samples: FloatArray, bitsPerSample: Int): ByteArray {
    val bytesPerSample = bitsPerSample / 8
    val byteBuffer = ByteArray(samples.size * bytesPerSample)
    var bufferIndex = 0
    var i = 0

    // Multiply in binary by 111111.... to get proper signed value
    val multiplier = 2.0.pow(bitsPerSample - 1) - 1

    while (i < byteBuffer.size) {
        // Convert to PCM bits
        val x = (samples[bufferIndex++] * multiplier).toInt()

        for (j in 0 until bytesPerSample) {
            byteBuffer[i + j] = (x ushr (j * 8)).toByte()
        }

        i += bytesPerSample
    }

    return byteBuffer
}