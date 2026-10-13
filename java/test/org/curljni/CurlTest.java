package org.curljni;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CurlTest {

    @BeforeEach
    void setUp() {
        System.out.println("before each handler");
    }

    @Test
    void testAdd_PositiveNumbers() {
        int result = 5;
        assertEquals(5, result);
    }

}
