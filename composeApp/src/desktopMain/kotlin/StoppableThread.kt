class StoppableThread(private val block: () -> Unit) {
    private var running = true

    private val thread =  Thread {
        println("Stoppable thread started")
        while (running) {
            block()
        }
        println("Stoppable thread stopped")
    }

    fun start() {
        thread.start()
    }

    fun stop() {
        running = false
        thread.join()
    }
}