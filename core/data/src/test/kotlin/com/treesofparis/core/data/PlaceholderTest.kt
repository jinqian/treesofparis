package com.treesofparis.core.data

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlaceholderTest {
    @Test
    fun robolectric_providesContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(context.resources)
        assertEquals("data", PlaceholderModule.provideName())
    }
}
