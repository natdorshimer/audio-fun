class StoppableThread(private val block: () -> Unit) {
    private var running = true
    private lateinit var thread: Thread

    fun start() {
        thread = Thread {
            println("Stoppable thread started")
            while (running) {
                block()
            }
            println("Stoppable thread stopped")
        }.also{ it.start() }
    }

    fun stop() {
        running = false
        thread.join()
    }
}