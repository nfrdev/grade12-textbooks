package com.nfrdev.grade12textbooks.ui.reader

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BitmapPageCacheTest {
    private fun bitmap() = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
    @Test fun maxThreePagesAndFurthestEviction() {
        val cache = BitmapPageCache()
        cache.put(0, bitmap()); cache.put(1, bitmap()); cache.put(2, bitmap()); cache.put(3, bitmap())
        assertEquals(3, cache.residentPages().size)
        assertTrue(0 !in cache.residentPages())
    }
    @Test fun backwardNavigationEvictsFurthestPage() {
        val cache = BitmapPageCache()
        cache.put(5, bitmap()); cache.put(4, bitmap()); cache.put(3, bitmap()); cache.put(4, bitmap())
        assertTrue(5 !in cache.residentPages())
    }
}
