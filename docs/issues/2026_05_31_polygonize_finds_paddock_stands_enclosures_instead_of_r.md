# polygonize() finds paddock/stands enclosures instead of race course for route-type tracks

**Severity:** sev2  
**Date:** 2026-05-31

## Root cause

For tracks mapped as type=route (e.g. Isle of Man TT), the ways form an open linestring not a closed ring. polygonize() finds small enclosed areas (spectator stands, paddock buildings) instead of the 60km course.

## Resolution

Added geometry = 'linestring' config option and build_linestring() function using linemerge() only — no polygonize(). Returns MultiLineString for open routes.
