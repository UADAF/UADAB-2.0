import java.util.*

val timer = Timer()

fun Timer.scheduleAtFixedRate(delay: Long = 0, period: Long = 0, action: () -> Unit) {
    scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
            action()
        }
    }, delay, period)
}