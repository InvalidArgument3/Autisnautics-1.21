package dev.wyseman.portallinker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpeningSizeTest {
    @Test
    void acceptsOneByTwoThroughThirtyTwo() {
        assertTrue(OpeningSize.acceptable(1, 2, 32));
        assertTrue(OpeningSize.acceptable(2, 1, 32));
        assertTrue(OpeningSize.acceptable(2, 2, 32));
        assertTrue(OpeningSize.acceptable(2, 3, 32));
        assertTrue(OpeningSize.acceptable(32, 32, 32));
        assertTrue(OpeningSize.acceptable(1, 32, 32));
    }

    @Test
    void rejectsOneByOneAndThirtyThree() {
        assertFalse(OpeningSize.acceptable(1, 1, 32));
        assertFalse(OpeningSize.acceptable(33, 2, 32));
        assertFalse(OpeningSize.acceptable(32, 33, 32));
        assertFalse(OpeningSize.acceptable(33, 33, 32));
        assertFalse(OpeningSize.acceptable(0, 2, 32));
    }

    @Test
    void rotatedRectanglesMatchAndDifferentOnesDoNot() {
        assertTrue(OpeningSize.sameOpening(3, 5, 5, 3));
        assertTrue(OpeningSize.sameOpening(1, 2, 2, 1));
        assertFalse(OpeningSize.sameOpening(3, 5, 3, 6));
        assertFalse(OpeningSize.sameOpening(2, 2, 2, 3));
    }
}
