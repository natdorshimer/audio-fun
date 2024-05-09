import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

class AudioStreamingOutput(
    audioFormat: AudioFormat
) {
    private val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
    private val sourceDataLine = AudioSystem.getLine(info) as SourceDataLine
    private val buffers: LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue()

    init {
        sourceDataLine.open(audioFormat)
        sourceDataLine.start()
        println("Started")
        println("Is active: ${sourceDataLine.isActive}")
    }

    fun close() {
        sourceDataLine.flush()
        sourceDataLine.close()
        println("Source line Closed")

        thread.stop()
        println("Polling thread closed")
    }

    private lateinit var thread: StoppableThread

    fun start() {
        thread = StoppableThread {
            buffers.poll(100L, TimeUnit.MILLISECONDS)?.let {
                println(buffers.size + 1)
                println(sourceDataLine.available())
                sourceDataLine.write(it, 0, it.size.coerceAtMost(sourceDataLine.available()))
            }
        }.also{ it.start() }
    }

    fun write(buffer: ByteArray) {
        buffers.add(buffer)
    }
}