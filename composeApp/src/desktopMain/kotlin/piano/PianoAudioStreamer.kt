package piano

import audio.AudioSettings
import audio.AudioStreamingOutput
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.cos


class PianoAudioStreamer(
    private val piano: Piano,
    private val audioSettings: AudioSettings,
    private val audioStreamingOutput: AudioStreamingOutput,
) {
    private val defaultKeyAmplitude = 0.2

    private val maxIncreasingIterations = 1
    private val maxDecreasingIterations = 1

    private val linearModulator = LinearModulator(
        maxIncreasingIterations,
        maxDecreasingIterations,
        audioSettings.numOfSamplesInBuffer,
    )

    private val activeNotes: MutableMap<Note, NoteData> = mutableMapOf()

    // If this isn't synced up with the buffer size and the value is too small,
    // too much will enter the queue and it won't be in real time.
    // If the value is too high, the sound will be choppy
    private val timeToWaitMs = ((audioSettings.bufferSize / audioSettings.bytesPerSample) / audioSettings.sampleRate * 1000).toInt() - 1

    // This helps the sound remain continuous
    private var start = 0

    private lateinit var writingJob: Job

    suspend fun start() = coroutineScope {
        audioStreamingOutput.start()

        var startTime = System.currentTimeMillis()

        writingJob = launch(Dispatchers.Default, start = CoroutineStart.DEFAULT) {
            while (isActive) {
                val shouldUpdate = System.currentTimeMillis() - startTime > timeToWaitMs
                val notesToBePlayed = piano.notesPressed.isNotEmpty() || activeNotes.isNotEmpty()

                if (shouldUpdate && notesToBePlayed) {
                    startTime = System.currentTimeMillis()
                    updateActiveNotes()

                    val samples = createSamplesFromActiveNotes()
                    audioStreamingOutput.write(samples)
                }
            }
        }
    }

    private fun createSamplesFromActiveNotes(): FloatArray {
        val samples = FloatArray(audioSettings.numOfSamplesInBuffer)

        if (activeNotes.isEmpty()) {
            start = 0
            return samples
        }

        for (i in 0 until audioSettings.numOfSamplesInBuffer) {
            val seconds = start++ / audioSettings.sampleRate

            // To avoid clipping above 1.0 for sample value and many notes
            val effectiveAmplitude = getProperAmplitudePerFrequency(activeNotes, defaultKeyAmplitude)

            val sample = activeNotes.entries.sumOf { (note, noteData) ->
                val modifier = linearModulator.modulate(i, noteData)
                effectiveAmplitude * modifier * cos(2.0 * Math.PI * note.getFrequency(piano.octave) * seconds)
            }
            samples[i] = sample.toFloat()
        }

        return samples
    }

    private fun getProperAmplitudePerFrequency(activeNotes: Map<Note, NoteData>, defaultKeyAmplitude: Double): Double {
        return if (activeNotes.size > 1 / defaultKeyAmplitude) {
            1.0 / activeNotes.size
        } else {
            defaultKeyAmplitude
        }
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
                if (noteData.isNotPressed) {
                    noteData.iterationsWhileNotPressed++
                }
                noteData.isNotPressed = true
                if (noteData.iterationsWhileNotPressed >= maxDecreasingIterations) {
                    iterator.remove()
                }
            }
        }
    }

    private fun updatePressedNotes() {
        for (note in piano.notesPressed) {
            val activeNote = activeNotes[note]
            if (activeNote == null || activeNote.isNotPressed) {
                activeNotes[note] = NoteData(false)
                continue
            }
            activeNote.increaseIterationsWhilePressedOrClamp(maxIncreasingIterations)
        }
    }

    fun close() = runBlocking {
        println("Closing piano")

        audioStreamingOutput.close()

        writingJob.cancelAndJoin()

        println("Writing thread closed")
    }
}