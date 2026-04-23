package com.gis.speedlimit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpeedLimitLookupTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final SpeedLimitLookup lookup = new SpeedLimitLookup();

    // -------------------------------------------------------------------------
    // parseSpeedKph
    // -------------------------------------------------------------------------

    @Test
    void parseSpeedKph_numericKmh() {
        assertEquals(50, SpeedLimitLookup.parseSpeedKph("50"));
    }

    @Test
    void parseSpeedKph_numericMph() {
        // 30 mph ≈ 48 km/h
        assertEquals(48, SpeedLimitLookup.parseSpeedKph("30 mph"));
    }

    @Test
    void parseSpeedKph_60mph() {
        // 60 mph ≈ 97 km/h
        assertEquals(97, SpeedLimitLookup.parseSpeedKph("60 mph"));
    }

    @Test
    void parseSpeedKph_textualValue_returnsNull() {
        assertNull(SpeedLimitLookup.parseSpeedKph("national"));
        assertNull(SpeedLimitLookup.parseSpeedKph("urban"));
        assertNull(SpeedLimitLookup.parseSpeedKph("walk"));
    }

    @Test
    void parseSpeedKph_nullInput_returnsNull() {
        assertNull(SpeedLimitLookup.parseSpeedKph(null));
    }

    @Test
    void parseSpeedKph_blankInput_returnsNull() {
        assertNull(SpeedLimitLookup.parseSpeedKph("  "));
    }

    // -------------------------------------------------------------------------
    // buildQuery
    // -------------------------------------------------------------------------

    @Test
    void buildQuery_containsCoordinates() {
        String query = lookup.buildQuery(51.5074, -0.1278);
        assertTrue(query.contains("51.5074000"), "Expected lat in query: " + query);
        assertTrue(query.contains("-0.1278000"), "Expected lng in query: " + query);
        assertTrue(query.contains("[highway]"), "Expected highway filter: " + query);
        assertTrue(query.contains("[out:json]"), "Expected JSON output mode: " + query);
    }

    // -------------------------------------------------------------------------
    // parseResult
    // -------------------------------------------------------------------------

    @Test
    void parseResult_emptyElements_returnsNull() throws Exception {
        JsonNode root = mapper.readTree("{\"elements\":[]}");
        assertNull(lookup.parseResult(root));
    }

    @Test
    void parseResult_missingElements_returnsNull() throws Exception {
        JsonNode root = mapper.readTree("{}");
        assertNull(lookup.parseResult(root));
    }

    @Test
    void parseResult_singleWayWithMaxspeed() throws Exception {
        String json = """
                {
                  "elements": [{
                    "type": "way",
                    "id": 12345,
                    "tags": {
                      "highway": "residential",
                      "name": "High Street",
                      "maxspeed": "30"
                    }
                  }]
                }
                """;
        SpeedLimitResult result = lookup.parseResult(mapper.readTree(json));
        assertNotNull(result);
        assertEquals(12345L, result.getWayId());
        assertEquals("High Street", result.getRoadName());
        assertEquals("residential", result.getHighwayType());
        assertEquals("30", result.getRawSpeedLimit());
        assertEquals(30, result.getSpeedLimitKph());
    }

    @Test
    void parseResult_wayWithMphMaxspeed() throws Exception {
        String json = """
                {
                  "elements": [{
                    "type": "way",
                    "id": 99,
                    "tags": {
                      "highway": "primary",
                      "maxspeed": "60 mph"
                    }
                  }]
                }
                """;
        SpeedLimitResult result = lookup.parseResult(mapper.readTree(json));
        assertNotNull(result);
        assertEquals("60 mph", result.getRawSpeedLimit());
        assertEquals(97, result.getSpeedLimitKph());
    }

    @Test
    void parseResult_wayWithNoMaxspeed() throws Exception {
        String json = """
                {
                  "elements": [{
                    "type": "way",
                    "id": 7,
                    "tags": {
                      "highway": "residential"
                    }
                  }]
                }
                """;
        SpeedLimitResult result = lookup.parseResult(mapper.readTree(json));
        assertNotNull(result);
        assertNull(result.getRawSpeedLimit());
        assertNull(result.getSpeedLimitKph());
    }

    @Test
    void parseResult_prefersWayWithMaxspeedOverOneWithout() throws Exception {
        String json = """
                {
                  "elements": [
                    {
                      "type": "way",
                      "id": 1,
                      "tags": { "highway": "residential" }
                    },
                    {
                      "type": "way",
                      "id": 2,
                      "tags": { "highway": "residential", "maxspeed": "50" }
                    }
                  ]
                }
                """;
        SpeedLimitResult result = lookup.parseResult(mapper.readTree(json));
        assertNotNull(result);
        assertEquals(2L, result.getWayId());
        assertEquals("50", result.getRawSpeedLimit());
    }

    @Test
    void parseResult_prefersHigherPriorityHighwayType() throws Exception {
        String json = """
                {
                  "elements": [
                    {
                      "type": "way",
                      "id": 1,
                      "tags": { "highway": "service", "maxspeed": "20" }
                    },
                    {
                      "type": "way",
                      "id": 2,
                      "tags": { "highway": "primary", "maxspeed": "60" }
                    }
                  ]
                }
                """;
        SpeedLimitResult result = lookup.parseResult(mapper.readTree(json));
        assertNotNull(result);
        assertEquals(2L, result.getWayId());
    }

    @Test
    void parseResult_wayWithNationalSpeedLimit() throws Exception {
        String json = """
                {
                  "elements": [{
                    "type": "way",
                    "id": 55,
                    "tags": {
                      "highway": "trunk",
                      "maxspeed": "national"
                    }
                  }]
                }
                """;
        SpeedLimitResult result = lookup.parseResult(mapper.readTree(json));
        assertNotNull(result);
        assertEquals("national", result.getRawSpeedLimit());
        assertNull(result.getSpeedLimitKph());
    }

    @Test
    void parseResult_noWayElements_returnsNull() throws Exception {
        // node elements should be ignored
        String json = """
                {
                  "elements": [{
                    "type": "node",
                    "id": 1,
                    "lat": 51.5,
                    "lon": -0.1
                  }]
                }
                """;
        assertNull(lookup.parseResult(mapper.readTree(json)));
    }
}
