import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import view.PianoApp


fun main() = application {
    val pianoAudioStreamer = PianoAudioStreamer()
    val pianoApp =  PianoApp(pianoAudioStreamer)

    pianoAudioStreamer.start()
    val windowState = rememberWindowState(size = DpSize(Dp(400.0F), Dp(200.0F)))

    Window(
        onCloseRequest = { pianoAudioStreamer.close(); exitApplication();  },
        title = "AudioFun",
        state = windowState
    ) {
        pianoApp.app()
    }
}