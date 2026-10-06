package com.youtubeagent.agent.tools;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorToolTest {

    private final CalculatorTool tool = new CalculatorTool();

    @Test
    void shouldAddNumbers() {
        assertEquals(
                9.0,
                tool.execute(Map.of("expression", "4 + 5")));
    }

    @Test
    void shouldSubtractNumbers() {
        assertEquals(
                3.0,
                tool.execute(Map.of("expression", "5 - 2")));
    }

    @Test
    void shouldMultiplyNumbers() {
        assertEquals(
                20.0,
                tool.execute(Map.of("expression", "4 * 5")));
    }

    @Test
    void shouldDivideNumbers() {
        assertEquals(
                5.0,
                tool.execute(Map.of("expression", "20 / 4")));
    }

    @Test
    void shouldRejectMissingExpression() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tool.execute(Map.of()));

        assertEquals(
                "Missing required argument: expression",
                exception.getMessage());
    }

    @Test
    void shouldRejectDivisionByZero() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tool.execute(
                        Map.of("expression", "10 / 0")));

        assertEquals(
                "Cannot divide by zero",
                exception.getMessage());
    }

    @Test
    void shouldRejectUnsupportedExpression() {

        assertThrows(
                IllegalArgumentException.class,
                () -> tool.execute(
                        Map.of("expression", "10 % 3")));
    }
}