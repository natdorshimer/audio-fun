import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

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

    fun write(buffer: ByteArray) {
        sourceDataLine.write(buffer, 0, buffer.size.coerceAtMost(sourceDataLine.available()))
    }
}