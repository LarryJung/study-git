import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author Larry
 */
class SimpleTestTest {

    @Test
    fun `static now test`() {
        val now = LocalDateTime.of(2021, 8, 1, 0, 0)
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns now

        assertEquals(now, LocalDateTime.now())

        clearStaticMockk(LocalDateTime::class)
    }

    @Test
    fun `staticContext test`() {
        val now = LocalDateTime.of(2021, 8, 1, 0, 0)

        staticContext(now = now) {
            assertEquals(now, LocalDateTime.now())
        }
    }

    @Test
    fun `timeLoggerContext test`() {
        timeLoggerContext {
            Thread.sleep(1000)
            assertEquals(true, true)
        }
    }

    @Test
    fun `context combination test`() {
        timeLoggerContext {
            val now = LocalDateTime.of(2021, 8, 1, 0, 0)
            staticContext(now) {
                Thread.sleep(1000)
                assertEquals(now, LocalDateTime.now())
            }
        }
    }

    @Test
    fun `context combination test2`() {
        val now = LocalDateTime.of(2021, 8, 1, 0, 0)
        timeLoggerDecorator
            .with(staticDecorator(now))
            .invoke {
                staticContext(now) {
                    Thread.sleep(1000)
                    assertEquals(now, LocalDateTime.now())
                }
            }.invoke()
    }

    @Test
    fun `context combination test3`() {
        val now = LocalDateTime.of(2021, 8, 1, 0, 0)
        timeLoggerDecorator
            .with(staticDecorator(now))
            .test {
                staticContext(now) {
                    Thread.sleep(1000)
                    assertEquals(now, LocalDateTime.now())
                }
            }
    }

    @Test
    fun `context combination test4`() {
        val now = LocalDateTime.of(2021, 8, 1, 0, 0)
        decorateWith { testBlock ->
            timeLoggerContext(testBlock)
        }.decorate { testBlock ->
            staticContext(now = now, block = testBlock)
        }.test {
            Thread.sleep(1000)
            assertEquals(now, LocalDateTime.now())
        }
    }

}

inline fun <T> staticContext(
    now: LocalDateTime = LocalDateTime.now(),
    block: () -> T
): T {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns now
    return try {
        block()
    } finally {
        clearStaticMockk(LocalDateTime::class)
    }
}

inline fun <T> timeLoggerContext(
    block: () -> T
): T {
    val start = System.currentTimeMillis()
    return try {
        block()
    } finally {
        val end = System.currentTimeMillis()
        println("Took : ${(end - start)} milli seconds")
    }
}

typealias Effect = () -> Unit
typealias EffectDecorator = (Effect) -> Effect

val staticDecorator: (LocalDateTime) -> EffectDecorator =
    { now ->
        { testBlock: Effect ->
            { staticContext(now = now, block = testBlock) }
        }
    }

val timeLoggerDecorator: EffectDecorator =
    { testBlock: Effect ->
        { timeLoggerContext(testBlock) }
    }

fun EffectDecorator.with(next: EffectDecorator): EffectDecorator =
    { effect: Effect ->
        next(this(effect))
    }

fun EffectDecorator.test(effect: Effect): Unit =
    this(effect).invoke()

fun decorateWith(f: (Effect) -> Unit): EffectDecorator =
    { effect ->
        {
            f(effect)
        }
    }

fun EffectDecorator.decorate(f: (Effect) -> Unit): EffectDecorator =
    this.with(decorateWith(f))