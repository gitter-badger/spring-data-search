package com.weedow.spring.data.search.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

// Not much to test, but exercise to prevent code coverage tool from showing red for default methods
internal class KeywordTest {

    @Test
    fun testToString() {
        assertThat(Keyword.toString()).isEqualTo("Keyword")
    }
}