package com.treesofparis.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceholderTest {
    @Test
    fun name_isDomain() = assertEquals("domain", Placeholder.NAME)
}
