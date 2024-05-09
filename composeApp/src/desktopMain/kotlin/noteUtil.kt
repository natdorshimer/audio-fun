import kotlin.math.pow

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


fun getNoteValue(note: String): Int {
    return noteMappings[note] ?: throw IllegalArgumentException("Invalid note")
}

fun getNoteFrequency(note: String, octave: Int): Double {
    val noteValue = getNoteValue(note)
    return 2.0.pow((noteValue / 12.0) + octave) * baseFrequency
}



// Minor refers to semi tones
// Major refers to tones
// Minor = 1 semi tone less than major

// Major third : 0 1 2 3 4 , 0 + 4 - 1

// Squish em down until the most compressed a scale, the lowest is the root note which is the chord



// E G# B
// Chord = root, third, fifth,  only for major and minor chords
// Seventh, seven notes away from the root (tonic) tonic + third + third
// Unisons, fourths, fifths, and octaves are perfect intervals
// When you hear them, harmonic overtones are the same
// -> Figure out the math behind this later


// 4 semi tones major third
// 3 semi tones minor third
// major third + minor third = major chord
