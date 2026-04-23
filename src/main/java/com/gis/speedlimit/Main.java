package com.gis.speedlimit;

import java.io.IOException;

/**
 * Command-line entry point for the GIS speed-limit tool.
 *
 * <p>Usage:
 * <pre>
 *   java -jar speed-limit-1.0-SNAPSHOT-jar-with-dependencies.jar &lt;lat&gt; &lt;lng&gt;
 * </pre>
 *
 * <p>Example:
 * <pre>
 *   java -jar speed-limit-1.0-SNAPSHOT-jar-with-dependencies.jar 51.5074 -0.1278
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: speed-limit <lat> <lng>");
            System.err.println("Example: speed-limit 51.5074 -0.1278");
            System.exit(1);
        }

        double lat;
        double lng;
        try {
            lat = Double.parseDouble(args[0]);
            lng = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: lat and lng must be valid decimal numbers.");
            System.exit(1);
            return;
        }

        if (lat < -90 || lat > 90) {
            System.err.println("Error: latitude must be between -90 and 90.");
            System.exit(1);
        }
        if (lng < -180 || lng > 180) {
            System.err.println("Error: longitude must be between -180 and 180.");
            System.exit(1);
        }

        SpeedLimitLookup lookup = new SpeedLimitLookup();
        try {
            System.out.printf("Querying OSM for road at (%.7f, %.7f)...%n", lat, lng);
            SpeedLimitResult result = lookup.lookup(lat, lng);

            if (result == null) {
                System.out.println("No road found within 50 metres of that coordinate.");
                System.exit(2);
            }

            System.out.println(result);
            if (result.getRawSpeedLimit() != null) {
                System.exit(0);
            } else {
                // Found a road but no maxspeed tag – exit with a distinct code
                System.exit(3);
            }
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Request interrupted.");
            System.exit(1);
        }
    }
}
