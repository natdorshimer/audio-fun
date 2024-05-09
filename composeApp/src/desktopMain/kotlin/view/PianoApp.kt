package view

import Piano
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import org.jetbrains.compose.ui.tooling.preview.Preview

class PianoApp(private val piano: Piano) {
    private val text = mutableStateOf(getNoteText())
    private val octaveValue = mutableStateOf(getOctaveText())

    @Composable
    @Preview
    fun app() {
        val textValue by remember { text }
        val octaveValue by remember { octaveValue }
        val requester = remember { FocusRequester() }

        MaterialTheme {
            Column(
                Modifier.fillMaxWidth().onKeyEvent {
                    piano.handleKeyEvent(it)
                    updateText()
                    false
                }.focusRequester(requester).focusable(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(textValue)
                Text(octaveValue)
            }
            LaunchedEffect(Unit) {
                requester.requestFocus()
            }
        }
    }

    private fun updateText() {
        text.value = getNoteText()
        octaveValue.value = getOctaveText()
    }

    private fun getOctaveText() = "Octave: ${piano.octave}"

    private fun getNoteText() = "Notes: [${piano.getNotesPressed().joinToString("  ")}]"
}