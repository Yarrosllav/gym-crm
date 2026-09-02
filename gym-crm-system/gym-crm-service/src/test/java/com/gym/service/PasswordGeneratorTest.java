package com.gym.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PasswordGeneratorTest {

    @Test
    void generate_shouldReturnStringOfLengthTen() {
        assertEquals(10, PasswordGenerator.generate().length());
    }

    @Test
    void generate_shouldReturnDifferentValuesOnEachCall() {
        assertNotEquals(PasswordGenerator.generate(), PasswordGenerator.generate());
    }
}
