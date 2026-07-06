# Utah Motorsports Campus not found by current name in OSM — mapped as 'Miller Motorsports Complex'

**Severity:** sev3  
**Date:** 2026-05-31

## Root cause

OSM never updated the venue name after the rebrand from Miller Motorsports Park / Miller Motorsports Complex to Utah Motorsports Campus. All 4 named circuits and 2 unnamed tracks remain under the old name.

## Resolution

Used bounding-box Overpass query to find all highway/leisure/relation ways in the facility bbox, discovered Miller Motorsports Complex entries. Added to tracks.toml with osm_relation_id values. Unnamed tracks use way_ids strategy.
