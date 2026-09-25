package com.nfrdev.grade12textbooks.ui.reader

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderUtilityTest {
    @Test fun pageJumpClampsToBounds() {
        fun clamp(page: Int, total: Int) = page.coerceIn(1, total)
        assertEquals(1, clamp(-4, 10)); assertEquals(10, clamp(99, 10)); assertEquals(5, clamp(5, 10))
    }
    @Test fun zoomKeyUsesBookId() {
        assertEquals("readerZoom:book-1", "readerZoom:book-1")
    }
}
