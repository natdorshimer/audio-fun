import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import audio.AudioSettings
import audio.AudioStreamingOutput
import audio.createAudioFormat
import piano.Piano
import piano.PianoAudioStreamer
import view.PianoApp


fun main() = application {
    val audioSettings = AudioSettings(
        sampleRate = 44100.0,
        bitsPerSample = 16,
        bufferSize = 1024,
    )
    val audioStreamingOutput = AudioStreamingOutput(createAudioFormat(audioSettings))

    val piano = Piano()
    val pianoAudioStreamer = PianoAudioStreamer(piano, audioSettings, audioStreamingOutput)

    pianoAudioStreamer.start()

    val pianoApp =  PianoApp(piano)
    val windowState = rememberWindowState(size = DpSize(Dp(400.0F), Dp(200.0F)))

    Window(
        onCloseRequest = { pianoAudioStreamer.close(); exitApplication();  },
        title = "AudioFun",
        state = windowState
    ) {
        pianoApp.app()
    }
}