package piano

import kotlin.math.pow

interface NoteModulator {
    fun modulate(index: Int, noteData: NoteData): Double
}

class ExponentialModulator(
    val maxIncreasingIterations: Int,
    val maxDecreasingIterations: Int,
    val numOfSamples: Int
) {
    fun modulate(index: Int, noteData: NoteData): Double {
        return if (noteData.isNotPressed) {
            exponentialDecreaseWithTime(index, noteData.iterationsWhileNotPressed)
        } else if (noteData.iterationsWhilePressed < maxIncreasingIterations) {
            exponentialIncreaseWithTime(index, noteData.iterationsWhilePressed)
        } else {
            1.0
        }
    }

    private fun exponentialDecreaseWithTime(i: Int, numberOfIterations: Int): Double {
        val base = 0.5
        val exponent = (i + numberOfIterations * numOfSamples) / (numOfSamples.toDouble() * maxDecreasingIterations)
        return base.pow(exponent)
    }

    private fun exponentialIncreaseWithTime(i: Int, numberOfIterations: Int): Double {
        val base = 0.5
        val exponent = (i + numberOfIterations * numOfSamples) / (numOfSamples.toDouble() * maxIncreasingIterations)
        return 1 - base.pow(exponent)
    }
}

class LinearModulator(
    private val maxIncreasingIterations: Int,
    private val maxDecreasingIterations: Int,
    private val numOfSamples: Int
): NoteModulator {
    override fun modulate(index: Int, noteData: NoteData): Double {
        return if (noteData.isNotPressed) {
            linearDecreaseWithTime(index, noteData.iterationsWhileNotPressed)
        } else if (noteData.iterationsWhilePressed < maxIncreasingIterations) {
            linearIncreaseWithTime(index, noteData.iterationsWhilePressed)
        } else {
            1.0
        }
    }

    private fun linearDecreaseWithTime(i: Int, numberOfIterations: Int): Double {
        return 1 - (i + numberOfIterations * numOfSamples) / (numOfSamples.toDouble() * maxDecreasingIterations)
    }

    private fun linearIncreaseWithTime(i: Int, numberOfIterations: Int): Double {
        return (i + numberOfIterations * numOfSamples) / (numOfSamples.toDouble() * maxIncreasingIterations)
    }
}