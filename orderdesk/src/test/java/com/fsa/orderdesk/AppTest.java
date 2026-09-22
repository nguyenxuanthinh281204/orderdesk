package com.fsa.orderdesk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void reportsItsVersion() {
        assertEquals("1.0", App.version());
    }
}