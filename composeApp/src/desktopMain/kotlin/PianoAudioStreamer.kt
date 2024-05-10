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

    private val bufferSize = 1024

    private val output = AudioStreamingOutput(format)

    private val bytes = (bits / 8)

    // If this isn't synced up with the buffer size, too much will enter the queue and it won't be in real time
    // If the value is too high, the sound will be choppy
    private val timeToWaitMs = ((bufferSize / bytes) / sampleRate * 1000).toInt() - 1

    private lateinit var writingThread: StoppableThread


    fun start() {
        output.start()

        var startTime = System.currentTimeMillis()
        writingThread = StoppableThread {
            if (System.currentTimeMillis() - startTime > timeToWaitMs && (piano.notesPressed.isNotEmpty() || activeNotes.isNotEmpty())) {
                startTime = System.currentTimeMillis()
                val samples = createSamplesFromCurrentlyPressedNotes()
                val outputBytes = transformFloatArrayToByteArray(samples)
                output.write(outputBytes)
            }
        }

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


    private val activeNotes: MutableMap<Note, NoteData> = mutableMapOf()


    private val maxIncreasingIterations = 1
    private val maxDecreasingIterations = 1

    private val numOfSamplesInBuffer = bufferSize / bytes

    private val linearModulator = LinearModulator(maxIncreasingIterations, maxDecreasingIterations, numOfSamplesInBuffer)
//    val exponentialModulator = ExponentialModulator(maxIncreasingIterations, maxDecreasingIterations, numOfSamples)


    private fun createSamplesFromCurrentlyPressedNotes(): FloatArray {
        updateActiveNotes()
        val samples = FloatArray(numOfSamplesInBuffer)

        if (activeNotes.isEmpty()) {
            start = 0
            return samples
        }

        for (i in 0 until numOfSamplesInBuffer) {
            val seconds = start++ / sampleRate
            val sample = activeNotes.entries.sumOf { (note, noteData) ->
                val modifier = linearModulator.modulate(i, noteData)
                keyAmplitude * modifier * cos(2.0 * Math.PI * note.getFrequency(piano.octave) * seconds)
            }
            samples[i] = sample.toFloat()
        }

        return samples
    }

    private fun updateActiveNotes() {
        piano.notesPressed.forEach {
            if (it !in activeNotes) {
                activeNotes[it] = NoteData(false)
            } else {
                if (activeNotes[it]!!.isDecreasing) {
                    activeNotes[it]!!.decreasingIterations = 0
                    activeNotes[it]!!.isDecreasing = false
                }
                val iterationValue = activeNotes[it]!!.increasingIterations + 1
                activeNotes[it]!!.increasingIterations = iterationValue.coerceAtMost(maxIncreasingIterations)
            }
        }

        val iterator = activeNotes.iterator()
        while (iterator.hasNext()) {
            val (note, noteData) = iterator.next()
            if (note !in piano.notesPressed) {
                if (noteData.isDecreasing) {
                    noteData.decreasingIterations++
                }
                noteData.isDecreasing = true
                if (noteData.decreasingIterations >= maxDecreasingIterations) {
                    iterator.remove()
                }
            }
        }
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


