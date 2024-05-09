import javax.sound.sampled.AudioFormat
import kotlin.math.cos
import kotlin.math.pow

class PianoAudioStreamer(
    val piano: Piano = Piano()
) {
    private val sampleRate = 44100.0
    private val bits = 16
    private val bigEndian = false
    private val signed = true
    private val channels = 1
    private val format = AudioFormat(sampleRate.toFloat(), bits, channels, signed, bigEndian)

    private val bufferSize = 4096

    private val output = AudioStreamingOutput(format)

    private val bytes = (bits / 8)

    // If this isn't synced up with the buffer size, too much will enter the queue and it won't be in real time
    // If the value is too high, the sound will be choppy
    private val timeToWaitMs = ((bufferSize / bytes) / sampleRate * 1000).toInt() - 2

    private lateinit var writingThread: StoppableThread

    fun start() {
        var startTime = System.currentTimeMillis()
        writingThread = StoppableThread {
            if (System.currentTimeMillis() - startTime > timeToWaitMs && piano.notesPressed.isNotEmpty()) {
                startTime = System.currentTimeMillis()
                val samples = createSamplesFromCurrentlyPressedNotes()
                val outputBytes = transformFloatArrayToByteArray(samples)
                output.write(outputBytes)
            }
        }

        output.start()
        writingThread.start()
    }

    // This helps the sound remain continuous
    private var start = 0

    private fun transformFloatArrayToByteArray(samples: FloatArray): ByteArray {
        val byteBuffer = ByteArray(samples.size * bytes)
        var bufferIndex = 0
        var i = 0

        // Multiply in binary by 111111.... to get proper signed value
        val multiplier = 2.0.pow(bits - 1) - 1

        while (i < byteBuffer.size) {
            // Convert to PCM bits
            val x = (samples[bufferIndex++] * multiplier).toInt()

            for (j in 0 until bytes) {
                byteBuffer[i + j] = (x ushr (j * 8)).toByte()
            }

            i += bytes
        }

        return byteBuffer
    }

    private val keyAmplitude = 0.2

    private val previousNotes = mutableSetOf<Note>()

    private fun createSamplesFromCurrentlyPressedNotes(): FloatArray {
        val numOfSamples = bufferSize / bytes
        val notesToGraduallyIncrease = mutableSetOf<Note>()

        val gradualIncreaseWithTime = { i: Int ->
            val time = i / sampleRate * 5000
            (1 - Math.E.pow(-time)).also{ println(it) }
        }

        previousNotes.clear()
        previousNotes.addAll(piano.notesPressed)

        val samples = FloatArray(numOfSamples)

        for (i in 0 until numOfSamples) {
            val seconds = start++ / sampleRate
            val sample = piano.notesPressed.sumOf {
                val modifier = if (it in notesToGraduallyIncrease) {
                    gradualIncreaseWithTime(i)
                } else {
                    1.0
                }
                keyAmplitude * modifier * cos(2.0 * Math.PI * it.getFrequency(piano.octave) * seconds)
            }
            samples[i] = sample.toFloat()
        }

        return samples
    }

    fun getNotesPressed(): List<String> {
        return piano.notesPressed.map { it.toString() }
    }

    fun close() {
        println("Closing piano")

        output.close()

        writingThread.stop()

        println("Writing thread closed")
    }
}


