import java.io.IOException
import java.net.SocketTimeoutException

inline fun <T> executeExceptionSafeContext(
    run: () -> T,
    failure: (e: ErrorResponse) -> T,
    unknown: (e: ErrorResponse) -> T,   // with timeout
) = try {
    run()
} catch (e: Exception) {
    if (e.isTimeoutException() || e.isCriticalException()) {
        unknown(ErrorResponse(e))
    } else {
        failure(ErrorResponse(e))
    }
}

fun Exception.isTimeoutException() =
    this is SocketTimeoutException || this.cause is SocketTimeoutException ||
            this is IOException || this.cause is IOException

fun Exception.isCriticalException() = true
