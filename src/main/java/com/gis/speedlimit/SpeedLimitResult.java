package com.gis.speedlimit;

/**
 * Holds the result of a speed limit lookup for a given GPS coordinate.
 */
public class SpeedLimitResult {

    private final long wayId;
    private final String roadName;
    private final String highwayType;
    private final String rawSpeedLimit;
    private final Integer speedLimitKph;

    public SpeedLimitResult(long wayId, String roadName, String highwayType,
                            String rawSpeedLimit, Integer speedLimitKph) {
        this.wayId = wayId;
        this.roadName = roadName;
        this.highwayType = highwayType;
        this.rawSpeedLimit = rawSpeedLimit;
        this.speedLimitKph = speedLimitKph;
    }

    /** The OSM way ID of the road. */
    public long getWayId() {
        return wayId;
    }

    /** The name of the road, or {@code null} if not set in OSM. */
    public String getRoadName() {
        return roadName;
    }

    /** The OSM {@code highway} tag value (e.g. {@code "residential"}, {@code "motorway"}). */
    public String getHighwayType() {
        return highwayType;
    }

    /**
     * The raw {@code maxspeed} value from OSM (e.g. {@code "50"}, {@code "30 mph"},
     * {@code "national"}).  Returns {@code null} when no {@code maxspeed} tag is present.
     */
    public String getRawSpeedLimit() {
        return rawSpeedLimit;
    }

    /**
     * The speed limit converted to km/h, or {@code null} when the raw value could not be
     * parsed into a number (e.g. {@code "national"}, {@code "urban"}).
     */
    public Integer getSpeedLimitKph() {
        return speedLimitKph;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Road: ").append(roadName != null ? roadName : "(unnamed)");
        sb.append(" [").append(highwayType).append("]");
        sb.append(", OSM way ID: ").append(wayId);
        if (rawSpeedLimit != null) {
            sb.append(", Speed limit: ").append(rawSpeedLimit);
            if (speedLimitKph != null) {
                sb.append(" (").append(speedLimitKph).append(" km/h)");
            }
        } else {
            sb.append(", Speed limit: not specified in OSM");
        }
        return sb.toString();
    }
}
