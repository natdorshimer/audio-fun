import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

class Piano {
    val notesPressed: MutableSet<Note> = mutableSetOf()
    var octave = 3
        private set

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
        Key.U to Note("A#", 0),
        Key.J to Note("B", 0),
        Key.K to Note("C", 1),
        Key.O to Note("C#", 1),
        Key.L to Note("D", 1),
        Key.P to Note("D#", 1),
        Key.Semicolon to Note("E", 1),
        Key.Apostrophe to Note("F", 1),
        Key.RightBracket to Note("F#", 1),
        Key.Enter to Note("G", 1),
        Key.Backslash to Note("G#", 1),
    )
}