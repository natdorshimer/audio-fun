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
        return if (noteData.isDecreasing) {
            exponentialDecreaseWithTime(index, noteData.decreasingIterations)
        } else if (noteData.increasingIterations < maxIncreasingIterations) {
            exponentialIncreaseWithTime(index, noteData.increasingIterations)
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
        return if (noteData.isDecreasing) {
            linearDecreaseWithTime(index, noteData.decreasingIterations)
        } else if (noteData.increasingIterations < maxIncreasingIterations) {
            linearIncreaseWithTime(index, noteData.increasingIterations)
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