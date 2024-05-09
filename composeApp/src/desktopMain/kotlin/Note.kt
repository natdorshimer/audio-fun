import kotlin.math.pow

class Note(
    private val note: String,
    private val relativeOctave: Int
) {
    fun getFrequency(octave: Int): Double {
        return getNoteFrequency(note, relativeOctave + octave)
    }

    private fun getNoteValue(note: String): Int {
        return noteMappings[note] ?: throw IllegalArgumentException("Invalid note")
    }

    private fun getNoteFrequency(note: String, octave: Int): Double {
        val noteValue = getNoteValue(note)
        return 2.0.pow((noteValue / 12.0) + octave) * baseFrequency
    }


    override fun toString(): String {
        return "$note.$relativeOctave"
    }
}


const val baseFrequency = 16.35 // C0
val noteMappings = mapOf(
    "C" to 0,
    "C#" to 1,
    "Db" to 1,
    "D" to 2,
    "D#" to 3,
    "Eb" to 3,
    "E" to 4,
    "Fb" to 4,
    "F" to 5,
    "F#" to 6,
    "Gb" to 6,
    "G" to 7,
    "G#" to 8,
    "Ab" to 8,
    "A" to 9,
    "A#" to 10,
    "Bb" to 10,
    "B" to 11
)
