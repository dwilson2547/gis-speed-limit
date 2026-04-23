package com.gis.speedlimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpeedLimitResultTest {

    @Test
    void toString_withSpeedLimit() {
        SpeedLimitResult r = new SpeedLimitResult(42L, "Main Road", "primary", "50", 50);
        String s = r.toString();
        assertTrue(s.contains("Main Road"));
        assertTrue(s.contains("primary"));
        assertTrue(s.contains("50"));
        assertTrue(s.contains("42"));
    }

    @Test
    void toString_withMphSpeedLimit() {
        SpeedLimitResult r = new SpeedLimitResult(1L, null, "residential", "30 mph", 48);
        String s = r.toString();
        assertTrue(s.contains("(unnamed)"));
        assertTrue(s.contains("30 mph"));
        assertTrue(s.contains("48 km/h"));
    }

    @Test
    void toString_withNoSpeedLimit() {
        SpeedLimitResult r = new SpeedLimitResult(7L, "Oak Lane", "residential", null, null);
        String s = r.toString();
        assertTrue(s.contains("not specified"));
    }

    @Test
    void toString_withNationalLimit() {
        SpeedLimitResult r = new SpeedLimitResult(8L, "A1", "trunk", "national", null);
        String s = r.toString();
        assertTrue(s.contains("national"));
        assertFalse(s.contains("km/h"));
    }
}
