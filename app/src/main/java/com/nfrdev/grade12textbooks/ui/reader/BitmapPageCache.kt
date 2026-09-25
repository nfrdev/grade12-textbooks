package com.nfrdev.grade12textbooks.ui.reader

import android.graphics.Bitmap

class BitmapPageCache(private val capacity: Int = 3) {
    private val pages = LinkedHashMap<Int, Bitmap>()
    fun get(page: Int): Bitmap? = pages[page]
    fun put(currentPage: Int, bitmap: Bitmap) {
        pages[currentPage] = bitmap
        while (pages.size > capacity) {
            val evict = pages.keys.maxByOrNull { kotlin.math.abs(it - currentPage) } ?: break
            if (evict != currentPage) pages.remove(evict) else break
        }
    }
    fun residentPages(): Set<Int> = pages.keys.toSet()
}
