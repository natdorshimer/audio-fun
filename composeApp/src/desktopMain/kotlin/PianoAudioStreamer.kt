import javax.sound.sampled.AudioFormat
import kotlin.math.cos

class PianoAudioStreamer(
    val piano: Piano = Piano()
) {
    private val sampleRate = 44100.0
    private val bitsPerSample = 16
    private val bigEndian = false
    private val signed = true
    private val channels = 1
    private val format = AudioFormat(sampleRate.toFloat(), bitsPerSample, channels, signed, bigEndian)

    private val bufferSize = 1024

    private val output = AudioStreamingOutput(format)

    private val bytesPerSample = (bitsPerSample / 8)

    // If this isn't synced up with the buffer size, too much will enter the queue and it won't be in real time
    // If the value is too high, the sound will be choppy
    private val timeToWaitMs = ((bufferSize / bytesPerSample) / sampleRate * 1000).toInt() - 1

    private lateinit var writingThread: StoppableThread


    fun start() {
        output.start()

        var startTime = System.currentTimeMillis()
        writingThread = StoppableThread {
            val shouldUpdate = System.currentTimeMillis() - startTime > timeToWaitMs
            val notesToBePlayed = piano.notesPressed.isNotEmpty() || activeNotes.isNotEmpty()

            if (shouldUpdate && notesToBePlayed) {
                startTime = System.currentTimeMillis()
                updateActiveNotes()

                val samples = createSamplesFromActiveNotes()
                output.write(samples)
            }
        }

        writingThread.start()
    }

    // This helps the sound remain continuous
    private var start = 0

    private val keyAmplitude = 0.2


    private val activeNotes: MutableMap<Note, NoteData> = mutableMapOf()


    private val maxIncreasingIterations = 1
    private val maxDecreasingIterations = 1

    private val numOfSamplesInBuffer = bufferSize / bytesPerSample

    private val linearModulator = LinearModulator(maxIncreasingIterations, maxDecreasingIterations, numOfSamplesInBuffer)
//    val exponentialModulator = ExponentialModulator(maxIncreasingIterations, maxDecreasingIterations, numOfSamples)


    private fun createSamplesFromActiveNotes(): FloatArray {
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
        updatePressedNotes()
        updateUnpressedNotes()
    }

    private fun updateUnpressedNotes() {
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

    private fun updatePressedNotes() {
        for (note in piano.notesPressed) {
            val activeNote = activeNotes[note]
            if (activeNote == null || activeNote.isDecreasing) {
                activeNotes[note] = NoteData(false)
                continue
            }
            val iterationValue = activeNote.increasingIterations + 1
            activeNote.increasingIterations = iterationValue.coerceAtMost(maxIncreasingIterations)
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