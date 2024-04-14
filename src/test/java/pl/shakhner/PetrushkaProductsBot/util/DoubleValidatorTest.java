package pl.shakhner.PetrushkaProductsBot.util;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class DoubleValidatorTest {
    @Test
    public void testIsDoubleValidInputReturnsTrue() {
        assertTrue(DoubleValidator.isDouble("123.45"));
    }

    @Test
    public void testIsDoubleInvalidInputReturnsFalse() {
        assertFalse(DoubleValidator.isDouble("abc"));
    }

    @Test
    public void testParseDoubleValidInputReturnsDouble() {
        assertEquals(123.45d, DoubleValidator.parseDouble("123.45"), 0);
    }

    @Test
    public void testParseDoubleInvalidInputThrowsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> DoubleValidator.parseDouble("abc"));
    }

    @Test
    public void testIsRoundRoundDoubleWithPointReturnsTrue() {
        assertTrue(DoubleValidator.isRound("10.0"));
    }

    @Test
    public void testIsRoundRoundDoubleWithCommaReturnsTrue1() {
        assertTrue(DoubleValidator.isRound("10,0"));
    }
    @Test
    public void testIsRoundNotRoundDoubleReturnsFalse() {
        assertFalse(DoubleValidator.isRound("10.5"));
    }
}
