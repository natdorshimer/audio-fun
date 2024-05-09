import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import java.util.Collections
import javax.sound.sampled.AudioFormat
import kotlin.math.cos
import kotlin.math.pow



class Piano {
    private val sampleRate = 44100.0
    private val bits = 16
    private val bigEndian = false
    private val signed = true
    private val channels = 1
    private val format = AudioFormat(sampleRate.toFloat(), bits, channels, signed, bigEndian)

    var octave: Int = 3
        private set

    private val notesPressed = Collections.synchronizedSet(mutableSetOf<Note>())
    private val bufferSize = 4096

    private val output = AudioStreamingOutput(format, bufferSize)

    private val bytes = (bits / 8)


    fun close() {
        println("Closing piano")

        output.close()

        writingThread.stop()

        println("Writing thread closed")
    }

    fun handleKeyEvent(keyEvent: KeyEvent) {
        if (keyEvent.type == KeyEventType.KeyDown) {
            val key = keyEvent.key

            keyToNoteMapping[key]?.let(notesPressed::add)

            if (key == Key.ShiftLeft) {
                octave--
                return
            }

            if (key == Key.ShiftRight) {
                octave++
                return
            }
        }

        if (keyEvent.type == KeyEventType.KeyUp) {
            val key = keyEvent.key
            keyToNoteMapping[key]?.let(notesPressed::remove)
        }
    }

    // If this isn't synced up with the buffer size, too much will enter the queue and it won't be in real time
    // If the value is too high, the sound will be choppy
    private val timeToWaitMs = ((bufferSize / bytes) / sampleRate * 1000).toInt() - 2

    private lateinit var writingThread: StoppableThread

    fun start() {
        output.run()

        var startTime = System.currentTimeMillis()
        writingThread = StoppableThread {
            if (System.currentTimeMillis() - startTime > timeToWaitMs && notesPressed.isNotEmpty()) {
                startTime = System.currentTimeMillis()
                val samples = createSamplesFromCurrentlyPressedNotes()
                val outputBytes = transformFloatArrayToByteArray(samples)
                output.write(outputBytes)
            }
        }.also{ it.start() }
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
        val frequencies = notesPressed.forEach {
            if (it !in previousNotes) {
                notesToGraduallyIncrease.add(it)
            }
        }

        val gradualIncreaseWithTime = { i: Int ->
            val time = i / sampleRate * 5000
            (1 - Math.E.pow(-time)).also{ println(it) }
        }

        previousNotes.clear()
        previousNotes.addAll(notesPressed)

        val samples = FloatArray(numOfSamples)

        for (i in 0 until numOfSamples) {
            val seconds = start++ / sampleRate
            val sample = notesPressed.sumOf {
                val modifier = if (it in notesToGraduallyIncrease) {
                    gradualIncreaseWithTime(i)
                } else {
                    1.0
                }
                keyAmplitude * modifier * cos(2.0 * Math.PI * it.getFrequency(octave) * seconds)
            }
//            notesPressed.sumOf {
//                if (it in notesToGraduallyIncrease) {
//                    gradualIncreaseWithTime(i, it.getFrequency(octave))
//                } else {
//                    keyAmplitude * cos(2.0 * Math.PI * it.getFrequency(octave) * seconds)
//                }
//            }.let { samples[i] = it.toFloat() }

//            val sample = frequencies.sumOf { keyAmplitude * cos(2.0 * Math.PI * it * seconds) }
            samples[i] = sample.toFloat()
        }

        return samples
    }

    fun getNotesPressed(): List<String> {
        return notesPressed.map { it.toString() }
    }

    private val keyToNoteMapping = mapOf(
        Key.A to Note("C", 0),
        Key.W to Note("C#", 0),
        Key.S to Note("D", 0),
        Key.E to Note("D#", 0),
        Key.D to Note("E", 0),
        Key.F to Note("F", 0),
        Key.T to Note("F#", 0),
        Key.G to Note("G", 0),
        Key.Y to Note("G#", 0),
        Key.H to Note("A", 0),
        Key.J to Note("A#", 0),
        Key.K to Note("B", 0),
        Key.O to Note("C", 1),
        Key.L to Note("C#", 1),
        Key.P to Note("D", 1),
        Key.Semicolon to Note("D#", 1),
        Key.Apostrophe to Note("E", 1),
        Key.Enter to Note("F", 1),
        Key.Backslash to Note("F#", 1),
    )
}


private class Note(
    val note: String,
    val relativeOctave: Int
) {
    fun getFrequency(octave: Int): Double {
        return getNoteFrequency(note, relativeOctave + octave)
    }

    override fun toString(): String {
        return "$note.$relativeOctave"
    }
}

