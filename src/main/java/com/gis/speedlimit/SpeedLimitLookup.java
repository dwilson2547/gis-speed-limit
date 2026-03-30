package com.gis.speedlimit;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Looks up the speed limit of the road nearest to a given GPS coordinate using
 * OpenStreetMap data via the public Overpass API (no API key required).
 *
 * <p>Usage:
 * <pre>{@code
 * SpeedLimitLookup lookup = new SpeedLimitLookup();
 * SpeedLimitResult result = lookup.lookup(51.5074, -0.1278); // London
 * System.out.println(result);
 * }</pre>
 */
public class SpeedLimitLookup {

    /**
     * Road types ordered from most-specific / highest-priority to least, so that when
     * multiple ways overlap the point we prefer motorways/trunk roads over footpaths.
     */
    private static final List<String> HIGHWAY_PRIORITY = List.of(
            "motorway", "trunk", "primary", "secondary", "tertiary",
            "unclassified", "residential", "living_street", "service",
            "motorway_link", "trunk_link", "primary_link", "secondary_link",
            "tertiary_link", "road"
    );

    /** Pattern that matches a leading integer in the maxspeed value, e.g. "50" or "30 mph". */
    private static final Pattern SPEED_NUMBER = Pattern.compile("^(\\d+)\\s*(mph)?");

    /** Metres around the coordinate to search for road segments. */
    private static final int SEARCH_RADIUS_M = 50;

    private final OverpassClient overpassClient;

    /** Creates a lookup using the default public Overpass API endpoint. */
    public SpeedLimitLookup() {
        this(new OverpassClient());
    }

    /**
     * Creates a lookup with a custom {@link OverpassClient} (useful for testing).
     *
     * @param overpassClient the client to use for Overpass queries
     */
    public SpeedLimitLookup(OverpassClient overpassClient) {
        this.overpassClient = overpassClient;
    }

    /**
     * Looks up the speed limit for the road nearest to the given GPS coordinate.
     *
     * @param lat latitude in decimal degrees (WGS 84)
     * @param lng longitude in decimal degrees (WGS 84)
     * @return the best matching {@link SpeedLimitResult}, or {@code null} if no road
     *         was found within {@value #SEARCH_RADIUS_M} metres
     * @throws IOException          if an I/O or HTTP error occurs
     * @throws InterruptedException if the HTTP request is interrupted
     */
    public SpeedLimitResult lookup(double lat, double lng)
            throws IOException, InterruptedException {

        String query = buildQuery(lat, lng);
        JsonNode root = overpassClient.query(query);
        return parseResult(root);
    }

    // -------------------------------------------------------------------------
    // Package-private helpers (also used by tests)
    // -------------------------------------------------------------------------

    String buildQuery(double lat, double lng) {
        return String.format(
                "[out:json][timeout:25];\n"
                + "way(around:%d,%.7f,%.7f)[highway];\n"
                + "out tags;",
                SEARCH_RADIUS_M, lat, lng);
    }

    SpeedLimitResult parseResult(JsonNode root) {
        JsonNode elements = root.path("elements");
        if (elements.isMissingNode() || !elements.isArray() || elements.isEmpty()) {
            return null;
        }

        // Collect all ways found
        List<JsonNode> ways = new ArrayList<>();
        for (JsonNode element : elements) {
            if ("way".equals(element.path("type").asText())) {
                ways.add(element);
            }
        }

        if (ways.isEmpty()) {
            return null;
        }

        // Pick the best way: prefer ways with a maxspeed tag, then rank by highway type
        JsonNode best = chooseBestWay(ways);
        return buildSpeedLimitResult(best);
    }

    private JsonNode chooseBestWay(List<JsonNode> ways) {
        // Prefer ways that already have a maxspeed tag
        List<JsonNode> withSpeed = ways.stream()
                .filter(w -> !w.path("tags").path("maxspeed").isMissingNode())
                .toList();

        List<JsonNode> candidates = withSpeed.isEmpty() ? ways : withSpeed;

        // Among candidates, pick the one with the highest-priority highway type
        JsonNode best = candidates.get(0);
        int bestPriority = highwayPriority(tagValue(best, "highway"));

        for (JsonNode way : candidates) {
            int priority = highwayPriority(tagValue(way, "highway"));
            if (priority < bestPriority) {
                best = way;
                bestPriority = priority;
            }
        }

        return best;
    }

    private static SpeedLimitResult buildSpeedLimitResult(JsonNode way) {
        long wayId = way.path("id").asLong();
        JsonNode tags = way.path("tags");

        String roadName = tags.has("name") ? tags.get("name").asText() : null;
        String highwayType = tags.has("highway") ? tags.get("highway").asText() : null;
        String rawSpeedLimit = tags.has("maxspeed") ? tags.get("maxspeed").asText() : null;

        Integer speedLimitKph = parseSpeedKph(rawSpeedLimit);
        return new SpeedLimitResult(wayId, roadName, highwayType, rawSpeedLimit, speedLimitKph);
    }

    /**
     * Parses a raw OSM {@code maxspeed} value into km/h.
     *
     * <ul>
     *   <li>{@code "50"} → 50 (km/h assumed)</li>
     *   <li>{@code "30 mph"} → 48 (converted to km/h)</li>
     *   <li>{@code "national"} / {@code "urban"} → {@code null}</li>
     * </ul>
     */
    static Integer parseSpeedKph(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        Matcher m = SPEED_NUMBER.matcher(raw.trim());
        if (!m.find()) {
            return null;
        }
        int value = Integer.parseInt(m.group(1));
        boolean isMph = "mph".equalsIgnoreCase(m.group(2));
        return isMph ? (int) Math.round(value * 1.60934) : value;
    }

    private static int highwayPriority(String highway) {
        if (highway == null) {
            return Integer.MAX_VALUE;
        }
        int idx = HIGHWAY_PRIORITY.indexOf(highway);
        return idx < 0 ? HIGHWAY_PRIORITY.size() : idx;
    }

    private static String tagValue(JsonNode way, String tag) {
        return way.path("tags").path(tag).asText(null);
    }
}
