package net.igng.bbs.core.network

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SingleFlightTokenRefresherTest {

    @Test
    fun concurrent401TriggersOnlyOneRefresh() = runTest {
        val refreshCount = AtomicInteger(0)
        var current = "old_tok"

        val refresher = SingleFlightTokenRefresher(
            currentAccessToken = { current },
            performRefresh = {
                refreshCount.incrementAndGet()
                delay(40)
                current = "new_tok"
                current
            },
        )

        val results = (1..6).map {
            async { refresher.refreshIfNeeded("old_tok") }
        }.awaitAll()

        assertEquals(1, refreshCount.get())
        results.forEach { assertEquals("new_tok", it) }
    }
}
