# gis-speed-limit

A Java tool that returns the speed limit of the road at a given GPS coordinate.
Data is sourced from [OpenStreetMap](https://www.openstreetmap.org/) via the
free public [Overpass API](https://overpass-api.de/) — **no API key required**.

## Requirements

- Java 17+
- Maven 3.6+ (for building)

## Build

```bash
mvn package
```

This produces a self-contained fat jar:

```
target/speed-limit-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Usage

### Command line

```bash
java -jar target/speed-limit-1.0-SNAPSHOT-jar-with-dependencies.jar <lat> <lng>
```

**Example — a point in central London:**

```bash
java -jar target/speed-limit-1.0-SNAPSHOT-jar-with-dependencies.jar 51.5074 -0.1278
```

**Example output:**

```
Querying OSM for road at (51.5074000, -0.1278000)...
Road: Whitehall [primary], OSM way ID: 4422023, Speed limit: 30 (30 km/h)
```

### As a library

```java
SpeedLimitLookup lookup = new SpeedLimitLookup();
SpeedLimitResult result = lookup.lookup(51.5074, -0.1278);

if (result == null) {
    System.out.println("No road found near that coordinate.");
} else {
    System.out.println("Highway type : " + result.getHighwayType());
    System.out.println("Road name    : " + result.getRoadName());
    System.out.println("Max speed    : " + result.getRawSpeedLimit());   // raw OSM value
    System.out.println("Max speed kph: " + result.getSpeedLimitKph());  // null if non-numeric
}
```

## How it works

1. An [Overpass QL](https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL) query
   fetches all road segments (`way` elements tagged `highway=*`) within **50 metres** of
   the supplied coordinate.
2. Ways that carry a `maxspeed` tag are preferred over those that do not.
3. Among those candidates the road with the highest-priority highway type (motorway >
   trunk > primary > … > service) is selected.
4. The `maxspeed` value is returned as-is (e.g. `"50"`, `"30 mph"`, `"national"`) and,
   where the value is numeric, also converted to km/h.

## Exit codes

| Code | Meaning |
|------|---------|
| `0`  | Road found and speed limit returned |
| `1`  | Bad arguments or network / HTTP error |
| `2`  | No road found within 50 m |
| `3`  | Road found but no `maxspeed` tag in OSM |

## Running the tests

```bash
mvn test
```
