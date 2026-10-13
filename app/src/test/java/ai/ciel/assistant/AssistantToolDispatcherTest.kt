package ai.ciel.assistant

import org.junit.Assert.*
import org.junit.Test

class AssistantToolDispatcherTest {

    @Test
    fun testToolParsing() {
        val query = "search quantum computing"
        val isSearch = query.startsWith("search")
        assertTrue(isSearch)
        val extracted = query.replace("search", "").trim()
        assertEquals("quantum computing", extracted)
    }
}
